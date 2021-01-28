package com.qpsoft.cdc

import android.app.Application
import android.content.Context
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.Utils
import com.lzy.okgo.OkGo
import com.lzy.okgo.interceptor.HttpLoggingInterceptor
import com.qpsoft.cdc.ui.entity.CheckItem
import com.qpsoft.cdc.ui.entity.School
import com.qpsoft.cdc.utils.BleDeviceOpUtil
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.api.RefreshFooter
import com.scwang.smart.refresh.layout.api.RefreshHeader
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.DefaultRefreshFooterCreator
import com.scwang.smart.refresh.layout.listener.DefaultRefreshHeaderCreator
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

        init {
            //设置全局的Header构建器
            SmartRefreshLayout.setDefaultRefreshHeaderCreator { context, layout ->
                //layout.setPrimaryColorsId(R.color.colorPrimary, android.R.color.white);//全局设置主题颜色
                ClassicsHeader(context);//.setTimeFormat(new DynamicTimeFormat("更新于 %s"));//指定为经典Header，默认是 贝塞尔雷达Header
            }
            //设置全局的Footer构建器
            SmartRefreshLayout.setDefaultRefreshFooterCreator { context, layout -> //指定为经典Footer，默认是 BallPulseFooter
                ClassicsFooter(context).setDrawableSize(20f)
            }
        }
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
        BleDeviceOpUtil.initBle(this)
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