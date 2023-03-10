package org.jordynsblog.naviplayer

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import java.io.File
import java.io.Serializable
import java.nio.file.Path


// Parameter arguments
private const val ARG_ALBUMNAME = "albumname";
private const val ARG_ALBUMID = "albumid";
private const val ARG_ALBUMART = "albumart";

/**
 * A simple [Fragment] subclass.
 * Use the [album.newInstance] factory method to
 * create an instance of this fragment.
 */
class albumfragment : Fragment(R.layout.fragment_album) {
    // TODO: Rename and change types of parameters
    private var albumname: String? = null
    private var albumid: String? = null
    private var albumart: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            albumname = it.getString(ARG_ALBUMNAME)
            albumid = it.getString(ARG_ALBUMID)
            albumart = it.getString(ARG_ALBUMART)
        }

        Log.d("album.onCreate", "Creating album art fragment using name = $albumname and cover art file path = $albumart")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_album, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val albumartimageview = view.findViewById<ImageView>(R.id.albumArtImageView)


        // We use a let statement here because the albumart file name
        // can very well be null, and we don't want an exception.
        albumart?.let {
            val file = File(it)
            if (file.exists()) {
                val data = file.readBytes()
                val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
                albumartimageview.setImageBitmap(bitmap)
            }
        }

        val albumnametextview = view.findViewById<TextView>(R.id.albumTextView)
        albumnametextview?.setText(albumname)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment album.
         */
        @JvmStatic
        fun newInstance(albumname: String, albumid: String, albumart: String) =
            albumfragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_ALBUMNAME, albumname)
                    putString(ARG_ALBUMID, albumid)
                    putString(ARG_ALBUMART, albumart)
                }
            }
    }
}