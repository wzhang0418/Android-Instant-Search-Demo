package com.apolis.wenzhao.instantsearchdemo.network.model

class ContactsResponse : ArrayList<ContactsResponseItem>()

data class ContactsResponseItem(
    val email: String? = null,
    val image: String? = null,
    val name: String? = null,
    val phone: String? = null,
    val source: String? = null
)
