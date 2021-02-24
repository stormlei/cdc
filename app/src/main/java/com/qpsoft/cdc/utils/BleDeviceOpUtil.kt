package com.qpsoft.cdc.utils

import android.bluetooth.BluetoothGatt
import android.os.Handler
import android.text.TextUtils
import com.blankj.utilcode.util.CacheDiskStaticUtils
import com.blankj.utilcode.util.EncodeUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.clj.fastble.BleManager
import com.clj.fastble.callback.*
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.clj.fastble.scan.BleScanRuleConfig
import com.qpsoft.cdc.App
import com.qpsoft.cdc.eventbus.DeviceNotifyDataEvent
import com.qpsoft.cdc.eventbus.DeviceStatusEvent
import com.qpsoft.cdc.model.DeviceCreate
import com.qpsoft.cdc.ui.entity.QrCodeInfo
import org.greenrobot.eventbus.EventBus
import java.util.*

object BleDeviceOpUtil {
    var uuid_service = "0000ffe0-0000-1000-8000-00805F9B34FB"
    var uuid_notify = "0000ffe1-0000-1000-8000-00805F9B34FB"
    var uuid_write = "0000ffe1-0000-1000-8000-00805F9B34FB"

    const val uuid_service_bp = "00001810-0000-1000-8000-00805f9b34fb"
    const val uuid_notify_bp = "00002a35-0000-1000-8000-00805f9b34fb"

    fun notifyData(deviceType: String) {
        var bleDevice: BleDevice? = null
        var deviceInfo: QrCodeInfo? = null
        when(deviceType) {
            "diopter" -> {
                bleDevice = diopterBleDevice()
                deviceInfo = diopterDeviceInfo()
            }
            "heightWeight" -> {
                bleDevice = hwBleDevice()
                deviceInfo = hwDeviceInfo()
            }
            "bloodPressure" -> {
                bleDevice = bpBleDevice()
                deviceInfo = bpDeviceInfo()
            }
            "eyePressure" -> {
                bleDevice = epBleDevice()
                deviceInfo = epDeviceInfo()
            }
            "vitalCapacity" -> {
                bleDevice = vcBleDevice()
                deviceInfo = vcDeviceInfo()
            }
        }
        if (deviceType == "bloodPressure") {
            uuid_service = uuid_service_bp
            uuid_notify = uuid_notify_bp
        } else {
            uuid_service = "0000ffe0-0000-1000-8000-00805F9B34FB"
            uuid_notify = "0000ffe1-0000-1000-8000-00805F9B34FB"
        }
        val receiveByteList = mutableListOf<Byte>()
        var flag = true
        BleManager.getInstance().notify(bleDevice, uuid_service, uuid_notify, object : BleNotifyCallback() {
            override fun onNotifySuccess() {
                //连接肺活量
                if(deviceType == "vitalCapacity") sendData("#db_sk_b1_ip,0b1,B901090952,B11.00,1,20,1,234,1.23,1.24,0.01,-0.01,0.01,0,24,183,52,0,2016-09-02 16:30:33,0,da".toByteArray(), deviceType)
            }

            override fun onNotifyFailure(exception: BleException?) {
            }

            override fun onCharacteristicChanged(data: ByteArray?) {
                if (deviceInfo?.model == "fr710") {
                    handleFaliao710(data!!, deviceInfo, deviceType)
                } else {
                    if (data != null) for (b in data) receiveByteList.add(b)
                    if (flag) {
                        flag = false
                        Handler().postDelayed({
                            val oriData = EncodeUtils.base64Encode2String(receiveByteList.toByteArray())
                            parseData(deviceInfo!!, oriData, deviceType)
                            receiveByteList.clear()
                            flag = true
                        }, 1200)
                    }
                }
            }
        })
    }

