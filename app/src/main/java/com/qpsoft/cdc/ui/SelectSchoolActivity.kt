package com.qpsoft.cdc.ui

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.qpsoft.cdc.Api
import com.qpsoft.cdc.App
import com.qpsoft.cdc.R
import com.qpsoft.cdc.base.BaseActivity
import com.qpsoft.cdc.okgo.callback.DialogCallback
import com.qpsoft.cdc.okgo.model.LzyResponse
import com.qpsoft.cdc.ui.entity.School
import kotlinx.android.synthetic.main.activity_grade_clazz_list.*
import kotlinx.android.synthetic.main.activity_select_school.*


class SelectSchoolActivity : BaseActivity() {

    private lateinit var mAdapter: BaseQuickAdapter<School, BaseViewHolder>

    private var fromMain: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_school)

        fromMain = intent.getBooleanExtra("fromMain", false)

        setBackBtn()
        setTitle("选择学校")

        getSchool()

        rvSchool.layoutManager = LinearLayoutManager(this)
        rvSchool.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        mAdapter = object: BaseQuickAdapter<School, BaseViewHolder>(R.layout.item_select_school) {
            override fun convert(holder: BaseViewHolder, item: School) {
                holder.setText(R.id.tvName, item.name)
                if (item.name == App.instance.selectSchool?.name) {
                    holder.setTextColor(R.id.tvName, resources.getColor(R.color.color_cb7))
                }
            }

        }
        rvSchool.adapter = mAdapter

        mAdapter.setOnItemClickListener { adapter, view, position ->
            val school = mAdapter.getItem(position)
            App.instance.selectSchool = school
            if (fromMain) {
                finish()
                return@setOnItemClickListener
            }
            startActivity(Intent(this@SelectSchoolActivity, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }



    private fun getSchool() {
        OkGo.get<LzyResponse<MutableList<School>>>(Api.SCHOOL)
                .execute(object : DialogCallback<LzyResponse<MutableList<School>>>(this) {
                    override fun onSuccess(response: Response<LzyResponse<MutableList<School>>>) {
                        val schoolList = response.body()?.data

                        mAdapter.setNewInstance(schoolList)
                    }
                })
    }
}