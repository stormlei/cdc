package com.qpsoft.cdc.ui

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.qpsoft.cdc.Api
import com.qpsoft.cdc.App
import com.qpsoft.cdc.R
import com.qpsoft.cdc.base.BaseActivity
import com.qpsoft.cdc.okgo.callback.DialogCallback
import com.qpsoft.cdc.okgo.model.LzyResponse
import com.qpsoft.cdc.ui.adapter.SchoolAdapter
import com.qpsoft.cdc.ui.entity.School
import com.qpsoft.cdc.ui.physical.GradeClazzListActivity
import com.qpsoft.cdc.ui.physical.retest.RetestTitleListActivity
import kotlinx.android.synthetic.main.activity_select_school.*


class SelectSchoolActivity : BaseActivity() {

    private lateinit var mAdapter: SchoolAdapter

    private var isReSel: Boolean = false
    private var noSelSchool: Boolean = false
    private var noSelSchoolReTest: Boolean = false
    private var planId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_school)

        isReSel = intent.getBooleanExtra("isReSel", false)
        noSelSchool = intent.getBooleanExtra("noSelSchool", false)
        noSelSchoolReTest = intent.getBooleanExtra("noSelSchoolReTest", false)
        planId = intent.getStringExtra("planId")

        setBackBtn()
        setTitle("选择学校")

        rvSchool.setLayoutManager(LinearLayoutManager(this))
        rvSchool.recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        rvSchool.setOverlayStyle_MaterialDesign(R.color.color_cb7)
        mAdapter = SchoolAdapter(this)
        rvSchool.setAdapter(mAdapter)

        mAdapter.setOnItemContentClickListener { v, originalPosition, currentPosition, entity ->
            val school = entity
            App.instance.selectSchool = school
            if (isReSel) {
                finish()
                return@setOnItemContentClickListener
            }
            if (noSelSchool) {
                startActivity(
                    Intent(this@SelectSchoolActivity, GradeClazzListActivity::class.java)
                        .putExtra("school", school)
                )
                return@setOnItemContentClickListener
            }
            if (noSelSchoolReTest) {
                startActivity(
                    Intent(this@SelectSchoolActivity, RetestTitleListActivity::class.java)
                        .putExtra("school", school).putExtra("planId", planId)
                )
                return@setOnItemContentClickListener
            }
            startActivity(Intent(this@SelectSchoolActivity, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }


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
}