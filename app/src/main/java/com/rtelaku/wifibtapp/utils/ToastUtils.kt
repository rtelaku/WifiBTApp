package com.rtelaku.wifibtapp.utils

import android.content.Context
import android.widget.Toast

object ToastUtils {
    fun displayToast(context: Context, msg: String){
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }
}