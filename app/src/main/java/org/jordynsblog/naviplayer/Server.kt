package org.jordynsblog.naviplayer

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.beust.klaxon.Klaxon
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import java.security.MessageDigest
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/*
class CustomException(message: String? = null, cause: Throwable? = null) : Exception(message, cause) {
    constructor(cause: Throwable) : this(null, cause)
}
 */

class ErrorCodeException(code: Int, message: String): Exception() {
}

// TODO: convert to singleton as currently we are using a different token for each Server instance
// and there is one Server instance per activity.
class Server (serverUrl: String, username: String, password: String, authMethod: String, context: Context) {
    // These two variables are the subsonic API version we're compliant with
    // along with our application name... these should not normally be changed.
    private var apiVersion = "1.16.1"
    private var apiApplcation = "NaviPlayer-Testing"

    private var KTorClient = HttpClient()
    private lateinit var vollyQueue: RequestQueue
    private lateinit var serverUrl: String
    private lateinit var context: Context
    private lateinit var salt: String
    private lateinit var token: String
    private lateinit var username: String
    private lateinit var password: String
    private lateinit var authMethod: String

    init {
        Log.d("Server.init","Server class initialized using serverUrl = $serverUrl")
        //Log.d("Server.init", "Creating Volly Request Queue")
        this.vollyQueue = Volley.newRequestQueue(context)
        this.serverUrl = serverUrl
        this.context = context
        this.username = username
        this.password = password
        this.authMethod = authMethod
    }

    fun MD5HashString(input: String): String {
        val bytes = MessageDigest.getInstance("MD5").digest(input.toByteArray())
        val hex: StringBuilder = StringBuilder()

        for (byte in bytes) {
            val formatted_byte = String.format("%02x", byte)
            hex.append(formatted_byte)
        }

        Log.d("Server.MD5HashString", "Hash for $input: ${hex.toString()}")

        return hex.toString()
    }

    fun HexEncodeString(input: String): String {
        val bytes = input.toByteArray()
        val hex: StringBuilder = StringBuilder()

        for (byte in bytes) {
            val formatted_byte = String.format("%02x", byte)
            hex.append(formatted_byte)
        }

        Log.d("Server.HexEncodeString", "Encoded string for $input: ${hex.toString()}")
        return hex.toString()
    }

    suspend fun getCoverArt(id: String, size: Int?): Bitmap? =
        suspendCoroutine { cont ->
            val builder = getBuilder()
            builder.appendPath("rest")
            builder.appendPath("getCoverArt")

            builder.appendQueryParameter("id", id)
            if (size != null) {
                builder.appendQueryParameter("size", size.toString())
            }

            val url = builder.build().toString()

            val successHandler = Response.Listener<Bitmap> { bitmap ->
                    Log.d("server.getCoverArt", "Successfully retrieved cover art for ID $id")
                    cont.resume(bitmap)
            }

            val errorHandler = Response.ErrorListener { error ->
                Log.d("server.getCoverArt", "Error occurred while retrieving cover art for ID $id")
                cont.resume(null)
            }

            val request = ImageRequest(url, {}, 0, 0, ImageView.ScaleType.CENTER, null, {})
            vollyQueue.add(request)
        }

    suspend fun getAlbumList(
        type: String,
        size: Int,
        offset: Int,
        fromYear: Int?,
        toYear: Int?,
        genre: String?)=
            suspendCoroutine<JSON.response?> { cout ->
                val builder = getBuilder()
                builder.appendPath("rest")
                builder.appendPath("getAlbumList")

                builder.appendQueryParameter("type", type)
                builder.appendQueryParameter("size", size.toString())
                builder.appendQueryParameter("offset", offset.toString())
                builder.appendQueryParameter("fromYear", fromYear.toString())
                builder.appendQueryParameter("toYear", toYear.toString())
                builder.appendQueryParameter("genre", genre)

                val url = builder.build().toString()
                Log.d("Server.getAlbumList", "Using REST URL: $url")

                val successCallback = Response.Listener<String> { response ->
                    Log.d("Server.getAlbumList", "REST Response: ${response}")

                    val result = Klaxon().parse<JSON.response>(response) as JSON.response

                    if (result.subsonic_response.status == "ok") {
                        Toast.makeText(context, "Artist listing successful", Toast.LENGTH_SHORT)
                            .show()
                        cout.resume(result)
                    } else if (result.subsonic_response.status == "failed") {
                        cout.resume(null)
                    } else {
                        // what?
                        throw Exception("Unknown status: ${result.subsonic_response.status}")
                    }

                }

                val errorCallback = Response.ErrorListener { error ->
                    Log.d("Server.login", "ErrorHandler called during login GET: $error")
                    cout.resume(null)
                }

                val request = StringRequest(Request.Method.GET, url, successCallback, errorCallback)

                vollyQueue.add(request)
            }