    private var tempList = mutableListOf<Byte>()
    private fun handleFaliao710(originByteArray: ByteArray, deviceInfo: QrCodeInfo, deviceType: String) {
        for (i in originByteArray.indices) {
            tempList.add(originByteArray[i])
        }
        if (tempList.size >= 1 && tempList[tempList.size - 1].toInt() == 0x3E) {
            val tempArray = ByteArray(tempList.size)
            for (i in tempList.indices) {
                tempArray[i] = tempList[i]
            }
            tempList.clear()
            handleReturnData(tempArray, deviceInfo, deviceType)
        }
    }

    private val allByteList = mutableListOf<Byte>()
    private fun handleReturnData(data: ByteArray, deviceInfo: QrCodeInfo, deviceType: String) {
        LogUtils.e("--------" + String(data))
        val str = String(data)
        if (str.contains("DATY")) {
            //发送右眼数据
            sendData("#!<REFR01#!>".toByteArray(), deviceType)
        }
        if (str.contains("REFR")) {
            //handleR(str)
            for (i in data.indices) {
                allByteList.add(data[i])
            }
            //发送左眼数据
            sendData("#!<REFL01#!>".toByteArray(), deviceType)
        }
        if (str.contains("REFL")) {
            //handleL(str)
            for (i in data.indices) {
                allByteList.add(data[i])
            }
            val oriData = EncodeUtils.base64Encode2String(allByteList.toByteArray())
            parseData(deviceInfo, oriData, deviceType)
        }
    }

