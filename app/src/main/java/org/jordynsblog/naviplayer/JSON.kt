package org.jordynsblog.naviplayer

import com.beust.klaxon.Json

class JSON {
    data class response (
        @Json(name = "subsonic-response")
        var subsonic_response: subsonic_response
    )

    data class subsonic_response (
        var status: String,
        var version: String,
        var type: String,
        var serverVersion: String,
        var error: error? = null,
        var albumList: albumList? = null,
    )

     data class error (
         var code: Int,
         var message: String
    )

    data class albumList (
        var album: List<album>
    )

    //{"id":"3b662b9d5fb546e753ab398b0b79231b",
    // "parent":"e7c6c1f25dd7986f31660373e07d9a5a",
    // "isDir":true,
    // "title":"* * * * Live in Phoenix",
    // "name":"* * * * Live in Phoenix",
    // "album":"* * * * Live in Phoenix",
    // "artist":"Fall Out Boy",
    // "year":2008,
    // "genre":"Rock",
    // "coverArt":"al-3b662b9d5fb546e753ab398b0b79231b",
    // "duration":3185,
    // "created":"2023-01-20T12:15:49.468196005Z",
    // "artistId":"e7c6c1f25dd7986f31660373e07d9a5a","songCount":15,"isVideo":false}
    data class album (
        var id: String,
        var parent: String,
        var isDir: Boolean,
        var title: String,
        var name: String,
        var album: String, // idk why title, name, and album exist and are the same thing, but whatever.
        var year: Int,
        var genre: String,
        var coverArt: String,
        var duration: Int,
        var created: String, // TODO: figure out wtf that is and how to convert it to a Kotlin class
        var artistId: String,
        var songCount: Int,
        var isVideo: Boolean // Since this library is for navidrome, technically this should always be false, but I'm including it for completion.
    )
}