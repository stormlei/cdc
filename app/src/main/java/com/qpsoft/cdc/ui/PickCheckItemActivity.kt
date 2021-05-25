package com.qpsoft.cdc.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.fastjson.JSON
import com.blankj.utilcode.util.CacheDiskStaticUtils
import com.blankj.utilcode.util.LogUtils
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.qpsoft.cdc.Api
import com.qpsoft.cdc.App
import com.qpsoft.cdc.R
import com.qpsoft.cdc.base.BaseActivity
import com.qpsoft.cdc.constant.Keys
import com.qpsoft.cdc.okgo.callback.DialogCallback
import com.qpsoft.cdc.okgo.model.LzyResponse
import com.qpsoft.cdc.ui.adapter.PickCheckItemAdapter
import com.qpsoft.cdc.ui.entity.CheckItem
import com.qpsoft.cdc.ui.entity.CurrentPlan
import com.qpsoft.cdc.ui.entity.MySection
import com.qpsoft.cdc.utils.PlanTypeConvert
import kotlinx.android.synthetic.main.activity_pick_checkitem.*


class PickCheckItemActivity : BaseActivity() {

    private lateinit var mAdapter: PickCheckItemAdapter

    private var isReSel: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pick_checkitem)

        isReSel = intent.getBooleanExtra("isReSel", false)

        setBackBtn()
        setTitle("本次筛查我负责的项目")

        rvCheckItem.setHasFixedSize(true)
        rvCheckItem.layoutManager = LinearLayoutManager(this)
        mAdapter = PickCheckItemAdapter(R.layout.item_pick_checkitem, R.layout.pick_chekitem_section_head, null)
        rvCheckItem.adapter = mAdapter

        tvNext.setOnClickListener {
            startActivity(Intent(this@PickCheckItemActivity, SelectSchoolActivity::class.java))
        }

        if (isReSel) {
            tvNext.visibility = View.GONE
        }

        val offline = CacheDiskStaticUtils.getString(Keys.OFFLINE)
        if ("1" == offline) {
            getCurrentPlanLocal()
        } else {
            getCurrentPlan()
        }
    }



    private fun getCurrentPlanLocal() {
        LogUtils.e("----"+CacheDiskStaticUtils.getString(Keys.CURRENTPLAN))
        val currentPlan = JSON.parseObject(CacheDiskStaticUtils.getString(Keys.CURRENTPLAN), CurrentPlan::class.java)
        val itemList = currentPlan?.itemList!!
        val list = getItemData(itemList, currentPlan.planType)
        mAdapter.setNewInstance(list)
    }


    private fun getCurrentPlan() {
        OkGo.get<LzyResponse<CurrentPlan>>(Api.CURRENT_PLAN)
                .execute(object : DialogCallback<LzyResponse<CurrentPlan>>(this) {
                    override fun onSuccess(response: Response<LzyResponse<CurrentPlan>>) {
                        val currentPlan = response.body()?.data

                        val itemList = currentPlan?.itemList!!
                        val list = getItemData(itemList, currentPlan.planType)
                        mAdapter.setNewInstance(list)
                    }
                })
    }



    fun getItemData(itemList: MutableList<CheckItem>, planType: String?): MutableList<MySection> {
        val ml = mutableListOf<MySection>()

        if (planType == "Vision" || planType == "CommonDisease" || planType == "Nation") {
            ml.add(MySection(true, "", ""))
            for (checkItem in itemList) {
                App.instance.checkItemList.forEach {
                    if (it.key == checkItem.key) {
                        checkItem.check = it.check
                    }
                }
                ml.add(MySection(false, checkItem, ""))
            }
            return ml
        }

        val gb = itemList.groupBy { it.group }
        gb.forEach { (s, list) ->
            ml.add(MySection(true, s, ""))
            for (checkItem in list) {
                App.instance.checkItemList.forEach {
                    if (it.key == checkItem.key) {
                        checkItem.check = it.check
                    }
                }
                ml.add(MySection(false, checkItem, ""))
            }
        }
        return ml
    }
}