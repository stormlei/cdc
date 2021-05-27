package com.qpsoft.cdc.ui.physical.retest

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.alibaba.fastjson.JSON
import com.blankj.utilcode.util.CacheDiskStaticUtils
import com.blankj.utilcode.util.LogUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.qpsoft.cdc.Api
import com.qpsoft.cdc.App
import com.qpsoft.cdc.R
import com.qpsoft.cdc.base.BaseActivity
import com.qpsoft.cdc.constant.Keys
import com.qpsoft.cdc.okgo.callback.DialogCallback
import com.qpsoft.cdc.okgo.model.LzyResponse
import com.qpsoft.cdc.ui.entity.CurrentPlan
import com.qpsoft.cdc.ui.entity.RetestTitle
import com.qpsoft.cdc.ui.entity.School
import com.qpsoft.cdc.utils.LevelConvert
import kotlinx.android.synthetic.main.activity_retest_title_list.*
import kotlinx.android.synthetic.main.activity_retest_title_list.tvLevel
import kotlinx.android.synthetic.main.activity_retest_title_list.tvPlanName
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt


class RetestTitleListActivity : BaseActivity() {

    private lateinit var mAdapter: BaseQuickAdapter<RetestTitle, BaseViewHolder>

    private var school: School? = null
    private var planId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_retest_title_list)

        school = intent.getParcelableExtra("school")
        planId = intent.getStringExtra("planId")

        setBackBtn()
        setTitle(school?.name+"")

        rvRetestTitle.layoutManager = LinearLayoutManager(this)
        rvRetestTitle.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        mAdapter = object: BaseQuickAdapter<RetestTitle, BaseViewHolder>(R.layout.item_retest_title_list) {
            override fun convert(holder: BaseViewHolder, item: RetestTitle) {
                holder.setText(R.id.tvTitle, item.title)
                if (item.allStatisticalTable != null) {
                    holder.setText(R.id.tvRetestCount, ""+item.allStatisticalTable.retestCount)
                    holder.setText(R.id.tvRetestItemCount, ""+item.allStatisticalTable.retestItemCount)
                    holder.setText(R.id.tvErrorCount, ""+item.allStatisticalTable.errorCount)
                    val rate = (item.allStatisticalTable.errorCount) * 100f/(item.allStatisticalTable.retestItemCount)
                    holder.setText(R.id.tvErrorRate, ""+rate.roundToInt()+"%")
                }
            }

        }
        rvRetestTitle.adapter = mAdapter

        mAdapter.setOnItemClickListener { adapter, view, position ->
            val retestTitle = mAdapter.getItem(position)
            startActivity(Intent(this@RetestTitleListActivity, RetestListActivity::class.java)
                .putExtra("school", school)
                .putExtra("planId", planId)
                .putExtra("retestTitle", retestTitle.title)
                .putExtra("planType", planType)
            )
        }



        tvCreateRetestTitle.setOnClickListener {
            MaterialDialog(this).show {
                input(hint = "请输入复测组名称") {dialog, text ->
                    retestTitleList?.add(RetestTitle(text.toString()))
                    mAdapter.setNewInstance(retestTitleList)
                }
                positiveButton {  }
                negativeButton {  }
            }

        }

        llRetestPickCheckItem.setOnClickListener {
            startActivity(
                    Intent(this@RetestTitleListActivity, RetestPickCheckItemActivity::class.java)
            )
        }
    }

    override fun onStart() {
        super.onStart()
        getCurrentPlan()

        val offline = CacheDiskStaticUtils.getString(Keys.OFFLINE)
        if ("1" == offline) {
            getRetestTitleListLocal()
        } else {
            getRetestTitleList()
        }

        val checkItemList = App.instance.retestCheckItemList
        tvRetestCheckItem.text = "我负责的项目："+checkItemList.joinToString { checkItem -> checkItem.name }
    }


    private var retestTitleList: MutableList<RetestTitle>? = null
    private fun getRetestTitleList() {
        OkGo.get<LzyResponse<MutableList<RetestTitle>>>(Api.RETEST_TITLE_LIST)
            .params("schoolId", school?.id)
            .params("planId", planId)
            .params("expand", "all")
            .execute(object : DialogCallback<LzyResponse<MutableList<RetestTitle>>>(this) {
                override fun onSuccess(response: Response<LzyResponse<MutableList<RetestTitle>>>) {
                    retestTitleList = response.body()?.data
                    val customTitle = SimpleDateFormat("yyyy年MM月dd日").format(Date())
                    if (retestTitleList?.size == 0) {
                        retestTitleList!!.add(RetestTitle(customTitle))
                    } else {
                        val titleList = retestTitleList?.map { it.title }
                        if (!titleList?.contains(customTitle)!!) {
                            retestTitleList!!.add(0, RetestTitle(customTitle))
                        }
                    }
                    mAdapter.setNewInstance(retestTitleList)
                }
            })
    }

    private var planType: String? = null
    private fun getCurrentPlan() {
        val currentPlan = JSON.parseObject(CacheDiskStaticUtils.getString(Keys.CURRENTPLAN), CurrentPlan::class.java)
        LogUtils.e("-------"+currentPlan)
        val planName = currentPlan?.name
        val level = LevelConvert.toCh(currentPlan?.level)
        tvPlanName.text = planName
        tvLevel.text = level
        planType = currentPlan?.planType
        val retestItem = when (currentPlan?.planType) {
            "Vision" -> "视力、屈光"
            "CommonDisease","Nation" -> "视力、屈光、身高、体重"
            "Checkup" -> "视力、屈光、身高、体重"
            else -> ""
        }
        tvRetestMustCheckItem.text = "复测必查项目：$retestItem"
    }


    //<!------------------ local ----------------->
    private fun getRetestTitleListLocal() {
        retestTitleList = mutableListOf()
        val customTitle = SimpleDateFormat("yyyy年MM月dd日").format(Date())
        if (retestTitleList?.size == 0) {
            retestTitleList!!.add(RetestTitle(customTitle))
        } else {
            val titleList = retestTitleList?.map { it.title }
            if (!titleList?.contains(customTitle)!!) {
                retestTitleList!!.add(0, RetestTitle(customTitle))
            }
        }
        mAdapter.setNewInstance(retestTitleList)
    }
}