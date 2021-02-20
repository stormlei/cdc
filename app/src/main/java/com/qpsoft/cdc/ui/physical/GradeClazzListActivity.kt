package com.qpsoft.cdc.ui.physical

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.qpsoft.cdc.Api
import com.qpsoft.cdc.R
import com.qpsoft.cdc.base.BaseActivity
import com.qpsoft.cdc.okgo.callback.DialogCallback
import com.qpsoft.cdc.okgo.model.LzyResponse
import com.qpsoft.cdc.ui.adapter.GradeClazzListAdapter
import com.qpsoft.cdc.ui.entity.*
import com.qpsoft.cdc.ui.physical.retest.RetestStudentListActivity
import kotlinx.android.synthetic.main.activity_grade_clazz_list.*


class GradeClazzListActivity : BaseActivity() {

    private lateinit var mAdapter: GradeClazzListAdapter

    private var school: School? = null

    private var isRetest: Boolean = false
    private var retestTitle: String? = null
    private var planType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grade_clazz_list)

        school = intent.getParcelableExtra("school")

        isRetest = intent.getBooleanExtra("isRetest", false)
        retestTitle = intent.getStringExtra("retestTitle")
        planType = intent.getStringExtra("planType")

        setBackBtn()
        setTitle(school?.name)

        if (isRetest) {
            llTop.visibility = View.GONE
        } else {
            llTop.visibility = View.VISIBLE
            getCompleteStatus()
        }


        rvGradeClazz.layoutManager = LinearLayoutManager(this)
        rvGradeClazz.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        mAdapter = GradeClazzListAdapter(R.layout.gradeclazz_item_content, R.layout.gradeclazz_section_head, null, isRetest)
        rvGradeClazz.adapter = mAdapter

        mAdapter.setOnItemClickListener { adapter, view, position ->
            val mySection = mAdapter.getItem(position)
            if (!mySection.isHeader) {
                val grade = mySection.header as String
                val clazz = mySection.any as String
                if (isRetest) {
                    startActivity(Intent(this@GradeClazzListActivity, RetestStudentListActivity::class.java)
                        .putExtra("school", school)
                        .putExtra("grade", grade)
                        .putExtra("clazz", clazz)
                        .putExtra("retestTitle", retestTitle)
                        .putExtra("planType", planType)
                    )
                } else {
                    startActivity(Intent(this@GradeClazzListActivity, StudentListActivity::class.java)
                        .putExtra("school", school)
                        .putExtra("grade", grade)
                        .putExtra("clazz", clazz)
                    )
                }

            }

        }
    }


    private fun getCompleteStatus() {
        OkGo.get<LzyResponse<CompleteStatus>>(Api.STU_COMPLETE_STATUS)
                .params("schoolId", school?.id)
                .execute(object : DialogCallback<LzyResponse<CompleteStatus>>(this) {
                    override fun onSuccess(response: Response<LzyResponse<CompleteStatus>>) {
                        val stuCompleteStatus = response.body()?.data
                        tvComNum.text = stuCompleteStatus?.complateNum
                        tvUnComNum.text = stuCompleteStatus?.unComplateNum

                        getGradeClazzList(stuCompleteStatus?.gradeComplateMap)
                    }
                })
    }


    lateinit var list: MutableList<MySection>
    private fun getGradeClazzList(gradeComplateMap: MutableMap<String, Int>?) {
        OkGo.get<LzyResponse<MutableMap<String, MutableList<String>>>>(Api.GRADE_CLAZZ_LIST)
                .params("schoolId", school?.id)
                .execute(object : DialogCallback<LzyResponse<MutableMap<String, MutableList<String>>>>(this) {
                    override fun onSuccess(response: Response<LzyResponse<MutableMap<String, MutableList<String>>>>) {
                        val dataMap = response.body()?.data

                        list = getItemData(dataMap!!, gradeComplateMap)
                        mAdapter.setNewInstance(list)
                    }
                })
    }



    fun getItemData(itemList: MutableMap<String, MutableList<String>>, gradeComplateMap: MutableMap<String, Int>?): MutableList<MySection> {
        val ml = mutableListOf<MySection>()

        itemList.forEach { (key, list) ->
            ml.add(MySection(true, key, ""+gradeComplateMap?.get(key)))
            for (item in list) {
                ml.add(MySection(false, item, key))
            }
        }
        return ml
    }
}