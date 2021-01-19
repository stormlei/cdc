package com.qpsoft.cdc.ui

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.alibaba.fastjson.JSON
import com.blankj.utilcode.util.CacheDiskStaticUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.king.zxing.CameraScan
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
import com.qpsoft.cdc.ui.physical.GradeClazzListActivity
import com.qpsoft.cdc.ui.physical.retest.RetestTitleListActivity
import com.qpsoft.cdc.utils.HC08OpUtil
import com.qpsoft.cdc.utils.LevelConvert
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!checkLogin()) return

        getCurrentPlan()

        llRePickCheckItem.setOnClickListener {
            startActivity(
                Intent(this@MainActivity, PickCheckItemActivity::class.java)
                    .putExtra("fromMain", true)
            )
        }

        llReSelSchool.setOnClickListener {
            startActivity(
                Intent(this@MainActivity, SelectSchoolActivity::class.java)
                    .putExtra("fromMain", true)
            )
        }

        tvGradeClazzList.setOnClickListener {
            val selSchool = App.instance.selectSchool
            if (selSchool != null) {
                startActivity(
                    Intent(this@MainActivity, GradeClazzListActivity::class.java)
                        .putExtra("school", selSchool)
                )
            } else {
                ToastUtils.showShort("请先选择学校")
            }
        }

        tvReTest.setOnClickListener {
            val selSchool = App.instance.selectSchool
            if (selSchool != null) {
                startActivity(
                    Intent(this@MainActivity, RetestTitleListActivity::class.java)
                        .putExtra("school", selSchool).putExtra("planId", planId)
                )
            } else {
                ToastUtils.showShort("请先选择学校")
            }
        }

        tvManage.setOnClickListener {
            startActivity(Intent(this@MainActivity, ManageActivity::class.java))
        }

        ivScan.setOnClickListener {
            startActivityForResult(
                Intent(this@MainActivity, CustomCaptureActivity::class.java),
                300
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 300 && resultCode == RESULT_OK) {
            var result = data?.getStringExtra("result")
            if (result == null) {
                result = CameraScan.parseScanResult(data)
            }
            connBleDevice(result)
        }
    }

    private fun connBleDevice(result: String?) {
        val deviceObj = JSON.parseObject(result)
        val mac = deviceObj.getString("bluetooth_mac")
        HC08OpUtil.connectBleDevice(mac)
    }

    override fun onResume() {
        super.onResume()

        LogUtils.e("------" + App.instance.checkItemList)
        LogUtils.e("------" + App.instance.selectSchool)

        val checkItemList = App.instance.checkItemList
        val ciStr = checkItemList.joinToString { checkItem -> checkItem.name }
        tvCheckItem.text = ciStr

        val selSchool = App.instance.selectSchool
        tvSchool.text = selSchool?.name ?: "请选择"

        handleDeviceUI(ciStr)
    }

    private fun handleDeviceUI(ciStr: String) {
        if (ciStr.contains("视力")) {
            llEyeChart.visibility = View.VISIBLE
            //是否连接
        } else {
            llEyeChart.visibility = View.GONE
        }
        if (ciStr.contains("屈光")) {
            llDiopter.visibility = View.VISIBLE
            //是否连接
        } else {
            llDiopter.visibility = View.GONE
        }
    }

    private var planId: String? = null
    private fun getCurrentPlan() {
        OkGo.get<LzyResponse<CurrentPlan>>(Api.CURRENT_PLAN)
                .execute(object : DialogCallback<LzyResponse<CurrentPlan>>(this) {
                    override fun onSuccess(response: Response<LzyResponse<CurrentPlan>>) {
                        val currentPlan = response.body()?.data

                        planId = currentPlan?.id
                        val planName = currentPlan?.name
                        val level = LevelConvert.toCh(currentPlan?.level)
                        tvPlanName.text = planName
                        tvLevel.text = level
                    }
                })
    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (!checkLogin()) return
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


    override fun onBackPressed() {
        exitApp()
    }

    private var exitTime: Long = 0

    private fun exitApp() {
        if (System.currentTimeMillis() - exitTime > 2000) {
            Toast.makeText(this, "再按一次退出应用程序", Toast.LENGTH_SHORT).show()
            exitTime = System.currentTimeMillis()
        } else {
            finish()
        }
    }

}