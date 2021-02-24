package com.qpsoft.cdc.qrcodegenerator

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import com.alibaba.fastjson.JSONObject
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.bigkoo.pickerview.view.OptionsPickerView
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ToastUtils
import com.clj.fastble.data.BleDevice
import com.king.zxing.util.CodeUtils
import com.qpsoft.cdc.R
import com.qpsoft.cdc.base.BaseActivity
import com.qpsoft.cdc.ui.MainActivity
import com.qpsoft.cdc.utils.AlbumUtil
import com.qpsoft.cdc.utils.BleDeviceOpUtil
import kotlinx.android.synthetic.main.activity_gen_qrcode.*
import java.util.*


class GenQrcodeActivity : BaseActivity() {

    private var bleDevice: BleDevice? = null

    private val typeList = listOf("验光仪", "身高体重秤", "血压计", "肺活量", "眼生物测量仪", "眼压计")
    private val brandMap = mapOf(
        "验光仪" to listOf("法里奥", "天乐", "尼德克", "拓普康", "新缘", "雄博", "多美", "Unicos", "维伦", "索维", "美沃", "佳乐普"),
        "身高体重秤" to listOf("上禾", "乐佳"),
        "血压计" to listOf("鱼跃"),
        "肺活量" to listOf("呼吸家"),
        "眼生物测量仪" to listOf("尼德克", "索维"),
        "眼压计" to listOf("尼德克")
    )
    private val modelMap = mapOf(
        "法里奥" to listOf("fr710", "fr8900", "fl800"),
        "天乐" to listOf("kr9800", "rm9000"),
        "尼德克" to listOf("ark1", "al-scan", "nt510"),
        "拓普康" to listOf("rm8900", "kr800"),
        "新缘" to listOf("fa6500", "fa6500k"),
        "雄博" to listOf("rmk800", "rmk150"),
        "多美" to listOf("rc800", "rt6000"),
        "Unicos" to listOf("urk700"),
        "维伦" to listOf("vs100"),
        "索维" to listOf("sw800", "sw9000"),
        "美沃" to listOf("v100"),
        "佳乐普" to listOf("crk8800"),
        "上禾" to listOf("sh01"),
        "乐佳" to listOf("lj700"),
        "鱼跃" to listOf("ye900"),
        "呼吸家" to listOf("b1")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gen_qrcode)

        bleDevice = intent.getParcelableExtra("bleDevice")

        setBackBtn()
        setTitle("蓝牙设备二维码生成")

        tvBleName.text = bleDevice?.name

        rlType.setOnClickListener {
            KeyboardUtils.hideSoftInput(this@GenQrcodeActivity)
            op(typeList, 0)
        }
        
        rlBrand.setOnClickListener {
            KeyboardUtils.hideSoftInput(this@GenQrcodeActivity)
            val typeStr = tvType.text.toString()
            if (!TextUtils.isEmpty(typeStr)) {
                val brandList = brandMap[typeStr]!!
                op(brandList, 1)
            } else {
                ToastUtils.showShort("请先选择设备类型")
            }
        }


        rlMode.setOnClickListener {
            KeyboardUtils.hideSoftInput(this@GenQrcodeActivity)
            val brandStr = tvBrand.text.toString()
            if (!TextUtils.isEmpty(brandStr)) {
                val modelList = modelMap[brandStr]!!
                //val filterList = modelList.stream().filter { it== "" }.collect(Collectors.toList())
                op(modelList, 2)
            } else {
                ToastUtils.showShort("请先选择设备品牌")
            }
        }

        edtNo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                val no = p0.toString()
                tvDeviceName.text = typeStr+"_"+brandStr+"_"+modelStr+"_"+no
            }
        })


        tvDownQrcode.setOnClickListener {
            val deviceName = tvDeviceName.text.toString()
            if (TextUtils.isEmpty(deviceName)) {
                ToastUtils.showShort("请完善设备信息")
                return@setOnClickListener
            }
            val jsonObj = JSONObject()
            jsonObj.put("type", tvType.text.toString())
            jsonObj.put("brand", tvBrand.text.toString())
            jsonObj.put("model", tvModel.text.toString())
            jsonObj.put("no", edtNo.text.toString())
            jsonObj.put("name", tvBrand.text.toString()+"_"+tvModel.text.toString()+"_"+edtNo.text.toString())
            jsonObj.put("bluetooth_name", bleDevice?.name)
            jsonObj.put("bluetooth_mac", bleDevice?.mac)
            jsonObj.put("service_id", if(tvModel.text.toString() == "ye900") BleDeviceOpUtil.uuid_service_bp else BleDeviceOpUtil.uuid_service)
            jsonObj.put("notify_id", if(tvModel.text.toString() == "ye900") BleDeviceOpUtil.uuid_notify_bp else BleDeviceOpUtil.uuid_notify)
            jsonObj.put("write_id", BleDeviceOpUtil.uuid_write)
            val txtStr: String = jsonObj.toJSONString()
            val qrCodeBitmap: Bitmap = CodeUtils.createQRCode(txtStr, 200)
            AlbumUtil.saveBitmap2file(qrCodeBitmap, tvDeviceName.text.toString(), this@GenQrcodeActivity)
            startActivity(Intent(this@GenQrcodeActivity, MainActivity::class.java))
        }
    }

    private var typeStr: String = ""
    private var brandStr: String = ""
    private var modelStr: String = ""
    private fun op(list: List<String>, pos: Int) {
        val pvOptions: OptionsPickerView<String> = OptionsPickerBuilder(this@GenQrcodeActivity) { options1, option2, options3, v ->
            val tx: String = list[options1]
            when(pos) {
                0 -> {
                    tvType.text = tx
                    typeStr = tx
                    tvDeviceName.text = typeStr
                }
                1 -> {
                    tvBrand.text = tx
                    brandStr = tx
                    tvDeviceName.text = typeStr+"_"+brandStr
                }
                2 -> {
                    tvModel.text = tx
                    modelStr = tx
                    tvDeviceName.text = typeStr+"_"+brandStr+"_"+modelStr
                }
            }

        }.setSubmitColor(Color.WHITE).setCancelColor(Color.WHITE).setContentTextSize(26).build()
        pvOptions.setNPicker(list, null, null)
        pvOptions.show()
    }
}