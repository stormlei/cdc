package com.qpsoft.cdc.ui

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import com.blankj.utilcode.util.CacheDiskStaticUtils
import com.blankj.utilcode.util.LogUtils
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.qpsoft.cdc.Api
import com.qpsoft.cdc.App
import com.qpsoft.cdc.R
import com.qpsoft.cdc.base.BaseActivity
import com.qpsoft.cdc.constant.Keys
import com.qpsoft.cdc.utils.LevelConvert
import com.qpsoft.cdc.ui.entity.CurrentPlan
import com.qpsoft.cdc.okgo.callback.DialogCallback
import com.qpsoft.cdc.okgo.model.LzyResponse
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //CacheDiskStaticUtils.remove(Keys.TOKEN)
        if (!checkLogin()) return

        getCurrentPlan()

        llRePickCheckItem.setOnClickListener {
            startActivity(Intent(this@MainActivity, PickCheckItemActivity::class.java)
                    .putExtra("fromMain", true))
        }

        llReSelSchool.setOnClickListener {
            startActivity(Intent(this@MainActivity, SelectSchoolActivity::class.java)
                    .putExtra("fromMain", true))
        }


    }

    override fun onResume() {
        super.onResume()

        LogUtils.e("------"+ App.instance.checkItemList)
        LogUtils.e("------"+ App.instance.selectSchool)

        val checkItemList = App.instance.checkItemList
        val ciStr = checkItemList.joinToString { checkItem -> checkItem.name }
        tvCheckItem.text = ciStr

        val selSchool = App.instance.selectSchool
        tvSchool.text = selSchool?.name ?: "请选择"
    }

    private fun getCurrentPlan() {
        OkGo.get<LzyResponse<CurrentPlan>>(Api.CURRENT_PLAN)
                .execute(object : DialogCallback<LzyResponse<CurrentPlan>>(this) {
                    override fun onSuccess(response: Response<LzyResponse<CurrentPlan>>) {
                        val currentPlan = response.body()?.data

                        val planName = currentPlan?.name
                        val level = LevelConvert.toCh(currentPlan?.level)
                        tvPlanName.text = planName
                        tvLevel.text = level
                    }
                })
    }








    private fun checkLogin(): Boolean {
        val token = CacheDiskStaticUtils.getString(Keys.TOKEN)
        if (TextUtils.isEmpty(token)) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return false
        }
        return true
    }



}