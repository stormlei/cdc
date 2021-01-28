package com.qpsoft.cdc.ui.physical.retest

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.qpsoft.cdc.Api
import com.qpsoft.cdc.R
import com.qpsoft.cdc.base.BaseActivity
import com.qpsoft.cdc.okgo.callback.DialogCallback
import com.qpsoft.cdc.okgo.model.LzyResponse
import com.qpsoft.cdc.ui.adapter.StudentAdapter
import com.qpsoft.cdc.ui.entity.*
import com.qpsoft.cdc.ui.physical.GradeClazzListActivity
import kotlinx.android.synthetic.main.activity_retest_list.*
import java.util.*
import kotlin.math.roundToInt


class RetestListActivity : BaseActivity() {

    private lateinit var mAdapter: StudentAdapter

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

        rvRetest.setLayoutManager(LinearLayoutManager(this))
        rvRetest.recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        rvRetest.setOverlayStyle_MaterialDesign(R.color.color_cb7)
        mAdapter = StudentAdapter(this)
        rvRetest.setAdapter(mAdapter)

        mAdapter.setOnItemContentClickListener { v, originalPosition, currentPosition, entity ->
            startActivity(
                Intent(this@RetestListActivity, ReTestActivity::class.java)
                    .putExtra("student", entity)
                    .putExtra("retestTitle", retestTitle)
                    .putExtra("planType", planType)
            )
        }

        tvRetestStuList.setOnClickListener {
            startActivity(Intent(this@RetestListActivity, GradeClazzListActivity::class.java)
                .putExtra("school", school)
                .putExtra("isRetest", true)
                .putExtra("retestTitle", retestTitle)
                .putExtra("planType", planType)
            )
        }
    }

    override fun onStart() {
        super.onStart()
        getRetestSummary()
        getRetestList()
    }


    private fun getRetestList() {
        OkGo.get<LzyResponse<Page<MutableList<Student>>>>(Api.RETEST)
            .params("schoolId", school?.id)
            .params("planId", planId)
            .params("title", retestTitle)
            .execute(object : DialogCallback<LzyResponse<Page<MutableList<Student>>>>(this) {
                override fun onSuccess(response: Response<LzyResponse<Page<MutableList<Student>>>>) {
                    val studentList = response.body()?.data?.items

                    mAdapter.setDatas(studentList)
                }
            })
    }

    private fun getRetestSummary() {
        var url = Api.RETEST_SUMMARY_VISION
        when(planType) {
            "Vision" -> url = Api.RETEST_SUMMARY_VISION
            "CommonDisease" -> url = Api.RETEST_SUMMARY_HEIGHT_WEIGHT
            "Checkup" -> url = Api.RETEST_SUMMARY_ALL
        }
        OkGo.get<LzyResponse<RetestSummary>>(url)
            .params("schoolId", school?.id)
            .params("planId", planId)
            .params("title", retestTitle)
                .execute(object : DialogCallback<LzyResponse<RetestSummary>>(this) {
                    override fun onSuccess(response: Response<LzyResponse<RetestSummary>>) {
                        val retestSummary = response.body()?.data
                        tvRecordCount.text = "" + retestSummary?.recordCount
                        tvRetestCount.text = "" + retestSummary?.retestCount
                        tvRetestItemCount.text = "" + retestSummary?.retestItemCount
                        tvErrorCount.text = "" + retestSummary?.errorCount
                        if (retestSummary?.retestItemCount != 0) {
                            val rate = (retestSummary?.errorCount!!) * 100f / (retestSummary.retestItemCount)
                            tvErrorRate.text = "" + rate.roundToInt() + "%"
                        }
                    }
                })
    }
}