package com.qpsoft.cdc

import android.app.Application
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.Utils
import com.lzy.okgo.OkGo
import com.lzy.okgo.interceptor.HttpLoggingInterceptor
import com.qpsoft.cdc.ui.entity.CheckItem
import com.qpsoft.cdc.ui.entity.School
import com.qpsoft.cdc.utils.HC08OpUtil
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import java.util.logging.Level


class App: Application() {

    var checkItemList = mutableListOf<CheckItem>()
    var selectSchool: School? = null

    companion object {
        lateinit var instance: App
        //fun instance() = instance
    }


    override fun onCreate() {
        super.onCreate()

        instance = this

        // utilcode
        Utils.init(this)
        LogUtils.getConfig().setLogSwitch(true)
        // okgo
        initOkGo()
        //ble
        HC08OpUtil.initBle(this)
    }

    private fun initOkGo() {
        val builder = OkHttpClient.Builder()
        val httpLoggingInterceptor = HttpLoggingInterceptor("OkGo")
        httpLoggingInterceptor.setPrintLevel(HttpLoggingInterceptor.Level.BODY)
        httpLoggingInterceptor.setColorLevel(Level.INFO)
        builder.addInterceptor(httpLoggingInterceptor as Interceptor)
        builder.readTimeout(60000L, TimeUnit.MILLISECONDS)
        builder.writeTimeout(60000L, TimeUnit.MILLISECONDS)
        builder.connectTimeout(60000L, TimeUnit.MILLISECONDS)
        OkGo.getInstance().init(this).okHttpClient = builder.build()
    }
}