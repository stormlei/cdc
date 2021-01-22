package com.qpsoft.cdc.utils

import android.os.Handler
import com.blankj.utilcode.util.CacheDiskStaticUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleMtuChangedCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.qpsoft.cdc.ui.entity.QrCodeInfo

object EyeChartOpUtil {
    const val uuid_service = "0000181C-0000-1000-8000-00805F9B34FB"
    const val uuid_notify = "0000C101-0000-1000-8000-00805F9B3402"
    const val uuid_write = "0000C101-0000-1000-8000-00805F9B3403"

    private fun send(sendStr: String) {
        if (!isConnected()) {
            ToastUtils.showShort("视力表未连接")
            return
        }
        LogUtils.e("----------$sendStr")
        val bleDevice: BleDevice? = bleDevice()
        Handler().postDelayed({
            BleManager.getInstance().write(
                bleDevice,
                uuid_service,
                uuid_write,
                sendStr.toByteArray(),
                false,
                null
            )
        }, 300)
    }

    fun isConnected(): Boolean {
        return BleManager.getInstance().isConnected(bleDevice())
    }

    fun disConnected() {
        BleManager.getInstance().disconnect(bleDevice())
    }

    private fun bleDevice(): BleDevice? {
        return CacheDiskStaticUtils.getParcelable("visionBleDevice", BleDevice.CREATOR)
    }

    fun deviceInfo(): QrCodeInfo? {
        return CacheDiskStaticUtils.getSerializable("visionInfo") as QrCodeInfo?
    }


    fun setMtu() {
        BleManager.getInstance().setMtu(bleDevice(), 512, object: BleMtuChangedCallback() {
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