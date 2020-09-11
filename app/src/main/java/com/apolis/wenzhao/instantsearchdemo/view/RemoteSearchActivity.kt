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
import com.apolis.wenzhao.instantsearchdemo.adapter.ContactsAdapter
import com.apolis.wenzhao.instantsearchdemo.network.ApiClient
import com.apolis.wenzhao.instantsearchdemo.network.ApiService
import com.apolis.wenzhao.instantsearchdemo.network.model.ContactsResponseItem
import com.jakewharton.rxbinding2.widget.RxTextView
import com.jakewharton.rxbinding2.widget.TextViewTextChangeEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_local_search.*
import kotlinx.android.synthetic.main.content_local_search.*
import java.util.concurrent.TimeUnit

class RemoteSearchActivity : AppCompatActivity(), ContactsAdapter.ContactsAdapterListener {

    private val TAG = RemoteSearchActivity::class.java.simpleName
    private val disposable = CompositeDisposable()
    private val publishSubject = PublishSubject.create<String>()
    private var apiService: ApiService? = null
    private var mAdapter: ContactsAdapter? = null
    private val contactsList: ArrayList<ContactsResponseItem> = ArrayList()
    private var unbinder: Unbinder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remote_search)

        init()
    }

    private fun init() {
        unbinder = ButterKnife.bind(this)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mAdapter = ContactsAdapter(this, contactsList, this)

        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.itemAnimator = DefaultItemAnimator()
        recycler_view.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recycler_view.adapter = mAdapter

        whiteNotificationBar(recycler_view)

        apiService = ApiClient.getClient()?.create(ApiService::class.java)

        val observer: DisposableObserver<List<ContactsResponseItem?>?>? = getSearchObserver()

        disposable.add(
            publishSubject.debounce(300, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .switchMapSingle { t ->
                    apiService?.getContacts(null, t)
                        ?.subscribeOn(Schedulers.io())
                        ?.observeOn(AndroidSchedulers.mainThread())
                }
                .subscribeWith(observer)!!
        )

        // skipInitialValue() - skip for the first time when EditText empty
        disposable.add(
            RxTextView.textChangeEvents(input_search)
                .skipInitialValue()
                .debounce(300, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(searchContactsTextWatcher())!!
        );
        if (observer != null) {
            disposable.add(observer)
        }

        // passing empty string fetches all the contacts
        publishSubject.onNext("")
    }

    private fun searchContactsTextWatcher(): DisposableObserver<TextViewTextChangeEvent?>? {
        return object : DisposableObserver<TextViewTextChangeEvent?>() {
            override fun onNext(textViewTextChangeEvent: TextViewTextChangeEvent) {
                Log.d(TAG, "Search query: " + textViewTextChangeEvent.text())
                publishSubject.onNext(textViewTextChangeEvent.text().toString())
            }

            override fun onError(e: Throwable) {
                Log.e(TAG, "MYTAG" + e.message)
            }

            override fun onComplete() {
                Log.e(TAG, "MYTAG" )
            }
        }
    }

    private fun getSearchObserver(): DisposableObserver<List<ContactsResponseItem?>?> {
        return object : DisposableObserver<List<ContactsResponseItem?>?>() {
            override fun onNext(contacts: List<ContactsResponseItem?>) {
                contactsList.clear()
                contactsList.addAll(contacts as Collection<ContactsResponseItem>)
                mAdapter!!.notifyDataSetChanged()
            }

            override fun onError(e: Throwable) {
                Log.e(TAG, "getSearchObserver(): onError()")
            }

            override fun onComplete() {
                Log.e(TAG,  "getSearchObserver(): onComplete()")
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