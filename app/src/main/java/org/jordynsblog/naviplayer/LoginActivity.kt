package org.jordynsblog.naviplayer

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.ui.AppBarConfiguration
import androidx.preference.PreferenceManager
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.beust.klaxon.Klaxon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jordynsblog.naviplayer.databinding.ActivityLoginBinding
import java.security.MessageDigest
import java.util.*

class LoginActivity: AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityLoginBinding
    private lateinit var sharedPreferences: SharedPreferences
    lateinit var server: Server

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        // We have to init shared prefs late, otherwise we get a null object reference as the class
        // is not init'ed yet (fun)...

        //Config(applicationContext)
        //sharedPreferences = Config.instance.sharedPreferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        // Load saved values
        Log.d("onCreate", "Loading saved values from SharedPrefs")
        val serverUrlTextView = findViewById<TextView>(R.id.serverUrlText)
        val usernameTextView = findViewById<TextView>(R.id.usernameText)
        val passwordTextView = findViewById<TextView>(R.id.passwordText)
        val authRadioGroup = findViewById<RadioGroup>(R.id.authRadioGroup)

        serverUrlTextView.text = sharedPreferences.getString("serverUrl", "") as String
        usernameTextView.text = sharedPreferences.getString("username", "") as String
        passwordTextView.text = sharedPreferences.getString("password", "") as String

        val authMethod = sharedPreferences.getString("authMethod", "token")
        if (authMethod == "plaintext") {
           authRadioGroup.check(R.id.radioPlainTextAuth)
        } else if (authMethod == "obfuscated") {
            authRadioGroup.check(R.id.radioObfuAuth)
        } else if (authMethod == "token" ) {
            authRadioGroup.check(R.id.radioTokenAuth)
        } else {
            throw Exception("authMethod in sharedPrefs is not one of the three valid values! How did we get here??? (authMethod = $authMethod)")
        }
    }

    fun login(view: View?) {
        val serverUrl = findViewById<TextView>(R.id.serverUrlText).text.toString()
        val username = findViewById<TextView>(R.id.usernameText).text.toString()
        val password = findViewById<TextView>(R.id.passwordText).text.toString()
        val authRadioGroup = findViewById<RadioGroup>(R.id.authRadioGroup)
        if (authRadioGroup.checkedRadioButtonId == -1) {
            Log.d("login", "No authentication method selected... how did we get here???")
            return
        }

        val authRadio = authRadioGroup.findViewById<RadioButton>(authRadioGroup.checkedRadioButtonId)
        var authMethod: String = ""

        if (authRadio.id == R.id.radioTokenAuth) {
            authMethod = "token"
        } else if (authRadio.id == R.id.radioPlainTextAuth) {
            authMethod = "plaintext"
        } else if (authRadio.id == R.id.radioObfuAuth) {
            authMethod = "obfuscated"
        }

        val editor = sharedPreferences.edit()
        Log.d("login", "Saving values to SharedPrefs")
        editor.putString("serverUrl", serverUrl)
        editor.putString("username", username)
        editor.putString("password", password)
        editor.putString("authMethod", authMethod)
        editor.apply()

        server = Server(serverUrl, username, password, authMethod,this)
        this.lifecycleScope.launch(Dispatchers.IO) {
            if (server.login()) {
                Log.d("LoginActivity.login", "Login coroutine returned true, launching home activity.")
                startActivity(Intent(applicationContext, HomeActivity::class.java))
            } else {
                Log.d("LoginActivity.login", "Login coroutine returned false.")
                Toast.makeText(applicationContext, "Login Unsuccessful", Toast.LENGTH_SHORT).show()
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