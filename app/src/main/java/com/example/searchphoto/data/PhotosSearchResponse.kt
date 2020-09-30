package com.example.searchphoto.data

open class PhotosSearchResponse(val photos: PhotosMetaData)

data class PhotosMetaData(
    val photo: List<PhotoResponse>
)

data class PhotoResponse(
    val id: String,
    val secret: String,
    val server: String,
    val farm: Int,
    val title: String
)