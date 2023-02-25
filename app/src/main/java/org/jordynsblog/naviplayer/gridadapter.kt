package org.jordynsblog.naviplayer

import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.nio.file.Path

data class album (
    val name: String,
    val artpath: String? = null
)

class CustomAdapter() :
    RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    private val dataSet: MutableList<album> = mutableListOf()

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val albumartview: ImageView
        val albumnameview: TextView

        init {
            albumartview = view.findViewById(R.id.albumArtImageView)
            albumnameview = view.findViewById(R.id.albumTextView)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.fragment_album, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        val album = dataSet[position]
        Log.d("CustomAdapter.onBindViewHolder", "Replacing contents of view with album = $album @ position = $position")

        viewHolder.albumnameview.text = album.name
        album.artpath?.let {
            val file = File(it)
            if (file.exists()) {
                val data = file.readBytes()
                val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
                viewHolder.albumartview.setImageBitmap(bitmap)
            }
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

    fun addAlbum(name: String, artpath: String) {
        val album = album(name = name, artpath = artpath)
        dataSet.add(album)

        notifyItemInserted(dataSet.size)
    }
}
