package org.jordynsblog.naviplayer

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.preference.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jordynsblog.naviplayer.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityHomeBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var server: Server

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        //Config(applicationContext)
        //sharedPreferences = Config.instance.sharedPreferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        // Load saved values
        Log.d("HomeActivity.onCreate", "Loading saved values from SharedPrefs")
        val serverUrl = sharedPreferences.getString("serverUrl", "") as String
        val username = sharedPreferences.getString("username", "") as String
        val password = sharedPreferences.getString("password", "") as String
        val authMethod = sharedPreferences.getString("authMethod", "token") as String

        // Init server
        server = Server(serverUrl, username, password, authMethod,this)
        server.login()
        rememberCoroutineScope
    }

    fun onLoginFailure(exc: Throwable) {
        Log.d("HomeActivity.onLoginFailure", "login failure with message = ${exc.message}, starting login activity")
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    /*                    Log.d("HomeActivity.populateAlbums", "First Album: ${response.subsonic_response.albumList!!.album[0].name}")
                    server.getCoverArt(response.subsonic_response.albumList!!.album[0].id, null)*/
    suspend fun populateAlbums() {
        server.getAlbumList("alphabeticalByName", 10, 0, null, null, null)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (item.itemId == R.id.action_about) {
            // start about activity
            Log.d(
                "LoginActivity.onOptionsItemSelected",
                "About button pressed, starting about activity"
            )
            startActivity(Intent(this, AboutActivity::class.java))
            return true
        } else if (item.itemId == R.id.action_settings) {
            // start settings activity
            Log.d(
                "LoginActivity.onOptionsItemSelected",
                "Settings button pressed, starting prefs activity"
            )
            startActivity(Intent(this, SettingsActivity::class.java))
            return true
        } else {
            return super.onOptionsItemSelected(item)
        }
    }
}