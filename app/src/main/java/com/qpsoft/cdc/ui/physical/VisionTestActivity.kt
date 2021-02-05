package com.qpsoft.cdc.ui.physical

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleNotifyCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.qpsoft.cdc.R
import com.qpsoft.cdc.base.BaseActivity
import com.qpsoft.cdc.ui.entity.VisionValue
import com.qpsoft.cdc.utils.EyeChartOpUtil
import fynn.app.PromptDialog
import kotlinx.android.synthetic.main.activity_vision_test.*
import java.math.BigDecimal


class VisionTestActivity : BaseActivity() {

    private var glassType: String? = null
    private var schoolCategory: String? = null
    private lateinit var mAdapter: BaseQuickAdapter<VisionValue, BaseViewHolder>

    private var type = ""
    private var item = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vision_test)

        glassType = intent.getStringExtra("glassType")
        schoolCategory = intent.getStringExtra("schoolCategory")

        setBackBtn()
        setTitle("电子视力表遥控器")

        val startLine = when(schoolCategory) {
            "Kindergarten,PrimarySchool" -> 4.7f
            "MiddleSchool" -> 4.5f
            else -> 4.3f
        }
        //初始化设置
        //sendSetting(startLine)

        when(glassType) {
            "No" -> {
                llVisionRight.visibility = View.VISIBLE
                llVisionLeft.visibility = View.VISIBLE
                type = "vision"
                item = "vision_od"
            }
            "Frame", "ContactLens" -> {
                llVisionRight.visibility = View.VISIBLE
                llVisionLeft.visibility = View.VISIBLE
                llGlassRight.visibility = View.VISIBLE
                llGlassLeft.visibility = View.VISIBLE
                type = "all"
                item = "vision_od"
            }
            "OkGlass" -> {
                llGlassRight.visibility = View.VISIBLE
                llGlassLeft.visibility = View.VISIBLE
                type = "glass"
                item = "glass_od"
            }
        }
        //开始测试
        sendStart()
        //查询状态
        Handler().postDelayed({ queryStatus() }, 1000)

        rvEyeChart.layoutManager = GridLayoutManager(this, 5)
        mAdapter = object: BaseQuickAdapter<VisionValue, BaseViewHolder>(R.layout.item_eyechart, getEyeChartData()) {
            override fun convert(holder: BaseViewHolder, item: VisionValue) {
                holder.setText(R.id.tvValue, ""+transformSubScore(item.value.toFloat()))
                if (item.color) {
                    holder.setTextColorRes(R.id.tvValue, R.color.color_cb7)
                } else {
                    holder.setTextColorRes(R.id.tvValue, R.color.red)
                }
                if (item.check) {
                    holder.setBackgroundResource(R.id.tvValue, R.drawable.bg_cb7_shape)
                    holder.setTextColorRes(R.id.tvValue, R.color.white)
                } else {
                    holder.setBackgroundColor(R.id.tvValue, resources.getColor(android.R.color.transparent))
                    holder.setTextColorRes(R.id.tvValue, R.color.color_26)
                }

            }

        }
        rvEyeChart.adapter = mAdapter

        mAdapter.setOnItemClickListener { adapter, view, position ->
            val eyeValue = mAdapter.getItem(position)
            sendLine(eyeValue.value.toFloat())
        }

        ivOk.setOnClickListener { sendOk() }
        ivUp.setOnClickListener { sendDirection("up") }
        ivDown.setOnClickListener { sendDirection("down") }
        ivLeft.setOnClickListener { sendDirection("left") }
        ivRight.setOnClickListener { sendDirection("right") }
        llCommit.setOnClickListener { submit() }

        handNotifyData()
    }

    private var lowVision = false
    private fun showEyeChart(testCategory: Int) {
        if (testCategory == 40) { //低视力
            lowVision = true
            when(item) {
                "vision_od" -> alertRightEyeLowUi()
                "vision_os" -> alertLeftEyeTipsStationUi(startStationDistance)
            }
        } else {
            lowVision = false
            when(item) {
                "vision_od" -> alertRightEyeUi()
                "glass_od" -> alertGlassRightEyeUi()
            }
        }
    }


    private fun submit() {
        val jsonObj = JSONObject()
        jsonObj["action"] = "command"
        val payloadObj = JSONObject()
        payloadObj["cmd"] = "next"
        jsonObj["payload"] = payloadObj
        EyeChartOpUtil.send(jsonObj.toJSONString())
    }


    private fun sendLine(lineValue: Float) {
        val jsonObj = JSONObject()
        jsonObj["action"] = "command"
        val payloadObj = JSONObject()
        payloadObj["cmd"] = "line"
        val dataObj = JSONObject()
        dataObj["line"] = lineValue
        payloadObj["data"] = dataObj
        jsonObj["payload"] = payloadObj
        EyeChartOpUtil.send(jsonObj.toJSONString())
    }

    private fun sendDirection(direction: String) {
        val jsonObj = JSONObject()
        jsonObj["action"] = "command"
        val payloadObj = JSONObject()
        payloadObj["cmd"] = "direction"
        val dataObj = JSONObject()
        dataObj["direction"] = direction
        payloadObj["data"] = dataObj
        jsonObj["payload"] = payloadObj
        EyeChartOpUtil.send(jsonObj.toJSONString())
    }

    private fun sendRetest() {
        val jsonObj = JSONObject()
        jsonObj["action"] = "command"
        val payloadObj = JSONObject()
        payloadObj["cmd"] = "retest"
        jsonObj["payload"] = payloadObj
        EyeChartOpUtil.send(jsonObj.toJSONString())
    }

    private fun sendOk() {
        val jsonObj = JSONObject()
        jsonObj["action"] = "command"
        val payloadObj = JSONObject()
        payloadObj["cmd"] = "ok"
        jsonObj["payload"] = payloadObj
        EyeChartOpUtil.send(jsonObj.toJSONString())
    }


    private fun sendStart() {
        val jsonObj = JSONObject()
        jsonObj["action"] = "command"
        val payloadObj = JSONObject()
        payloadObj["cmd"] = "start"
        val dataObj = JSONObject()
        dataObj["test_category"] = 10
        dataObj["type"] = type
        dataObj["item"] = item
        payloadObj["data"] = dataObj
        jsonObj["payload"] = payloadObj
        EyeChartOpUtil.send(jsonObj.toJSONString())
    }

    private fun sendSetting(startLine: Float) {
        val jsonObj = JSONObject()
        jsonObj["action"] = "command"
        val payloadObj = JSONObject()
        payloadObj["cmd"] = "setting"
        val dataObj = JSONObject()
        //dataObj["distance"] = 5f
        //dataObj["test_category"] = 10
        dataObj["start_line"] = startLine
        payloadObj["data"] = dataObj
        jsonObj["payload"] = payloadObj
        EyeChartOpUtil.send(jsonObj.toJSONString())
    }

    private fun queryStatus() {
        val jsonObj = JSONObject()
        jsonObj["action"] = "command"
        val payloadObj = JSONObject()
        payloadObj["cmd"] = "state"
        jsonObj["payload"] = payloadObj
        EyeChartOpUtil.send(jsonObj.toJSONString())
    }

    private val sb = StringBuilder()
    private var bleDevice: BleDevice? = null
    private fun handNotifyData() {
        val bleDevice: BleDevice = EyeChartOpUtil.bleDevice() ?: return
        BleManager.getInstance().notify(bleDevice, EyeChartOpUtil.uuid_service, EyeChartOpUtil.uuid_notify, object : BleNotifyCallback() {
            override fun onNotifySuccess() {

            }

            override fun onNotifyFailure(exception: BleException?) {

            }

            override fun onCharacteristicChanged(data: ByteArray?) {
                val message = String(data!!)
                val result: String = sb.append(message).toString()
                if (result.endsWith("}}")) {
                    parseData(result)
                    sb.setLength(0)
                }
            }
        })
    }

    private var vision_od = -1f
    private var vision_os = -1f
    private var glass_od = -1f
    private var glass_os = -1f

    private fun parseData(message: String) {
        LogUtils.e("----------$message")
        if (message.contains("action")) {
            val jsonObj = JSON.parseObject(message)
            val action = jsonObj.getString("action")
            val payloadObj = jsonObj.getJSONObject("payload")
            if ("state" == action) {
                val curTestObj = payloadObj.getJSONObject("cur_test")
                val state = curTestObj.getIntValue("state")
                if (state == 0) {
                    ToastUtils.showShort("选择戴镜类型开始测试")
                } else if (state == 10) {
                    //测试中
                    val testCategory = curTestObj.getIntValue("test_category")
                    var line = curTestObj.getFloatValue("line")
                    line = transformAddScore(line)
                    val direction = curTestObj.getString("direction")
                    val count = curTestObj.getIntValue("line_count")
                    val inCorrect = curTestObj.getIntValue("line_incorrect")
                    val correct = curTestObj.getIntValue("line_correct")
                    val curDirectionCorrect = curTestObj.getIntValue("cur_direction_correct")
                    val linePass = curTestObj.getIntValue("line_pass")
                    type = curTestObj.getString("type")
                    item = curTestObj.getString("item")
                    currentStationDistance  = curTestObj.getFloatValue("cur_distance")
                    startStationDistance = curTestObj.getFloatValue("start_distance")

                    //val currentVisionLevel = (line * 10f).toInt()
                    setVisionBg(line)
                    showRealTimeEyeValue(line, direction, correct, inCorrect, count)
                    val distance = payloadObj.getFloatValue("distance")
                    tvDistance.setText("" + distance + "米")
                    //当前测试项目
                    setCurEye(item)
                    val resultObj = payloadObj.getJSONObject("result")
                    val visionOd = resultObj.getFloatValue("vision_od")
                    val visionOs = resultObj.getFloatValue("vision_os")
                    val glassOd = resultObj.getFloatValue("glass_od")
                    val glassOs = resultObj.getFloatValue("glass_os")
                    tvVisionRight.text = "裸眼右\n${if (visionOd == -1f) "" else visionOd}"
                    tvVisionLeft.text = "裸眼左\n${if (visionOs == -1f) "" else visionOs}"
                    tvGlassRight.text = "戴镜右\n${if (glassOd == -1f) "" else glassOd}"
                    tvGlassLeft.text = "戴镜左\n${if (glassOs == -1f) "" else glassOs}"

                    //alert
                    showEyeChart(testCategory)
                }
            } else if ("event" == action) {
                val event = payloadObj.getString("event")
                val stateObj = payloadObj.getJSONObject("state")
                val line = stateObj.getFloatValue("line")
                val direction = stateObj.getString("direction")
                val count = stateObj.getIntValue("line_count")
                val inCorrect = stateObj.getIntValue("line_incorrect")
                val correct = stateObj.getIntValue("line_correct")
                val curDirectionCorrect = stateObj.getIntValue("cur_direction_correct")
                val linePass = stateObj.getIntValue("line_pass")
                val type = stateObj.getString("type")
                val item = stateObj.getString("item")
                //val currentVisionLevel = (line * 10f).toInt()

                //当前测试项目
                setCurEye(item)

                if(!lowVision) closeAlert() else closeAlertLow()

                //handle event
                if ("line" == event) {
                    setVisionBg(line)
                    showRealTimeEyeValue(line, direction, correct, inCorrect, count)
                } else if ("direction" == event) {
                    if (curDirectionCorrect == 1) {
                        playSound(R.raw.correct)
                    } else {
                        playSound(R.raw.error)
                    }
                    if (linePass == 1) {
                        //change green
                        setVisionColor(line, true)
                    } else if (linePass == 2) {
                        //change red
                        setVisionColor(line, false)
                    }
                } else if ("cur_result" == event) {
                    if ("vision_od" == item) {
                        tvVisionRight.text = "裸眼右\n$line"
                        if(!lowVision) alertLeftEyeUi(line) else handleEyeLow(line, inCorrect, item, linePass)
                    } else if ("vision_os" == item) {
                        tvVisionLeft.text = "裸眼左\n$line"
                        if (!lowVision) {
                            alertResultUi(line)
                            if ("all" == type) {
                                resultEyePd?.dismiss()
                                alertGlassRightEyeUi()
                            }
                        } else {
                            handleEyeLow(line, inCorrect, item, linePass)
                        }
                    } else if ("glass_od" == item) {
                        tvGlassRight.text = "戴镜右\n$line"
                        alertGlassLeftEyeUi(line)
                    } else if ("glass_os" == item) {
                        tvGlassLeft.text = "戴镜左\n$line"
                        alertGlassResultUi(line)
                    }
                    //重置示标
                    recoveryVisionColor()
                } else if ("next" == event) {
                    val resultObj = stateObj.getJSONObject("result")
                    val visionOd = resultObj.getFloatValue("vision_od")
                    val visionOs = resultObj.getFloatValue("vision_os")
                    val glassOd = resultObj.getFloatValue("glass_od")
                    val glassOs = resultObj.getFloatValue("glass_os")
                    if ("vision_od" == item) {
                        tvVisionRight.text = "裸眼右\n${if (visionOd == -1f) "" else visionOd}"
                        if(!lowVision) alertLeftEyeUi(visionOd) else handleEyeLow(visionOd, inCorrect, item, linePass)
                    } else if ("vision_os" == item) {
                        tvVisionRight.text = "裸眼右\n${if (visionOd == -1f) "" else visionOd}"
                        tvVisionLeft.text = "裸眼左\n${if (visionOs == -1f) "" else visionOs}"
                        if (!lowVision) {
                            alertResultUi(visionOs)
                            if ("all" == type) {
                                resultEyePd?.dismiss()
                                alertGlassRightEyeUi()
                            }
                        } else {
                            handleEyeLow(visionOs, inCorrect, item, linePass)
                        }
                    } else if ("glass_od" == item) {
                        tvVisionRight.text = "裸眼右\n${if (visionOd == -1f) "" else visionOd}"
                        tvVisionLeft.text = "裸眼左\n${if (visionOs == -1f) "" else visionOs}"
                        tvGlassRight.text = "戴镜右\n${if (glassOd == -1f) "" else glassOd}"
                        alertGlassLeftEyeUi(glassOd)
                    } else if ("glass_os" == item) {
                        tvVisionRight.text = "裸眼右\n${if (visionOd == -1f) "" else visionOd}"
                        tvVisionLeft.text = "裸眼左\n${if (visionOs == -1f) "" else visionOs}"
                        tvGlassRight.text = "戴镜右\n${if (glassOd == -1f) "" else glassOd}"
                        tvGlassLeft.text = "戴镜左\n${if (glassOs == -1f) "" else glassOs}"
                        alertGlassResultUi(glassOs)
                    }
                    //重置示标
                    recoveryVisionColor()
                } else if ("end" == event) {
                    vision_od = stateObj.getFloatValue("vision_od")
                    vision_os = stateObj.getFloatValue("vision_os")
                    glass_od = stateObj.getFloatValue("glass_od")
                    glass_os = stateObj.getFloatValue("glass_os")
                    exitTest()
                }
            } else if ("result" == action) {
                vision_od = payloadObj.getFloatValue("vision_od")
                vision_os = payloadObj.getFloatValue("vision_os")
                glass_od = payloadObj.getFloatValue("glass_od")
                glass_os = payloadObj.getFloatValue("glass_os")
                exitTest()
            }
        } else {
            ToastUtils.showShort("无效指令")
        }
    }


    private fun setCurEye(item: String?) {
        if ("vision_od" == item) {
            tvVisionRight.setTextColor(resources.getColor(R.color.color_cb7))
            tvVisionLeft.setTextColor(resources.getColor(R.color.color_26))
            tvGlassRight.setTextColor(resources.getColor(R.color.color_26))
            tvGlassLeft.setTextColor(resources.getColor(R.color.color_26))
        } else if ("vision_os" == item) {
            tvVisionRight.setTextColor(resources.getColor(R.color.color_26))
            tvVisionLeft.setTextColor(resources.getColor(R.color.color_cb7))
            tvGlassRight.setTextColor(resources.getColor(R.color.color_26))
            tvGlassLeft.setTextColor(resources.getColor(R.color.color_26))
        } else if ("glass_od" == item) {
            tvVisionRight.setTextColor(resources.getColor(R.color.color_26))
            tvVisionLeft.setTextColor(resources.getColor(R.color.color_26))
            tvGlassRight.setTextColor(resources.getColor(R.color.color_cb7))
            tvGlassLeft.setTextColor(resources.getColor(R.color.color_26))
        } else if ("glass_os" == item) {
            tvVisionRight.setTextColor(resources.getColor(R.color.color_26))
            tvVisionLeft.setTextColor(resources.getColor(R.color.color_26))
            tvGlassRight.setTextColor(resources.getColor(R.color.color_26))
            tvGlassLeft.setTextColor(resources.getColor(R.color.color_cb7))
        }
    }

    private fun showRealTimeEyeValue(eyeValue: Float, direction: String?, correct: Int, incorrect: Int, eCount: Int) {
        if ("up" == direction) {
            tvEyeValue.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.icon_up, 0)
        } else if ("down" == direction) {
            tvEyeValue.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.icon_down, 0)
        } else if ("left" == direction) {
            tvEyeValue.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.icon_left, 0)
        } else {
            tvEyeValue.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.icon_right, 0)
        }
        tvEyeValue.text = ""+eyeValue
        val txt = "(<font color='#247CB7'>$correct</font> | <font color='#F44336'>$incorrect</font>)/$eCount"
        tvTxt.text = Html.fromHtml(txt)
    }


    private fun setVisionBg(line: Float) {
        val dataList = getEyeChartData()
        val eyeValue = transformAddScore(line)
        val visionValue = dataList[getPosition(eyeValue)]
        visionValue.value = "" + eyeValue
        visionValue.check = true
        visionValue.color = false
        mAdapter.setNewInstance(dataList)
    }

    private fun setVisionColor(line: Float, isOk: Boolean){
        val dataList = getEyeChartData()
        val eyeValue = transformAddScore(line)
        val visionValue = dataList[getPosition(eyeValue)]
        visionValue.value = "" + eyeValue
        visionValue.check = false
        visionValue.color = isOk
        mAdapter.setNewInstance(dataList)
    }

    private fun recoveryVisionColor() {
        mAdapter.setNewInstance(getEyeChartData())
    }


    private fun exitTest() {
        //ToastUtils.showShort("已退出视力测试");
        if(!lowVision) closeAlert() else closeAlertLow()
        val intent = Intent()
        intent.putExtra("vision_right", vision_od)
        intent.putExtra("vision_left", vision_os)
        intent.putExtra("vision_glass_right", glass_od)
        intent.putExtra("vision_glass_left", glass_os)
        intent.putExtra("glass_type", glassType)
        setResult(RESULT_OK, intent)
        finish()
    }


    private var mp: MediaPlayer? = null
    private fun playSound(resid: Int) {
        try {
            if (mp != null) {
                mp!!.release()
                mp = null
            }
            mp = MediaPlayer.create(this@VisionTestActivity, resid)
            mp!!.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        if (mp != null) {
            mp!!.stop()
            mp!!.release()
            mp = null
        }
        BleManager.getInstance().removeNotifyCallback(bleDevice, EyeChartOpUtil.uuid_notify)
        super.onDestroy()
    }

    private fun getEyeChartData(): MutableList<VisionValue> {
        val dataList = mutableListOf<VisionValue>()
        dataList.add(VisionValue("4.0", false, false))
        dataList.add(VisionValue("4.1", false, false))
        dataList.add(VisionValue("4.2", false, false))
        dataList.add(VisionValue("4.3", false, false))
        dataList.add(VisionValue("4.4", false, false))
        dataList.add(VisionValue("4.5", false, false))
        dataList.add(VisionValue("4.6", false, false))
        dataList.add(VisionValue("4.7", false, false))
        dataList.add(VisionValue("4.8", false, false))
        dataList.add(VisionValue("4.9", false, false))
        dataList.add(VisionValue("5.0", false, false))
        dataList.add(VisionValue("5.1", false, false))
        dataList.add(VisionValue("5.2", false, false))
        dataList.add(VisionValue("5.3", false, false))
        return dataList
    }

    private fun getPosition(eyeValue: Float): Int {
        return when(eyeValue) {
            4.0f -> 0
            4.1f -> 1
            4.2f -> 2
            4.3f -> 3
            4.4f -> 4
            4.5f -> 5
            4.6f -> 6
            4.7f -> 7
            4.8f -> 8
            4.9f -> 9
            5.0f -> 10
            5.1f -> 11
            5.2f -> 12
            5.3f -> 13
            else -> 0
        }
    }


    private var glassResultEyePd: PromptDialog? = null
    private fun alertGlassResultUi(score: Float) {
        val glassResultEyeView = LayoutInflater.from(this).inflate(R.layout.dialog_glass_result_layout, null)
        val params = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1320)
        glassResultEyeView.layoutParams = params
        glassResultEyePd = PromptDialog.Builder(this)
                .setView(glassResultEyeView)
                .show()
        glass_os = score
        var rev = "--"
        if (vision_od != -1f) rev = "" + vision_od
        var lev = "--"
        if (vision_os != -1f) lev = "" + vision_os
        (glassResultEyeView.findViewById<View>(R.id.tvRightEyeValue) as TextView).text = rev
        (glassResultEyeView.findViewById<View>(R.id.tvLeftEyeValue) as TextView).text = lev
        (glassResultEyeView.findViewById<View>(R.id.tvGlassRightEyeValue) as TextView).setText("" + glass_od)
        (glassResultEyeView.findViewById<View>(R.id.tvGlassLeftEyeValue) as TextView).setText("" + glass_os)
        glassResultEyeView.findViewById<View>(R.id.btnOk).setOnClickListener {
            glassResultEyePd?.dismiss()
            sendOk()
        }
        glassResultEyeView.findViewById<View>(R.id.btnRetestGlassLeft).setOnClickListener {
            glassResultEyePd?.dismiss()
            sendRetest()
        }
    }

    private var glassLeftEyePd: PromptDialog? = null
    private fun alertGlassLeftEyeUi(score: Float) {
        val glassLeftEyeView = LayoutInflater.from(this).inflate(R.layout.dialog_glass_left_eye_layout, null)
        val params = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1320)
        glassLeftEyeView.layoutParams = params
        glassLeftEyePd = PromptDialog.Builder(this)
                .setView(glassLeftEyeView)
                .show()
        glass_od = score
        var rev = "--"
        if (vision_od != -1f) rev = "" + vision_od
        var lev = "--"
        if (vision_os != -1f) lev = "" + vision_os
        (glassLeftEyeView.findViewById<View>(R.id.tvRightEyeValue) as TextView).text = rev
        (glassLeftEyeView.findViewById<View>(R.id.tvLeftEyeValue) as TextView).text = lev
        (glassLeftEyeView.findViewById<View>(R.id.tvGlassRightEyeValue) as TextView).setText("" + glass_od)
        glassLeftEyeView.findViewById<View>(R.id.btnOk).setOnClickListener {
            glassLeftEyePd?.dismiss()
            //currentEyePos = 4
            sendOk()
        }
        glassLeftEyeView.findViewById<View>(R.id.btnRetestGlassRight).setOnClickListener {
            glassLeftEyePd?.dismiss()
            sendRetest()
        }
    }

    private var glassRightEyePd: PromptDialog? = null
    private fun alertGlassRightEyeUi() {
        val glassRightEyeView = LayoutInflater.from(this).inflate(R.layout.dialog_glass_right_eye_layout, null)
        val params = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1320)
        glassRightEyeView.layoutParams = params
        glassRightEyePd = PromptDialog.Builder(this)
                .setView(glassRightEyeView)
                .show()
        var rev = "--"
        if (vision_od != -1f) rev = "" + vision_od
        var lev = "--"
        if (vision_os != -1f) lev = "" + vision_os
        (glassRightEyeView.findViewById<View>(R.id.tvRightEyeValue) as TextView).text = rev
        (glassRightEyeView.findViewById<View>(R.id.tvLeftEyeValue) as TextView).text = lev
        glassRightEyeView.findViewById<View>(R.id.btnOk).setOnClickListener {
            glassRightEyePd?.dismiss()
            //currentEyePos = 3
            sendOk()
        }
        glassRightEyeView.findViewById<View>(R.id.btnOk).isFocusableInTouchMode = true
    }

    private var resultEyePd: PromptDialog? = null
    private fun alertResultUi(score: Float) {
        val resultEyeView = LayoutInflater.from(this).inflate(R.layout.dialog_result_layout, null)
        val params = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1320)
        resultEyeView.layoutParams = params
        resultEyePd = PromptDialog.Builder(this)
                .setView(resultEyeView)
                .show()
        vision_os = score
        (resultEyeView.findViewById<View>(R.id.tvRightEyeValue) as TextView).setText("" + vision_od)
        (resultEyeView.findViewById<View>(R.id.tvLeftEyeValue) as TextView).setText("" + vision_os)
        resultEyeView.findViewById<View>(R.id.btnOk).setOnClickListener {
            resultEyePd?.dismiss()
            sendOk()
        }
        resultEyeView.findViewById<View>(R.id.btnRetestLeft).setOnClickListener {
            resultEyePd?.dismiss()
            sendRetest()
        }

        //裸眼模式
        if ("vision" == type) {
            resultEyeView.findViewById<View>(R.id.llGlassValue).visibility = View.GONE
            //resultEyeView.findViewById<View>(R.id.btnGlassCheck).visibility = View.GONE
        }
    }

    private var leftEyePd: PromptDialog? = null
    private fun alertLeftEyeUi(score: Float) {
        val leftEyeView = LayoutInflater.from(this).inflate(R.layout.dialog_left_eye_layout, null)
        val params = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1320)
        leftEyeView.layoutParams = params
        leftEyePd = PromptDialog.Builder(this)
                .setView(leftEyeView)
                .show()
        vision_od = score
        (leftEyeView.findViewById<View>(R.id.tvRightEyeValue) as TextView).setText("" + vision_od)
        leftEyeView.findViewById<View>(R.id.btnOk).setOnClickListener {
            leftEyePd?.dismiss()
            //currentEyePos = 2
            sendOk()
        }

        leftEyeView.findViewById<View>(R.id.btnRetestRight).setOnClickListener {
            leftEyePd?.dismiss()
            sendRetest()
        }

        //裸眼模式
        if ("vision" == type) {
            leftEyeView.findViewById<View>(R.id.tvGlassRight).visibility = View.GONE
            leftEyeView.findViewById<View>(R.id.tvGlassLeft).visibility = View.GONE
        }
    }

    private var rightEyePd: PromptDialog? = null
    private fun alertRightEyeUi() {
        val rightEyeView: View = LayoutInflater.from(this).inflate(R.layout.dialog_right_eye_layout, null)
        val params = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1320)
        rightEyeView.layoutParams = params
        rightEyePd = PromptDialog.Builder(this)
                .setView(rightEyeView)
                .setCanceledOnTouchOutside(false)
                .show()
        rightEyeView.findViewById<View>(R.id.btnOk).setOnClickListener {
            rightEyePd?.dismiss()
            //currentEyePos = 1
            sendOk()
        }
        rightEyeView.findViewById<View>(R.id.btnGlassCheck).setOnClickListener {
            rightEyePd?.dismiss()
            //alertGlassRightEyeUi()
        }

        //裸眼模式
        if ("vision" == type) {
            rightEyeView.findViewById<View>(R.id.tvGlassRight).visibility = View.GONE
            rightEyeView.findViewById<View>(R.id.tvGlassLeft).visibility = View.GONE
        }
        rightEyeView.findViewById<View>(R.id.btnGlassCheck).visibility = View.GONE
        rightEyeView.findViewById<View>(R.id.btnOk).isFocusableInTouchMode = true
    }

    /**
     * 关闭所有弹窗
     */
    private fun closeAlert() {
        if (rightEyePd != null && rightEyePd!!.isShowing) rightEyePd!!.dismiss()
        if (leftEyePd != null && leftEyePd!!.isShowing) leftEyePd!!.dismiss()
        if (resultEyePd != null && resultEyePd!!.isShowing) resultEyePd!!.dismiss()
        if (glassRightEyePd != null && glassRightEyePd!!.isShowing) glassRightEyePd!!.dismiss()
        if (glassLeftEyePd != null && glassLeftEyePd!!.isShowing) glassLeftEyePd!!.dismiss()
        if (glassResultEyePd != null && glassResultEyePd!!.isShowing) glassResultEyePd!!.dismiss()
    }


    //低视力-------------------------

    private var resultEyeLowPd: PromptDialog? = null
    private fun alertResultLowUi(score: Float, testState: Int) {
        val resultEyeView = LayoutInflater.from(this).inflate(R.layout.dialog_lowvision_result_layout, null)
        val params = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1320)
        resultEyeView.layoutParams = params
        resultEyeLowPd = PromptDialog.Builder(this)
                .setView(resultEyeView)
                .show()
        vision_os = score
        if (currentStationDistance == 4.0f) {
            vision_os = BigDecimal("" + vision_os).subtract(BigDecimal("0.1")).toFloat()
        } else if (currentStationDistance == 3.0f) {
            vision_os = BigDecimal("" + vision_os).subtract(BigDecimal("0.2")).toFloat()
        } else if (currentStationDistance == 2.5f) {
            vision_os = BigDecimal("" + vision_os).subtract(BigDecimal("0.3")).toFloat()
        } else if (currentStationDistance == 2.0f) {
            vision_os = BigDecimal("" + vision_os).subtract(BigDecimal("0.4")).toFloat()
        } else if (currentStationDistance == 1.5f) {
            vision_os = BigDecimal("" + vision_os).subtract(BigDecimal("0.5")).toFloat()
        } else if (currentStationDistance == 1.2f) {
            vision_os = BigDecimal("" + vision_os).subtract(BigDecimal("0.6")).toFloat()
        } else if (currentStationDistance == 1.0f) {
            vision_os = BigDecimal("" + vision_os).subtract(BigDecimal("0.7")).toFloat()
        } else {
            vision_os = BigDecimal("" + vision_os).toFloat()
        }
        (resultEyeView.findViewById<View>(R.id.tvRightEyeValue) as TextView).setText("" + vision_od)
        if (vision_os == 3.3f && testState == 2) {
            vision_os = 0f
        }
        (resultEyeView.findViewById<View>(R.id.tvLeftEyeValue) as TextView).setText("" + vision_os)
        resultEyeView.findViewById<View>(R.id.btnOk).setOnClickListener {
            resultEyeLowPd?.dismiss()
            sendOk()
        }
        resultEyeView.findViewById<View>(R.id.btnRetestLeftLow).setOnClickListener {
            resultEyeLowPd?.dismiss()
            sendRetest()
        }
    }

    private var leftEyeTipsPd: PromptDialog? = null
    private fun alertLeftEyeTipsStationUi(stationDistance: Float) {
        val leftEyeView = LayoutInflater.from(this).inflate(R.layout.dialog_lowvision_left_eye_tips_layout, null)
        val params = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1320)
        leftEyeView.layoutParams = params
        leftEyeTipsPd = PromptDialog.Builder(this)
                .setView(leftEyeView)
                .show()
        (leftEyeView.findViewById<View>(R.id.tvRightEyeValue) as TextView).setText("" + vision_od)
        leftEyeView.findViewById<View>(R.id.btnOk).setOnClickListener {
            leftEyeTipsPd?.dismiss()
            currentStationDistance = stationDistance
            sendOk()
        }
        (leftEyeView.findViewById<View>(R.id.tvStationDistance) as TextView).text = ""+stationDistance
    }

    private var leftEyeLowPd: PromptDialog? = null
    private fun alertLeftEyeLowUi(score: Float, testState: Int) {
        val leftEyeView = LayoutInflater.from(this).inflate(R.layout.dialog_lowvision_left_eye_layout, null)
        val params = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1320)
        leftEyeView.layoutParams = params
        leftEyeLowPd = PromptDialog.Builder(this)
                .setView(leftEyeView)
                .show()
        vision_od = score
        LogUtils.e("-------"+score)
        if (currentStationDistance == 4.0f) {
            vision_od = BigDecimal("" + vision_od).subtract(BigDecimal("0.1")).toFloat()
        } else if (currentStationDistance == 3.0f) {
            vision_od = BigDecimal("" + vision_od).subtract(BigDecimal("0.2")).toFloat()
        } else if (currentStationDistance == 2.5f) {
            vision_od = BigDecimal("" + vision_od).subtract(BigDecimal("0.3")).toFloat()
        } else if (currentStationDistance == 2.0f) {
            vision_od = BigDecimal("" + vision_od).subtract(BigDecimal("0.4")).toFloat()
        } else if (currentStationDistance == 1.5f) {
            vision_od = BigDecimal("" + vision_od).subtract(BigDecimal("0.5")).toFloat()
        } else if (currentStationDistance == 1.2f) {
            vision_od = BigDecimal("" + vision_od).subtract(BigDecimal("0.6")).toFloat()
        } else if (currentStationDistance == 1.0f) {
            vision_od = BigDecimal("" + vision_od).subtract(BigDecimal("0.7")).toFloat()
        } else {
            vision_od = BigDecimal("" + vision_od).toFloat()
        }
        if (vision_od == 3.3f && testState == 2) {
            vision_od = 0f
        }
        (leftEyeView.findViewById<View>(R.id.tvRightEyeValue) as TextView).setText("" + vision_od)
        leftEyeView.findViewById<View>(R.id.btnOk).setOnClickListener {
            leftEyeLowPd?.dismiss()
            //currentEyePos = 2
            currentStationDistance = 5f
            sendOk()
        }
        leftEyeView.findViewById<View>(R.id.btnRetestRightLow).setOnClickListener {
            leftEyeLowPd?.dismiss()
            sendRetest()
        }
    }

    private var rightEyeTipsPd: PromptDialog? = null
    private fun alertRightEyeTipsStationUi(stationDistance: Float) {
        val rightEyeView = LayoutInflater.from(this).inflate(R.layout.dialog_lowvision_right_eye_tips_layout, null)
        val params = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1320)
        rightEyeView.layoutParams = params
        rightEyeTipsPd = PromptDialog.Builder(this)
                .setView(rightEyeView)
                .show()
        rightEyeView.findViewById<View>(R.id.btnOk).setOnClickListener {
            rightEyeTipsPd?.dismiss()
            currentStationDistance = stationDistance
            sendOk()
        }
        (rightEyeView.findViewById<View>(R.id.tvStationDistance) as TextView).text = ""+stationDistance
    }


    private var rightEyeLowPd: PromptDialog? = null
    private fun alertRightEyeLowUi() {
        val rightEyeView = LayoutInflater.from(this).inflate(R.layout.dialog_lowvision_right_eye_layout, null)
        val params = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1320)
        rightEyeView.layoutParams = params
        rightEyeLowPd = PromptDialog.Builder(this)
                .setView(rightEyeView)
                .show()
        rightEyeView.findViewById<View>(R.id.btnOk).setOnClickListener {
            rightEyeLowPd?.dismiss()
            //currentEyePos = 1
            sendOk()
        }
        (rightEyeView.findViewById<View>(R.id.tvStationDistance) as TextView).setText(startStationDistance.toString() + "")
    }


    private fun closeAlertLow() {
        if (rightEyeLowPd != null && rightEyeLowPd!!.isShowing) rightEyeLowPd!!.dismiss()
        if (rightEyeTipsPd != null && rightEyeTipsPd!!.isShowing) rightEyeTipsPd!!.dismiss()
        if (leftEyeLowPd != null && leftEyeLowPd!!.isShowing) leftEyeLowPd!!.dismiss()
        if (leftEyeTipsPd != null && leftEyeTipsPd!!.isShowing) leftEyeTipsPd!!.dismiss()
        if (resultEyeLowPd != null && resultEyeLowPd!!.isShowing) resultEyeLowPd!!.dismiss()
    }

    private fun handleEyeLow(lineT: Float, inCorrect: Int, item: String, linePass: Int) {
        val line = transformAddScore(lineT)
        if ("vision_od" == item) {
            if (line == 4.0f && inCorrect == 2 && currentStationDistance == 4.0f) {
                alertRightEyeTipsStationUi(3.0f)
            } else if (line == 4.0f && inCorrect == 2 && currentStationDistance == 3.0f) {
                alertRightEyeTipsStationUi(2.5f)
            } else if (line == 4.0f && inCorrect == 2 && currentStationDistance == 2.5f) {
                alertRightEyeTipsStationUi(2.0f)
            } else if (line == 4.0f && inCorrect == 2 && currentStationDistance == 2.0f) {
                alertRightEyeTipsStationUi(1.5f)
            } else if (line == 4.0f && inCorrect == 2 && currentStationDistance == 1.5f) {
                alertRightEyeTipsStationUi(1.2f)
            } else if (line == 4.0f && inCorrect == 2 && currentStationDistance == 1.2f) {
                alertRightEyeTipsStationUi(1.0f)
            } else if (line == 4.0f && inCorrect == 2 && currentStationDistance == 1.0f) {
                alertLeftEyeLowUi(line, linePass)
            } else {
                alertLeftEyeLowUi(line, linePass)
            }
        }
        if ("vision_os" == item) {
            if (line == 4.0f && inCorrect == 2 && currentStationDistance == 5f) {
                alertLeftEyeTipsStationUi(startStationDistance)
            } else if (line == 4.0f && inCorrect == 2 && currentStationDistance == 4.0f) {
                alertLeftEyeTipsStationUi(3.0f)
            } else if (line == 4.0f && inCorrect == 2 && currentStationDistance == 3.0f) {
                alertLeftEyeTipsStationUi(2.5f)
            } else if (line == 4.0f && inCorrect == 2 && currentStationDistance == 2.5f) {
                alertLeftEyeTipsStationUi(2.0f)
            } else if (line == 4.0f && inCorrect == 2 && currentStationDistance == 2.0f) {
                alertLeftEyeTipsStationUi(1.5f)
            } else if (line == 4.0f && inCorrect == 2 && currentStationDistance == 1.5f) {
                alertLeftEyeTipsStationUi(1.2f)
            } else if (line == 4.0f && inCorrect == 2 && currentStationDistance == 1.2f) {
                alertLeftEyeTipsStationUi(1.0f)
            } else if (line == 4.0f && inCorrect == 2 && currentStationDistance == 1.0f) {
                alertResultLowUi(line, linePass)
                if ("all" == type) {
                    resultEyeLowPd?.dismiss()
                    lowVision = false
                    alertGlassRightEyeUi()
                }
            } else {
                alertResultLowUi(line, linePass)
                if ("all" == type) {
                    resultEyeLowPd?.dismiss()
                    lowVision = false
                    alertGlassRightEyeUi()
                }
            }
        }
    }

    private fun transformAddScore(scoreTemp: Float): Float {
        if (!lowVision) return scoreTemp
        val vTemp: Float
        if (currentStationDistance == 4.0f) {
            vTemp = BigDecimal("" + scoreTemp).add(BigDecimal("0.1")).toFloat()
        } else if (currentStationDistance == 3.0f) {
            vTemp = BigDecimal("" + scoreTemp).add(BigDecimal("0.2")).toFloat()
        } else if (currentStationDistance == 2.5f) {
            vTemp = BigDecimal("" + scoreTemp).add(BigDecimal("0.3")).toFloat()
        } else if (currentStationDistance == 2.0f) {
            vTemp = BigDecimal("" + scoreTemp).add(BigDecimal("0.4")).toFloat()
        } else if (currentStationDistance == 1.5f) {
            vTemp = BigDecimal("" + scoreTemp).add(BigDecimal("0.5")).toFloat()
        } else if (currentStationDistance == 1.2f) {
            vTemp = BigDecimal("" + scoreTemp).add(BigDecimal("0.6")).toFloat()
        } else if (currentStationDistance == 1.0f) {
            vTemp = BigDecimal("" + scoreTemp).add(BigDecimal("0.7")).toFloat()
        } else {
            vTemp = BigDecimal("" + scoreTemp).toFloat()
        }
        //LogUtils.e("vvvvvvvv-------"+vTemp);
        return vTemp
    }

    private fun transformSubScore(scoreTemp: Float): Float {
        if (!lowVision) return scoreTemp
        val vTemp: Float
        if (currentStationDistance == 4.0f) {
            vTemp = BigDecimal("" + scoreTemp).subtract(BigDecimal("0.1")).toFloat()
        } else if (currentStationDistance == 3.0f) {
            vTemp = BigDecimal("" + scoreTemp).subtract(BigDecimal("0.2")).toFloat()
        } else if (currentStationDistance == 2.5f) {
            vTemp = BigDecimal("" + scoreTemp).subtract(BigDecimal("0.3")).toFloat()
        } else if (currentStationDistance == 2.0f) {
            vTemp = BigDecimal("" + scoreTemp).subtract(BigDecimal("0.4")).toFloat()
        } else if (currentStationDistance == 1.5f) {
            vTemp = BigDecimal("" + scoreTemp).subtract(BigDecimal("0.5")).toFloat()
        } else if (currentStationDistance == 1.2f) {
            vTemp = BigDecimal("" + scoreTemp).subtract(BigDecimal("0.6")).toFloat()
        } else if (currentStationDistance == 1.0f) {
            vTemp = BigDecimal("" + scoreTemp).subtract(BigDecimal("0.7")).toFloat()
        } else {
            vTemp = BigDecimal("" + scoreTemp).toFloat()
        }
        //LogUtils.e("vvvvvvvv-------"+vTemp);
        return vTemp
    }

    //开始站的距离
    private var startStationDistance = 4f //4.0 2.5


    //当前站的距离
    private var currentStationDistance = 4.0f //4.0 3.0 2.5 2.0 1.5 1.2 1.0


}