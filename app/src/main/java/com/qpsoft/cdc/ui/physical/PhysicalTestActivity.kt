package com.qpsoft.cdc.ui.physical

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.recyclerview.widget.GridLayoutManager
import com.alibaba.fastjson.JSON
import com.blankj.utilcode.util.CacheDiskStaticUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.qpsoft.cdc.Api
import com.qpsoft.cdc.App
import com.qpsoft.cdc.R
import com.qpsoft.cdc.base.BaseActivity
import com.qpsoft.cdc.constant.Keys
import com.qpsoft.cdc.okgo.callback.DialogCallback
import com.qpsoft.cdc.okgo.model.LzyResponse
import com.qpsoft.cdc.ui.PickCheckItemActivity
import com.qpsoft.cdc.ui.adapter.UploadImageAdapter
import com.qpsoft.cdc.ui.entity.LoginRes
import com.qpsoft.cdc.ui.entity.Student
import kotlinx.android.synthetic.main.activity_physical_test.*
import kotlinx.android.synthetic.main.activity_physical_test.tvName
import kotlinx.android.synthetic.main.view_diopter.*
import kotlinx.android.synthetic.main.view_vision.*
import me.shaohui.advancedluban.Luban
import me.shaohui.advancedluban.OnCompressListener
import org.json.JSONObject
import java.io.File
import java.util.*

class PhysicalTestActivity : BaseActivity() {

