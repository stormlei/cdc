package com.qpsoft.cdc.ui

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.qpsoft.cdc.Api
import com.qpsoft.cdc.App
import com.qpsoft.cdc.R
import com.qpsoft.cdc.base.BaseActivity
import com.qpsoft.cdc.okgo.callback.DialogCallback
import com.qpsoft.cdc.okgo.model.LzyResponse
import com.qpsoft.cdc.ui.adapter.PickCheckItemAdapter
import com.qpsoft.cdc.ui.entity.CheckItem
import com.qpsoft.cdc.ui.entity.CurrentPlan
import com.qpsoft.cdc.ui.entity.MySection
import kotlinx.android.synthetic.main.activity_pick_checkitem.*


class PickCheckItemActivity : BaseActivity() {

    private lateinit var mAdapter: PickCheckItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pick_checkitem)

        setBackBtn()
        setTitle("本次筛查我负责的项目")

        getCurrentPlan()

        rvCheckItem.layoutManager = LinearLayoutManager(this)
        mAdapter = PickCheckItemAdapter(
            R.layout.item_pick_checkitem,
            R.layout.def_section_head, null)
        rvCheckItem.adapter = mAdapter

        tvNext.setOnClickListener {
            startActivity(Intent(this@PickCheckItemActivity, SelectSchoolActivity::class.java))
        }
    }



    private fun getCurrentPlan() {
        OkGo.get<LzyResponse<CurrentPlan>>(Api.CURRENT_PLAN)
                .execute(object : DialogCallback<LzyResponse<CurrentPlan>>(this) {
                    override fun onSuccess(response: Response<LzyResponse<CurrentPlan>>) {
                        val currentPlan = response.body()?.data

                        val itemList = currentPlan?.itemList
                        val list = getItemData(itemList)
                        mAdapter.setNewInstance(list)
                    }
                })
    }



    fun getItemData(itemList: MutableList<CheckItem>?): MutableList<MySection> {
        val ml = mutableListOf<MySection>()

        val gb = itemList?.groupBy { it.group }
        gb?.forEach { s, list ->
            ml.add(MySection(true, s))
            for (checkItem in list) {
                App.instance.checkItemList.forEach {
                    if (it.key == checkItem.key) {
                        checkItem.check = it.check
                    }
                }
                ml.add(MySection(false, checkItem))
            }
        }
        return ml
    }
}