    private fun sendData(data: ByteArray, deviceType: String) {
        var bleDevice: BleDevice? = null
        when(deviceType) {
            "diopter" -> {
                bleDevice = diopterBleDevice()
            }
            "vitalCapacity" -> {
                bleDevice = vcBleDevice()
            }
        }
        BleManager.getInstance().write(bleDevice, uuid_service, uuid_write, data, object : BleWriteCallback() {
            override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray) {}
            override fun onWriteFailure(exception: BleException) { LogUtils.e("--------$exception") }
        })
    }


    fun parseData(deviceInfo: QrCodeInfo, oriData: String, deviceType: String) {
        val deviceCreate = DeviceCreate()
        deviceCreate.type = deviceInfo.type
        deviceCreate.brand = deviceInfo.brand
        deviceCreate.model = deviceInfo.model
        deviceCreate.oriData = oriData
        val result = DeviceParseUtil.parse(deviceCreate)
        if (result.first) {
            //send receive data
            EventBus.getDefault().post(DeviceNotifyDataEvent(deviceType, result.third))
        } else {
            ToastUtils.showLong(result.second)
        }
    }

    fun initBle(app: App) {
        BleManager.getInstance().init(app)
    }

    fun connectDevice(qrCodeInfo: QrCodeInfo) {
        val mac = qrCodeInfo.bluetooth_mac
        if (TextUtils.isEmpty(mac)) {
            scanAndConnectDevice(qrCodeInfo)
            return
        }
        BleManager.getInstance().connect(mac, object : BleGattCallback() {
            override fun onStartConnect() {
            }

            override fun onConnectFail(bleDevice: BleDevice?, exception: BleException?) {
                ToastUtils.showShort("连接失败，请重试")
            }

            override fun onConnectSuccess(bleDevice: BleDevice?, gatt: BluetoothGatt, status: Int) {
                ToastUtils.showShort("连接成功")
                when (qrCodeInfo.type) {
                    "视力表" -> {
                        CacheDiskStaticUtils.put("visionInfo", qrCodeInfo)
                        CacheDiskStaticUtils.put("visionBleDevice", bleDevice)
                        EyeChartOpUtil.setMtu()
                    }
                    "验光仪" -> {
                        CacheDiskStaticUtils.put("diopterInfo", qrCodeInfo)
                        CacheDiskStaticUtils.put("diopterBleDevice", bleDevice)
                    }
                    "身高体重秤" -> {
                        CacheDiskStaticUtils.put("hwInfo", qrCodeInfo)
                        CacheDiskStaticUtils.put("hwBleDevice", bleDevice)
                    }
                    "血压计" -> {
                        CacheDiskStaticUtils.put("bpInfo", qrCodeInfo)
                        CacheDiskStaticUtils.put("bpBleDevice", bleDevice)
                        indicateBPData()
                    }
                    "眼压计" -> {
                        CacheDiskStaticUtils.put("epInfo", qrCodeInfo)
                        CacheDiskStaticUtils.put("epBleDevice", bleDevice)
                    }
                    "肺活量" -> {
                        CacheDiskStaticUtils.put("vcInfo", qrCodeInfo)
                        CacheDiskStaticUtils.put("vcBleDevice", bleDevice)
                    }
                }

                //send conn status
                EventBus.getDefault().post(DeviceStatusEvent("success"))
            }

            override fun onDisConnected(isActiveDisConnected: Boolean, device: BleDevice?, gatt: BluetoothGatt?, status: Int) {
                ToastUtils.showShort("已断开连接")

                //send conn status
                EventBus.getDefault().post(DeviceStatusEvent("fail"))
            }
        })
    }

    private fun scanAndConnectDevice(qrCodeInfo: QrCodeInfo) {
        val serviceUuids = arrayOf(UUID.fromString(EyeChartOpUtil.uuid_service), UUID.fromString(uuid_service)) //bug &关系且搜索中加了uuid才行
        val scanRuleConfig = BleScanRuleConfig.Builder()
            //.setServiceUuids(serviceUuids) // 只扫描指定的服务的设备，可选
            .setDeviceName(true, qrCodeInfo.bluetooth_name) // 只扫描指定广播名的设备，可选
            //.setDeviceMac(mac)                  // 只扫描指定mac的设备，可选
            //.setAutoConnect(isAutoConnect)      // 连接时的autoConnect参数，可选，默认false
            .setScanTimeOut(10000) // 扫描超时时间，可选，默认10秒；小于等于0表示不限制扫描时间
            .build()
        BleManager.getInstance().initScanRule(scanRuleConfig)
        BleManager.getInstance().scan(object : BleScanCallback() {
            override fun onScanStarted(success: Boolean) {
            }

            override fun onScanning(bleDevice: BleDevice?) {
                BleManager.getInstance().cancelScan()
                BleManager.getInstance().connect(bleDevice, object : BleGattCallback() {
                    override fun onStartConnect() {
                    }

                    override fun onConnectFail(bleDevice: BleDevice?, exception: BleException?) {
                        ToastUtils.showShort("连接失败，请重试")
                    }

                    override fun onConnectSuccess(bleDevice: BleDevice?, gatt: BluetoothGatt?, status: Int) {
                        ToastUtils.showShort("连接成功")
                        when (qrCodeInfo.type) {
                            "视力表" -> {
                                CacheDiskStaticUtils.put("visionInfo", qrCodeInfo)
                                CacheDiskStaticUtils.put("visionBleDevice", bleDevice)
                                EyeChartOpUtil.setMtu()
                            }
                            "验光仪" -> {
                                CacheDiskStaticUtils.put("diopterInfo", qrCodeInfo)
                                CacheDiskStaticUtils.put("diopterBleDevice", bleDevice)
                            }
                            "身高体重秤" -> {
                                CacheDiskStaticUtils.put("hwInfo", qrCodeInfo)
                                CacheDiskStaticUtils.put("hwBleDevice", bleDevice)
                            }
                            "血压计" -> {
                                CacheDiskStaticUtils.put("bpInfo", qrCodeInfo)
                                CacheDiskStaticUtils.put("bpBleDevice", bleDevice)
                                indicateBPData()
                            }
                            "眼压计" -> {
                                CacheDiskStaticUtils.put("epInfo", qrCodeInfo)
                                CacheDiskStaticUtils.put("epBleDevice", bleDevice)
                            }
                            "肺活量" -> {
                                CacheDiskStaticUtils.put("vcInfo", qrCodeInfo)
                                CacheDiskStaticUtils.put("vcBleDevice", bleDevice)
                            }
                        }

                        //send conn status
                        EventBus.getDefault().post(DeviceStatusEvent("success"))
                    }

                    override fun onDisConnected(isActiveDisConnected: Boolean, device: BleDevice?, gatt: BluetoothGatt?, status: Int) {
                        ToastUtils.showShort("已断开连接")

                        //send conn status
                        EventBus.getDefault().post(DeviceStatusEvent("fail"))
                    }

                })
            }

            override fun onScanFinished(scanResultList: MutableList<BleDevice>?) {
            }

        })
    }

    //解决点击开始按钮就断开的问题
    fun indicateBPData() {
        BleManager.getInstance().indicate(bpBleDevice(), uuid_service_bp, uuid_notify_bp, object : BleIndicateCallback() {
            override fun onIndicateSuccess() {
            }
            override fun onIndicateFailure(exception: BleException?) {
            }
            override fun onCharacteristicChanged(data: ByteArray?) {
            }
        })
    }

    //diopter
    fun isDiopterConnected(): Boolean {
        return BleManager.getInstance().isConnected(diopterBleDevice())
    }

    fun diopterDisConnected() {
        return BleManager.getInstance().disconnect(diopterBleDevice())
    }

    private fun diopterBleDevice(): BleDevice? {
        return CacheDiskStaticUtils.getParcelable("diopterBleDevice", BleDevice.CREATOR)
    }

    fun diopterDeviceInfo(): QrCodeInfo? {
        return CacheDiskStaticUtils.getSerializable("diopterInfo") as QrCodeInfo?
    }

    //hw
    fun isHWConnected(): Boolean {
        return BleManager.getInstance().isConnected(hwBleDevice())
    }

    fun hwDisConnected() {
        return BleManager.getInstance().disconnect(hwBleDevice())
    }

    private fun hwBleDevice(): BleDevice? {
        return CacheDiskStaticUtils.getParcelable("hwBleDevice", BleDevice.CREATOR)
    }

    fun hwDeviceInfo(): QrCodeInfo? {
        return CacheDiskStaticUtils.getSerializable("hwInfo") as QrCodeInfo?
    }


    //bp
    fun isBPConnected(): Boolean {
        return BleManager.getInstance().isConnected(bpBleDevice())
    }

    fun bpDisConnected() {
        return BleManager.getInstance().disconnect(bpBleDevice())
    }

    private fun bpBleDevice(): BleDevice? {
        return CacheDiskStaticUtils.getParcelable("bpBleDevice", BleDevice.CREATOR)
    }

    fun bpDeviceInfo(): QrCodeInfo? {
        return CacheDiskStaticUtils.getSerializable("bpInfo") as QrCodeInfo?
    }


    //ep
    fun isEPConnected(): Boolean {
        return BleManager.getInstance().isConnected(epBleDevice())
    }

    fun epDisConnected() {
        return BleManager.getInstance().disconnect(epBleDevice())
    }

    private fun epBleDevice(): BleDevice? {
        return CacheDiskStaticUtils.getParcelable("epBleDevice", BleDevice.CREATOR)
    }

    fun epDeviceInfo(): QrCodeInfo? {
        return CacheDiskStaticUtils.getSerializable("epInfo") as QrCodeInfo?
    }

    //vc
    fun isVCConnected(): Boolean {
        return BleManager.getInstance().isConnected(vcBleDevice())
    }

    fun vcDisConnected() {
        return BleManager.getInstance().disconnect(vcBleDevice())
    }

    private fun vcBleDevice(): BleDevice? {
        return CacheDiskStaticUtils.getParcelable("vcBleDevice", BleDevice.CREATOR)
    }

    fun vcDeviceInfo(): QrCodeInfo? {
        return CacheDiskStaticUtils.getSerializable("vcInfo") as QrCodeInfo?
    }
}
