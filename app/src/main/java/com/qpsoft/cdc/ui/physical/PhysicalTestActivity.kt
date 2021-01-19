package com.qpsoft.cdc.ui.physical

import android.content.Intent
import android.os.Bundle
import com.blankj.utilcode.util.CacheDiskStaticUtils
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.qpsoft.cdc.Api
import com.qpsoft.cdc.App
import com.qpsoft.cdc.R
import com.qpsoft.cdc.base.BaseActivity
import com.qpsoft.cdc.constant.Keys
import com.qpsoft.cdc.ui.entity.LoginRes
import com.qpsoft.cdc.okgo.callback.DialogCallback
import com.qpsoft.cdc.okgo.model.LzyResponse
import com.qpsoft.cdc.ui.PickCheckItemActivity
import com.qpsoft.cdc.ui.entity.Student
import kotlinx.android.synthetic.main.activity_physical_test.*
import org.json.JSONObject

class PhysicalTestActivity : BaseActivity() {

    private var student: Student? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_physical_test)

        student = intent.getParcelableExtra("student")

        setBackBtn()
        setTitle("健康监测")

        val checkItemList = App.instance.checkItemList
        val ciStr = checkItemList.joinToString { checkItem -> checkItem.key }
        when(ciStr) {
            "vision" -> {
                val visionView = layoutInflater.inflate(R.layout.view_vision, null)
                llContent.addView(visionView)
            }
            "diopter" -> {
                val diopterView = layoutInflater.inflate(R.layout.view_diopter, null)
                llContent.addView(diopterView)
            }
        }


        tvSubmit.setOnClickListener { doSubmit() }
    }

    private fun doSubmit() {
        val upMap = mutableMapOf<Any?, Any?>()
        upMap["studentId"] = student?.id
        upMap["data"] = "Aa@123456"
        val jsonObj = JSONObject(upMap)
        OkGo.post<LzyResponse<LoginRes>>(Api.RECORD_SUBMIT)
            .upJson(jsonObj)
            .execute(object : DialogCallback<LzyResponse<LoginRes>>(this) {
                override fun onSuccess(response: Response<LzyResponse<LoginRes>>) {
                    val loginRes = response.body()?.data
                    CacheDiskStaticUtils.put(Keys.TOKEN, loginRes?.token)
                    startActivity(Intent(this@PhysicalTestActivity, PickCheckItemActivity::class.java))
                }
            })

    }
}