package com.qpsoft.cdc.base

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ScreenUtils
import com.qpsoft.cdc.R
import kotlinx.android.synthetic.main.activity_base.*


open class BaseActivity : AppCompatActivity() {
    companion object {
        val TAG: String = BaseActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        initOrientation()
        super.onCreate(savedInstanceState)

        LogUtils.i(TAG, this.javaClass.simpleName)

        // 这句很关键，注意是调用父类的方法
        super.setContentView(R.layout.activity_base)
        initToolBar()
    }

    private fun initToolBar() {
        setSupportActionBar(toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    fun setTitle(title: String) {
        supportActionBar?.title = title
        //toolBar.title = title
    }

    fun setBackBtn() {
       supportActionBar?.setDisplayHomeAsUpEnabled(true)
       toolBar.setNavigationOnClickListener { finish() }
    }

    override fun setContentView(layoutResID: Int) {
        setContentView(View.inflate(this, layoutResID, null));
    }

    override fun setContentView(view: View?) {
        rootLayout.addView(view, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        //initToolBar()
    }

    private fun initOrientation() {
        ScreenUtils.setPortrait(this)
    }
}