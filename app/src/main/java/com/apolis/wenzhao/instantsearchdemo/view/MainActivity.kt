package com.apolis.wenzhao.instantsearchdemo.view

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import butterknife.ButterKnife
import com.apolis.wenzhao.instantsearchdemo.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()
    }

    private fun init(){
        ButterKnife.bind(this)
        setSupportActionBar(toolbar)
        whiteNotificationBar(toolbar)

        btn_local_search.setOnClickListener {
            startActivity(Intent(this, LocalSearchActivity::class.java))
        }

        btn_remote_search.setOnClickListener {
            startActivity(Intent(this, RemoteSearchActivity::class.java))
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
}