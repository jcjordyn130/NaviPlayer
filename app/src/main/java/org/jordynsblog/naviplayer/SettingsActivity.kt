package org.jordynsblog.naviplayer

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.io.path.Path


class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }

        //Preference button = findPreference(getString(R.string.myCoolButton));
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        suspend fun onPingServer() = withContext(Dispatchers.IO) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())

            // Load saved values
            Log.d("SettingsActivity.onPreferenceClickListener", "Loading saved values from SharedPrefs")
            val serverUrl = sharedPreferences.getString("serverUrl", "") as String
            val username = sharedPreferences.getString("username", "") as String
            val password = sharedPreferences.getString("password", "") as String
            val authMethod = sharedPreferences.getString("authMethod", "token") as String

            // Init server
            val server = Server(serverUrl, username, password, authMethod, requireContext())
            val loginsuccessful = server.login()

            val snackbar: Snackbar
            if (loginsuccessful) {
                snackbar = Snackbar.make(requireView(), "Server Returned Login Success", Snackbar.LENGTH_LONG)
            } else {
                snackbar = Snackbar.make(requireView(), "Server Returned Login Failure", Snackbar.LENGTH_LONG)
            }

            withContext(Dispatchers.Main) {
                snackbar.show()
            }
        }

        suspend fun onDeleteCachedAlbumArt() = withContext(Dispatchers.IO) {
            val snackbar = Snackbar.make(requireView(), "Deleting all cached album art...", Snackbar.LENGTH_LONG)
            snackbar.show()

            val coverartdir = Path(requireContext().cacheDir.toString(), "albumart")
            Log.d("SettingsFragment.onDeleteCachedAlbumArt", "Deleting cached album art from $coverartdir via user request!")

            coverartdir.toFile().walk(FileWalkDirection.BOTTOM_UP).forEach {
                Log.d("SettingsFragment.onDeleteCachedAlbumArt", "Deleting $it!")
                if ( ! it.delete() ) {
                    Log.e("SettingsFragment.onDeleteCachedAlbumArt", "Error occurred while deleting $it")
                }
            }

            withContext(Dispatchers.Main) {
                @Suppress("NAME_SHADOWING") val snackbar = Snackbar.make(requireView(), "Successfully removed all cached album art!", Snackbar.LENGTH_LONG)
                snackbar.show()
            }
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            val pingserverbutton: Preference = findPreference("pingserverswitch")!!
            pingserverbutton.setOnPreferenceClickListener {
                lifecycleScope.launch {
                    onPingServer()
                }

                return@setOnPreferenceClickListener true
            }

            val deletecachebutton: Preference = findPreference("deletecachedalbumartswitch")!!
            deletecachebutton.setOnPreferenceClickListener {
                lifecycleScope.launch {
                    onDeleteCachedAlbumArt()
                }

                return@setOnPreferenceClickListener true
            }
        }
    }
}