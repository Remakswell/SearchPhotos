package com.example.searchphoto.networking

import com.example.searchphoto.data.PhotosSearchResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("?method=flickr.photos.search&format=json&nojsoncallback=1&api_key=919975ea27a04974a5922fe8710272e3")
    fun fetchPhotos(@Query(value = "text") searchTerm: String): Single<PhotosSearchResponse>
}