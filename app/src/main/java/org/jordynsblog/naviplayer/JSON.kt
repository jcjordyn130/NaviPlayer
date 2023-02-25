package org.jordynsblog.naviplayer

import kotlinx.serialization.Required
import kotlinx.serialization.SerialName

class JSON {
    @kotlinx.serialization.Serializable
    data class response (
        @SerialName("subsonic-response")
        var subsonic_response: subsonic_response
    )

    @kotlinx.serialization.Serializable
    data class subsonic_response (
        var status: String,
        var version: String,
        var type: String,
        var serverVersion: String,
        var error: error? = null,
        var albumList: albumList = albumList(),
    )

    @kotlinx.serialization.Serializable
     data class error (
         var code: Int,
         var message: String
    )

    @kotlinx.serialization.Serializable
    data class albumList (
        var album: List<album> = listOf()
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
    // "artistId":"e7c
    // 6c1f25dd7986f31660373e07d9a5a","songCount":15,"isVideo":false}

    @kotlinx.serialization.Serializable
    data class album (
        var id: String,
        var parent: String? = null,
        var isDir: Boolean,
        var title: String,
        var name: String,
        var album: String? = null, // idk why title, name, and album exist and are the same thing, but whatever.
        var artist: String? = null,
        var year: Int? = null,
        var genre: String? = null,
        var coverArt: String? = null,
        var duration: Int? = null,
        var created: String? = null, // TODO: figure out wtf that is and how to convert it to a Kotlin class
        var artistId: String? = null,
        var songCount: Int,
        var isVideo: Boolean? = null // Since this library is for navidrome, technically this should always be false, but I'm including it for completion.
    )
}