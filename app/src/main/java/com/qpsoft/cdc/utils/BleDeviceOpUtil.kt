package com.qpsoft.cdc.utils

import android.bluetooth.BluetoothGatt
import android.os.Handler
import android.text.TextUtils
import com.blankj.utilcode.util.CacheDiskStaticUtils
import com.blankj.utilcode.util.EncodeUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleGattCallback
import com.clj.fastble.callback.BleNotifyCallback
import com.clj.fastble.callback.BleScanCallback
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
    const val uuid_service = "0000ffe0-0000-1000-8000-00805F9B34FB"
    const val uuid_notify = "0000ffe1-0000-1000-8000-00805F9B34FB"
    const val uuid_write = "0000ffe1-0000-1000-8000-00805F9B34FB"

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
        val receiveByteList = mutableListOf<Byte>()
        var flag = true
        BleManager.getInstance().notify(bleDevice, uuid_service, uuid_notify, object : BleNotifyCallback() {
            override fun onNotifySuccess() {
            }

            override fun onNotifyFailure(exception: BleException?) {
            }

            override fun onCharacteristicChanged(data: ByteArray?) {
                if(data != null ) for (b in data) receiveByteList.add(b)
                if (flag) {
                    flag = false
                    Handler().postDelayed({
                        val oriData = EncodeUtils.base64Encode2String(receiveByteList.toByteArray())
                        parseData(deviceInfo!!, oriData, deviceType)
                        receiveByteList.clear()
                        flag = true
                    }, 2000)
                }
            }
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
                when(qrCodeInfo.type) {
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
        BleManager.getInstance().scan(object: BleScanCallback() {
            override fun onScanStarted(success: Boolean) {
            }

            override fun onScanning(bleDevice: BleDevice?) {
                BleManager.getInstance().cancelScan()
                BleManager.getInstance().connect(bleDevice, object: BleGattCallback() {
                    override fun onStartConnect() {
                    }

                    override fun onConnectFail(bleDevice: BleDevice?, exception: BleException?) {
                        ToastUtils.showShort("连接失败，请重试")
                    }

                    override fun onConnectSuccess(bleDevice: BleDevice?, gatt: BluetoothGatt?, status: Int) {
                        ToastUtils.showShort("连接成功")
                        when(qrCodeInfo.type) {
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
