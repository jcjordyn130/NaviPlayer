package org.jordynsblog.naviplayer

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment


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
    private var albumart: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            albumname = it.getString(ARG_ALBUMNAME)
            albumid = it.getString(ARG_ALBUMID)
            albumart = it.getSerializable(ARG_ALBUMART, Bitmap) as Bitmap
        }
        val albumartimageview = view?.findViewById<ImageView>(R.id.albumArtImageView)
        albumartimageview?.setImageBitmap(albumart)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_album, container, false)
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
        fun newInstance(albumname: String, albumid: String, albumart: ByteArray) =
            albumfragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_ALBUMNAME, albumname)
                    putString(ARG_ALBUMID, albumid)
                    putByteArray(ARG_ALBUMART, albumart)
                }
            }
    }
}