    private var student: Student? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_physical_test)

        student = intent.getParcelableExtra("student")

        setBackBtn()
        setTitle("健康监测")

        tvName.text = student?.name
        tvGradeClazz.text = student?.grade + student?.clazz

        tvSubmit.setOnClickListener { doSubmit() }


        val checkItemList = App.instance.checkItemList
        val ciStr = checkItemList.joinToString { checkItem -> checkItem.key }
        handleUI(ciStr)
    }

    private fun handleUI(ciStr: String) {
        LogUtils.e("-----------$ciStr")

        if (ciStr.contains("vision")) {
            val visionView = layoutInflater.inflate(R.layout.view_vision, null)
            llContent.addView(visionView)

            rgGlassType.setOnCheckedChangeListener { radioGroup, checkId ->
                when(checkId) {
                    R.id.rbUnGlass -> {
                        llUnGlass.visibility = View.VISIBLE
                        llGlass.visibility = View.GONE
                        llOkGlass.visibility = View.GONE
                    }
                    R.id.rbGlass -> {
                        llUnGlass.visibility = View.VISIBLE
                        llGlass.visibility = View.VISIBLE
                        llOkGlass.visibility = View.GONE
                    }
                    R.id.rbLens -> {
                        llUnGlass.visibility = View.VISIBLE
                        llGlass.visibility = View.VISIBLE
                        llOkGlass.visibility = View.GONE
                    }
                    R.id.rbOkGlass -> {
                        llUnGlass.visibility = View.GONE
                        llGlass.visibility = View.VISIBLE
                        llOkGlass.visibility = View.VISIBLE
                    }
                }
            }

        }
        if (ciStr.contains("diopter")) {
            val diopterView = layoutInflater.inflate(R.layout.view_diopter, null)
            llContent.addView(diopterView)


            val mList: List<LocalMedia> = ArrayList<LocalMedia>()
            rvOriPic.layoutManager = GridLayoutManager(this, 5)
            mUploadAdapter = UploadImageAdapter(this, mList)
            rvOriPic.adapter = mUploadAdapter
            mUploadAdapter.setOnItemClickListener { adapter, view, position ->
                if (adapter.getItemViewType(position) == 1) {
                    PictureSelector.create(this@PhysicalTestActivity)
                        .themeStyle(R.style.picture_default_style)
                        .openExternalPreview(position, selPicList)
                } else {
                    PictureSelector.create(this@PhysicalTestActivity)
                        .openCamera(PictureMimeType.ofImage()) //.maxSelectNum(4 - selPicList.size())
                        .maxSelectNum(1)
                        .enableCrop(true)
                        .freeStyleCropEnabled(true)
                        .showCropGrid(false) //.selectionMedia(selectList)
                        //.compress(true)
                        .forResult(300)
                }
            }

            mUploadAdapter.setOnItemDelListener { adapter, view, position ->
                LogUtils.e("-----", licensePics.size)
                LogUtils.e("-----", "" + position)
                licensePics.removeAt(position)
                selPicList.removeAt(position)
                mUploadAdapter.setItems(selPicList)
            }
        }
        if (ciStr.contains("medicalHistory")) {
            val medicalHistoryView = layoutInflater.inflate(R.layout.view_medicalhistory, null)
            llContent.addView(medicalHistoryView)
        }
        if (ciStr.contains("caries")) {
            val cariesView = layoutInflater.inflate(R.layout.view_caries, null)
            llContent.addView(cariesView)
        }
        if (ciStr.contains("height") || ciStr.contains("weight")) {
            val heightWeightView = layoutInflater.inflate(R.layout.view_heightweight, null)
            llContent.addView(heightWeightView)
        }
        if (ciStr.contains("bloodPressure")) {
            val bloodPressureView = layoutInflater.inflate(R.layout.view_bloodpressure, null)
            llContent.addView(bloodPressureView)
        }
        if (ciStr.contains("spine")) {
            val spineView = layoutInflater.inflate(R.layout.view_spine, null)
            llContent.addView(spineView)
        }
        if (ciStr.contains("sexuality")) {
            val sexView = layoutInflater.inflate(R.layout.view_sex, null)
            llContent.addView(sexView)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == 300) {
                // 图片、视频、音频选择结果回调
                val backList = PictureSelector.obtainMultipleResult(data)
                val oriFile = File(backList[0].cutPath)
                val selectList = mutableListOf<LocalMedia>()
                val localMedia = LocalMedia()
                Luban.compress(this@PhysicalTestActivity, oriFile).setMaxSize(100)
                    .launch(object : OnCompressListener {
                        override fun onStart() {}
                        override fun onSuccess(file: File) {
                            localMedia.path = file.path
                            selectList.add(localMedia)
                            //上传到oss
                            uploadImage(selectList)
                        }

                        override fun onError(e: Throwable) {
                            localMedia.path = oriFile.path
                            selectList.add(localMedia)
                            //上传到oss
                            uploadImage(selectList)
                        }
                    })
            }
        }
    }

    private fun uploadImage(selectList: MutableList<LocalMedia>) {
        val files = mutableListOf<File>()
        for (i in selectList.indices) {
            files.add(File(selectList[i].path))
        }
        OkGo.post<LzyResponse<Any>>(Api.OSS_UPLOAD)
            .addFileParams("file", files)
            .params("objectName", "abc/efg/123.jpg")
            .execute(object : DialogCallback<LzyResponse<Any>>(this) {
                override fun onSuccess(response: Response<LzyResponse<Any>>) {
                    val s = response.body()?.data.toString()
                    licensePics.add(s)
                    ToastUtils.showShort("上传成功")
                    selPicList.addAll(selectList)
                    mUploadAdapter.setItems(selPicList)
                }
            })
    }


    private lateinit var mUploadAdapter: UploadImageAdapter
    private val selPicList = mutableListOf<LocalMedia>()
    private val licensePics = mutableListOf<String>()


    private fun doSubmit() {
        val upMap = mutableMapOf<Any?, Any?>()
        upMap["studentId"] = student?.id
        upMap["data"] = "Aa@123456"
        val jsonObj = JSONObject(upMap)
        OkGo.post<LzyResponse<LoginRes>>(Api.RECORD_SUBMIT)
            .upJson(jsonObj)
            .execute(object : DialogCallback<LzyResponse<LoginRes>>(this) {
                override fun onSuccess(response: Response<LzyResponse<LoginRes>>) {
                    val loginRes = response.body()?.data
                    CacheDiskStaticUtils.put(Keys.TOKEN, loginRes?.token)
                    startActivity(
                        Intent(
                            this@PhysicalTestActivity,
                            PickCheckItemActivity::class.java
                        )
                    )
                }
            })

    }
}