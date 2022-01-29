package com.rtelaku.wifibtapp.ui.starter

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rtelaku.wifibtapp.R
import com.rtelaku.wifibtapp.ui.main.MainActivity
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class StarterActivity : AppCompatActivity() {

    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_starter)

        val startApp = Observable.timer(1, java.util.concurrent.TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .subscribe{
                val intent = Intent(this@StarterActivity, MainActivity::class.java)
                startActivity(intent)
            }

        compositeDisposable.add(startApp)
    }

    override fun onRestart() {
        super.onRestart()
        finish()
    }
}