package com.qpsoft.cdc.ui.physical

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
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
import com.qpsoft.cdc.constant.SchoolCategory
import com.qpsoft.cdc.okgo.callback.DialogCallback
import com.qpsoft.cdc.okgo.model.LzyResponse
import com.qpsoft.cdc.ui.adapter.UploadImageAdapter
import com.qpsoft.cdc.ui.entity.Student
import kotlinx.android.synthetic.main.activity_physical_test.*
import kotlinx.android.synthetic.main.view_bloodpressure.*
import kotlinx.android.synthetic.main.view_caries.*
import kotlinx.android.synthetic.main.view_cj.*
import kotlinx.android.synthetic.main.view_conjunctivitis.*
import kotlinx.android.synthetic.main.view_diopter.*
import kotlinx.android.synthetic.main.view_heightweight.*
import kotlinx.android.synthetic.main.view_medicalhistory.*
import kotlinx.android.synthetic.main.view_medicalhistory.view.*
import kotlinx.android.synthetic.main.view_redgreenblind.*
import kotlinx.android.synthetic.main.view_sex.*
import kotlinx.android.synthetic.main.view_spine.*
import kotlinx.android.synthetic.main.view_trachoma.*
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

        val checkItemList = App.instance.checkItemList
        ciStr = checkItemList.joinToString { checkItem -> checkItem.key }
        handleUI()

        getPhysical()

        tvSubmit.setOnClickListener { doSubmit() }
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

            val myItems = mutableListOf("肝炎", "肾炎", "心脏病", "高血压", "贫血", "过敏性哮喘", "身体残疾", "均无")
            val indexSel = mutableListOf<Int>()
            for(mh in medicalHistoryList) {
                for(item in myItems) {
                    if (mh == item) {
                        indexSel.add(myItems.indexOf(item))
                    }
                }
            }
            var initSel = intArrayOf(elements = indexSel.toIntArray())
            tvMedicalHistory.setOnClickListener {
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
            //乳牙
            tvDeciTooth15.setOnClickListener {handleDeciTooth(15)}
            tvDeciTooth14.setOnClickListener {handleDeciTooth(14)}
            tvDeciTooth13.setOnClickListener {handleDeciTooth(13)}
            tvDeciTooth12.setOnClickListener {handleDeciTooth(12)}
            tvDeciTooth11.setOnClickListener {handleDeciTooth(11)}
            tvDeciTooth21.setOnClickListener {handleDeciTooth(21)}
            tvDeciTooth22.setOnClickListener {handleDeciTooth(22)}
            tvDeciTooth23.setOnClickListener {handleDeciTooth(23)}
            tvDeciTooth24.setOnClickListener {handleDeciTooth(24)}
            tvDeciTooth25.setOnClickListener {handleDeciTooth(25)}
            tvDeciTooth45.setOnClickListener {handleDeciTooth(45)}
            tvDeciTooth44.setOnClickListener {handleDeciTooth(44)}
            tvDeciTooth43.setOnClickListener {handleDeciTooth(43)}
            tvDeciTooth42.setOnClickListener {handleDeciTooth(42)}
            tvDeciTooth41.setOnClickListener {handleDeciTooth(41)}
            tvDeciTooth31.setOnClickListener {handleDeciTooth(31)}
            tvDeciTooth32.setOnClickListener {handleDeciTooth(32)}
            tvDeciTooth33.setOnClickListener {handleDeciTooth(33)}
            tvDeciTooth34.setOnClickListener {handleDeciTooth(34)}
            tvDeciTooth35.setOnClickListener {handleDeciTooth(35)}
            //恒牙
            tvPermTooth18.setOnClickListener {handlePermTooth(18)}
            tvPermTooth17.setOnClickListener {handlePermTooth(17)}
            tvPermTooth16.setOnClickListener {handlePermTooth(16)}
            tvPermTooth15.setOnClickListener {handlePermTooth(15)}
            tvPermTooth14.setOnClickListener {handlePermTooth(14)}
            tvPermTooth13.setOnClickListener {handlePermTooth(13)}
            tvPermTooth12.setOnClickListener {handlePermTooth(12)}
            tvPermTooth11.setOnClickListener {handlePermTooth(11)}
            tvPermTooth21.setOnClickListener {handlePermTooth(21)}
            tvPermTooth22.setOnClickListener {handlePermTooth(22)}
            tvPermTooth23.setOnClickListener {handlePermTooth(23)}
            tvPermTooth24.setOnClickListener {handlePermTooth(24)}
            tvPermTooth25.setOnClickListener {handlePermTooth(25)}
            tvPermTooth26.setOnClickListener {handlePermTooth(26)}
            tvPermTooth27.setOnClickListener {handlePermTooth(27)}
            tvPermTooth28.setOnClickListener {handlePermTooth(28)}
            tvPermTooth48.setOnClickListener {handlePermTooth(48)}
            tvPermTooth47.setOnClickListener {handlePermTooth(47)}
            tvPermTooth46.setOnClickListener {handlePermTooth(46)}
            tvPermTooth45.setOnClickListener {handlePermTooth(45)}
            tvPermTooth44.setOnClickListener {handlePermTooth(44)}
            tvPermTooth43.setOnClickListener {handlePermTooth(43)}
            tvPermTooth42.setOnClickListener {handlePermTooth(42)}
            tvPermTooth41.setOnClickListener {handlePermTooth(41)}
            tvPermTooth31.setOnClickListener {handlePermTooth(31)}
            tvPermTooth32.setOnClickListener {handlePermTooth(32)}
            tvPermTooth33.setOnClickListener {handlePermTooth(33)}
            tvPermTooth34.setOnClickListener {handlePermTooth(34)}
            tvPermTooth35.setOnClickListener {handlePermTooth(35)}
            tvPermTooth36.setOnClickListener {handlePermTooth(36)}
            tvPermTooth37.setOnClickListener {handlePermTooth(37)}
            tvPermTooth38.setOnClickListener {handlePermTooth(38)}


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

            tvXiong.setOnClickListener { handleCeWan(tvXiong, llXiongDegree) }
            tvYaoXiong.setOnClickListener { handleCeWan(tvYaoXiong, llYaoXiongDegree) }
            tvYao.setOnClickListener { handleCeWan(tvYao, llYaoDegree) }

            tvQianHou.setOnClickListener { handleWanQu(tvQianHou, llQianHouDegree) }

            tvXiongDegree.setOnClickListener { handleDegree(tvXiongDegree) }
            tvYaoXiongDegree.setOnClickListener { handleDegree(tvYaoXiongDegree) }
            tvYaoDegree.setOnClickListener { handleDegree(tvYaoDegree) }
            tvQianHouDegree.setOnClickListener { handleDegree(tvQianHouDegree) }
        }
        if (ciStr.contains("sexuality") && student?.schoolCategory != SchoolCategory.Kindergarten.name) {
            val sexView = layoutInflater.inflate(R.layout.view_sex, null)
            llContent.addView(sexView)

            if (student?.schoolCategory != SchoolCategory.University.name) {
                llZhongXiaoXue.visibility = View.VISIBLE
                llDaXue.visibility = View.GONE
                if (student?.gender == "Male") {
                    llMen.visibility = View.VISIBLE
                    llWomen.visibility = View.GONE
                } else {
                    llMen.visibility = View.GONE
                    llWomen.visibility = View.VISIBLE
                }
            } else {
                llZhongXiaoXue.visibility = View.GONE
                llDaXue.visibility = View.VISIBLE
                if (student?.gender == "Male") {
                    llMenDaXue.visibility = View.VISIBLE
                    llWomenDaXue.visibility = View.GONE
                } else {
                    llMenDaXue.visibility = View.GONE
                    llWomenDaXue.visibility = View.VISIBLE
                }
            }
        }
        if (ciStr.contains("trachoma")) {
            val trachomaView = layoutInflater.inflate(R.layout.view_trachoma, null)
            llContent.addView(trachomaView)

            val myItems = listOf("无", "可疑", "沙眼I", "沙眼II", "沙眼III")
            tvTrachoma.setOnClickListener { handleSingleChoice(tvTrachoma, myItems) }
        }
        if (ciStr.contains("conjunctivitis")) {
            val conjuncView = layoutInflater.inflate(R.layout.view_conjunctivitis, null)
            llContent.addView(conjuncView)

            val myItems = listOf("无", "有")
            tvConjunc.setOnClickListener { handleSingleChoice(tvConjunc, myItems) }
        }
        if (ciStr.contains("redGreenBlind")) {
            val rgbView = layoutInflater.inflate(R.layout.view_redgreenblind, null)
            llContent.addView(rgbView)

            val myItems = listOf("无", "有")
            tvRgb.setOnClickListener { handleSingleChoice(tvRgb, myItems) }
        }

        if (ciStr.contains("cj")) {
            val cjView = layoutInflater.inflate(R.layout.view_cj, null)
            llContent.addView(cjView)

            val myItems1 = listOf("正常", "近视", "远视", "其他")
            tvQuGuangRight.setOnClickListener { handleSingleChoice(tvQuGuangRight, myItems1) }
            tvQuGuangLeft.setOnClickListener { handleSingleChoice(tvQuGuangLeft, myItems1) }
            val myItems2 = listOf("正常", "上升", "下降", "其他")
            tvCjRight.setOnClickListener { handleSingleChoice(tvCjRight, myItems2) }
            tvCjLeft.setOnClickListener { handleSingleChoice(tvCjLeft, myItems2) }
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

    //处理乳牙
    private fun handleDeciTooth(pos: Int) {
        val myItems = listOf("龋（d）", "失（m）", "补（f）", "正常")
        MaterialDialog(this).show {
            listItems(items = myItems){ dialog, index, text ->
                when(index) {
                    0 -> {
                        when (pos) {
                            15 -> this@PhysicalTestActivity.tvDeciTooth15.text = "d"
                            14 -> this@PhysicalTestActivity.tvDeciTooth14.text = "d"
                            13 -> this@PhysicalTestActivity.tvDeciTooth13.text = "d"
                            12 -> this@PhysicalTestActivity.tvDeciTooth12.text = "d"
                            11 -> this@PhysicalTestActivity.tvDeciTooth11.text = "d"
                            21 -> this@PhysicalTestActivity.tvDeciTooth21.text = "d"
                            22 -> this@PhysicalTestActivity.tvDeciTooth22.text = "d"
                            23 -> this@PhysicalTestActivity.tvDeciTooth23.text = "d"
                            24 -> this@PhysicalTestActivity.tvDeciTooth24.text = "d"
                            25 -> this@PhysicalTestActivity.tvDeciTooth25.text = "d"
                            45 -> this@PhysicalTestActivity.tvDeciTooth45.text = "d"
                            44 -> this@PhysicalTestActivity.tvDeciTooth44.text = "d"
                            43 -> this@PhysicalTestActivity.tvDeciTooth43.text = "d"
                            42 -> this@PhysicalTestActivity.tvDeciTooth42.text = "d"
                            41 -> this@PhysicalTestActivity.tvDeciTooth41.text = "d"
                            31 -> this@PhysicalTestActivity.tvDeciTooth31.text = "d"
                            32 -> this@PhysicalTestActivity.tvDeciTooth32.text = "d"
                            33 -> this@PhysicalTestActivity.tvDeciTooth33.text = "d"
                            34 -> this@PhysicalTestActivity.tvDeciTooth34.text = "d"
                            35 -> this@PhysicalTestActivity.tvDeciTooth35.text = "d"
                        }

                        if (!babyDList.contains(pos)) {
                            babyDList.add(pos)
                        }
                        if (babyMList.contains(pos)) {
                            babyMList.remove(pos)
                        }
                        if (babyFList.contains(pos)) {
                            babyFList.remove(pos)
                        }
                    }
                    1 -> {
                        when (pos) {
                            15 -> this@PhysicalTestActivity.tvDeciTooth15.text = "m"
                            14 -> this@PhysicalTestActivity.tvDeciTooth14.text = "m"
                            13 -> this@PhysicalTestActivity.tvDeciTooth13.text = "m"
                            12 -> this@PhysicalTestActivity.tvDeciTooth12.text = "m"
                            11 -> this@PhysicalTestActivity.tvDeciTooth11.text = "m"
                            21 -> this@PhysicalTestActivity.tvDeciTooth21.text = "m"
                            22 -> this@PhysicalTestActivity.tvDeciTooth22.text = "m"
                            23 -> this@PhysicalTestActivity.tvDeciTooth23.text = "m"
                            24 -> this@PhysicalTestActivity.tvDeciTooth24.text = "m"
                            25 -> this@PhysicalTestActivity.tvDeciTooth25.text = "m"
                            45 -> this@PhysicalTestActivity.tvDeciTooth45.text = "m"
                            44 -> this@PhysicalTestActivity.tvDeciTooth44.text = "m"
                            43 -> this@PhysicalTestActivity.tvDeciTooth43.text = "m"
                            42 -> this@PhysicalTestActivity.tvDeciTooth42.text = "m"
                            41 -> this@PhysicalTestActivity.tvDeciTooth41.text = "m"
                            31 -> this@PhysicalTestActivity.tvDeciTooth31.text = "m"
                            32 -> this@PhysicalTestActivity.tvDeciTooth32.text = "m"
                            33 -> this@PhysicalTestActivity.tvDeciTooth33.text = "m"
                            34 -> this@PhysicalTestActivity.tvDeciTooth34.text = "m"
                            35 -> this@PhysicalTestActivity.tvDeciTooth35.text = "m"
                        }

                        if (!babyMList.contains(pos)) {
                            babyMList.add(pos)
                        }
                        if (babyDList.contains(pos)) {
                            babyDList.remove(pos)
                        }
                        if (babyFList.contains(pos)) {
                            babyFList.remove(pos)
                        }
                    }
                    2 -> {
                        when (pos) {
                            15 -> this@PhysicalTestActivity.tvDeciTooth15.text = "f"
                            14 -> this@PhysicalTestActivity.tvDeciTooth14.text = "f"
                            13 -> this@PhysicalTestActivity.tvDeciTooth13.text = "f"
                            12 -> this@PhysicalTestActivity.tvDeciTooth12.text = "f"
                            11 -> this@PhysicalTestActivity.tvDeciTooth11.text = "f"
                            21 -> this@PhysicalTestActivity.tvDeciTooth21.text = "f"
                            22 -> this@PhysicalTestActivity.tvDeciTooth22.text = "f"
                            23 -> this@PhysicalTestActivity.tvDeciTooth23.text = "f"
                            24 -> this@PhysicalTestActivity.tvDeciTooth24.text = "f"
                            25 -> this@PhysicalTestActivity.tvDeciTooth25.text = "f"
                            45 -> this@PhysicalTestActivity.tvDeciTooth45.text = "f"
                            44 -> this@PhysicalTestActivity.tvDeciTooth44.text = "f"
                            43 -> this@PhysicalTestActivity.tvDeciTooth43.text = "f"
                            42 -> this@PhysicalTestActivity.tvDeciTooth42.text = "f"
                            41 -> this@PhysicalTestActivity.tvDeciTooth41.text = "f"
                            31 -> this@PhysicalTestActivity.tvDeciTooth31.text = "f"
                            32 -> this@PhysicalTestActivity.tvDeciTooth32.text = "f"
                            33 -> this@PhysicalTestActivity.tvDeciTooth33.text = "f"
                            34 -> this@PhysicalTestActivity.tvDeciTooth34.text = "f"
                            35 -> this@PhysicalTestActivity.tvDeciTooth35.text = "f"
                        }

                        if (!babyFList.contains(pos)) {
                            babyFList.add(pos)
                        }
                        if (babyDList.contains(pos)) {
                            babyDList.remove(pos)
                        }
                        if (babyMList.contains(pos)) {
                            babyMList.remove(pos)
                        }
                    }
                    3 -> {
                        when (pos) {
                            15 -> this@PhysicalTestActivity.tvDeciTooth15.text = ""
                            14 -> this@PhysicalTestActivity.tvDeciTooth14.text = ""
                            13 -> this@PhysicalTestActivity.tvDeciTooth13.text = ""
                            12 -> this@PhysicalTestActivity.tvDeciTooth12.text = ""
                            11 -> this@PhysicalTestActivity.tvDeciTooth11.text = ""
                            21 -> this@PhysicalTestActivity.tvDeciTooth21.text = ""
                            22 -> this@PhysicalTestActivity.tvDeciTooth22.text = ""
                            23 -> this@PhysicalTestActivity.tvDeciTooth23.text = ""
                            24 -> this@PhysicalTestActivity.tvDeciTooth24.text = ""
                            25 -> this@PhysicalTestActivity.tvDeciTooth25.text = ""
                            45 -> this@PhysicalTestActivity.tvDeciTooth45.text = ""
                            44 -> this@PhysicalTestActivity.tvDeciTooth44.text = ""
                            43 -> this@PhysicalTestActivity.tvDeciTooth43.text = ""
                            42 -> this@PhysicalTestActivity.tvDeciTooth42.text = ""
                            41 -> this@PhysicalTestActivity.tvDeciTooth41.text = ""
                            31 -> this@PhysicalTestActivity.tvDeciTooth31.text = ""
                            32 -> this@PhysicalTestActivity.tvDeciTooth32.text = ""
                            33 -> this@PhysicalTestActivity.tvDeciTooth33.text = ""
                            34 -> this@PhysicalTestActivity.tvDeciTooth34.text = ""
                            35 -> this@PhysicalTestActivity.tvDeciTooth35.text = ""
                        }

                        if (babyDList.contains(pos)) {
                            babyDList.remove(pos)
                        }
                        if (babyMList.contains(pos)) {
                            babyMList.remove(pos)
                        }
                        if (babyFList.contains(pos)) {
                            babyFList.remove(pos)
                        }
                    }
                }
            }
        }
    }
    //处理恒牙
    private fun handlePermTooth(pos: Int) {
        val myItems = listOf("龋（D）", "失（M）", "补（F）", "正常")
        MaterialDialog(this).show {
            listItems(items = myItems){ dialog, index, text ->
                when(index) {
                    0 -> {
                        when (pos) {
                            18 -> this@PhysicalTestActivity.tvPermTooth18.text = "D"
                            17 -> this@PhysicalTestActivity.tvPermTooth17.text = "D"
                            16 -> this@PhysicalTestActivity.tvPermTooth16.text = "D"
                            15 -> this@PhysicalTestActivity.tvPermTooth15.text = "D"
                            14 -> this@PhysicalTestActivity.tvPermTooth14.text = "D"
                            13 -> this@PhysicalTestActivity.tvPermTooth13.text = "D"
                            12 -> this@PhysicalTestActivity.tvPermTooth12.text = "D"
                            11 -> this@PhysicalTestActivity.tvPermTooth11.text = "D"
                            21 -> this@PhysicalTestActivity.tvPermTooth21.text = "D"
                            22 -> this@PhysicalTestActivity.tvPermTooth22.text = "D"
                            23 -> this@PhysicalTestActivity.tvPermTooth23.text = "D"
                            24 -> this@PhysicalTestActivity.tvPermTooth24.text = "D"
                            25 -> this@PhysicalTestActivity.tvPermTooth25.text = "D"
                            26 -> this@PhysicalTestActivity.tvPermTooth26.text = "D"
                            27 -> this@PhysicalTestActivity.tvPermTooth27.text = "D"
                            28 -> this@PhysicalTestActivity.tvPermTooth28.text = "D"
                            48 -> this@PhysicalTestActivity.tvPermTooth48.text = "D"
                            47 -> this@PhysicalTestActivity.tvPermTooth47.text = "D"
                            46 -> this@PhysicalTestActivity.tvPermTooth46.text = "D"
                            45 -> this@PhysicalTestActivity.tvPermTooth45.text = "D"
                            44 -> this@PhysicalTestActivity.tvPermTooth44.text = "D"
                            43 -> this@PhysicalTestActivity.tvPermTooth43.text = "D"
                            42 -> this@PhysicalTestActivity.tvPermTooth42.text = "D"
                            41 -> this@PhysicalTestActivity.tvPermTooth41.text = "D"
                            31 -> this@PhysicalTestActivity.tvPermTooth31.text = "D"
                            32 -> this@PhysicalTestActivity.tvPermTooth32.text = "D"
                            33 -> this@PhysicalTestActivity.tvPermTooth33.text = "D"
                            34 -> this@PhysicalTestActivity.tvPermTooth34.text = "D"
                            35 -> this@PhysicalTestActivity.tvPermTooth35.text = "D"
                            36 -> this@PhysicalTestActivity.tvPermTooth36.text = "D"
                            37 -> this@PhysicalTestActivity.tvPermTooth37.text = "D"
                            38 -> this@PhysicalTestActivity.tvPermTooth38.text = "D"
                        }

                        if (!adultDList.contains(pos)) {
                            adultDList.add(pos)
                        }
                        if (adultMList.contains(pos)) {
                            adultMList.remove(pos)
                        }
                        if (adultFList.contains(pos)) {
                            adultFList.remove(pos)
                        }
                    }
                    1 -> {
                        when (pos) {
                            18 -> this@PhysicalTestActivity.tvPermTooth18.text = "M"
                            17 -> this@PhysicalTestActivity.tvPermTooth17.text = "M"
                            16 -> this@PhysicalTestActivity.tvPermTooth16.text = "M"
                            15 -> this@PhysicalTestActivity.tvPermTooth15.text = "M"
                            14 -> this@PhysicalTestActivity.tvPermTooth14.text = "M"
                            13 -> this@PhysicalTestActivity.tvPermTooth13.text = "M"
                            12 -> this@PhysicalTestActivity.tvPermTooth12.text = "M"
                            11 -> this@PhysicalTestActivity.tvPermTooth11.text = "M"
                            21 -> this@PhysicalTestActivity.tvPermTooth21.text = "M"
                            22 -> this@PhysicalTestActivity.tvPermTooth22.text = "M"
                            23 -> this@PhysicalTestActivity.tvPermTooth23.text = "M"
                            24 -> this@PhysicalTestActivity.tvPermTooth24.text = "M"
                            25 -> this@PhysicalTestActivity.tvPermTooth25.text = "M"
                            26 -> this@PhysicalTestActivity.tvPermTooth26.text = "M"
                            27 -> this@PhysicalTestActivity.tvPermTooth27.text = "M"
                            28 -> this@PhysicalTestActivity.tvPermTooth28.text = "M"
                            48 -> this@PhysicalTestActivity.tvPermTooth48.text = "M"
                            47 -> this@PhysicalTestActivity.tvPermTooth47.text = "M"
                            46 -> this@PhysicalTestActivity.tvPermTooth46.text = "M"
                            45 -> this@PhysicalTestActivity.tvPermTooth45.text = "M"
                            44 -> this@PhysicalTestActivity.tvPermTooth44.text = "M"
                            43 -> this@PhysicalTestActivity.tvPermTooth43.text = "M"
                            42 -> this@PhysicalTestActivity.tvPermTooth42.text = "M"
                            41 -> this@PhysicalTestActivity.tvPermTooth41.text = "M"
                            31 -> this@PhysicalTestActivity.tvPermTooth31.text = "M"
                            32 -> this@PhysicalTestActivity.tvPermTooth32.text = "M"
                            33 -> this@PhysicalTestActivity.tvPermTooth33.text = "M"
                            34 -> this@PhysicalTestActivity.tvPermTooth34.text = "M"
                            35 -> this@PhysicalTestActivity.tvPermTooth35.text = "M"
                            36 -> this@PhysicalTestActivity.tvPermTooth36.text = "M"
                            37 -> this@PhysicalTestActivity.tvPermTooth37.text = "M"
                            38 -> this@PhysicalTestActivity.tvPermTooth38.text = "M"
                        }

                        if (!adultMList.contains(pos)) {
                            adultMList.add(pos)
                        }
                        if (adultDList.contains(pos)) {
                            adultDList.remove(pos)
                        }
                        if (adultFList.contains(pos)) {
                            adultFList.remove(pos)
                        }
                    }
                    2 -> {
                        when (pos) {
                            18 -> this@PhysicalTestActivity.tvPermTooth18.text = "F"
                            17 -> this@PhysicalTestActivity.tvPermTooth17.text = "F"
                            16 -> this@PhysicalTestActivity.tvPermTooth16.text = "F"
                            15 -> this@PhysicalTestActivity.tvPermTooth15.text = "F"
                            14 -> this@PhysicalTestActivity.tvPermTooth14.text = "F"
                            13 -> this@PhysicalTestActivity.tvPermTooth13.text = "F"
                            12 -> this@PhysicalTestActivity.tvPermTooth12.text = "F"
                            11 -> this@PhysicalTestActivity.tvPermTooth11.text = "F"
                            21 -> this@PhysicalTestActivity.tvPermTooth21.text = "F"
                            22 -> this@PhysicalTestActivity.tvPermTooth22.text = "F"
                            23 -> this@PhysicalTestActivity.tvPermTooth23.text = "F"
                            24 -> this@PhysicalTestActivity.tvPermTooth24.text = "F"
                            25 -> this@PhysicalTestActivity.tvPermTooth25.text = "F"
                            26 -> this@PhysicalTestActivity.tvPermTooth26.text = "F"
                            27 -> this@PhysicalTestActivity.tvPermTooth27.text = "F"
                            28 -> this@PhysicalTestActivity.tvPermTooth28.text = "F"
                            48 -> this@PhysicalTestActivity.tvPermTooth48.text = "F"
                            47 -> this@PhysicalTestActivity.tvPermTooth47.text = "F"
                            46 -> this@PhysicalTestActivity.tvPermTooth46.text = "F"
                            45 -> this@PhysicalTestActivity.tvPermTooth45.text = "F"
                            44 -> this@PhysicalTestActivity.tvPermTooth44.text = "F"
                            43 -> this@PhysicalTestActivity.tvPermTooth43.text = "F"
                            42 -> this@PhysicalTestActivity.tvPermTooth42.text = "F"
                            41 -> this@PhysicalTestActivity.tvPermTooth41.text = "F"
                            31 -> this@PhysicalTestActivity.tvPermTooth31.text = "F"
                            32 -> this@PhysicalTestActivity.tvPermTooth32.text = "F"
                            33 -> this@PhysicalTestActivity.tvPermTooth33.text = "F"
                            34 -> this@PhysicalTestActivity.tvPermTooth34.text = "F"
                            35 -> this@PhysicalTestActivity.tvPermTooth35.text = "F"
                            36 -> this@PhysicalTestActivity.tvPermTooth36.text = "F"
                            37 -> this@PhysicalTestActivity.tvPermTooth37.text = "F"
                            38 -> this@PhysicalTestActivity.tvPermTooth38.text = "F"
                        }

                        if (!adultFList.contains(pos)) {
                            adultFList.add(pos)
                        }
                        if (adultMList.contains(pos)) {
                            adultMList.remove(pos)
                        }
                        if (adultDList.contains(pos)) {
                            adultDList.remove(pos)
                        }
                    }
                    3 -> {
                        when (pos) {
                            18 -> this@PhysicalTestActivity.tvPermTooth18.text = ""
                            17 -> this@PhysicalTestActivity.tvPermTooth17.text = ""
                            16 -> this@PhysicalTestActivity.tvPermTooth16.text = ""
                            15 -> this@PhysicalTestActivity.tvPermTooth15.text = ""
                            14 -> this@PhysicalTestActivity.tvPermTooth14.text = ""
                            13 -> this@PhysicalTestActivity.tvPermTooth13.text = ""
                            12 -> this@PhysicalTestActivity.tvPermTooth12.text = ""
                            11 -> this@PhysicalTestActivity.tvPermTooth11.text = ""
                            21 -> this@PhysicalTestActivity.tvPermTooth21.text = ""
                            22 -> this@PhysicalTestActivity.tvPermTooth22.text = ""
                            23 -> this@PhysicalTestActivity.tvPermTooth23.text = ""
                            24 -> this@PhysicalTestActivity.tvPermTooth24.text = ""
                            25 -> this@PhysicalTestActivity.tvPermTooth25.text = ""
                            26 -> this@PhysicalTestActivity.tvPermTooth26.text = ""
                            27 -> this@PhysicalTestActivity.tvPermTooth27.text = ""
                            28 -> this@PhysicalTestActivity.tvPermTooth28.text = ""
                            48 -> this@PhysicalTestActivity.tvPermTooth48.text = ""
                            47 -> this@PhysicalTestActivity.tvPermTooth47.text = ""
                            46 -> this@PhysicalTestActivity.tvPermTooth46.text = ""
                            45 -> this@PhysicalTestActivity.tvPermTooth45.text = ""
                            44 -> this@PhysicalTestActivity.tvPermTooth44.text = ""
                            43 -> this@PhysicalTestActivity.tvPermTooth43.text = ""
                            42 -> this@PhysicalTestActivity.tvPermTooth42.text = ""
                            41 -> this@PhysicalTestActivity.tvPermTooth41.text = ""
                            31 -> this@PhysicalTestActivity.tvPermTooth31.text = ""
                            32 -> this@PhysicalTestActivity.tvPermTooth32.text = ""
                            33 -> this@PhysicalTestActivity.tvPermTooth33.text = ""
                            34 -> this@PhysicalTestActivity.tvPermTooth34.text = ""
                            35 -> this@PhysicalTestActivity.tvPermTooth35.text = ""
                            36 -> this@PhysicalTestActivity.tvPermTooth36.text = ""
                            37 -> this@PhysicalTestActivity.tvPermTooth37.text = ""
                            38 -> this@PhysicalTestActivity.tvPermTooth38.text = ""
                        }

                        if (adultDList.contains(pos)) {
                            adultDList.remove(pos)
                        }
                        if (adultMList.contains(pos)) {
                            adultMList.remove(pos)
                        }
                        if (adultFList.contains(pos)) {
                            adultFList.remove(pos)
                        }
                    }
                }
            }
        }
    }

    //脊柱
    private fun handleCeWan(tv: TextView, ll: LinearLayout) {
        val myItems = listOf("无侧弯", "左凸", "右凸")
        MaterialDialog(this).show {
            listItems(items = myItems){ dialog, index, text ->
                when(index) {
                    0 -> {
                        tv.text = text
                        ll.visibility = View.INVISIBLE
                    }
                    1, 2 -> {
                        tv.text = text
                        ll.visibility = View.VISIBLE
                    }
                }
            }
        }
    }
    private fun handleWanQu(tv: TextView, ll: LinearLayout) {
        val myItems = listOf("无前后弯曲异常", "直背", "前凸异常", "后凸异常")
        MaterialDialog(this).show {
            listItems(items = myItems){ dialog, index, text ->
                when(index) {
                    0 -> {
                        tv.text = text
                        ll.visibility = View.INVISIBLE
                    }
                    1, 2, 3 -> {
                        tv.text = text
                        ll.visibility = View.VISIBLE
                    }
                }
            }
        }
    }
    private fun handleDegree(tv: TextView) {
        val myItems = listOf("I", "II", "III")
        MaterialDialog(this).show {
            listItems(items = myItems){ dialog, index, text ->
                when(index) {
                    0, 1, 2 -> tv.text = text
                }
            }
        }
    }

    //沙眼，结膜炎，红绿色盲，串镜
    private fun handleSingleChoice(tv: TextView, items: List<String>) {
        MaterialDialog(this).show {
            listItems(items = items){ dialog, index, text ->
                tv.text = text
            }
        }
    }



    private fun getPhysical() {
        OkGo.get<LzyResponse<Student>>(Api.STUDENT + "/" + student?.id + "?expand=record")
            .execute(object : DialogCallback<LzyResponse<Student>>(this) {
                override fun onSuccess(response: Response<LzyResponse<Student>>) {
                    val student = response.body()?.data!!
                    val data = student.record?.data
                    //vision
                    val vision = data?.vision
                    if (ciStr.contains("vision") && vision != null) {
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
                    val diopter = data?.diopter
                    if (ciStr.contains("diopter") && diopter != null) {
                        edtSRight.setText(diopter.sph?.od)
                        edtSLeft.setText(diopter.sph?.os)
                        edtCRight.setText(diopter.cyl?.od)
                        edtCLeft.setText(diopter.cyl?.os)
                        edtARight.setText(diopter.axle?.od)
                        edtALeft.setText(diopter.axle?.os)

                        downloadImage(diopter.optometryFile)

                        sbtnEyeAbnormalDiopter.isChecked = diopter.eyeAbnormal

                    }

                    //medicalHistory
                    val medicalHistory = data?.medicalHistory
                    if (ciStr.contains("medicalHistory") && medicalHistory != null) {
                        tvMedicalHistory.text = medicalHistory.data?.joinToString(limit = 4)
                        medicalHistoryList = medicalHistory.data as MutableList<String>
                    }

                    //caries
                    val caries = data?.caries
                    if (ciStr.contains("caries") && caries != null) {
                        val babyTooth = caries?.babyTooth

                        val bTDList = babyTooth?.caries?.list
                        if (bTDList != null) {
                            babyDList = bTDList
                            for (i in bTDList) {
                                if (i == 15) tvDeciTooth15.text = "d"
                                if (i == 14) tvDeciTooth14.text = "d"
                                if (i == 13) tvDeciTooth13.text = "d"
                                if (i == 12) tvDeciTooth12.text = "d"
                                if (i == 11) tvDeciTooth11.text = "d"
                                if (i == 21) tvDeciTooth21.text = "d"
                                if (i == 22) tvDeciTooth22.text = "d"
                                if (i == 23) tvDeciTooth23.text = "d"
                                if (i == 24) tvDeciTooth24.text = "d"
                                if (i == 25) tvDeciTooth25.text = "d"
                                if (i == 45) tvDeciTooth45.text = "d"
                                if (i == 44) tvDeciTooth44.text = "d"
                                if (i == 43) tvDeciTooth43.text = "d"
                                if (i == 42) tvDeciTooth42.text = "d"
                                if (i == 41) tvDeciTooth41.text = "d"
                                if (i == 31) tvDeciTooth31.text = "d"
                                if (i == 32) tvDeciTooth32.text = "d"
                                if (i == 33) tvDeciTooth33.text = "d"
                                if (i == 34) tvDeciTooth34.text = "d"
                                if (i == 35) tvDeciTooth35.text = "d"
                            }
                        }
                        val bTMList = babyTooth?.missing?.list
                        if (bTMList != null) {
                            babyMList = bTMList
                            for (i in bTMList) {
                                if (i == 15) tvDeciTooth15.text = "m"
                                if (i == 14) tvDeciTooth14.text = "m"
                                if (i == 13) tvDeciTooth13.text = "m"
                                if (i == 12) tvDeciTooth12.text = "m"
                                if (i == 11) tvDeciTooth11.text = "m"
                                if (i == 21) tvDeciTooth21.text = "m"
                                if (i == 22) tvDeciTooth22.text = "m"
                                if (i == 23) tvDeciTooth23.text = "m"
                                if (i == 24) tvDeciTooth24.text = "m"
                                if (i == 25) tvDeciTooth25.text = "m"
                                if (i == 45) tvDeciTooth45.text = "m"
                                if (i == 44) tvDeciTooth44.text = "m"
                                if (i == 43) tvDeciTooth43.text = "m"
                                if (i == 42) tvDeciTooth42.text = "m"
                                if (i == 41) tvDeciTooth41.text = "m"
                                if (i == 31) tvDeciTooth31.text = "m"
                                if (i == 32) tvDeciTooth32.text = "m"
                                if (i == 33) tvDeciTooth33.text = "m"
                                if (i == 34) tvDeciTooth34.text = "m"
                                if (i == 35) tvDeciTooth35.text = "m"
                            }
                        }
                        val bTFList = babyTooth?.fill?.list
                        if (bTFList != null) {
                            babyFList = bTFList
                            for (i in bTFList) {
                                if (i == 15) tvDeciTooth15.text = "f"
                                if (i == 14) tvDeciTooth14.text = "f"
                                if (i == 13) tvDeciTooth13.text = "f"
                                if (i == 12) tvDeciTooth12.text = "f"
                                if (i == 11) tvDeciTooth11.text = "f"
                                if (i == 21) tvDeciTooth21.text = "f"
                                if (i == 22) tvDeciTooth22.text = "f"
                                if (i == 23) tvDeciTooth23.text = "f"
                                if (i == 24) tvDeciTooth24.text = "f"
                                if (i == 25) tvDeciTooth25.text = "f"
                                if (i == 45) tvDeciTooth45.text = "f"
                                if (i == 44) tvDeciTooth44.text = "f"
                                if (i == 43) tvDeciTooth43.text = "f"
                                if (i == 42) tvDeciTooth42.text = "f"
                                if (i == 41) tvDeciTooth41.text = "f"
                                if (i == 31) tvDeciTooth31.text = "f"
                                if (i == 32) tvDeciTooth32.text = "f"
                                if (i == 33) tvDeciTooth33.text = "f"
                                if (i == 34) tvDeciTooth34.text = "f"
                                if (i == 35) tvDeciTooth35.text = "f"
                            }
                        }


                        val adultTooth = caries?.adultTooth
                        val aTDList = adultTooth?.caries?.list
                        if (aTDList != null) {
                            adultDList = aTDList
                            for (i in aTDList) {
                                if (i == 18) tvPermTooth18.text = "D"
                                if (i == 17) tvPermTooth17.text = "D"
                                if (i == 16) tvPermTooth16.text = "D"
                                if (i == 15) tvPermTooth15.text = "D"
                                if (i == 14) tvPermTooth14.text = "D"
                                if (i == 13) tvPermTooth13.text = "D"
                                if (i == 12) tvPermTooth12.text = "D"
                                if (i == 11) tvPermTooth11.text = "D"
                                if (i == 21) tvPermTooth21.text = "D"
                                if (i == 22) tvPermTooth22.text = "D"
                                if (i == 23) tvPermTooth23.text = "D"
                                if (i == 24) tvPermTooth24.text = "D"
                                if (i == 25) tvPermTooth25.text = "D"
                                if (i == 26) tvPermTooth26.text = "D"
                                if (i == 27) tvPermTooth27.text = "D"
                                if (i == 28) tvPermTooth28.text = "D"
                                if (i == 48) tvPermTooth48.text = "D"
                                if (i == 47) tvPermTooth47.text = "D"
                                if (i == 46) tvPermTooth46.text = "D"
                                if (i == 45) tvPermTooth45.text = "D"
                                if (i == 44) tvPermTooth44.text = "D"
                                if (i == 43) tvPermTooth43.text = "D"
                                if (i == 42) tvPermTooth42.text = "D"
                                if (i == 41) tvPermTooth41.text = "D"
                                if (i == 31) tvPermTooth31.text = "D"
                                if (i == 32) tvPermTooth32.text = "D"
                                if (i == 33) tvPermTooth33.text = "D"
                                if (i == 34) tvPermTooth34.text = "D"
                                if (i == 35) tvPermTooth35.text = "D"
                                if (i == 36) tvPermTooth36.text = "D"
                                if (i == 37) tvPermTooth37.text = "D"
                                if (i == 38) tvPermTooth38.text = "D"
                            }
                        }
                        val aTMList = adultTooth?.missing?.list
                        if (aTMList != null) {
                            adultMList = aTMList
                            for (i in aTMList) {
                                if (i == 18) tvPermTooth18.text = "M"
                                if (i == 17) tvPermTooth17.text = "M"
                                if (i == 16) tvPermTooth16.text = "M"
                                if (i == 15) tvPermTooth15.text = "M"
                                if (i == 14) tvPermTooth14.text = "M"
                                if (i == 13) tvPermTooth13.text = "M"
                                if (i == 12) tvPermTooth12.text = "M"
                                if (i == 11) tvPermTooth11.text = "M"
                                if (i == 21) tvPermTooth21.text = "M"
                                if (i == 22) tvPermTooth22.text = "M"
                                if (i == 23) tvPermTooth23.text = "M"
                                if (i == 24) tvPermTooth24.text = "M"
                                if (i == 25) tvPermTooth25.text = "M"
                                if (i == 26) tvPermTooth26.text = "M"
                                if (i == 27) tvPermTooth27.text = "M"
                                if (i == 28) tvPermTooth28.text = "M"
                                if (i == 48) tvPermTooth48.text = "M"
                                if (i == 47) tvPermTooth47.text = "M"
                                if (i == 46) tvPermTooth46.text = "M"
                                if (i == 45) tvPermTooth45.text = "M"
                                if (i == 44) tvPermTooth44.text = "M"
                                if (i == 43) tvPermTooth43.text = "M"
                                if (i == 42) tvPermTooth42.text = "M"
                                if (i == 41) tvPermTooth41.text = "M"
                                if (i == 31) tvPermTooth31.text = "M"
                                if (i == 32) tvPermTooth32.text = "M"
                                if (i == 33) tvPermTooth33.text = "M"
                                if (i == 34) tvPermTooth34.text = "M"
                                if (i == 35) tvPermTooth35.text = "M"
                                if (i == 36) tvPermTooth36.text = "M"
                                if (i == 37) tvPermTooth37.text = "M"
                                if (i == 38) tvPermTooth38.text = "M"
                            }
                        }
                        val aTFList = adultTooth?.fill?.list
                        if (aTFList != null) {
                            adultFList = aTFList
                            for (i in aTFList) {
                                if (i == 18) tvPermTooth18.text = "F"
                                if (i == 17) tvPermTooth17.text = "F"
                                if (i == 16) tvPermTooth16.text = "F"
                                if (i == 15) tvPermTooth15.text = "F"
                                if (i == 14) tvPermTooth14.text = "F"
                                if (i == 13) tvPermTooth13.text = "F"
                                if (i == 12) tvPermTooth12.text = "F"
                                if (i == 11) tvPermTooth11.text = "F"
                                if (i == 21) tvPermTooth21.text = "F"
                                if (i == 22) tvPermTooth22.text = "F"
                                if (i == 23) tvPermTooth23.text = "F"
                                if (i == 24) tvPermTooth24.text = "F"
                                if (i == 25) tvPermTooth25.text = "F"
                                if (i == 26) tvPermTooth26.text = "F"
                                if (i == 27) tvPermTooth27.text = "F"
                                if (i == 28) tvPermTooth28.text = "F"
                                if (i == 48) tvPermTooth48.text = "F"
                                if (i == 47) tvPermTooth47.text = "F"
                                if (i == 46) tvPermTooth46.text = "F"
                                if (i == 45) tvPermTooth45.text = "F"
                                if (i == 44) tvPermTooth44.text = "F"
                                if (i == 43) tvPermTooth43.text = "F"
                                if (i == 42) tvPermTooth42.text = "F"
                                if (i == 41) tvPermTooth41.text = "F"
                                if (i == 31) tvPermTooth31.text = "F"
                                if (i == 32) tvPermTooth32.text = "F"
                                if (i == 33) tvPermTooth33.text = "F"
                                if (i == 34) tvPermTooth34.text = "F"
                                if (i == 35) tvPermTooth35.text = "F"
                                if (i == 36) tvPermTooth36.text = "F"
                                if (i == 37) tvPermTooth37.text = "F"
                                if (i == 38) tvPermTooth38.text = "F"
                            }
                        }
                    }

                    //height weight
                    val height = data?.height
                    val weight = data?.weight
                    if ((ciStr.contains("height") || ciStr.contains("weight")) && (height != null || weight != null)) {
                        edtHeight.setText(height?.data)
                        edtWeight.setText(weight?.data)
                    }

                    //bloodPressure
                    val bloodPressure = data?.bloodPressure
                    if (ciStr.contains("bloodPressure") && bloodPressure != null) {
                        edtSystolic.setText(bloodPressure.sbp)
                        edtDiastolic.setText(bloodPressure.dbp)
                    }

                    //spine
                    val spine = data?.spine
                    if (ciStr.contains("spine") && spine != null) {
                        val chest = spine.sideBend?.chest
                        tvXiong.text = chest?.category
                        tvXiongDegree.text = chest?.degree
                        if ("无侧弯" == chest?.category) llXiongDegree.visibility = View.INVISIBLE

                        val waistChest = spine.sideBend?.waistChest
                        tvYaoXiong.text = waistChest?.category
                        tvYaoXiongDegree.text = waistChest?.degree
                        if ("无侧弯" == waistChest?.category) llYaoXiongDegree.visibility =
                            View.INVISIBLE

                        val waist = spine.sideBend?.waist
                        tvYao.text = waist?.category
                        tvYaoDegree.text = waist?.degree
                        if ("无侧弯" == waist?.category) llYaoDegree.visibility = View.INVISIBLE

                        val baBend = spine.baBend
                        tvQianHou.text = baBend?.category
                        tvQianHouDegree.text = baBend?.degree
                        if ("无前后弯曲异常" == baBend?.category) llQianHouDegree.visibility =
                            View.INVISIBLE
                    }

                    //sexuality
                    val sexuality = data?.sexuality
                    if (ciStr.contains("sexuality") && sexuality != null) {
                        if (student.schoolCategory != SchoolCategory.University.name) {
                            llZhongXiaoXue.visibility = View.VISIBLE
                            llDaXue.visibility = View.GONE
                            if (student.gender == "Male") {
                                llMen.visibility = View.VISIBLE
                                llWomen.visibility = View.GONE
                            } else {
                                llMen.visibility = View.GONE
                                llWomen.visibility = View.VISIBLE
                            }
                        } else {
                            llZhongXiaoXue.visibility = View.GONE
                            llDaXue.visibility = View.VISIBLE
                            if (student.gender == "Male") {
                                llMenDaXue.visibility = View.VISIBLE
                                llWomenDaXue.visibility = View.GONE
                            } else {
                                llMenDaXue.visibility = View.GONE
                                llWomenDaXue.visibility = View.VISIBLE
                            }
                        }

                        val menstruation = sexuality.menstruation
                        if (menstruation?.whether == 1) {
                            cbWomenWhether.isChecked = true
                            cbWomenWhetherDaXue.isChecked = true
                        }
                        edtWomenAge.setText("" + menstruation?.startAge)
                        edtFrequency.setText("" + menstruation?.frequency)
                        edtDuration.setText("" + menstruation?.duration)

                        val nocturnalEmission = sexuality.nocturnalEmission
                        if (nocturnalEmission?.whether == 1) {
                            cbMenWhether.isChecked = true
                            cbMenWhetherDaXue.isChecked = true
                        }
                        edtMenAge.setText("" + nocturnalEmission?.startAge)


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

    //medicalHistory
    private var medicalHistoryList = mutableListOf<String>()

    //caries
    //乳牙数据
    private var babyDList = mutableListOf<Int>()
    private var babyMList = mutableListOf<Int>()
    private var babyFList = mutableListOf<Int>()
    //恒牙数据
    private var adultDList = mutableListOf<Int>()
    private var adultMList = mutableListOf<Int>()
    private var adultFList = mutableListOf<Int>()



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
        if(licensePics.size != 0) diopterObj["optometryFile"] = licensePics[0]
        diopterObj["eyeAbnormal"] = eyeAbnormalDiopter
        diopterObj["context"] = contextObj

        //medicalHistory
        val medicalHistoryObj = com.alibaba.fastjson.JSONObject()
        medicalHistoryObj["data"] = medicalHistoryList.toTypedArray()
        medicalHistoryObj["context"] = contextObj

        //caries
        val cariesObj = com.alibaba.fastjson.JSONObject()
        //乳牙
        val babyObj = com.alibaba.fastjson.JSONObject()

        val babyDObj = com.alibaba.fastjson.JSONObject()
        babyDObj["list"] = babyDList
        babyDObj["count"] = babyDList.size
        babyObj["caries"] = babyDObj
        val babyMObj = com.alibaba.fastjson.JSONObject()
        babyMObj["list"] = babyMList
        babyMObj["count"] = babyMList.size
        babyObj["missing"] = babyMObj
        val babyFObj = com.alibaba.fastjson.JSONObject()
        babyFObj["list"] = babyFList
        babyFObj["count"] = babyFList.size
        babyObj["fill"] = babyFObj

        cariesObj["babyTooth"] = babyObj
        //恒牙
        val adultObj = com.alibaba.fastjson.JSONObject()

        val adultDObj = com.alibaba.fastjson.JSONObject()
        adultDObj["list"] = adultDList
        adultDObj["count"] = adultDList.size
        adultObj["caries"] = adultDObj
        val adultMObj = com.alibaba.fastjson.JSONObject()
        adultMObj["list"] = adultMList
        adultMObj["count"] = adultMList.size
        adultObj["missing"] = adultMObj
        val adultFObj = com.alibaba.fastjson.JSONObject()
        adultFObj["list"] = adultFList
        adultFObj["count"] = adultFList.size
        adultObj["fill"] = adultFObj

        cariesObj["adultTooth"] = adultObj
        cariesObj["context"] = contextObj

        //height weight
        val heightObj = com.alibaba.fastjson.JSONObject()
        heightObj["data"] = edtHeight.text.toString().trim()
        heightObj["context"] = contextObj
        val weightObj = com.alibaba.fastjson.JSONObject()
        weightObj["data"] = edtWeight.text.toString().trim()
        weightObj["context"] = contextObj

        //bloodPressure
        val bloodPressureObj = com.alibaba.fastjson.JSONObject()
        bloodPressureObj["sbp"] = edtSystolic.text.toString().trim()
        bloodPressureObj["dbp"] = edtDiastolic.text.toString().trim()
        bloodPressureObj["context"] = contextObj

        //spineObj
        val spineObj = com.alibaba.fastjson.JSONObject()

        val sideBendObj = com.alibaba.fastjson.JSONObject()
        val xiongObj = com.alibaba.fastjson.JSONObject()
        xiongObj["category"] = tvXiong.text.toString()
        xiongObj["degree"] = tvXiongDegree.text.toString()
        sideBendObj["chest"] = xiongObj
        val yaoXiongObj = com.alibaba.fastjson.JSONObject()
        yaoXiongObj["category"] = tvYaoXiong.text.toString()
        yaoXiongObj["degree"] = tvYaoXiongDegree.text.toString()
        sideBendObj["waistChest"] = yaoXiongObj
        val yaoObj = com.alibaba.fastjson.JSONObject()
        yaoObj["category"] = tvYao.text.toString()
        yaoObj["degree"] = tvYaoDegree.text.toString()
        sideBendObj["waist"] = yaoObj
        spineObj["sideBend"] = sideBendObj

        val baBendObj = com.alibaba.fastjson.JSONObject()
        baBendObj["category"] = tvQianHou.text.toString()
        baBendObj["degree"] = tvQianHouDegree.text.toString()

        spineObj["baBend"] = baBendObj
        spineObj["context"] = contextObj

        //sexuality
        val sexualityObj = com.alibaba.fastjson.JSONObject()
        if (student?.gender == "Male") {
            val nocturnalEmissionObj = com.alibaba.fastjson.JSONObject()
            if (student?.schoolCategory == SchoolCategory.University.name) {
                nocturnalEmissionObj["whether"] = if (cbMenWhetherDaXue.isChecked) 1 else 0
            } else {
                nocturnalEmissionObj["whether"] = if (cbMenWhether.isChecked) 1 else 0
            }
            nocturnalEmissionObj["startAge"] = edtMenAge.text.toString().trim()
            sexualityObj["nocturnalEmission"] = nocturnalEmissionObj
        } else {
            val menstruationObj = com.alibaba.fastjson.JSONObject()
            if (student?.schoolCategory == SchoolCategory.University.name) {
                menstruationObj["whether"] = if (cbWomenWhetherDaXue.isChecked) 1 else 0
            } else {
                menstruationObj["whether"] = if (cbWomenWhether.isChecked) 1 else 0
            }
            menstruationObj["startAge"] = edtWomenAge.text.toString().trim()
            menstruationObj["frequency"] = edtFrequency.text.toString().trim()
            menstruationObj["duration"] = edtDuration.text.toString().trim()
            sexualityObj["menstruation"] = menstruationObj
        }
        sexualityObj["context"] = contextObj



        val dataObj = com.alibaba.fastjson.JSONObject()
        if (ciStr.contains("vision")) dataObj["vision"] = visionObj
        if (ciStr.contains("diopter")) dataObj["diopter"] = diopterObj
        if (ciStr.contains("medicalHistory")) dataObj["medicalHistory"] = medicalHistoryObj
        if (ciStr.contains("caries")) dataObj["caries"] = cariesObj
        if (ciStr.contains("height")) dataObj["height"] = heightObj
        if (ciStr.contains("weight")) dataObj["weight"] = weightObj
        if (ciStr.contains("bloodPressure")) dataObj["bloodPressure"] = bloodPressureObj
        if (ciStr.contains("spine")) dataObj["spine"] = spineObj
        if (ciStr.contains("sexuality")) dataObj["sexuality"] = sexualityObj

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