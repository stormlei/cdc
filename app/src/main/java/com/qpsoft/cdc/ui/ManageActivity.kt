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
import com.qpsoft.cdc.ui.offline.OffLineActivity
import kotlinx.android.synthetic.main.activity_manage.*
import kotlinx.android.synthetic.main.view_vision.*

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
                        CacheDiskStaticUtils.remove(Keys.OFFLINE)
                        CacheDiskStaticUtils.remove(Keys.CURRENTPLAN)
                        App.instance.checkItemList.clear()
                        App.instance.selectSchool = null
                        App.instance.retestCheckItemList.clear()
                        startActivity(Intent(this@ManageActivity, MainActivity::class.java))
                    }
                    .negativeButton {  }

            dialog.show()
        }

        llBle.setOnClickListener {
            startActivity(Intent(this@ManageActivity, BleDeviceListActivity::class.java))
        }

        llConnHelp.setOnClickListener {
            startActivity(Intent(this@ManageActivity, WebViewActivity::class.java)
                    .putExtra("url", "https://qpa.qingpai365.com/qpsc/help/index.html"))
        }

        val offline = CacheDiskStaticUtils.getString(Keys.OFFLINE)
        if ("1" == offline) sbtnOffLine.setCheckedNoEvent(true) else sbtnOffLine.setCheckedNoEvent(false)

        llOffLine.setOnClickListener {
            if ("1" == offline) startActivity(Intent(this@ManageActivity, OffLineActivity::class.java))
        }

        sbtnOffLine.setOnCheckedChangeListener { compoundButton, isChecked ->
            if (isChecked) {
                CacheDiskStaticUtils.put(Keys.OFFLINE, "1")
                startActivity(Intent(this@ManageActivity, OffLineActivity::class.java))
            } else {
                CacheDiskStaticUtils.put(Keys.OFFLINE, "0")
            }
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