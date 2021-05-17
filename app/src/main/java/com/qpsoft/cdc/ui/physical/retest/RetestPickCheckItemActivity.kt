package com.qpsoft.cdc.ui.physical.retest

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.fastjson.JSON
import com.blankj.utilcode.util.CacheDiskStaticUtils
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.qpsoft.cdc.Api
import com.qpsoft.cdc.App
import com.qpsoft.cdc.R
import com.qpsoft.cdc.base.BaseActivity
import com.qpsoft.cdc.constant.Keys
import com.qpsoft.cdc.okgo.callback.DialogCallback
import com.qpsoft.cdc.okgo.model.LzyResponse
import com.qpsoft.cdc.ui.adapter.RetestPickCheckItemAdapter
import com.qpsoft.cdc.ui.entity.CheckItem
import com.qpsoft.cdc.ui.entity.CurrentPlan
import com.qpsoft.cdc.ui.entity.MySection
import kotlinx.android.synthetic.main.activity_retest_pick_checkitem.*
import java.util.stream.Collectors


class RetestPickCheckItemActivity : BaseActivity() {

    private lateinit var mAdapter: RetestPickCheckItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_retest_pick_checkitem)

        setBackBtn()
        setTitle("本次复测我负责的项目")

        rvRetestCheckItem.setHasFixedSize(true)
        rvRetestCheckItem.layoutManager = LinearLayoutManager(this)
        mAdapter = RetestPickCheckItemAdapter(R.layout.item_pick_checkitem, R.layout.pick_chekitem_section_head, null)
        rvRetestCheckItem.adapter = mAdapter

        getCurrentPlan()
    }



    private fun getCurrentPlan() {
//        OkGo.get<LzyResponse<CurrentPlan>>(Api.CURRENT_PLAN)
//                .execute(object : DialogCallback<LzyResponse<CurrentPlan>>(this) {
//                    override fun onSuccess(response: Response<LzyResponse<CurrentPlan>>) {
//                        val currentPlan = response.body()?.data
//
//                        var itemList = currentPlan?.itemList!!
//
//                        itemList = when (currentPlan.planType) {
//                            "Vision" -> {
//                                itemList.stream().filter { it.name=="视力"||it.name=="屈光"}.collect(Collectors.toList())
//                            }
//                            "CommonDisease" -> itemList.stream().filter { it.name=="视力"||it.name=="屈光"||it.name=="身高"||it.name=="体重"}.collect(Collectors.toList())
//                            "Checkup" -> itemList.stream().filter { it.name=="身高"||it.name=="体重"||it.name=="龋齿"||it.name=="沙眼"}.collect(Collectors.toList())
//                            else -> mutableListOf()
//                        }
//                        val list = getItemData(itemList)
//                        mAdapter.setNewInstance(list)
//                    }
//                })

        val currentPlan = JSON.parseObject(CacheDiskStaticUtils.getString(Keys.CURRENTPLAN), CurrentPlan::class.java)
        var itemList = currentPlan?.itemList!!

        itemList = when (currentPlan.planType) {
            "Vision" -> {
                itemList.stream().filter { it.name=="视力"||it.name=="屈光"}.collect(Collectors.toList())
            }
            "CommonDisease" -> itemList.stream().filter { it.name=="视力"||it.name=="屈光"||it.name=="身高"||it.name=="体重"}.collect(Collectors.toList())
            "Checkup" -> itemList.stream().filter { it.name=="身高"||it.name=="体重"||it.name=="龋齿"||it.name=="沙眼"}.collect(Collectors.toList())
            else -> mutableListOf()
        }
        val list = getItemData(itemList)
        mAdapter.setNewInstance(list)
    }



    fun getItemData(itemList: MutableList<CheckItem>): MutableList<MySection> {
        val ml = mutableListOf<MySection>()

        ml.add(MySection(true, "", ""))
        for (checkItem in itemList) {
            App.instance.retestCheckItemList.forEach {
                if (it.key == checkItem.key) {
                    checkItem.check = it.check
                }
            }
            ml.add(MySection(false, checkItem, ""))
        }
        return ml
    }
}