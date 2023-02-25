package org.jordynsblog.naviplayer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.file.Path

class CollectionDemoFragment : Fragment() {
    // When requested, this adapter returns a DemoObjectFragment,
    // representing an object in the collection.
    private lateinit var demoCollectionAdapter: DemoCollectionAdapter
    private lateinit var viewPager: ViewPager2
    private lateinit var GridViewAdapter: CustomAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_album_listing, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        demoCollectionAdapter = DemoCollectionAdapter(this)
        viewPager = view.findViewById(R.id.pager)
        viewPager.adapter = demoCollectionAdapter

        val gridview = view.findViewById<RecyclerView>(R.id.albumgrid)
        GridViewAdapter = CustomAdapter()
        gridview.layoutManager = GridLayoutManager(requireContext(), 3)
        gridview.adapter = GridViewAdapter
    }
}

class DemoCollectionAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 1

    override fun createFragment(position: Int): Fragment {
        // Return a NEW fragment instance in createFragment(int)
        val fragment = DemoObjectFragment()
        return fragment
    }
}

// Instances of this class are fragments representing a single
// object in our collection.
class DemoObjectFragment : Fragment() {
    var server: Server

    init {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())

        Log.d("DemoObjectFragement.Init", "Loading saved values from SharedPrefs")
        val serverUrl = sharedPreferences.getString("serverUrl", "") as String
        val username = sharedPreferences.getString("username", "") as String
        val password = sharedPreferences.getString("password", "") as String
        val authMethod = sharedPreferences.getString("authMethod", "token") as String

        // Init server
        server = Server(serverUrl, username, password, authMethod, requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_album_listing, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val gridview = view.findViewById<RecyclerView>(R.id.albumgrid)
        val adapter = gridview.adapter as CustomAdapter

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
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(view.context, "Album listing failed!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}