package com.qpsoft.cdc.utils

import android.bluetooth.BluetoothAdapter
import android.os.Handler
import android.text.TextUtils
import com.blankj.utilcode.util.CacheDiskStaticUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleMtuChangedCallback
import com.clj.fastble.callback.BleWriteCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException

object EyeChartOpUtil {
    private const val uuid_service = "0000181C-0000-1000-8000-00805F9B34FB"
    private const val uuid_notify = "0000C101-0000-1000-8000-00805F9B3402"
    private const val uuid_write = "0000C101-0000-1000-8000-00805F9B3403"

    private fun send(sendStr: String) {
        if (!isConnected()) {
            ToastUtils.showShort("视力表未连接")
            return
        }
        LogUtils.e("----------$sendStr")
        val bleDevice: BleDevice? = getBleDevice()
        Handler().postDelayed({
            BleManager.getInstance().write(bleDevice, uuid_service, uuid_write, sendStr.toByteArray(), false, null)
        }, 300)
    }

    fun isConnected(): Boolean {
        val bleDevice: BleDevice = getBleDevice() ?: return false
        return BleManager.getInstance().isConnected(bleDevice)
    }

    private fun disconnectEyeChart() {
        val bleDevice: BleDevice = getBleDevice() ?: return
        CacheDiskStaticUtils.remove("eyechart")
        BleManager.getInstance().disconnect(bleDevice)
    }

    private fun getBleDevice(): BleDevice? {
        val bleAddress: String = CacheDiskStaticUtils.getString("eyechart")
        //LogUtils.e("----------"+bleAddress);
        if (!TextUtils.isEmpty(bleAddress)) {
            val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            return BleDevice(mBluetoothAdapter.getRemoteDevice(bleAddress))
        }
        ToastUtils.showShort("视力表未连接")
        return null
    }

    private fun setMtu() {
        val bleDevice: BleDevice = getBleDevice() ?: return
        BleManager.getInstance().setMtu(bleDevice, 512, object : BleMtuChangedCallback() {
            override fun onSetMTUFailure(exception: BleException) {
                LogUtils.e("---------$exception")
                ToastUtils.showShort("mtu设置异常，请联系厂商")
            }

            override fun onMtuChanged(mtu: Int) {
                LogUtils.e("---------$mtu")
            }
        })
    }
}