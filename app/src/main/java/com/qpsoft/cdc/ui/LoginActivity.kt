package com.qpsoft.cdc.ui

import android.content.Intent
import android.os.Bundle
import com.blankj.utilcode.util.CacheDiskStaticUtils
import com.blankj.utilcode.util.DeviceUtils
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.qpsoft.cdc.Api
import com.qpsoft.cdc.R
import com.qpsoft.cdc.base.BaseActivity
import com.qpsoft.cdc.constant.Keys
import com.qpsoft.cdc.ui.entity.LoginRes
import com.qpsoft.cdc.okgo.callback.DialogCallback
import com.qpsoft.cdc.okgo.model.LzyResponse
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_base.*
import org.json.JSONObject

class LoginActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        setTitle(resources.getString(R.string.app_name))

        tvLogin.setOnClickListener { doLogin() }

    }

    private fun doLogin() {
        val userName = edtUserName.text.toString().trim()
        val passWord = edtPassWord.text.toString().trim()

        val upMap = mutableMapOf<Any?, Any?>()
        //upMap["username"] = "李娟娟"
        //upMap["password"] = "Aa@123456"
        upMap["username"] = userName
        upMap["password"] = passWord
        //upMap["loginSerial"] = DeviceUtils.getUniqueDeviceId()
        upMap["client"] = "Android"
        upMap["deviceInfo"] = DeviceUtils.getModel()
        val jsonObj = JSONObject(upMap)
        OkGo.post<LzyResponse<LoginRes>>(Api.LOGIN)
            .upJson(jsonObj)
            .execute(object : DialogCallback<LzyResponse<LoginRes>>(this) {
                override fun onSuccess(response: Response<LzyResponse<LoginRes>>) {
                    val loginRes = response.body()?.data
                    CacheDiskStaticUtils.put(Keys.TOKEN, loginRes?.token)
                    startActivity(Intent(this@LoginActivity, PickCheckItemActivity::class.java))
                }
            })

    }
}