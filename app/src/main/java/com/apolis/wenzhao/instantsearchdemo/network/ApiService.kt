package com.apolis.wenzhao.instantsearchdemo.network

import com.apolis.wenzhao.instantsearchdemo.network.model.ContactsResponseItem
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("contacts.php")
    fun getContacts(
        @Query("source") source: String?,
        @Query("search") query: String?
    ): Single<List<ContactsResponseItem?>?>?
}