package com.example.searchphoto.data

import io.realm.RealmObject
import io.realm.annotations.RealmClass

@RealmClass
open class Photo(
    var id: String = "",
    var url: String = "",
    var title: String = ""
) : RealmObject()