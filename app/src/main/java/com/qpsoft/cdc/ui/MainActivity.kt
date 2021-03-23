package com.qpsoft.cdc.ui

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.view.View
import android.widget.Toast
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
import com.qpsoft.cdc.eventbus.DeviceStatusEvent
import com.qpsoft.cdc.okgo.callback.DialogCallback
import com.qpsoft.cdc.okgo.model.LzyResponse
import com.qpsoft.cdc.okgo.utils.Convert
import com.qpsoft.cdc.ui.entity.CurrentPlan
import com.qpsoft.cdc.ui.entity.QrCodeInfo
import com.qpsoft.cdc.ui.entity.Student
import com.qpsoft.cdc.ui.physical.GradeClazzListActivity
import com.qpsoft.cdc.ui.physical.PhysicalTestActivity
import com.qpsoft.cdc.ui.physical.retest.RetestTitleListActivity
import com.qpsoft.cdc.utils.BleDeviceOpUtil
import com.qpsoft.cdc.utils.EyeChartOpUtil
import com.qpsoft.cdc.utils.LevelConvert
import com.tbruyelle.rxpermissions3.RxPermissions
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_student_list.*
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

        tvEnv.setOnClickListener {
            val selSchool = App.instance.selectSchool
            if (selSchool != null) {
                var url = ""
                if (planType == "CommonDisease") {
                    url = "${Api.URL}/sc/school?planId=$planId&schoolId=${selSchool.id}&stationId=$stationId&wx=1"
                }
                if (planType == "Checkup") {
                    url = "${Api.URL}/sc/teaching?type=41&planId=$planId&schoolId=${selSchool.id}&stationId=$stationId&wx=1"
                }
                startActivity(Intent(this@MainActivity, WebViewActivity::class.java)
                    .putExtra("url", url))
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

        tvScan.setOnClickListener {
            startActivityForResult(
                    Intent(this@MainActivity, CustomCaptureActivity::class.java),
                    300
            )
        }

        //permissions
        val rxPermissions = RxPermissions(this@MainActivity)
        rxPermissions
                .request(Manifest.permission.CAMERA, Manifest.permission.ACCESS_COARSE_LOCATION)
                .subscribe { granted ->
                    if (granted) {

                    } else {
                        ToastUtils.showShort("相机权限或位置信息未打开")
                    }
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
        tvEPDisconn.setOnClickListener {
            BleDeviceOpUtil.epDisConnected()
            updateDeviceStatusUi()
        }
        tvVCDisconn.setOnClickListener {
            BleDeviceOpUtil.vcDisConnected()
            updateDeviceStatusUi()
        }


        refreshLayout.setEnableLoadMore(false)
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
            if (result?.contains("bluetooth_name")!!) {
                connBleDevice(result)
            } else if (result?.contains("id=")) {
                val startIndex = result.indexOf("=") + 1
                val stuId = result.substring(startIndex)
                handleStuId(stuId)
            } else {
                handleStuId(result)
            }

        }
    }

    private fun handleStuId(stuId: String) {
        OkGo.get<LzyResponse<Student>>(Api.STUDENT + "/" + stuId)
                .execute(object : DialogCallback<LzyResponse<Student>>(this) {
                    override fun onSuccess(response: Response<LzyResponse<Student>>) {
                        val student = response.body()?.data
                        startActivity(Intent(this@MainActivity, PhysicalTestActivity::class.java)
                                .putExtra("student", student))
                    }
                })
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
        val ciStr = checkItemList.joinToString { checkItem -> checkItem.key }

        if (ciStr.contains("vision")) {
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
        if (ciStr.contains("diopter")) {
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
        if (ciStr.contains("height") || ciStr.contains("weight")) {
            llHeightWeight.visibility = View.VISIBLE
            //是否连接
            if(BleDeviceOpUtil.isHWConnected()) {
                val hwName = BleDeviceOpUtil.hwDeviceInfo()?.name
                tvHWName.text = Html.fromHtml("身高体重秤 已连接 <font color=\"#247CB7\">$hwName</font>")
                tvHWDisconn.text = "断开连接"
            } else {
                tvHWName.text = "身高体重秤 未连接"
                tvHWDisconn.text = ""
            }
        } else {
            llHeightWeight.visibility = View.GONE
            BleDeviceOpUtil.hwDisConnected()
        }
        if (ciStr.contains("bloodPressure")) {
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
        if (ciStr.contains("eyePressure")) {
            llEyePressure.visibility = View.VISIBLE
            //是否连接
            if(BleDeviceOpUtil.isEPConnected()) {
                val epName = BleDeviceOpUtil.epDeviceInfo()?.name
                tvEPName.text = Html.fromHtml("眼压计 已连接 <font color=\"#247CB7\">$epName</font>")
                tvEPDisconn.text = "断开连接"
            } else {
                tvEPName.text = "眼压计 未连接"
                tvEPDisconn.text = ""
            }
        } else {
            llEyePressure.visibility = View.GONE
            BleDeviceOpUtil.epDisConnected()
        }
        if (ciStr.contains("vitalCapacity")) {
            llVitalCapacity.visibility = View.VISIBLE
            //是否连接
            if(BleDeviceOpUtil.isVCConnected()) {
                val vcName = BleDeviceOpUtil.vcDeviceInfo()?.name
                tvVCName.text = Html.fromHtml("肺活量 已连接 <font color=\"#247CB7\">$vcName</font>")
                tvVCDisconn.text = "断开连接"
            } else {
                tvVCName.text = "肺活量 未连接"
                tvVCDisconn.text = ""
            }
        } else {
            llVitalCapacity.visibility = View.GONE
            BleDeviceOpUtil.vcDisConnected()
        }
    }

    private var planId: String? = null
    private var stationId: String? = null
    private var planType: String? = null
    private fun getCurrentPlan() {
        OkGo.get<LzyResponse<CurrentPlan>>(Api.CURRENT_PLAN)
                .execute(object : DialogCallback<LzyResponse<CurrentPlan>>(this) {
                    override fun onSuccess(response: Response<LzyResponse<CurrentPlan>>) {
                        refreshLayout.finishRefresh()
                        val currentPlan = response.body()?.data

                        if (planId != null && planId != currentPlan?.id) {
                            App.instance.checkItemList.clear()
                            App.instance.selectSchool = null
                            App.instance.retestCheckItemList.clear()
                            refreshUI()
                        }
                        planId = currentPlan?.id
                        stationId = currentPlan?.stationId
                        planType = currentPlan?.planType
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