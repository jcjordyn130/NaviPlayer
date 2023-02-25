package org.jordynsblog.naviplayer

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.ui.AppBarConfiguration
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import org.jordynsblog.naviplayer.databinding.ActivityHomeBinding
import java.nio.file.Path


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

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        // Debug logging
        Log.d("HomeActivity.onCreate", "Cache Directory Listing: ")
        applicationContext.cacheDir.walk().forEach {
            Log.d("HomeActivity.onCreate", "${it.name} ${it.length()}")
        }

        // Load saved values
        Log.d("HomeActivity.onCreate", "Loading saved values from SharedPrefs")
        val serverUrl = sharedPreferences.getString("serverUrl", "") as String
        val username = sharedPreferences.getString("username", "") as String
        val password = sharedPreferences.getString("password", "") as String
        val authMethod = sharedPreferences.getString("authMethod", "token") as String

        // Init server
        server = Server(serverUrl, username, password, authMethod,applicationContext)
        lifecycleScope.launch(Dispatchers.IO) {
            var loginsuccessful: Boolean

            try {
                loginsuccessful = server.login()
            } catch (e: java.net.ConnectException) {
                Log.d("HomeActivity", "java.net.ConnectException thrown with message ${e.message} when trying to login")
                loginsuccessful = false
            }

            if ( ! loginsuccessful) {
                Log.d("HomeActivity.onLoginFailure", "login failure, starting login activity")
                startActivity(Intent(applicationContext, LoginActivity::class.java))
                finish()
            }

            //withContext(Dispatchers.Main) {
             //   populateAlbums()
            //}
        }
    }

    fun populateAlbums() {
        val gridview = findViewById<RecyclerView>(R.id.albumgrid)
        val adapter = CustomAdapter()
        gridview.adapter = adapter
        gridview.layoutManager = GridLayoutManager(this, 3)

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val albumlist = server.getAlbumList("alphabeticalByName", 9999, 0, null, null, null)

                if (albumlist != null) {
                    for (album in albumlist.subsonic_response.albumList.album) {
                        Log.d("HomeActivity.onCreate", "Using album ${album.name}")

                        val albumart: Path? = album.coverArt?.let { server.getCoverArt(it, null) }

                        withContext(Dispatchers.Main) {
                            adapter.addAlbum(album.name, albumart.toString())
                        }
                        /*val fragment =
                            albumfragment.newInstance(album.name, album.id, albumart.toString())

                        withContext(Dispatchers.Main) {
                            val fragmentview = FragmentContainerView(applicationContext)
                            fragmentview.id = View.generateViewId()

                            val albumgrid = findViewById<GridView>(R.id.albumgrid)
                            //albumgrid.columnCount = 2
                            //albumgrid.

                            supportFragmentManager.commit {
                                setReorderingAllowed(true)
                                add(fragmentview.id, fragment)
                            }
                        }*/
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(applicationContext, "Album listing failed!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
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