package com.qpsoft.cdc.ui

import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.LogUtils
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.qpsoft.cdc.Api
import com.qpsoft.cdc.R
import com.qpsoft.cdc.base.BaseActivity
import com.qpsoft.cdc.okgo.callback.DialogCallback
import com.qpsoft.cdc.okgo.model.LzyResponse
import com.qpsoft.cdc.ui.adapter.GradeClazzListAdapter
import com.qpsoft.cdc.ui.entity.*
import kotlinx.android.synthetic.main.activity_grade_clazz_list.*


class GradeClazzListActivity : BaseActivity() {

    private lateinit var mAdapter: GradeClazzListAdapter

    private var school: School? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grade_clazz_list)

        school = intent.getParcelableExtra("school")

        setBackBtn()
        setTitle(school?.name)

        getGradeClazzList()

        rvGradeClazz.layoutManager = LinearLayoutManager(this)
        rvGradeClazz.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL))
        mAdapter = GradeClazzListAdapter(android.R.layout.simple_list_item_1, R.layout.def_section_head, null)
        rvGradeClazz.adapter = mAdapter
    }



    private fun getGradeClazzList() {
        OkGo.get<LzyResponse<MutableMap<String, MutableList<String>>>>(Api.GRADE_CLAZZ_LIST)
                .params("schoolId", school?.id)
                .execute(object : DialogCallback<LzyResponse<MutableMap<String, MutableList<String>>>>(this) {
                    override fun onSuccess(response: Response<LzyResponse<MutableMap<String, MutableList<String>>>>) {
                        val dataMap = response.body()?.data

                        val list = getItemData(dataMap!!)
                        mAdapter.setNewInstance(list)
                    }
                })
    }



    fun getItemData(itemList: MutableMap<String, MutableList<String>>): MutableList<MySection> {
        val ml = mutableListOf<MySection>()

        itemList.forEach { (key, list) ->
            ml.add(MySection(true, key))
            for (item in list) {
                ml.add(MySection(false, item))
            }
        }
        return ml
    }
}