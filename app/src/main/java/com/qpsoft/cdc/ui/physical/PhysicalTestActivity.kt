package com.qpsoft.cdc.ui.physical

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsMultiChoice
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
import com.qpsoft.cdc.okgo.callback.DialogCallback
import com.qpsoft.cdc.okgo.model.LzyResponse
import com.qpsoft.cdc.ui.adapter.UploadImageAdapter
import com.qpsoft.cdc.ui.entity.Student
import kotlinx.android.synthetic.main.activity_physical_test.*
import kotlinx.android.synthetic.main.view_diopter.*
import kotlinx.android.synthetic.main.view_medicalhistory.*
import kotlinx.android.synthetic.main.view_medicalhistory.view.*
import kotlinx.android.synthetic.main.view_vision.*
import me.shaohui.advancedluban.Luban
import me.shaohui.advancedluban.OnCompressListener
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class PhysicalTestActivity : BaseActivity() {

    private var student: Student? = null

    private var ciStr = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_physical_test)

        student = intent.getParcelableExtra("student")

        setBackBtn()
        setTitle("健康监测")

        tvName.text = student?.name
        tvGradeClazz.text = student?.grade + student?.clazz

        getPhysical()

        tvSubmit.setOnClickListener { doSubmit() }


        val checkItemList = App.instance.checkItemList
        ciStr = checkItemList.joinToString { checkItem -> checkItem.key }
        handleUI()
    }

    private fun handleUI() {
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
                        glassType = "No"
                    }
                    R.id.rbGlass -> {
                        llUnGlass.visibility = View.VISIBLE
                        llGlass.visibility = View.VISIBLE
                        llOkGlass.visibility = View.GONE
                        glassType = "Frame"
                    }
                    R.id.rbLens -> {
                        llUnGlass.visibility = View.VISIBLE
                        llGlass.visibility = View.VISIBLE
                        llOkGlass.visibility = View.GONE
                        glassType = "ContactLens"
                    }
                    R.id.rbOkGlass -> {
                        llUnGlass.visibility = View.GONE
                        llGlass.visibility = View.VISIBLE
                        llOkGlass.visibility = View.VISIBLE
                        glassType = "OkGlass"
                    }
                }
            }

            sbtnEyeAbnormalVision.setOnCheckedChangeListener { compoundButton, isChecked ->
                eyeAbnormalVision = isChecked
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

            sbtnEyeAbnormalDiopter.setOnCheckedChangeListener { compoundButton, isChecked ->
                eyeAbnormalDiopter = isChecked
            }
        }
        if (ciStr.contains("medicalHistory")) {
            val medicalHistoryView = layoutInflater.inflate(R.layout.view_medicalhistory, null)
            llContent.addView(medicalHistoryView)

            var initSel = intArrayOf()
            tvMedicalHistory.setOnClickListener {
                val myItems = mutableListOf("肝炎", "肾炎", "心脏病", "高血压", "贫血", "过敏性哮喘", "身体残疾", "均无")
                MaterialDialog(this).show {
                    listItemsMultiChoice(items = myItems, initialSelection = initSel) { dialog, indices, items ->
                        initSel = indices
                        this@PhysicalTestActivity.tvMedicalHistory.text = items.joinToString(limit = 4)
                        medicalHistoryList = items as MutableList<String>
                    }
                    positiveButton()
                }
            }
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
        val objectName = "cdc/"+ System.currentTimeMillis() + ".jpg"
        OkGo.post<LzyResponse<Any>>(Api.OSS_UPLOAD)
            .addFileParams("file", files)
            .params("objectName", objectName)
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

    private fun downloadImage(optometryFile: String?) {
        if (!TextUtils.isEmpty(optometryFile)) {
            OkGo.get<LzyResponse<Any>>(Api.OSS_DOWNLOAD)
                .params("objectName", optometryFile)
                .execute(object : DialogCallback<LzyResponse<Any>>(this) {
                    override fun onSuccess(response: Response<LzyResponse<Any>>) {
                        val s = response.body()?.data.toString()
                        val localMedia = LocalMedia()
                        localMedia.path = s
                        selPicList.add(localMedia)
                        mUploadAdapter.setItems(selPicList)
                        licensePics.add(optometryFile!!)
                    }
                })
        }
    }



    private fun getPhysical() {
        OkGo.get<LzyResponse<Student>>(Api.STUDENT + "/" + student?.id + "?expand=record")
            .execute(object : DialogCallback<LzyResponse<Student>>(this) {
                override fun onSuccess(response: Response<LzyResponse<Student>>) {
                    val student = response.body()?.data!!
                    //vision
                    val vision = student.record?.data?.vision
                    if (vision != null) {
                        when (vision.glassType) {
                            "No" -> rbUnGlass.isChecked = true
                            "Frame" -> rbGlass.isChecked = true
                            "ContactLens" -> rbLens.isChecked = true
                            "OkGlass" -> rbOkGlass.isChecked = true
                        }
                        edtUnGlassRight.setText(vision.nakedDegree?.od)
                        edtUnGlassLeft.setText(vision.nakedDegree?.os)
                        edtGlassRight.setText(vision.glassDegree?.od)
                        edtGlassLeft.setText(vision.glassDegree?.os)
                        edtOkGlassRight.setText(vision.spectacles?.od)
                        edtOkGlassLeft.setText(vision.spectacles?.os)

                        sbtnEyeAbnormalVision.isChecked = vision.eyeAbnormal

                    }
                    //diopter
                    val diopter = student.record?.data?.diopter
                    LogUtils.e("-----" + diopter)
                    if (diopter != null) {
                        edtSRight.setText(diopter.sph?.od)
                        edtSLeft.setText(diopter.sph?.os)
                        edtCRight.setText(diopter.cyl?.od)
                        edtCLeft.setText(diopter.cyl?.os)
                        edtARight.setText(diopter.axle?.od)
                        edtALeft.setText(diopter.axle?.os)

                        downloadImage(diopter.optometryFile)

                        sbtnEyeAbnormalDiopter.isChecked = diopter.eyeAbnormal

                    }
                }
            })
    }

    //vision
    private var glassType = "Frame"
    private var eyeAbnormalVision = false

    //diopter
    private lateinit var mUploadAdapter: UploadImageAdapter
    private val selPicList = mutableListOf<LocalMedia>()
    private val licensePics = mutableListOf<String>()
    private var eyeAbnormalDiopter = false


    private var medicalHistoryList = mutableListOf<String>()

    private fun doSubmit() {
        //vision
        val ndObj = com.alibaba.fastjson.JSONObject()
        ndObj["od"] = edtUnGlassRight.text.toString().trim()
        ndObj["os"] = edtUnGlassLeft.text.toString().trim()
        val gdObj = com.alibaba.fastjson.JSONObject()
        gdObj["od"] = edtGlassRight.text.toString().trim()
        gdObj["os"] = edtGlassLeft.text.toString().trim()
        val stObj = com.alibaba.fastjson.JSONObject()
        stObj["od"] = edtOkGlassRight.text.toString().trim()
        stObj["os"] = edtOkGlassLeft.text.toString().trim()
        val contextObj = com.alibaba.fastjson.JSONObject()
        contextObj["userId"] = ""
        contextObj["userName"] = ""
        contextObj["deviceId"] = ""
        contextObj["deviceName"] = ""
        contextObj["submitAt"] = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
        val visionObj = com.alibaba.fastjson.JSONObject()
        when(glassType) {
            "No" -> visionObj["nakedDegree"] = ndObj
            "Frame", "ContactLens" -> {
                visionObj["nakedDegree"] = ndObj
                visionObj["glassDegree"] = gdObj
            }
            "OkGlass" -> {
                visionObj["glassDegree"] = gdObj
                visionObj["spectacles"] = stObj
            }
        }
        visionObj["glassType"] = glassType
        visionObj["eyeAbnormal"] = eyeAbnormalVision
        visionObj["context"] = contextObj

        //diopter
        val sphObj = com.alibaba.fastjson.JSONObject()
        sphObj["od"] = edtSRight.text.toString().trim()
        sphObj["os"] = edtSLeft.text.toString().trim()
        val cylObj = com.alibaba.fastjson.JSONObject()
        cylObj["od"] = edtCRight.text.toString().trim()
        cylObj["os"] = edtCLeft.text.toString().trim()
        val axleObj = com.alibaba.fastjson.JSONObject()
        axleObj["od"] = edtARight.text.toString().trim().toInt()
        axleObj["os"] = edtALeft.text.toString().trim().toInt()
        val diopterObj = com.alibaba.fastjson.JSONObject()
        diopterObj["sph"] = sphObj
        diopterObj["cyl"] = cylObj
        diopterObj["axle"] = axleObj
        diopterObj["optometryFile"] = if(licensePics.size == 0) "" else licensePics[0]
        diopterObj["eyeAbnormal"] = eyeAbnormalDiopter
        diopterObj["context"] = contextObj



        val dataObj = com.alibaba.fastjson.JSONObject()
        if (ciStr.contains("vision")) dataObj["vision"] = visionObj
        if (ciStr.contains("diopter")) dataObj["diopter"] = diopterObj

        val upMap = mutableMapOf<Any?, Any?>()
        upMap["studentId"] = student?.id
        upMap["data"] = dataObj
        val jsonObj = JSONObject(upMap)
        OkGo.post<LzyResponse<Any>>(Api.RECORD_SUBMIT)
            .upJson(jsonObj)
            .execute(object : DialogCallback<LzyResponse<Any>>(this) {
                override fun onSuccess(response: Response<LzyResponse<Any>>) {
                    val any = response.body()?.data
                    ToastUtils.showShort("提交成功")
                    finish()
                }
            })

    }
}