package com.qpsoft.cdc.ui.offline

import android.content.Intent
import android.os.Bundle
import com.qpsoft.cdc.R
import com.qpsoft.cdc.base.BaseActivity
import kotlinx.android.synthetic.main.activity_offline.*


class OffLineActivity : BaseActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_offline)

        setBackBtn()
        setTitle("离线筛查")

        llSchoolDownLoad.setOnClickListener {
            startActivity(Intent(this@OffLineActivity, DownLoadSchoolActivity::class.java))
        }

        llUpLoad.setOnClickListener {
            startActivity(Intent(this@OffLineActivity, UpLoadDataActivity::class.java))
        }

        llRetestUpLoad.setOnClickListener {
            startActivity(Intent(this@OffLineActivity, RetestUpLoadDataActivity::class.java))
        }
    }
}