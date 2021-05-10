package com.qpsoft.cdc.ui.offline

import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.qpsoft.cdc.Api
import com.qpsoft.cdc.App
import com.qpsoft.cdc.R
import com.qpsoft.cdc.base.BaseActivity
import com.qpsoft.cdc.okgo.callback.DialogCallback
import com.qpsoft.cdc.okgo.model.LzyResponse
import com.qpsoft.cdc.ui.adapter.DownLoadSchoolAdapter
import com.qpsoft.cdc.ui.adapter.UpLoadDataAdapter
import com.qpsoft.cdc.ui.entity.Page
import com.qpsoft.cdc.ui.entity.School
import com.qpsoft.cdc.ui.entity.Student
import kotlinx.android.synthetic.main.activity_select_school.*
import org.json.JSONArray
import org.json.JSONObject


class UpLoadDataActivity : BaseActivity() {

    private lateinit var mAdapter: UpLoadDataAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_school)

        setBackBtn()
        setTitle("上传筛查数据")

        rvSchool.setLayoutManager(LinearLayoutManager(this))
        rvSchool.recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        rvSchool.setOverlayStyle_MaterialDesign(R.color.color_cb7)
        mAdapter = UpLoadDataAdapter(this)
        rvSchool.setAdapter(mAdapter)

        mAdapter.setOnItemContentClickListener { v, originalPosition, currentPosition, entity ->
            val school = entity
            upData(school.id)
        }
    }

    val realm = App.instance.backgroundThreadRealm

    override fun onStart() {
        super.onStart()

        getSchoolLocal()
    }

    //<!------------------ local ----------------->

    private fun getSchoolLocal() {
        val rr = realm.where(School::class.java).findAll()
        val schoolList = realm.copyFromRealm(rr)
        LogUtils.e("------"+schoolList)
        mAdapter.setDatas(schoolList)
    }

    private fun upData(schoolId: String) {
        val rr = realm.where(Student::class.java).equalTo("school.id", schoolId)
            .isNotNull("localRecord").findAll()
        val studentList = realm.copyFromRealm(rr)
        LogUtils.e("------"+studentList)

        val jsonArray = JSONArray()
        for (stu in studentList) {
            val upMap = mutableMapOf<Any?, Any?>()
            upMap["studentId"] = stu.id
            upMap["data"] = stu.localRecord
            val jsonObj = JSONObject(upMap)
            jsonArray.put(jsonObj)
        }
        OkGo.post<LzyResponse<Any>>(Api.RECORD_SUBMIT)
            .upJson(jsonArray)
            .execute(object : DialogCallback<LzyResponse<Any>>(this) {
                override fun onSuccess(response: Response<LzyResponse<Any>>) {
                    val any = response.body()?.data
                    realm.executeTransaction {
                        for (r in rr) {
                            r.upload = 1
                        }
                    }
                    ToastUtils.showShort("上传成功")
                    mAdapter.notifyDataSetChanged()
                }
            })
    }
}