package com.apolis.wenzhao.instantsearchdemo.view

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import butterknife.ButterKnife
import butterknife.Unbinder
import com.apolis.wenzhao.instantsearchdemo.R
import com.apolis.wenzhao.instantsearchdemo.adapter.ContactsAdapterFilterable
import com.apolis.wenzhao.instantsearchdemo.network.ApiClient
import com.apolis.wenzhao.instantsearchdemo.network.ApiService
import com.apolis.wenzhao.instantsearchdemo.network.model.ContactsResponseItem
import com.jakewharton.rxbinding2.widget.RxTextView
import com.jakewharton.rxbinding2.widget.TextViewTextChangeEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_local_search.*
import kotlinx.android.synthetic.main.content_local_search.*
import java.util.concurrent.TimeUnit


class LocalSearchActivity : AppCompatActivity(), ContactsAdapterFilterable.ContactsAdapterListener {

    private val TAG = LocalSearchActivity::class.java.simpleName
    private val disposable = CompositeDisposable()
    private var apiService: ApiService? = null
    private var mAdapter: ContactsAdapterFilterable? = null
    private val contactsList: ArrayList<ContactsResponseItem> = ArrayList()
    private var unbinder: Unbinder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_local_search)

        init()
    }

    private fun init() {
        unbinder = ButterKnife.bind(this)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mAdapter = ContactsAdapterFilterable(this, contactsList, this)

        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.itemAnimator = DefaultItemAnimator()
        recycler_view.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recycler_view.adapter = mAdapter

        whiteNotificationBar(recycler_view)

        apiService = ApiClient.getClient()?.create(ApiService::class.java)

        disposable.add(RxTextView.textChangeEvents(input_search)
            .skipInitialValue()
            .debounce(300, TimeUnit.MILLISECONDS)
            .distinctUntilChanged()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(searchContacts())
        )

        fetchContacts("gmail")
    }

    private fun fetchContacts(source: String) {
        disposable.add(
            apiService
                ?.getContacts(source, null)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())!!
                .subscribeWith(object : DisposableSingleObserver<List<ContactsResponseItem?>?>()
                {
                    override fun onSuccess(contacts: List<ContactsResponseItem?>) {
                        contactsList.clear()
                        contactsList.addAll(contacts as Collection<ContactsResponseItem>)
                        mAdapter!!.notifyDataSetChanged()
                        Log.d("MYTAG", "fetchContacts() onSuccess()")
                    }

                    override fun onError(e: Throwable) {
                        Log.d("MYTAG", "fetchContacts() onError()")
                        Log.d("MYTAG", e.message.toString())
                    }
                })
        )
    }

    private fun searchContacts(): DisposableObserver<TextViewTextChangeEvent> {
        return object : DisposableObserver<TextViewTextChangeEvent>() {
            override fun onComplete() {
                Log.d("MYTAG", "searchContacts(): onComplete()")
            }

            override fun onNext(textViewTextChangeEvent: TextViewTextChangeEvent) {
                mAdapter?.filter?.filter(textViewTextChangeEvent.text())
                Log.d("MYTAG", "searchContacts(): onNext()")
                Log.d(TAG, "Search query: " + textViewTextChangeEvent.text())
            }

            override fun onError(e: Throwable) {
                Log.d("MYTAG", "searchContacts(): onError()")
                Log.d("MYTAG", e.message.toString())
            }
        }
    }

    private fun whiteNotificationBar(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var flags: Int = view.getSystemUiVisibility()
            flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            view.setSystemUiVisibility(flags)
            window.statusBarColor = Color.WHITE
        }
    }

    override fun onContactSelected(contact: ContactsResponseItem?) {
        //Add click event for the items
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish()
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        disposable.clear()
        unbinder?.unbind()
        super.onDestroy()
    }
}