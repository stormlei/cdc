package com.qpsoft.cdc.utils

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.text.TextUtils
import com.blankj.utilcode.util.CacheDiskStaticUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleGattCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.qpsoft.cdc.App

object HC08OpUtil {
    const val uuid_server = "0000ffe0-0000-1000-8000-00805F9B34FB"
    const val uuid_notify = "0000ffe1-0000-1000-8000-00805F9B34FB"
    const val uuid_write = "0000ffe1-0000-1000-8000-00805F9B34FB"


    fun initBle(app: App) {
        BleManager.getInstance().init(app)
    }

    fun connectBleDevice(mac: String) {
        BleManager.getInstance().connect(mac, object : BleGattCallback() {
            override fun onStartConnect() {
            }

            override fun onConnectFail(bleDevice: BleDevice?, exception: BleException?) {
            }

            override fun onConnectSuccess(bleDevice: BleDevice?, gatt: BluetoothGatt, status: Int) {
                ToastUtils.showShort("连接成功")
            }

            override fun onDisConnected(isActiveDisConnected: Boolean, device: BleDevice?, gatt: BluetoothGatt?, status: Int) {

            }
        })
    }

    fun isConnected(): Boolean {
        val bleDevice: BleDevice = getBleDevice() ?: return false
        return BleManager.getInstance().isConnected(bleDevice)
    }

    fun sendMsg(msg: String) {

    }

    private fun getBleDevice(): BleDevice? {
        val bleAddress: String = CacheDiskStaticUtils.getString("hc08")
        //LogUtils.e("----------"+bleAddress);
        if (!TextUtils.isEmpty(bleAddress)) {
            val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            return BleDevice(mBluetoothAdapter.getRemoteDevice(bleAddress))
        }
        ToastUtils.showShort("蓝牙未连接")
        return null
    }
}