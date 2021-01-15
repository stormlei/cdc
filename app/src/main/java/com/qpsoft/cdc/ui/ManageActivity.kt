package com.qpsoft.cdc.ui

import android.content.Intent
import android.os.Bundle
import com.afollestad.materialdialogs.MaterialDialog
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
import com.qpsoft.cdc.qrcodegenerator.BleDeviceListActivity
import com.qpsoft.cdc.ui.entity.User
import kotlinx.android.synthetic.main.activity_manage.*

class ManageActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage)

        setBackBtn()
        setTitle("管理")

        getUser()

        tvLogout.setOnClickListener {

            val dialog = MaterialDialog(this)
                    .title(text="提示")
                    .message(text="你确定要退出吗？")
                    .positiveButton {
                        CacheDiskStaticUtils.remove(Keys.TOKEN)
                        App.instance.checkItemList.clear()
                        App.instance.selectSchool = null
                        startActivity(Intent(this@ManageActivity, MainActivity::class.java))
                    }
                    .negativeButton {  }

            dialog.show()
        }

        llBle.setOnClickListener {
            startActivity(Intent(this@ManageActivity, BleDeviceListActivity::class.java))
        }
    }

    private fun getUser() {
        OkGo.get<LzyResponse<User>>(Api.CURRENT_USER)
            .execute(object : DialogCallback<LzyResponse<User>>(this) {
                override fun onSuccess(response: Response<LzyResponse<User>>) {
                    val user = response.body()?.data
                    tvName.text = user?.name
                    tvUserName.text = user?.username
                }
            })
    }
}