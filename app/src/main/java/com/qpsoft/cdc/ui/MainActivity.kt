package com.qpsoft.cdc.ui

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.blankj.utilcode.util.CacheDiskStaticUtils
import com.blankj.utilcode.util.LogUtils
import com.king.zxing.CameraScan
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.qpsoft.cdc.Api
import com.qpsoft.cdc.App
import com.qpsoft.cdc.R
import com.qpsoft.cdc.base.BaseActivity
import com.qpsoft.cdc.constant.Keys
import com.qpsoft.cdc.eventbus.DeviceStatusEvent
import com.qpsoft.cdc.okgo.callback.DialogCallback
import com.qpsoft.cdc.okgo.model.LzyResponse
import com.qpsoft.cdc.okgo.utils.Convert
import com.qpsoft.cdc.ui.entity.CurrentPlan
import com.qpsoft.cdc.ui.entity.QrCodeInfo
import com.qpsoft.cdc.ui.physical.GradeClazzListActivity
import com.qpsoft.cdc.ui.physical.retest.RetestTitleListActivity
import com.qpsoft.cdc.utils.BleDeviceOpUtil
import com.qpsoft.cdc.utils.EyeChartOpUtil
import com.qpsoft.cdc.utils.LevelConvert
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!checkLogin()) return

        getCurrentPlan()

        llRePickCheckItem.setOnClickListener {
            startActivity(
                Intent(this@MainActivity, PickCheckItemActivity::class.java)
                    .putExtra("isReSel", true)
            )
        }

        llReSelSchool.setOnClickListener {
            startActivity(
                Intent(this@MainActivity, SelectSchoolActivity::class.java)
                    .putExtra("isReSel", true)
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
                //ToastUtils.showShort("请先选择学校")
                startActivity(Intent(this@MainActivity, SelectSchoolActivity::class.java)
                                .putExtra("noSelSchool", true)
                )
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
                //ToastUtils.showShort("请先选择学校")
                startActivity(Intent(this@MainActivity, SelectSchoolActivity::class.java)
                        .putExtra("noSelSchoolReTest", true).putExtra("planId", planId)
                )
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

        tvEyeChartDisconn.setOnClickListener {
            EyeChartOpUtil.disConnected()
            updateDeviceStatusUi()
        }
        tvDiopterDisconn.setOnClickListener {
            BleDeviceOpUtil.diopterDisConnected()
            updateDeviceStatusUi()
        }
        tvHWDisconn.setOnClickListener {
            BleDeviceOpUtil.hwDisConnected()
            updateDeviceStatusUi()
        }
        tvBPDisconn.setOnClickListener {
            BleDeviceOpUtil.bpDisConnected()
            updateDeviceStatusUi()
        }


        refreshLayout.setOnRefreshListener {
            getCurrentPlan()
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
        LogUtils.e("---------$result")
        val qrCodeInfo = Convert.fromJson(result, QrCodeInfo::class.java)
        BleDeviceOpUtil.connectDevice(qrCodeInfo)
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)

        refreshUI()
    }

    private fun refreshUI() {
        LogUtils.e("------" + App.instance.checkItemList)
        LogUtils.e("------" + App.instance.selectSchool)

        val checkItemList = App.instance.checkItemList
        val ciTxt = if (checkItemList.size > 2) {
            checkItemList.joinToString(limit = 2) { checkItem -> checkItem.name }
        } else {
            checkItemList.joinToString { checkItem -> checkItem.name }
        }

        tvCheckItem.text = ciTxt

        val selSchool = App.instance.selectSchool
        tvSchool.text = selSchool?.name ?: "请选择"

        updateDeviceStatusUi()
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDeviceStatusEvent(connStatusEvent: DeviceStatusEvent) {
        updateDeviceStatusUi()
    }


    private fun updateDeviceStatusUi() {
        val checkItemList = App.instance.checkItemList
        val ciStr = checkItemList.joinToString { checkItem -> checkItem.name }

        if (ciStr.contains("视力")) {
            llEyeChart.visibility = View.VISIBLE
            //是否连接
            if(EyeChartOpUtil.isConnected()) {
                val eyeChartName = EyeChartOpUtil.deviceInfo()?.name
                tvEyeChartName.text = Html.fromHtml("电子视力表 已连接 <font color=\"#247CB7\">$eyeChartName</font>")
                tvEyeChartDisconn.text = "断开连接"
            } else {
                tvEyeChartName.text = "电子视力表 未连接"
                tvEyeChartDisconn.text = ""
            }
        } else {
            llEyeChart.visibility = View.GONE
            EyeChartOpUtil.disConnected()
        }
        if (ciStr.contains("屈光")) {
            llDiopter.visibility = View.VISIBLE
            //是否连接
            if(BleDeviceOpUtil.isDiopterConnected()) {
                val diopterName = BleDeviceOpUtil.diopterDeviceInfo()?.name
                tvDiopterName.text = Html.fromHtml("电脑验光仪 已连接 <font color=\"#247CB7\">$diopterName</font>")
                tvDiopterDisconn.text = "断开连接"
            } else {
                tvDiopterName.text = "电脑验光仪 未连接"
                tvDiopterDisconn.text = ""
            }
        } else {
            llDiopter.visibility = View.GONE
            BleDeviceOpUtil.diopterDisConnected()
        }
        if (ciStr.contains("身高") || ciStr.contains("体重")) {
            llHeightWeight.visibility = View.VISIBLE
            //是否连接
            if(BleDeviceOpUtil.isHWConnected()) {
                val hwName = BleDeviceOpUtil.hwDeviceInfo()?.name
                tvHWName.text = Html.fromHtml("身高体重仪 已连接 <font color=\"#247CB7\">$hwName</font>")
                tvHWDisconn.text = "断开连接"
            } else {
                tvHWName.text = "身高体重仪 未连接"
                tvHWDisconn.text = ""
            }
        } else {
            llHeightWeight.visibility = View.GONE
            BleDeviceOpUtil.hwDisConnected()
        }
        if (ciStr.contains("血压")) {
            llBloodPressure.visibility = View.VISIBLE
            //是否连接
            if(BleDeviceOpUtil.isBPConnected()) {
                val bpName = BleDeviceOpUtil.bpDeviceInfo()?.name
                tvBPName.text = Html.fromHtml("电子血压计 已连接 <font color=\"#247CB7\">$bpName</font>")
                tvBPDisconn.text = "断开连接"
            } else {
                tvBPName.text = "电子血压计 未连接"
                tvBPDisconn.text = ""
            }
        } else {
            llBloodPressure.visibility = View.GONE
            BleDeviceOpUtil.bpDisConnected()
        }
    }

    private var planId: String? = null
    private fun getCurrentPlan() {
        OkGo.get<LzyResponse<CurrentPlan>>(Api.CURRENT_PLAN)
                .execute(object : DialogCallback<LzyResponse<CurrentPlan>>(this) {
                    override fun onSuccess(response: Response<LzyResponse<CurrentPlan>>) {
                        refreshLayout.finishRefresh()
                        val currentPlan = response.body()?.data

                        if (planId != currentPlan?.id) {
                            App.instance.checkItemList.clear()
                            App.instance.selectSchool = null
                            refreshUI()
                        }
                        planId = currentPlan?.id
                        val planName = currentPlan?.name
                        val level = LevelConvert.toCh(currentPlan?.level)
                        tvPlanName.text = planName
                        tvLevel.text = level

                        if (currentPlan?.planType == "Vision") tvEnv.visibility = View.GONE else tvEnv.visibility = View.VISIBLE
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