    // getBuilder is used in all of the REST methods to avoid having to handle
    // building a URL from scratch and to avoid having to redo the auth token
    // in every single call.
    private fun getBuilder(): Uri.Builder {
        val builder = Uri.parse(serverUrl).buildUpon();

        builder.appendQueryParameter("u", username)

        // handle auth methods
        // technically I could just handle token authentication,
        // but I might as well support the others and it makes for useful testing.
        // plus some ancient subsonic client probably doesn't support token auth.
        if (authMethod == "plaintext") {
            builder.appendQueryParameter("p", password)
        } else if (authMethod == "obfuscated") {
            val obfuscated_password: String = "enc:${HexEncodeString(password)}"
            Log.d("Server.login", "Using $obfuscated_password as obfuscated authentication password!")
            builder.appendQueryParameter("p", obfuscated_password)
        } else if (authMethod == "token" ) {
            Log.d("Server.login", "Generating auth token")
            this.salt = MD5HashString(UUID.randomUUID().toString()) // I did this basically because I hate the dashes in the UUID and this is the easiest way to get rid of them.
            this.token = MD5HashString("${password}${this.salt}")
            Log.d("Server.login", "Using salt = ${this.salt} and token = ${this.token}")
            builder.appendQueryParameter("t", this.token)
            builder.appendQueryParameter("s", this.salt)
        } else {
            throw Exception("authMethod must be one of: plaintext, obfuscated, token!")
        }

        builder.appendQueryParameter("v", apiVersion)
        builder.appendQueryParameter("c", apiApplcation)
        builder.appendQueryParameter("f", "json")

        return builder
    }

    suspend fun login(): Boolean {
        val builder = getBuilder()
        builder.appendPath("rest")
        builder.appendPath("ping.view")
        val url = builder.build().toString()
        Log.d("Server.login", "Using login URL: $url")

        val response = KTorClient.get(url).bodyAsText()
        val result = Klaxon().parse<JSON.response>(response) as JSON.response

        if (result.subsonic_response.status == "ok") {
            Log.d("Server.login", "Login successful")
            return true
        } else if (result.subsonic_response.status == "failed") {
            return false
        } else {
            // what?
            throw Exception("Unknown status: ${result.subsonic_response.status}")
        }
    }

    /*suspend fun login() = suspendCoroutine<Boolean> { cout ->
        val builder = getBuilder()
        builder.appendPath("rest")
        builder.appendPath("ping.view")
        val url = builder.build().toString()
        Log.d("Server.login", "Using login URL: $url")

        val successCallback = Response.Listener<String> { response ->
            Log.d("Server.login", "REST Response: ${response}")

            val result = Klaxon().parse<JSON.response>(response) as JSON.response

            if (result.subsonic_response.status == "ok") {
                Log.d("Server.login", "Login successful")
                cout.resume(true)
            } else if (result.subsonic_response.status == "failed") {
                cout.resume(false)
            } else {
                // what?
                throw Exception("Unknown status: ${result.subsonic_response.status}")
            }

        }

        val errorCallback = Response.ErrorListener { error ->
            Log.d("Server.login", "ErrorHandler called during login GET: $error")
            cout.resume(false)
        }

        val request = StringRequest(Request.Method.GET, url, successCallback, errorCallback)

        // Add the request to the RequestQueue.
        vollyQueue.add(request)
    }*/
}