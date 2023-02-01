package org.jordynsblog.naviplayer

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val aboutVersionView: TextView = findViewById(R.id.aboutVersionView) as TextView
        aboutVersionView.text = getString(R.string.about_version_message, applicationContext.packageName, BuildConfig.VERSION_CODE)
    }
}