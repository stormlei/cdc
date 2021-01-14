package com.qpsoft.cdc.ui

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.LogUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.lzy.okgo.request.base.Request
import com.qpsoft.cdc.Api
import com.qpsoft.cdc.App
import com.qpsoft.cdc.R
import com.qpsoft.cdc.base.BaseActivity
import com.qpsoft.cdc.okgo.callback.DialogCallback
import com.qpsoft.cdc.okgo.model.LzyResponse
import com.qpsoft.cdc.ui.entity.Page
import com.qpsoft.cdc.ui.entity.School
import com.qpsoft.cdc.ui.entity.CompleteStatus
import com.qpsoft.cdc.ui.entity.Student
import kotlinx.android.synthetic.main.activity_student_list.*
import kotlinx.android.synthetic.main.activity_student_list.tvComNum
import kotlinx.android.synthetic.main.activity_student_list.tvUnComNum


class StudentListActivity : BaseActivity() {

    private lateinit var mAdapter: BaseQuickAdapter<Student, BaseViewHolder>

    private var school: School? = null
    private var grade: String? = null
    private var clazz: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_list)

        school = intent.getParcelableExtra("school")
        grade = intent.getStringExtra("grade")
        clazz = intent.getStringExtra("clazz")

        setBackBtn()
        setTitle(school?.name + " " +grade+ " " +clazz)

        getStudentList()
        getCompleteStatus()

        rvStudent.layoutManager = LinearLayoutManager(this)
        mAdapter = object: BaseQuickAdapter<Student, BaseViewHolder>(android.R.layout.simple_list_item_1) {
            override fun convert(holder: BaseViewHolder, item: Student) {
                holder.setText(android.R.id.text1, item.name)
            }

        }
        rvStudent.adapter = mAdapter

        mAdapter.setOnItemClickListener { adapter, view, position ->
            val student = mAdapter.getItem(position)
            //startActivity(Intent(this@StudentListActivity, MainActivity::class.java))
        }

        sg.setOnCheckedChangeListener { radioGroup, checkedId ->
            when(checkedId) {
                R.id.rbWaiting -> getStudentList(0)
                R.id.rbAll -> getStudentList()
            }
        }
    }

    private fun getCompleteStatus() {
        OkGo.get<LzyResponse<CompleteStatus>>(Api.STU_COMPLETE_STATUS)
            .params("schoolId", school?.id)
            .params("grade", grade)
            .params("clazz", clazz)
            .execute(object : DialogCallback<LzyResponse<CompleteStatus>>(this) {
                override fun onSuccess(response: Response<LzyResponse<CompleteStatus>>) {
                    val stuCompleteStatus = response.body()?.data
                    tvComNum.text = stuCompleteStatus?.complateNum
                    tvUnComNum.text = stuCompleteStatus?.unComplateNum
                }
            })
    }


    private fun getStudentList(status: Int = -1) {
        OkGo.get<LzyResponse<Page<MutableList<Student>>>>(Api.STUDENT)
            .params("schoolId", school?.id)
            .params("grade", grade)
            .params("clazz", clazz)
            .params("size", 100)
            .execute(object : DialogCallback<LzyResponse<Page<MutableList<Student>>>>(this) {
                override fun onSuccess(response: Response<LzyResponse<Page<MutableList<Student>>>>) {
                    val studentList = response.body()?.data?.items

                    mAdapter.setNewInstance(studentList)
                }

                override fun onStart(request: Request<LzyResponse<Page<MutableList<Student>>>, out Request<Any, Request<*, *>>>) {
                    super.onStart(request)
                    if (status == 0) {
                        val checkItemStr = App.instance.checkItemList.joinToString(",") { it.key }
                        LogUtils.e("-----------$checkItemStr")
                        request.params("filterWaiting", checkItemStr)
                    }
                }
            })
    }
}