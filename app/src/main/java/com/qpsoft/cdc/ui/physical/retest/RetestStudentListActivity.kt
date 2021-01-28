package com.qpsoft.cdc.ui.physical.retest

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.LogUtils
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.lzy.okgo.request.base.Request
import com.qpsoft.cdc.Api
import com.qpsoft.cdc.App
import com.qpsoft.cdc.R
import com.qpsoft.cdc.base.BaseActivity
import com.qpsoft.cdc.okgo.callback.DialogCallback
import com.qpsoft.cdc.okgo.model.LzyResponse
import com.qpsoft.cdc.ui.adapter.StudentAdapter
import com.qpsoft.cdc.ui.entity.Page
import com.qpsoft.cdc.ui.entity.School
import com.qpsoft.cdc.ui.entity.Student
import kotlinx.android.synthetic.main.activity_retest_student_list.*


class RetestStudentListActivity : BaseActivity() {

    private lateinit var mAdapter: StudentAdapter

    private var school: School? = null
    private var grade: String? = null
    private var clazz: String? = null

    private var retestTitle: String? = null
    private var planType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_retest_student_list)

        school = intent.getParcelableExtra("school")
        grade = intent.getStringExtra("grade")
        clazz = intent.getStringExtra("clazz")

        retestTitle = intent.getStringExtra("retestTitle")
        planType = intent.getStringExtra("planType")

        setBackBtn()
        setTitle(school?.name + " " +grade+ " " +clazz)

        getRetestStudentList()

        rvRetestStudent.setLayoutManager(LinearLayoutManager(this))
        rvRetestStudent.setOverlayStyle_MaterialDesign(R.color.color_cb7)
        mAdapter = StudentAdapter(this)
        rvRetestStudent.setAdapter(mAdapter)

        mAdapter.setOnItemContentClickListener { v, originalPosition, currentPosition, entity ->
            entity.studentId = entity.id
            startActivity(
                Intent(this@RetestStudentListActivity, ReTestActivity::class.java)
                    .putExtra("student", entity)
                    .putExtra("retestTitle", retestTitle)
                    .putExtra("planType", planType)
            )
        }

    }


    private fun getRetestStudentList() {
        OkGo.get<LzyResponse<Page<MutableList<Student>>>>(Api.STUDENT)
            .params("schoolId", school?.id)
            .params("grade", grade)
            .params("clazz", clazz)
            .params("size", 100)
            .execute(object : DialogCallback<LzyResponse<Page<MutableList<Student>>>>(this) {
                override fun onSuccess(response: Response<LzyResponse<Page<MutableList<Student>>>>) {
                    val studentList = response.body()?.data?.items

                    mAdapter.setDatas(studentList)
                }

                override fun onStart(request: Request<LzyResponse<Page<MutableList<Student>>>, out Request<Any, Request<*, *>>>) {
                    super.onStart(request)
                    val checkItemStr = App.instance.checkItemList.joinToString(",") { it.key }
                    LogUtils.e("-----------$checkItemStr")
                    request.params("filterDone", checkItemStr)

                }
            })
    }
}