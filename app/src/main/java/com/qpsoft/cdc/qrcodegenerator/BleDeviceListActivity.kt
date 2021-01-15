package com.qpsoft.cdc.qrcodegenerator

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleScanCallback
import com.clj.fastble.data.BleDevice
import com.qpsoft.cdc.R
import com.qpsoft.cdc.base.BaseActivity
import kotlinx.android.synthetic.main.activity_bledevice_list.*


class BleDeviceListActivity : BaseActivity() {
    private lateinit var mAdapter: BaseQuickAdapter<BleDevice, BaseViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bledevice_list)

        setBackBtn()
        setTitle("蓝牙设备二维码生成")

        getDeviceList()

        rvBleDevice.layoutManager = LinearLayoutManager(this)
        rvBleDevice.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        mAdapter = object: BaseQuickAdapter<BleDevice, BaseViewHolder>(R.layout.item_bledevice_list) {
            override fun convert(holder: BaseViewHolder, item: BleDevice) {
                holder.setText(R.id.tvName, item.name)
                holder.setText(R.id.tvRssi, "信号：${item.rssi}")
            }

        }
        rvBleDevice.adapter = mAdapter

        mAdapter.setOnItemClickListener { adapter, view, position ->
            val bleDevice = mAdapter.getItem(position)
            startActivity(Intent(this@BleDeviceListActivity, GenQrcodeActivity::class.java)
                    .putExtra("bleDevice", bleDevice)
            )
        }

        tvRefresh.setOnClickListener {
            mAdapter.data.clear()
            getDeviceList()
        }
    }

    private fun getDeviceList() {
        BleManager.getInstance().scan(object : BleScanCallback() {
            override fun onScanStarted(success: Boolean) {}
            override fun onScanning(bleDevice: BleDevice) {
                if(bleDevice.name != null) {
                    mAdapter.addData(bleDevice)
                }
            }

            override fun onScanFinished(scanResultList: List<BleDevice>) {
            }
        })
    }
}