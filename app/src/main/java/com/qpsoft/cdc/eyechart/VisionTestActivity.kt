package com.qpsoft.cdc.eyechart

import android.os.Bundle
import com.blankj.utilcode.util.ToastUtils
import com.qpsoft.cdc.R
import com.qpsoft.cdc.base.BaseActivity
import kotlinx.android.synthetic.main.activity_vision_test.*


class VisionTestActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vision_test)

        tvName.setText("222222")
    }

    override fun onBackPressed() {
        exitApp()
    }


    private var exitTime: Long = 0
    private fun exitApp() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            ToastUtils.showShort("再按一次退出视力筛查测试")
            exitTime = System.currentTimeMillis()
        } else {
            finish()
        }
    }

}