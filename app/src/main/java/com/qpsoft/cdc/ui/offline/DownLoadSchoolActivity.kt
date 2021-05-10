package com.qpsoft.cdc.ui.offline

import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
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
import com.qpsoft.cdc.ui.entity.Page
import com.qpsoft.cdc.ui.entity.School
import com.qpsoft.cdc.ui.entity.Student
import kotlinx.android.synthetic.main.activity_select_school.*


class DownLoadSchoolActivity : BaseActivity() {

    private lateinit var mAdapter: DownLoadSchoolAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_school)

        setBackBtn()
        setTitle("学校下载")

        rvSchool.setLayoutManager(LinearLayoutManager(this))
        rvSchool.recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        rvSchool.setOverlayStyle_MaterialDesign(R.color.color_cb7)
        mAdapter = DownLoadSchoolAdapter(this)
        rvSchool.setAdapter(mAdapter)

        mAdapter.setOnItemContentClickListener { v, originalPosition, currentPosition, entity ->
            val school = entity
            getSchoolLocal(school.id)
        }
    }

    val realm = App.instance.backgroundThreadRealm

    override fun onStart() {
        super.onStart()

        getSchool()
    }


    private fun getSchool() {
        OkGo.get<LzyResponse<MutableList<School>>>(Api.SCHOOL)
                .execute(object : DialogCallback<LzyResponse<MutableList<School>>>(this) {
                    override fun onSuccess(response: Response<LzyResponse<MutableList<School>>>) {
                        val schoolList = response.body()?.data
                        mAdapter.setDatas(schoolList)
                    }
                })
    }

    //<!------------------ local ----------------->

    private fun getSchoolLocal(schoolId: String) {
        OkGo.get<LzyResponse<Page<MutableList<Student>>>>(Api.STUDENT)
                .params("schoolId", schoolId)
                .params("expand", "school")
                .params("size", 9999)
                .execute(object : DialogCallback<LzyResponse<Page<MutableList<Student>>>>(this) {
                    override fun onSuccess(response: Response<LzyResponse<Page<MutableList<Student>>>>) {
                        val studentList = response.body()?.data?.items

                        realm.executeTransaction {
                            it.copyToRealmOrUpdate(studentList)
                            ToastUtils.showShort("下载成功")
                            mAdapter.notifyDataSetChanged()
                        }
                    }
                })
    }
}