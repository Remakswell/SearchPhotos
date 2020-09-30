package com.example.searchphoto.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.searchphoto.R
import com.example.searchphoto.data.Photo
import com.example.searchphoto.data.PhotosSearchResponse
import com.example.searchphoto.networking.WebClient
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_photos.*

class PhotosActivity : AppCompatActivity() {

    private lateinit var photosAdapter: PhotosAdapter
    private lateinit var compositeDisposable: CompositeDisposable
    private lateinit var realm: Realm
    private lateinit var realmResults: RealmResults<Photo>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photos)

        photosAdapter = PhotosAdapter()
        photosRecyclerView.adapter = photosAdapter
        photosRecyclerView.layoutManager = LinearLayoutManager(this)

        compositeDisposable = CompositeDisposable()

        realm = Realm.getDefaultInstance()
        realmResults = realm.where(Photo::class.java).findAll()

        initView()
        updateAdapter(realmResults)
    }

    override fun onDestroy() {
        super.onDestroy()

        compositeDisposable.dispose()
        realm.close()
    }

    private fun fetchPhotos(queryText: String) {
        progress.visibility = View.VISIBLE

        val searchResponse = WebClient.client.fetchPhotos(queryText)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::onSuccess, this::onError)
        compositeDisposable.add(searchResponse)
    }

    private fun saveData(photos: List<Photo>) {
        val maxPosition = photos.size - 1
        val randomPosition = (0..maxPosition).random()

        realm.beginTransaction()
        val photo = realm.createObject(Photo::class.java)
        photo.id = photos[randomPosition].id
        photo.title = searchBox.text.toString().trim()
        photo.url = photos[randomPosition].url
        realm.commitTransaction()

        updateAdapter(realmResults)
    }

    private fun updateAdapter(realmResults: RealmResults<Photo>) {
        progress.visibility = View.GONE

        photosAdapter.photos.clear()
        photosAdapter.photos.addAll(realmResults)
        photosAdapter.notifyDataSetChanged()
    }

    private fun onSuccess(resp: PhotosSearchResponse?) {
        val photos = resp?.photos?.photo?.map { photo ->
            Photo(
                id = photo.id,
                url = "https://farm${photo.farm}.staticflickr.com/${photo.server}/${photo.id}_${photo.secret}.jpg",
                title = photo.title
            )
        }
        photos?.let {
            if (it.isEmpty()) {
                Toast.makeText(this, getString(R.string.nothing_found), Toast.LENGTH_SHORT).show()
                progress.visibility = View.GONE
                return
            }
            saveData(it)
        }
    }

    private fun onError(th: Throwable) {
        Toast.makeText(this, th.message, Toast.LENGTH_SHORT).show()
    }

    private fun initView() {
        searchBox.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER || actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {

                val queryText = v.text.toString()
                if (!queryText.isBlank()) {
                    fetchPhotos(queryText)
                }
            }
            false
        })
    }
}