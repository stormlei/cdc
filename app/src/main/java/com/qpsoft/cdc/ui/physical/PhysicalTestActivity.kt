package com.qpsoft.cdc.ui.physical

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
import com.afollestad.materialdialogs.list.listItemsMultiChoice
import com.alibaba.fastjson.JSON
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.king.zxing.CameraScan
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
import com.qpsoft.cdc.eventbus.DeviceNotifyDataEvent
import com.qpsoft.cdc.eventbus.DeviceStatusEvent
import com.qpsoft.cdc.okgo.callback.DialogCallback
import com.qpsoft.cdc.okgo.model.LzyResponse
import com.qpsoft.cdc.okgo.utils.Convert
import com.qpsoft.cdc.thirddevice.bloodpressure.yuwell.BPData
import com.qpsoft.cdc.thirddevice.diopter.RefractionData
import com.qpsoft.cdc.thirddevice.heightweight.HWData
import com.qpsoft.cdc.thirddevice.tonometer.IopData
import com.qpsoft.cdc.thirddevice.vitalcapacity.breathhome.VCData
import com.qpsoft.cdc.ui.CustomCaptureActivity
import com.qpsoft.cdc.ui.adapter.UploadImageAdapter
import com.qpsoft.cdc.ui.entity.DataItem
import com.qpsoft.cdc.ui.entity.QrCodeInfo
import com.qpsoft.cdc.ui.entity.Student
import com.qpsoft.cdc.utils.BleDeviceOpUtil
import com.qpsoft.cdc.utils.EyeChartOpUtil
import kotlinx.android.synthetic.main.activity_physical_test.*
import kotlinx.android.synthetic.main.view_bcgscar.*
import kotlinx.android.synthetic.main.view_bloodpressure.*
import kotlinx.android.synthetic.main.view_bloodtype.*
import kotlinx.android.synthetic.main.view_bust.*
import kotlinx.android.synthetic.main.view_caries.*
import kotlinx.android.synthetic.main.view_chest.*
import kotlinx.android.synthetic.main.view_cj.*
import kotlinx.android.synthetic.main.view_conjunctivitis.*
import kotlinx.android.synthetic.main.view_cornealcurvature.*
import kotlinx.android.synthetic.main.view_cornealradius.*
import kotlinx.android.synthetic.main.view_diopter.*
import kotlinx.android.synthetic.main.view_ear.*
import kotlinx.android.synthetic.main.view_eyeaxis.*
import kotlinx.android.synthetic.main.view_eyepressure.*
import kotlinx.android.synthetic.main.view_grip.*
import kotlinx.android.synthetic.main.view_head.*
import kotlinx.android.synthetic.main.view_hearing.*
import kotlinx.android.synthetic.main.view_heart.*
import kotlinx.android.synthetic.main.view_heightweight.*
import kotlinx.android.synthetic.main.view_hemoglobin.*
import kotlinx.android.synthetic.main.view_hips.*
import kotlinx.android.synthetic.main.view_limb.*
import kotlinx.android.synthetic.main.view_liver.*
import kotlinx.android.synthetic.main.view_liverfunction.*
import kotlinx.android.synthetic.main.view_lung.*
import kotlinx.android.synthetic.main.view_lymphaden.*
import kotlinx.android.synthetic.main.view_medicalhistory.*
import kotlinx.android.synthetic.main.view_medicalhistory.view.*
import kotlinx.android.synthetic.main.view_neck.*
import kotlinx.android.synthetic.main.view_nose.*
import kotlinx.android.synthetic.main.view_nutrition.*
import kotlinx.android.synthetic.main.view_pdd.*
import kotlinx.android.synthetic.main.view_periodontium.*
import kotlinx.android.synthetic.main.view_pulse.*
import kotlinx.android.synthetic.main.view_redgreenblind.*
import kotlinx.android.synthetic.main.view_sex.*
import kotlinx.android.synthetic.main.view_sittingheight.*
import kotlinx.android.synthetic.main.view_skin.*
import kotlinx.android.synthetic.main.view_spine.*
import kotlinx.android.synthetic.main.view_spleen.*
import kotlinx.android.synthetic.main.view_tonsil.*
import kotlinx.android.synthetic.main.view_trachoma.*
import kotlinx.android.synthetic.main.view_vision.*
import kotlinx.android.synthetic.main.view_vitalcapacity.*
import kotlinx.android.synthetic.main.view_waistline.*
import kotlinx.android.synthetic.main.view_worm.*
import me.shaohui.advancedluban.Luban
import me.shaohui.advancedluban.OnCompressListener
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
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

        //getPhysical()
        getPhysicalLocal()

        tvSubmit.setOnClickListener { doSubmit() }
    }

    private fun handleUI() {
        LogUtils.e("-----------$ciStr")

        if (ciStr.contains("vision")) {
            //val visionView = layoutInflater.inflate(R.layout.view_vision, null)
            //llContent.addView(visionView)
            visionView.visibility = View.VISIBLE

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

                val checkView = findViewById<View>(checkId)
                if (!checkView.isPressed){
                    return@setOnCheckedChangeListener
                }
                if (EyeChartOpUtil.isConnected()) {
                    startActivityForResult(Intent(this@PhysicalTestActivity, VisionTestActivity::class.java)
                            .putExtra("glassType", glassType).putExtra("schoolCategory", student?.schoolCategory), 200)
                }
            }

            sbtnEyeAbnormalVision.setOnCheckedChangeListener { compoundButton, isChecked ->
                eyeAbnormalVision = isChecked
            }

            //vision conn
            tvEyeChartDisconn.setOnClickListener {
                if (EyeChartOpUtil.isConnected()) {
                    if(glassType == "") {
                        ToastUtils.showShort("请选择戴镜类型")
                        return@setOnClickListener
                    }
                    startActivityForResult(Intent(this@PhysicalTestActivity, VisionTestActivity::class.java)
                            .putExtra("glassType", glassType).putExtra("schoolCategory", student?.schoolCategory), 200)
                } else {
                    startActivityForResult(Intent(this@PhysicalTestActivity, CustomCaptureActivity::class.java), 100)
                }
            }
        }
        if (ciStr.contains("diopter")) {
            //val diopterView = layoutInflater.inflate(R.layout.view_diopter, null)
            //llContent.addView(diopterView)
            diopterView.visibility = View.VISIBLE

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

            //diopter conn
            tvDiopterDisconn.setOnClickListener {
                if (BleDeviceOpUtil.isDiopterConnected()) {
                    BleDeviceOpUtil.diopterDisConnected()
                } else {
                    startActivityForResult(Intent(this@PhysicalTestActivity, CustomCaptureActivity::class.java), 100)
                }
            }
        }
        if (ciStr.contains("medicalHistory")) {
            //val medicalHistoryView = layoutInflater.inflate(R.layout.view_medicalhistory, null)
            //llContent.addView(medicalHistoryView)
            medicalHistoryView.visibility = View.VISIBLE

            val myItems = mutableListOf("肝炎", "肾炎", "心脏病", "高血压", "贫血", "过敏性哮喘", "身体残疾", "均无")
            tvMedicalHistory.setOnClickListener {
                val indexSel = mutableListOf<Int>()
                for(mh in medicalHistoryList) {
                    for(item in myItems) {
                        if (mh == item) {
                            indexSel.add(myItems.indexOf(item))
                        }
                    }
                }
                val initSel = intArrayOf(elements = indexSel.toIntArray())

                handleMultiChoice(tvMedicalHistory, myItems, initSel, "medicalHistory")
            }
        }
        if (ciStr.contains("caries")) {
            //val cariesView = layoutInflater.inflate(R.layout.view_caries, null)
            //llContent.addView(cariesView)
            cariesView.visibility = View.VISIBLE
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
            //val heightWeightView = layoutInflater.inflate(R.layout.view_heightweight, null)
            //llContent.addView(heightWeightView)
            heightWeightView.visibility = View.VISIBLE

            //height weight conn
            tvHWDisconn.setOnClickListener {
                if (BleDeviceOpUtil.isHWConnected()) {
                    BleDeviceOpUtil.hwDisConnected()
                } else {
                    startActivityForResult(Intent(this@PhysicalTestActivity, CustomCaptureActivity::class.java), 100)
                }
            }
        }
        if (ciStr.contains("bloodPressure")) {
            //val bloodPressureView = layoutInflater.inflate(R.layout.view_bloodpressure, null)
            //llContent.addView(bloodPressureView)
            bloodPressureView.visibility = View.VISIBLE

            //bloodPressure conn
            tvBPDisconn.setOnClickListener {
                if (BleDeviceOpUtil.isBPConnected()) {
                    BleDeviceOpUtil.bpDisConnected()
                } else {
                    startActivityForResult(Intent(this@PhysicalTestActivity, CustomCaptureActivity::class.java), 100)
                }
            }
        }
        if (ciStr.contains("spine")) {
            //val spineView = layoutInflater.inflate(R.layout.view_spine, null)
            //llContent.addView(spineView)
            spineView.visibility = View.VISIBLE

            tvXiong.setOnClickListener { handleCeWan(tvXiong, llXiongDegree) }
            tvYaoXiong.setOnClickListener { handleCeWan(tvYaoXiong, llYaoXiongDegree) }
            tvYao.setOnClickListener { handleCeWan(tvYao, llYaoDegree) }

            tvQianHou.setOnClickListener { handleWanQu(tvQianHou, llQianHouDegree) }

            val myItems = listOf("I", "II", "III")
            tvXiongDegree.setOnClickListener { handleSingleChoice(tvXiongDegree, myItems) }
            tvYaoXiongDegree.setOnClickListener { handleSingleChoice(tvYaoXiongDegree, myItems) }
            tvYaoDegree.setOnClickListener { handleSingleChoice(tvYaoDegree, myItems) }
            tvQianHouDegree.setOnClickListener { handleSingleChoice(tvQianHouDegree, myItems) }
        }
        if (ciStr.contains("sexuality") && student?.schoolCategory != SchoolCategory.Kindergarten.name) {
            //val sexView = layoutInflater.inflate(R.layout.view_sex, null)
            //llContent.addView(sexView)
            sexView.visibility = View.VISIBLE

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
            //val trachomaView = layoutInflater.inflate(R.layout.view_trachoma, null)
            //llContent.addView(trachomaView)
            trachomaView.visibility = View.VISIBLE

            val myItems = listOf("无", "可疑", "沙眼I", "沙眼II", "沙眼III")
            tvTrachoma.setOnClickListener { handleSingleChoice(tvTrachoma, myItems) }
        }
        if (ciStr.contains("conjunctivitis")) {
            //val conjuncView = layoutInflater.inflate(R.layout.view_conjunctivitis, null)
            //llContent.addView(conjuncView)
            conjuncView.visibility = View.VISIBLE

            val myItems = listOf("无", "有")
            tvConjunc.setOnClickListener { handleSingleChoice(tvConjunc, myItems) }
        }
        if (ciStr.contains("redGreenBlind")) {
            //val rgbView = layoutInflater.inflate(R.layout.view_redgreenblind, null)
            //llContent.addView(rgbView)
            rgbView.visibility = View.VISIBLE

            val myItems = listOf("无", "有")
            tvRgb.setOnClickListener { handleSingleChoice(tvRgb, myItems) }
        }
        if (ciStr.contains("eyeAxis")) {
            //val eyeAxisView = layoutInflater.inflate(R.layout.view_eyeaxis, null)
            //llContent.addView(eyeAxisView)
            eyeAxisView.visibility = View.VISIBLE
        }
        if (ciStr.contains("eyePressure")) {
            //val eyePressureView = layoutInflater.inflate(R.layout.view_eyepressure, null)
            //llContent.addView(eyePressureView)
            eyePressureView.visibility = View.VISIBLE

            //eyePressure conn
            tvEPDisconn.setOnClickListener {
                if (BleDeviceOpUtil.isEPConnected()) {
                    BleDeviceOpUtil.epDisConnected()
                } else {
                    startActivityForResult(Intent(this@PhysicalTestActivity, CustomCaptureActivity::class.java), 100)
                }
            }
        }
        if (ciStr.contains("cornealCurvature")) {
            //val ccView = layoutInflater.inflate(R.layout.view_cornealcurvature, null)
            //llContent.addView(ccView)
            ccView.visibility = View.VISIBLE
        }
        if (ciStr.contains("cornealRadius")) {
            //val crView = layoutInflater.inflate(R.layout.view_cornealradius, null)
            //llContent.addView(crView)
            crView.visibility = View.VISIBLE
        }
        if (ciStr.contains("cj")) {
            //val cjView = layoutInflater.inflate(R.layout.view_cj, null)
            //llContent.addView(cjView)
            cjView.visibility = View.VISIBLE

            val myItems1 = listOf("正常", "近视", "远视", "其他")
            tvQuGuangRight.setOnClickListener { handleSingleChoice(tvQuGuangRight, myItems1) }
            tvQuGuangLeft.setOnClickListener { handleSingleChoice(tvQuGuangLeft, myItems1) }
            val myItems2 = listOf("正常", "上升", "下降", "其他")
            tvCjRight.setOnClickListener { handleSingleChoice(tvCjRight, myItems2) }
            tvCjLeft.setOnClickListener { handleSingleChoice(tvCjLeft, myItems2) }
        }

        if (ciStr.contains("pulse")) {
            //val pulseView = layoutInflater.inflate(R.layout.view_pulse, null)
            //llContent.addView(pulseView)
            pulseView.visibility = View.VISIBLE
        }
        if (ciStr.contains("vitalCapacity")) {
            //val vcView = layoutInflater.inflate(R.layout.view_vitalcapacity, null)
            //llContent.addView(vcView)
            vcView.visibility = View.VISIBLE

            //vitalCapacity conn
            tvVCDisconn.setOnClickListener {
                if (BleDeviceOpUtil.isVCConnected()) {
                    BleDeviceOpUtil.vcDisConnected()
                } else {
                    startActivityForResult(Intent(this@PhysicalTestActivity, CustomCaptureActivity::class.java), 100)
                }
            }
        }
        if (ciStr.contains("bust")) {
            //val bustView = layoutInflater.inflate(R.layout.view_bust, null)
            //llContent.addView(bustView)
            bustView.visibility = View.VISIBLE
        }
        if (ciStr.contains("waistline")) {
            //val waistlineView = layoutInflater.inflate(R.layout.view_waistline, null)
            //llContent.addView(waistlineView)
            waistlineView.visibility = View.VISIBLE
        }
        if (ciStr.contains("hips")) {
            //val hipsView = layoutInflater.inflate(R.layout.view_hips, null)
            //llContent.addView(hipsView)
            hipsView.visibility = View.VISIBLE
        }
        if (ciStr.contains("sittingHeight")) {
            //val shView = layoutInflater.inflate(R.layout.view_sittingheight, null)
            //llContent.addView(shView)
            shView.visibility = View.VISIBLE
        }
        if (ciStr.contains("grip")) {
            //val gripView = layoutInflater.inflate(R.layout.view_grip, null)
            //llContent.addView(gripView)
            gripView.visibility = View.VISIBLE
        }
        if (ciStr.contains("nutrition")) {
            //val nutritionView = layoutInflater.inflate(R.layout.view_nutrition, null)
            //llContent.addView(nutritionView)
            nutritionView.visibility = View.VISIBLE

            val myItems = listOf("正常", "生长迟缓", "营养不良", "超重", "肥胖")
            tvNutrition.setOnClickListener { handleSingleChoice(tvNutrition, myItems) }
        }


        if (ciStr.contains("ear")) {
            //val earView = layoutInflater.inflate(R.layout.view_ear, null)
            //llContent.addView(earView)
            earView.visibility = View.VISIBLE

            val myItems = listOf("未见明显异常", "其他异常", "耵聍", "附耳", "中耳炎", "耳前萎管")
            tvEar.setOnClickListener {
                val indexSel = mutableListOf<Int>()
                for(mh in earList) {
                    for(item in myItems) {
                        if (mh == item) {
                            indexSel.add(myItems.indexOf(item))
                        }
                    }
                }
                val initSel = intArrayOf(elements = indexSel.toIntArray())

                handleMultiChoice(tvEar, myItems, initSel, "ear")
            }
        }
        if (ciStr.contains("nose")) {
            //val noseView = layoutInflater.inflate(R.layout.view_nose, null)
            //llContent.addView(noseView)
            noseView.visibility = View.VISIBLE

            val myItems = listOf("未见明显异常", "其他异常", "鼻炎", "鼻窦炎", "鼻中隔偏曲", "鼻息肉", "鼻衄", "鼻前庭炎")
            tvNose.setOnClickListener {
                val indexSel = mutableListOf<Int>()
                for(mh in noseList) {
                    for(item in myItems) {
                        if (mh == item) {
                            indexSel.add(myItems.indexOf(item))
                        }
                    }
                }
                val initSel = intArrayOf(elements = indexSel.toIntArray())

                handleMultiChoice(tvNose, myItems, initSel, "nose")
            }
        }
        if (ciStr.contains("tonsil")) {
            //val tonsilView = layoutInflater.inflate(R.layout.view_tonsil, null)
            //llContent.addView(tonsilView)
            tonsilView.visibility = View.VISIBLE

            val myItems = listOf("未见明显异常", "异常", "Ⅰ度肿大", "Ⅱ度肿大", "Ⅲ度肿大", "扁桃体切除术后")
            tvTonsil.setOnClickListener { handleSingleChoice(tvNose, myItems) }
        }
        if (ciStr.contains("periodontium")) {
            //val periodontiumView = layoutInflater.inflate(R.layout.view_periodontium, null)
            //llContent.addView(periodontiumView)
            periodontiumView.visibility = View.VISIBLE

            val myItems = listOf("未见明显异常", "牙结石", "牙龈炎", "牙周炎", "氟斑牙", "错颔", "牙釉质发育不全", "义齿", "其他牙病")
            tvPeriod.setOnClickListener {
                val indexSel = mutableListOf<Int>()
                for(mh in periodList) {
                    for(item in myItems) {
                        if (mh == item) {
                            indexSel.add(myItems.indexOf(item))
                        }
                    }
                }
                val initSel = intArrayOf(elements = indexSel.toIntArray())

                handleMultiChoice(tvPeriod, myItems, initSel, "periodontium")
            }
        }
        if (ciStr.contains("hearing")) {
            //val hearingView = layoutInflater.inflate(R.layout.view_hearing, null)
            //llContent.addView(hearingView)
            hearingView.visibility = View.VISIBLE

            val myItems = listOf("正常", "异常")
            tvHearingRight.setOnClickListener { handleSingleChoice(tvHearingRight, myItems) }
            tvHearingLeft.setOnClickListener { handleSingleChoice(tvHearingLeft, myItems) }
        }
        if (ciStr.contains("heart")) {
            //val heartView = layoutInflater.inflate(R.layout.view_heart, null)
            //llContent.addView(heartView)
            heartView.visibility = View.VISIBLE

            val myItems = listOf("心律齐未及明显杂音", "心脏I级杂音", "心脏II级杂音", "心脏III级以上杂音", "心动过速", "心动过缓", "心律不齐", "频发早搏")
            tvHeart.setOnClickListener {
                val indexSel = mutableListOf<Int>()
                for(mh in heartList) {
                    for(item in myItems) {
                        if (mh == item) {
                            indexSel.add(myItems.indexOf(item))
                        }
                    }
                }
                val initSel = intArrayOf(elements = indexSel.toIntArray())

                handleMultiChoice(tvHeart, myItems, initSel, "heart")
            }
        }
        if (ciStr.contains("lung")) {
            //val lungView = layoutInflater.inflate(R.layout.view_lung, null)
            //llContent.addView(lungView)
            lungView.visibility = View.VISIBLE

            val myItems = listOf("双肺呼吸音清未及明显干湿啰音", "异常", "哮鸣音(哮喘)", "干湿性啰音(肺炎)", "双肺呼吸音增粗")
            tvLung.setOnClickListener { handleSingleChoice(tvLung, myItems) }
        }
        if (ciStr.contains("liver")) {
            val keyList = ciStr.split(", ")
            var flag = false
            keyList.forEach { if(it == "liver") flag = true }
            if (flag) {
                //val liverView = layoutInflater.inflate(R.layout.view_liver, null)
                //llContent.addView(liverView)
                liverView.visibility = View.VISIBLE

                val myItems = listOf("肋下未及", "肿大", "肝脏肿大轻度", "肝脏肿大中度以上")
                tvLiver.setOnClickListener { handleSingleChoice(tvLiver, myItems) }
            }
        }
        if (ciStr.contains("spleen")) {
            //val spleenView = layoutInflater.inflate(R.layout.view_spleen, null)
            //llContent.addView(spleenView)
            spleenView.visibility = View.VISIBLE

            val myItems = listOf("肋下未及", "肿大", "脾脏肿大轻度")
            tvSpleen.setOnClickListener { handleSingleChoice(tvSpleen, myItems) }
        }


        if (ciStr.contains("head")) {
            //val headView = layoutInflater.inflate(R.layout.view_head, null)
            //llContent.addView(headView)
            headView.visibility = View.VISIBLE

            val myItems = listOf("未见明显异常", "异常")
            tvHead.setOnClickListener { handleSingleChoice(tvHead, myItems) }
        }
        if (ciStr.contains("neck")) {
            //val neckView = layoutInflater.inflate(R.layout.view_neck, null)
            //llContent.addView(neckView)
            neckView.visibility = View.VISIBLE

            val myItems = listOf("未见明显异常", "异常")
            tvNeck.setOnClickListener { handleSingleChoice(tvNeck, myItems) }
        }
        if (ciStr.contains("chest")) {
            //val chestView = layoutInflater.inflate(R.layout.view_chest, null)
            //llContent.addView(chestView)
            chestView.visibility = View.VISIBLE

            val myItems = listOf("未见明显异常", "异常", "鸡胸", "漏斗胸", "扁平胸", "纵膈肿瘤术后", "胸廓不对称")
            tvChest.setOnClickListener { handleSingleChoice(tvChest, myItems) }
        }
        if (ciStr.contains("limb")) {
            //val limbView = layoutInflater.inflate(R.layout.view_limb, null)
            //llContent.addView(limbView)
            limbView.visibility = View.VISIBLE

            val myItems = listOf("未见明显异常", "异常", "O型腿", "并指畸形", "内八字", "X型腿", "关节畸形", "关节功能受限", "外伤", "四肢残缺", "脑瘫", "小儿麻痹", "跛行")
            tvLeftTopLimb.setOnClickListener {
                val indexSel = mutableListOf<Int>()
                for(mh in leftTopLimbList) {
                    for(item in myItems) {
                        if (mh == item) {
                            indexSel.add(myItems.indexOf(item))
                        }
                    }
                }
                val initSel = intArrayOf(elements = indexSel.toIntArray())

                handleMultiChoice(tvLeftTopLimb, myItems, initSel, "left_top_limb")
            }
            tvRightTopLimb.setOnClickListener {
                val indexSel = mutableListOf<Int>()
                for(mh in rightTopLimbList) {
                    for(item in myItems) {
                        if (mh == item) {
                            indexSel.add(myItems.indexOf(item))
                        }
                    }
                }
                val initSel = intArrayOf(elements = indexSel.toIntArray())

                handleMultiChoice(tvRightTopLimb, myItems, initSel, "right_top_limb")
            }
            tvLeftBottomLimb.setOnClickListener {
                val indexSel = mutableListOf<Int>()
                for(mh in leftBottomLimbList) {
                    for(item in myItems) {
                        if (mh == item) {
                            indexSel.add(myItems.indexOf(item))
                        }
                    }
                }
                val initSel = intArrayOf(elements = indexSel.toIntArray())

                handleMultiChoice(tvLeftBottomLimb, myItems, initSel, "left_bottom_limb")
            }
            tvRightBottomLimb.setOnClickListener {
                val indexSel = mutableListOf<Int>()
                for(mh in rightBottomLimbList) {
                    for(item in myItems) {
                        if (mh == item) {
                            indexSel.add(myItems.indexOf(item))
                        }
                    }
                }
                val initSel = intArrayOf(elements = indexSel.toIntArray())

                handleMultiChoice(tvRightBottomLimb, myItems, initSel, "right_bottom_limb")
            }
        }
        if (ciStr.contains("skin")) {
            //val skinView = layoutInflater.inflate(R.layout.view_skin, null)
            //llContent.addView(skinView)
            skinView.visibility = View.VISIBLE

            val myItems = listOf("红润", "异常", "湿疹", "皮肤癣", "白癜风", "手术或外伤瘢痕", "血管瘤", "疥疮", "鱼鳞病", "皮炎")
            tvSkin.setOnClickListener {
                val indexSel = mutableListOf<Int>()
                for(mh in skinList) {
                    for(item in myItems) {
                        if (mh == item) {
                            indexSel.add(myItems.indexOf(item))
                        }
                    }
                }
                val initSel = intArrayOf(elements = indexSel.toIntArray())

                handleMultiChoice(tvSkin, myItems, initSel, "skin")
            }
        }
        if (ciStr.contains("lymphaden")) {
            //val lymphadenView = layoutInflater.inflate(R.layout.view_lymphaden, null)
            //llContent.addView(lymphadenView)
            lymphadenView.visibility = View.VISIBLE

            val myItems = listOf("未触及", "肿大")
            tvLymphaden.setOnClickListener { handleSingleChoice(tvLymphaden, myItems) }
        }
        if (ciStr.contains("bcgScar")) {
            //val bcgScarView = layoutInflater.inflate(R.layout.view_bcgscar, null)
            //llContent.addView(bcgScarView)
            bcgScarView.visibility = View.VISIBLE

            val myItems = listOf("有", "无")
            tvBcgScar.setOnClickListener { handleSingleChoice(tvBcgScar, myItems) }
        }
        if (ciStr.contains("hemoglobin")) {
            //val hemoglobinView = layoutInflater.inflate(R.layout.view_hemoglobin, null)
            //llContent.addView(hemoglobinView)
            hemoglobinView.visibility = View.VISIBLE
        }
        if (ciStr.contains("worm")) {
            //val wormView = layoutInflater.inflate(R.layout.view_worm, null)
            //llContent.addView(wormView)
            wormView.visibility = View.VISIBLE

            val myItems = listOf("无", "有")
            tvWorm.setOnClickListener { handleSingleChoice(tvWorm, myItems) }
        }
        if (ciStr.contains("bloodType")) {
            //val bloodTypeView = layoutInflater.inflate(R.layout.view_bloodtype, null)
            //llContent.addView(bloodTypeView)
            bloodTypeView.visibility = View.VISIBLE

            val myItems = listOf("A", "B", "AB", "O", "其他血型")
            tvBloodType.setOnClickListener { handleSingleChoice(tvBloodType, myItems) }
        }
        if (ciStr.contains("pdd")) {
            //val pddView = layoutInflater.inflate(R.layout.view_pdd, null)
            //llContent.addView(pddView)
            pddView.visibility = View.VISIBLE

            val myItems = listOf("阴性", "阳性")
            tvPdd.setOnClickListener { handleSingleChoice(tvPdd, myItems) }
        }
        if (ciStr.contains("liverFunction")) {
            //val liverFunctionView = layoutInflater.inflate(R.layout.view_liverfunction, null)
            //llContent.addView(liverFunctionView)
            liverFunctionView.visibility = View.VISIBLE
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
            if (requestCode == 100) {
                var result = data?.getStringExtra("result")
                if (result == null) {
                    result = CameraScan.parseScanResult(data)
                }
                connBleDevice(result)
            }
            if (requestCode == 200) {
                val right = data?.getFloatExtra("vision_right", -1f)
                val left = data?.getFloatExtra("vision_left", -1f)
                val glass_right = data?.getFloatExtra("vision_glass_right", -1f)
                val glass_left = data?.getFloatExtra("vision_glass_left", -1f)
                if (right != -1f) edtUnGlassRight.setText(""+right) else edtUnGlassRight.setText("")
                if (left != -1f) edtUnGlassLeft.setText(""+left) else edtUnGlassLeft.setText("")
                if (glass_right != -1f) edtGlassRight.setText(""+glass_right) else edtGlassRight.setText("")
                if (glass_left != -1f) edtGlassLeft.setText(""+glass_left) else edtGlassLeft.setText("")
            }
        }
    }

    private fun connBleDevice(result: String?) {
        LogUtils.e("---------$result")
        val qrCodeInfo = Convert.fromJson(result, QrCodeInfo::class.java)
        BleDeviceOpUtil.connectDevice(qrCodeInfo)
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

    //沙眼，结膜炎，红绿色盲，串镜。。。
    private fun handleSingleChoice(tv: TextView, items: List<String>) {
        MaterialDialog(this).show {
            listItems(items = items){ dialog, index, text ->
                tv.text = text
            }
        }
    }


    //疾病史。。。
    private fun handleMultiChoice(tv: TextView, items: List<String>, initSel: IntArray, key: String) {
        MaterialDialog(this).show {
            listItemsMultiChoice(items = items, initialSelection = initSel) { dialog, indices, items ->
                //initSel = indices
                tv.text = items.joinToString(limit = 4)
                when(key) {
                    "medicalHistory" -> medicalHistoryList = items as MutableList<String>
                    "ear" -> earList = items as MutableList<String>
                    "periodontium" -> earList = items as MutableList<String>
                    "heart" -> heartList = items as MutableList<String>
                }

            }
            positiveButton()
        }
    }




    private fun getPhysical() {
        OkGo.get<LzyResponse<Student>>(Api.STUDENT + "/" + student?.id + "?expand=record")
            .execute(object : DialogCallback<LzyResponse<Student>>(this) {
                override fun onSuccess(response: Response<LzyResponse<Student>>) {
                    val student = response.body()?.data!!
                    val data = student.record?.data
                    showData(data)
                }
            })
    }

    //<!------------------ local ----------------->
    private fun getPhysicalLocal() {
        val realm = App.instance.backgroundThreadRealm
        val student = realm.where(Student::class.java).equalTo("id", student?.id).findFirst()
        val localRecord = student?.localRecord
        LogUtils.e("--------"+localRecord)
        val data = Convert.fromJson(localRecord, DataItem::class.java)
        showData(data)
    }

    private fun showData(data: DataItem?){
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
            edtOkGlassRight.setText(vision.spectacles?.od?.replace("-", ""))
            edtOkGlassLeft.setText(vision.spectacles?.os?.replace("-", ""))

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

            val menstruation = sexuality.menstruation
            if (menstruation?.whether == 1) {
                cbWomenWhether.isChecked = true
                cbWomenWhetherDaXue.isChecked = true
            }
            edtWomenAge.setText("${menstruation?.startAge ?: ""}")
            edtFrequency.setText("${menstruation?.frequency ?: ""}")
            edtDuration.setText("${menstruation?.duration ?: ""}")

            val nocturnalEmission = sexuality.nocturnalEmission
            if (nocturnalEmission?.whether == 1) {
                cbMenWhether.isChecked = true
                cbMenWhetherDaXue.isChecked = true
            }
            edtMenAge.setText("${nocturnalEmission?.startAge ?: ""}")


        }


        //trachoma
        val trachoma = data?.trachoma
        if (ciStr.contains("trachoma") && trachoma != null) {
            tvTrachoma.text = trachoma.data
        }
        //conjunctivitis
        val conjunctivitis = data?.conjunctivitis
        if (ciStr.contains("conjunctivitis") && conjunctivitis != null) {
            tvConjunc.text = conjunctivitis.data
        }
        //redGreenBlind
        val redGreenBlind = data?.redGreenBlind
        if (ciStr.contains("redGreenBlind") && redGreenBlind != null) {
            tvRgb.text = redGreenBlind.data
        }
        //eyeAxis
        val eyeAxis = data?.eyeAxis
        if (ciStr.contains("eyeAxis") && eyeAxis != null) {
            edtEyeAxisRight.setText(eyeAxis.od)
            edtEyeAxisLeft.setText(eyeAxis.os)
        }
        //eyePressure
        val eyePressure = data?.eyePressure
        if (ciStr.contains("eyePressure") && eyePressure != null) {
            edtEyePressRight.setText(eyePressure.od)
            edtEyePressLeft.setText(eyePressure.os)
        }
        //cornealCurvature
        val cornealCurvature = data?.cornealCurvature
        if (ciStr.contains("cornealCurvature") && cornealCurvature != null) {
            edtKsRight.setText(cornealCurvature.ks?.od)
            edtKsLeft.setText(cornealCurvature.ks?.os)
            edtKfRight.setText(cornealCurvature.kf?.od)
            edtKfLeft.setText(cornealCurvature.kf?.os)
        }
        //cornealRadius
        val cornealRadius = data?.cornealRadius
        if (ciStr.contains("cornealRadius") && cornealRadius != null) {
            edtCrRight.setText(cornealRadius.od)
            edtCrLeft.setText(cornealRadius.os)
        }
        //cj
        val cj = data?.cj
        if (ciStr.contains("cj") && cj != null) {
            tvQuGuangRight.text = cj.refractiveError?.od
            tvQuGuangLeft.text = cj.refractiveError?.os
            tvCjRight.text = cj.cjData?.od
            tvCjLeft.text = cj.cjData?.os
        }

        //pulse
        val pulse = data?.pulse
        if (ciStr.contains("pulse") && pulse != null) {
            edtPulse.setText(pulse.data)
        }
        //vitalCapacity
        val vitalCapacity = data?.vitalCapacity
        if (ciStr.contains("vitalCapacity") && vitalCapacity != null) {
            edtVC.setText(vitalCapacity.data)
        }
        //bust
        val bust = data?.bust
        if (ciStr.contains("bust") && bust != null) {
            edtBust.setText(bust.data)
        }
        //waistline
        val waistline = data?.waistline
        if (ciStr.contains("waistline") && waistline != null) {
            edtWaistline.setText(waistline.data)
        }
        //hips
        val hips = data?.hips
        if (ciStr.contains("hips") && hips != null) {
            edtHips.setText(hips.data)
        }
        //sittingHeight
        val sittingHeight = data?.sittingHeight
        if (ciStr.contains("sittingHeight") && sittingHeight != null) {
            edtSh.setText(sittingHeight.data)
        }
        //grip
        val grip = data?.grip
        if (ciStr.contains("grip") && grip != null) {
            edtGrip.setText(grip.data)
        }
        //nutrition
        val nutrition = data?.nutrition
        if (ciStr.contains("nutrition") && nutrition != null) {
            tvNutrition.text = nutrition.data
        }


        //ear
        val ear = data?.ear
        if (ciStr.contains("ear") && ear != null) {
            tvEar.text = ear.data?.joinToString(limit = 4)
            earList = ear.data as MutableList<String>
        }
        //nose
        val nose = data?.nose
        if (ciStr.contains("nose") && nose != null) {
            tvNose.text = nose.data?.joinToString(limit = 4)
            noseList = nose.data as MutableList<String>
        }
        //tonsil
        val tonsil = data?.tonsil
        if (ciStr.contains("tonsil") && tonsil != null) {
            tvTonsil.text = tonsil.data
        }
        //periodontium
        val period = data?.periodontium
        if (ciStr.contains("periodontium") && period != null) {
            tvPeriod.text = period.data?.joinToString(limit = 4)
            periodList = period.data as MutableList<String>
        }
        //hearing
        val hearing = data?.hearing
        if (ciStr.contains("hearing") && hearing != null) {
            tvHearingRight.text = hearing.rightAbnormal
            tvHearingLeft.text = hearing.leftAbnormal
        }
        //heart
        val heart = data?.heart
        if (ciStr.contains("heart") && heart != null) {
            tvHeart.text = heart.data?.joinToString(limit = 4)
            heartList = heart.data as MutableList<String>
        }
        //lung
        val lung = data?.lung
        if (ciStr.contains("lung") && lung != null) {
            tvLung.text = lung.data
        }
        //liver
        val liver = data?.liver
        if (ciStr.contains("liver") && liver != null) {
            tvLiver.text = liver.data
        }
        //spleen
        val spleen = data?.spleen
        if (ciStr.contains("spleen") && spleen != null) {
            tvSpleen.text = spleen.data
        }


        //head
        val head = data?.head
        if (ciStr.contains("head") && head != null) {
            tvHead.text = head.data
        }
        //neck
        val neck = data?.neck
        if (ciStr.contains("neck") && neck != null) {
            tvNeck.text = neck.data
        }
        //chest
        val chest = data?.chest
        if (ciStr.contains("chest") && chest != null) {
            tvChest.text = chest.data
        }
        //limb
        val limb = data?.limb
        if (ciStr.contains("limb") && limb != null) {
            tvLeftTopLimb.text = limb.lt?.joinToString(limit = 4)
            leftTopLimbList = limb.lt as MutableList<String>

            tvRightTopLimb.text = limb.rt?.joinToString(limit = 4)
            rightTopLimbList = limb.rt as MutableList<String>

            tvLeftBottomLimb.text = limb.lb?.joinToString(limit = 4)
            leftBottomLimbList = limb.lb as MutableList<String>

            tvRightBottomLimb.text = limb.rb?.joinToString(limit = 4)
            rightBottomLimbList = limb.rb as MutableList<String>
        }
        //skin
        val skin = data?.skin
        if (ciStr.contains("skin") && skin != null) {
            tvSkin.text = skin.data?.joinToString(limit = 4)
            skinList = skin.data as MutableList<String>
        }
        //lymphaden
        val lymphaden = data?.lymphaden
        if (ciStr.contains("lymphaden") && lymphaden != null) {
            tvLymphaden.text = lymphaden.data
        }
        //bcgScar
        val bcgScar = data?.bcgScar
        if (ciStr.contains("bcgScar") && bcgScar != null) {
            tvBcgScar.text = bcgScar.data
        }
        //hemoglobin
        val hemoglobin = data?.hemoglobin
        if (ciStr.contains("hemoglobin") && hemoglobin != null) {
            edtHemoglobin.setText(hemoglobin.data)
        }
        //bloodType
        val bloodType = data?.bloodType
        if (ciStr.contains("bloodType") && bloodType != null) {
            tvBloodType.text = bloodType.data
        }
        //worm
        val worm = data?.worm
        if (ciStr.contains("worm") && worm != null) {
            tvWorm.text = worm.data
        }
        //pdd
        val pdd = data?.pdd
        if (ciStr.contains("pdd") && pdd != null) {
            tvPdd.text = pdd.data
        }
        //liverFunction
        val liverFunction = data?.liverFunction
        if (ciStr.contains("liverFunction") && liverFunction != null) {
            edtAlt.setText(liverFunction.alt)
            edtBc.setText(liverFunction.bc)
        }
    }

    //vision
    private var glassType = ""
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

    //ear
    private var earList = mutableListOf<String>()
    //nose
    private var noseList = mutableListOf<String>()
    //periodontium
    private var periodList = mutableListOf<String>()
    //heart
    private var heartList = mutableListOf<String>()
    //limb
    private var leftTopLimbList = mutableListOf<String>()
    private var rightTopLimbList = mutableListOf<String>()
    private var leftBottomLimbList = mutableListOf<String>()
    private var rightBottomLimbList = mutableListOf<String>()
    //limb
    private var skinList = mutableListOf<String>()



    private fun doSubmit() {
        //vision
        val ndObj = com.alibaba.fastjson.JSONObject()
        ndObj["od"] = edtUnGlassRight.text.toString().trim()
        ndObj["os"] = edtUnGlassLeft.text.toString().trim()
        val gdObj = com.alibaba.fastjson.JSONObject()
        gdObj["od"] = edtGlassRight.text.toString().trim()
        gdObj["os"] = edtGlassLeft.text.toString().trim()
        val stObj = com.alibaba.fastjson.JSONObject()
        stObj["od"] = "-"+edtOkGlassRight.text.toString().trim()
        stObj["os"] = "-"+edtOkGlassLeft.text.toString().trim()
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
        axleObj["od"] = edtARight.text.toString().trim()
        axleObj["os"] = edtALeft.text.toString().trim()
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

        //trachoma
        val trachomaObj = com.alibaba.fastjson.JSONObject()
        trachomaObj["data"] = tvTrachoma.text.toString()
        trachomaObj["context"] = contextObj
        //conjunctivitis
        val conjuncObj = com.alibaba.fastjson.JSONObject()
        conjuncObj["data"] = tvConjunc.text.toString()
        conjuncObj["context"] = contextObj
        //redGreenBlind
        val rgbObj = com.alibaba.fastjson.JSONObject()
        rgbObj["data"] = tvRgb.text.toString()
        rgbObj["context"] = contextObj
        //eyeAxis
        val eyeAxisObj = com.alibaba.fastjson.JSONObject()
        eyeAxisObj["od"] = edtEyeAxisRight.text.toString().trim()
        eyeAxisObj["os"] = edtEyeAxisLeft.text.toString().trim()
        eyeAxisObj["context"] = contextObj
        //eyePressure
        val eyePressureObj = com.alibaba.fastjson.JSONObject()
        eyePressureObj["od"] = edtEyePressRight.text.toString().trim()
        eyePressureObj["os"] = edtEyePressLeft.text.toString().trim()
        eyePressureObj["context"] = contextObj
        //cornealCurvature
        val ksObj = com.alibaba.fastjson.JSONObject()
        ksObj["od"] = edtKsRight.text.toString().trim()
        ksObj["os"] = edtKsLeft.text.toString().trim()
        val kfObj = com.alibaba.fastjson.JSONObject()
        kfObj["od"] = edtKfRight.text.toString().trim()
        kfObj["os"] = edtKfLeft.text.toString().trim()
        val ccObj = com.alibaba.fastjson.JSONObject()
        ccObj["ks"] = ksObj
        ccObj["kf"] = kfObj
        ccObj["context"] = contextObj
        //cornealRadius
        val crObj = com.alibaba.fastjson.JSONObject()
        crObj["od"] = edtCrRight.text.toString().trim()
        crObj["os"] = edtCrLeft.text.toString().trim()
        crObj["context"] = contextObj
        //cj
        val refErrorObj = com.alibaba.fastjson.JSONObject()
        refErrorObj["od"] = tvQuGuangRight.text.toString().trim()
        refErrorObj["os"] = tvQuGuangLeft.text.toString().trim()
        val cjDataObj = com.alibaba.fastjson.JSONObject()
        cjDataObj["od"] = tvCjRight.text.toString().trim()
        cjDataObj["os"] = tvCjLeft.text.toString().trim()
        val cjObj = com.alibaba.fastjson.JSONObject()
        cjObj["refractiveError"] = refErrorObj
        cjObj["cjData"] = cjDataObj
        cjObj["context"] = contextObj

        //pulse
        val pulseObj = com.alibaba.fastjson.JSONObject()
        pulseObj["data"] = edtPulse.text.toString().trim()
        pulseObj["context"] = contextObj
        //vitalCapacity
        val vcObj = com.alibaba.fastjson.JSONObject()
        vcObj["data"] = edtVC.text.toString().trim()
        vcObj["context"] = contextObj
        //bust
        val bustObj = com.alibaba.fastjson.JSONObject()
        bustObj["data"] = edtBust.text.toString().trim()
        bustObj["context"] = contextObj
        //waistline
        val waistlineObj = com.alibaba.fastjson.JSONObject()
        waistlineObj["data"] = edtWaistline.text.toString().trim()
        waistlineObj["context"] = contextObj
        //hips
        val hipsObj = com.alibaba.fastjson.JSONObject()
        hipsObj["data"] = edtHips.text.toString().trim()
        hipsObj["context"] = contextObj
        //sittingHeight
        val shObj = com.alibaba.fastjson.JSONObject()
        shObj["data"] = edtSh.text.toString().trim()
        shObj["context"] = contextObj
        //grip
        val gripObj = com.alibaba.fastjson.JSONObject()
        gripObj["data"] = edtGrip.text.toString().trim()
        gripObj["context"] = contextObj
        //nutrition
        val nutritionObj = com.alibaba.fastjson.JSONObject()
        nutritionObj["data"] = tvNutrition.text.toString()
        nutritionObj["context"] = contextObj

        //ear
        val earObj = com.alibaba.fastjson.JSONObject()
        earObj["data"] = earList.toTypedArray()
        earObj["context"] = contextObj
        //nose
        val noseObj = com.alibaba.fastjson.JSONObject()
        noseObj["data"] = noseList.toTypedArray()
        noseObj["context"] = contextObj
        //tonsil
        val tonsilObj = com.alibaba.fastjson.JSONObject()
        tonsilObj["data"] = tvTonsil.text.toString()
        tonsilObj["context"] = contextObj
        //periodontium
        val periodObj = com.alibaba.fastjson.JSONObject()
        periodObj["data"] = periodList.toTypedArray()
        periodObj["context"] = contextObj
        //hearing
        val hearingObj = com.alibaba.fastjson.JSONObject()
        hearingObj["rightAbnormal"] = tvHearingRight.text.toString()
        hearingObj["leftAbnormal"] = tvHearingLeft.text.toString()
        hearingObj["context"] = contextObj
        //heart
        val heartObj = com.alibaba.fastjson.JSONObject()
        heartObj["data"] = heartList.toTypedArray()
        heartObj["context"] = contextObj
        //lung
        val lungObj = com.alibaba.fastjson.JSONObject()
        lungObj["data"] = tvLung.text.toString()
        lungObj["context"] = contextObj
        //liver
        val liverObj = com.alibaba.fastjson.JSONObject()
        liverObj["data"] = tvLiver.text.toString()
        liverObj["context"] = contextObj
        //spleen
        val spleenObj = com.alibaba.fastjson.JSONObject()
        spleenObj["data"] = tvSpleen.text.toString()
        spleenObj["context"] = contextObj


        //head
        val headObj = com.alibaba.fastjson.JSONObject()
        headObj["data"] = tvHead.text.toString()
        headObj["context"] = contextObj
        //neck
        val neckObj = com.alibaba.fastjson.JSONObject()
        neckObj["data"] = tvNeck.text.toString()
        neckObj["context"] = contextObj
        //chest
        val chestObj = com.alibaba.fastjson.JSONObject()
        chestObj["data"] = tvChest.text.toString()
        chestObj["context"] = contextObj
        //limb
        val limbObj = com.alibaba.fastjson.JSONObject()
        limbObj["lt"] = leftTopLimbList.toTypedArray()
        limbObj["rt"] = rightTopLimbList.toTypedArray()
        limbObj["lb"] = leftBottomLimbList.toTypedArray()
        limbObj["rb"] = rightBottomLimbList.toTypedArray()
        limbObj["context"] = contextObj
        //skin
        val skinObj = com.alibaba.fastjson.JSONObject()
        skinObj["data"] = skinList.toTypedArray()
        skinObj["context"] = contextObj
        //lymphaden
        val lymphadenObj = com.alibaba.fastjson.JSONObject()
        lymphadenObj["data"] = tvLymphaden.text.toString()
        lymphadenObj["context"] = contextObj
        //bcgScar
        val bcgScarObj = com.alibaba.fastjson.JSONObject()
        bcgScarObj["data"] = tvBcgScar.text.toString()
        bcgScarObj["context"] = contextObj
        //hemoglobin
        val hemoglobinObj = com.alibaba.fastjson.JSONObject()
        hemoglobinObj["data"] = edtHemoglobin.text.toString()
        hemoglobinObj["context"] = contextObj
        //worm
        val wormObj = com.alibaba.fastjson.JSONObject()
        wormObj["data"] = tvWorm.text.toString()
        wormObj["context"] = contextObj
        //bloodType
        val bloodTypeObj = com.alibaba.fastjson.JSONObject()
        bloodTypeObj["data"] = tvBloodType.text.toString()
        bloodTypeObj["context"] = contextObj
        //pdd
        val pddObj = com.alibaba.fastjson.JSONObject()
        pddObj["data"] = tvPdd.text.toString()
        pddObj["context"] = contextObj
        //liverFunctionObj
        val liverFunctionObj = com.alibaba.fastjson.JSONObject()
        liverFunctionObj["alt"] = edtAlt.text.toString().trim()
        liverFunctionObj["bc"] = edtBc.text.toString().trim()
        liverFunctionObj["context"] = contextObj


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

        if (ciStr.contains("trachoma")) dataObj["trachoma"] = trachomaObj
        if (ciStr.contains("conjunctivitis")) dataObj["conjunctivitis"] = conjuncObj
        if (ciStr.contains("redGreenBlind")) dataObj["redGreenBlind"] = rgbObj
        if (ciStr.contains("eyeAxis")) dataObj["eyeAxis"] = eyeAxisObj
        if (ciStr.contains("eyePressure")) dataObj["eyePressure"] = eyePressureObj
        if (ciStr.contains("cornealCurvature")) dataObj["cornealCurvature"] = ccObj
        if (ciStr.contains("cornealRadius")) dataObj["cornealRadius"] = crObj
        if (ciStr.contains("cj")) dataObj["cj"] = cjObj

        if (ciStr.contains("pulse")) dataObj["pulse"] = pulseObj
        if (ciStr.contains("vitalCapacity")) dataObj["vitalCapacity"] = vcObj
        if (ciStr.contains("bust")) dataObj["bust"] = bustObj
        if (ciStr.contains("waistline")) dataObj["waistline"] = waistlineObj
        if (ciStr.contains("hips")) dataObj["hips"] = hipsObj
        if (ciStr.contains("sittingHeight")) dataObj["sittingHeight"] = shObj
        if (ciStr.contains("grip")) dataObj["grip"] = gripObj
        if (ciStr.contains("nutrition")) dataObj["nutrition"] = nutritionObj

        if (ciStr.contains("ear")) dataObj["ear"] = earObj
        if (ciStr.contains("nose")) dataObj["nose"] = noseObj
        if (ciStr.contains("tonsil")) dataObj["tonsil"] = tonsilObj
        if (ciStr.contains("periodontium")) dataObj["periodontium"] = periodObj
        if (ciStr.contains("hearing")) dataObj["hearing"] = hearingObj
        if (ciStr.contains("heart")) dataObj["heart"] = heartObj
        if (ciStr.contains("lung")) dataObj["lung"] = lungObj
        if (ciStr.contains("liver")) {
            val keyList = ciStr.split(", ")
            var flag = false
            keyList.forEach { if(it == "liver") flag = true }
            if (flag) dataObj["liver"] = liverObj
        }
        if (ciStr.contains("spleen")) dataObj["spleen"] = spleenObj

        if (ciStr.contains("head")) dataObj["head"] = headObj
        if (ciStr.contains("neck")) dataObj["neck"] = neckObj
        if (ciStr.contains("chest")) dataObj["chest"] = chestObj
        if (ciStr.contains("limb")) dataObj["limb"] = limbObj
        if (ciStr.contains("skin")) dataObj["skin"] = skinObj
        if (ciStr.contains("lymphaden")) dataObj["lymphaden"] = lymphadenObj
        if (ciStr.contains("bcgScar")) dataObj["bcgScar"] = bcgScarObj
        if (ciStr.contains("hemoglobin")) dataObj["hemoglobin"] = hemoglobinObj
        if (ciStr.contains("worm")) dataObj["worm"] = wormObj
        if (ciStr.contains("bloodType")) dataObj["bloodType"] = bloodTypeObj
        if (ciStr.contains("pdd")) dataObj["pdd"] = pddObj
        if (ciStr.contains("liverFunction")) dataObj["liverFunction"] = liverFunctionObj

//        val upMap = mutableMapOf<Any?, Any?>()
//        upMap["studentId"] = student?.id
//        upMap["data"] = dataObj
//        val jsonObj = JSONObject(upMap)
//        OkGo.post<LzyResponse<Any>>(Api.RECORD_SUBMIT)
//            .upJson(jsonObj)
//            .execute(object : DialogCallback<LzyResponse<Any>>(this) {
//                override fun onSuccess(response: Response<LzyResponse<Any>>) {
//                    val any = response.body()?.data
//                    ToastUtils.showShort("提交成功")
//                    finish()
//                }
//            })

        //<!------------------ local ----------------->
        val realm = App.instance.backgroundThreadRealm
        realm.executeTransaction {
            val student = it.where(Student::class.java).equalTo("id", student?.id).findFirst()
            student?.localRecord = dataObj.toJSONString()
            ToastUtils.showShort("提交成功")
            finish()
        }
    }

    private fun updateDeviceStatusUi() {
        if (ciStr.contains("vision")) {
            //是否连接
            if(EyeChartOpUtil.isConnected()) {
                val eyeChartName = EyeChartOpUtil.deviceInfo()?.name
                tvEyeChartName.text = Html.fromHtml("电子视力表 已连接 <font color=\"#247CB7\">$eyeChartName</font>")
                tvEyeChartDisconn.text = "打开遥控器"
            } else {
                tvEyeChartName.text = "电子视力表 未连接"
                tvEyeChartDisconn.text = "扫码连接设备"
            }
        }
        if (ciStr.contains("diopter")) {
            //是否连接
            if(BleDeviceOpUtil.isDiopterConnected()) {
                val diopterName = BleDeviceOpUtil.diopterDeviceInfo()?.name
                tvDiopterName.text = Html.fromHtml("电脑验光仪 已连接 <font color=\"#247CB7\">$diopterName</font>")
                tvDiopterDisconn.text = "断开连接"

                //notify data
                BleDeviceOpUtil.notifyData("diopter")
            } else {
                tvDiopterName.text = "电脑验光仪 未连接"
                tvDiopterDisconn.text = "扫码连接设备"
            }
        }
        if (ciStr.contains("height") || ciStr.contains("weight")) {
            //是否连接
            if(BleDeviceOpUtil.isHWConnected()) {
                val hwName = BleDeviceOpUtil.hwDeviceInfo()?.name
                tvHWName.text = Html.fromHtml("身高体重秤 已连接 <font color=\"#247CB7\">$hwName</font>")
                tvHWDisconn.text = "断开连接"

                //notify data
                BleDeviceOpUtil.notifyData("heightWeight")
            } else {
                tvHWName.text = "身高体重秤 未连接"
                tvHWDisconn.text = "扫码连接设备"
            }
        }
        if (ciStr.contains("bloodPressure")) {
            //是否连接
            if(BleDeviceOpUtil.isBPConnected()) {
                val bpName = BleDeviceOpUtil.bpDeviceInfo()?.name
                tvBPName.text = Html.fromHtml("电子血压计 已连接 <font color=\"#247CB7\">$bpName</font>")
                tvBPDisconn.text = "断开连接"

                //notify data
                BleDeviceOpUtil.notifyData("bloodPressure")
            } else {
                tvBPName.text = "电子血压计 未连接"
                tvBPDisconn.text = "扫码连接设备"
            }
        }
        if (ciStr.contains("eyePressure")) {
            //是否连接
            if(BleDeviceOpUtil.isEPConnected()) {
                val epName = BleDeviceOpUtil.epDeviceInfo()?.name
                tvEPName.text = Html.fromHtml("眼压计 已连接 <font color=\"#247CB7\">$epName</font>")
                tvEPDisconn.text = "断开连接"

                //notify data
                BleDeviceOpUtil.notifyData("eyePressure")
            } else {
                tvEPName.text = "眼压计 未连接"
                tvEPDisconn.text = "扫码连接设备"
            }
        }
        if (ciStr.contains("vitalCapacity")) {
            //是否连接
            if(BleDeviceOpUtil.isVCConnected()) {
                val vcName = BleDeviceOpUtil.vcDeviceInfo()?.name
                tvVCName.text = Html.fromHtml("肺活量 已连接 <font color=\"#247CB7\">$vcName</font>")
                tvVCDisconn.text = "断开连接"

                //notify data
                BleDeviceOpUtil.notifyData("vitalCapacity")
            } else {
                tvVCName.text = "肺活量 未连接"
                tvVCDisconn.text = "扫码连接设备"
            }
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)

        refreshUI()
    }

    private fun refreshUI() {
        updateDeviceStatusUi()
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDeviceStatusEvent(connStatusEvent: DeviceStatusEvent) {
        updateDeviceStatusUi()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDeviceNotifyDataEvent(deviceNotifyDataEvent: DeviceNotifyDataEvent) {
        when(deviceNotifyDataEvent.deviceType) {
            "diopter" -> {
                val refData = deviceNotifyDataEvent.any as RefractionData
                LogUtils.e("---------$refData")
                edtSRight.setText(refData.od.s)
                edtCRight.setText(refData.od.c)
                edtARight.setText(refData.od.a)
                edtSLeft.setText(refData.os.s)
                edtCLeft.setText(refData.os.c)
                edtALeft.setText(refData.os.a)
            }
            "heightWeight" -> {
                val hwData = deviceNotifyDataEvent.any as HWData
                LogUtils.e("---------$hwData")
                edtHeight.setText(hwData.h)
                edtWeight.setText(hwData.w)
            }
            "bloodPressure" -> {
                val bpData = deviceNotifyDataEvent.any as BPData
                LogUtils.e("---------$bpData")
                edtSystolic.setText("${(bpData.sys.toFloat()).toInt()}")
                edtDiastolic.setText("${(bpData.dia.toFloat()).toInt()}")
            }
            "eyePressure" -> {
                val lopData = deviceNotifyDataEvent.any as IopData
                LogUtils.e("---------$lopData")
                edtEyePressRight.setText(lopData.od.iop)
                edtEyePressLeft.setText(lopData.os.iop)
            }
            "vitalCapacity" -> {
                val vcData = deviceNotifyDataEvent.any as VCData
                LogUtils.e("---------$vcData")
                edtVC.setText("${(vcData.vc.toFloat() * 1000).toInt()}")
            }
        }
    }
}