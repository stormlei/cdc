package com.qpsoft.cdc.ui.physical.retest

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.qpsoft.cdc.Api
import com.qpsoft.cdc.R
import com.qpsoft.cdc.base.BaseActivity
import com.qpsoft.cdc.okgo.callback.DialogCallback
import com.qpsoft.cdc.okgo.model.LzyResponse
import com.qpsoft.cdc.ui.physical.GradeClazzListActivity
import com.qpsoft.cdc.ui.entity.CurrentPlan
import com.qpsoft.cdc.ui.entity.RetestTitle
import com.qpsoft.cdc.ui.entity.School
import kotlinx.android.synthetic.main.activity_retest_list.*
import java.text.SimpleDateFormat
import java.util.*


class RetestListActivity : BaseActivity() {

    private lateinit var mAdapter: BaseQuickAdapter<RetestTitle, BaseViewHolder>

    private var school: School? = null
    private var planId: String? = null
    private var retestTitle: String? = null
    private var planType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_retest_list)

        school = intent.getParcelableExtra("school")
        planId = intent.getStringExtra("planId")
        retestTitle = intent.getStringExtra("retestTitle")
        planType = intent.getStringExtra("planType")

        setBackBtn()
        setTitle(school?.name)

        tvRetestTitle.text = retestTitle
        when(planType) {
            "Vision" -> tvPlanType.text = "视力复测质控"
            "CommonDisease" -> tvPlanType.text = "形态复测质控"
            "Checkup" -> tvPlanType.text = "体检复测质控"
        }

        getRetestSummary()
        //getRetestList()

        rvRetest.layoutManager = LinearLayoutManager(this)
        rvRetest.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        mAdapter = object: BaseQuickAdapter<RetestTitle, BaseViewHolder>(R.layout.item_retest_list) {
            override fun convert(holder: BaseViewHolder, item: RetestTitle) {
                holder.setText(R.id.tvTitle, item.title)
            }

        }
        rvRetest.adapter = mAdapter

        mAdapter.setOnItemClickListener { adapter, view, position ->
            val retestTitle = mAdapter.getItem(position)
            //startActivity(Intent(this@RetestListActivity, MainActivity::class.java))
        }

        tvStuList.setOnClickListener {
            startActivity(Intent(this@RetestListActivity, GradeClazzListActivity::class.java)
                .putExtra("school", school)
                .putExtra("isRetest", true)
            )
        }
    }


    private var retestTitleList: MutableList<RetestTitle>? = null
    private fun getRetestList() {
        OkGo.get<LzyResponse<MutableList<RetestTitle>>>(Api.RETEST_TITLE_LIST)
            .params("schoolId", school?.id)
            .params("planId", planId)
            .execute(object : DialogCallback<LzyResponse<MutableList<RetestTitle>>>(this) {
                override fun onSuccess(response: Response<LzyResponse<MutableList<RetestTitle>>>) {
                    retestTitleList = response.body()?.data
                    val customTitle = SimpleDateFormat("yyyy年MM月dd日").format(Date())
                    if (retestTitleList?.size == 0) {
                        retestTitleList!!.add(RetestTitle(customTitle))
                    } else {
                        if (!retestTitleList?.contains(customTitle)!!) {
                            retestTitleList!!.add(0, RetestTitle(customTitle))
                        }
                    }
                    mAdapter.setNewInstance(retestTitleList)
                }
            })
    }

    private fun getRetestSummary() {
        var url = Api.RETEST_SUMMARY_VISION
        when(planType) {
            "Vision" -> url = Api.RETEST_SUMMARY_VISION
            "CommonDisease" -> url = Api.RETEST_SUMMARY_VISION
            "Checkup" -> url = Api.RETEST_SUMMARY_VISION
        }
        OkGo.get<LzyResponse<CurrentPlan>>(url)
            .params("schoolId", school?.id)
            .params("planId", planId)
            .params("title", retestTitle)
                .execute(object : DialogCallback<LzyResponse<CurrentPlan>>(this) {
                    override fun onSuccess(response: Response<LzyResponse<CurrentPlan>>) {
                        val currentPlan = response.body()?.data
                    }
                })
    }
}