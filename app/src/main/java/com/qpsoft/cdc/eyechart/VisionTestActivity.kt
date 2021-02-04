package com.qpsoft.cdc.eyechart

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.view.View
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
import kotlinx.android.synthetic.main.activity_vision_test.*


class VisionTestActivity : BaseActivity() {

    private var glassType: String? = null
    private var schoolCategory: String? = null
    private lateinit var mAdapter: BaseQuickAdapter<VisionValue, BaseViewHolder>


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

        var startType = ""
        var startItem = ""
        when(glassType) {
            "No" -> {
                llVisionRight.visibility = View.VISIBLE
                llVisionLeft.visibility = View.VISIBLE
                startType = "vision"
                startItem = "vision_od"
            }
            "Frame", "ContactLens" -> {
                llVisionRight.visibility = View.VISIBLE
                llVisionLeft.visibility = View.VISIBLE
                llGlassRight.visibility = View.VISIBLE
                llGlassLeft.visibility = View.VISIBLE
                startType = "all"
                startItem = "vision_od"
            }
            "OkGlass" -> {
                llGlassRight.visibility = View.VISIBLE
                llGlassLeft.visibility = View.VISIBLE
                startType = "glass"
                startItem = "glass_od"
            }
        }
        //开始测试
        sendStart(startType, startItem)
        //查询状态
        Handler().postDelayed({queryStatus()}, 1000)


        rvEyeChart.layoutManager = GridLayoutManager(this, 5)
        mAdapter = object: BaseQuickAdapter<VisionValue, BaseViewHolder>(R.layout.item_eyechart, getEyeChartData()) {
            override fun convert(holder: BaseViewHolder, item: VisionValue) {
                holder.setText(R.id.tvValue, item.value)
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

    private fun sendOk() {
        val jsonObj = JSONObject()
        jsonObj["action"] = "command"
        val payloadObj = JSONObject()
        payloadObj["cmd"] = "ok"
        jsonObj["payload"] = payloadObj
        EyeChartOpUtil.send(jsonObj.toJSONString())
    }


    private fun sendStart(type: String, item: String) {
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
                    val line = curTestObj.getFloatValue("line")
                    val direction = curTestObj.getString("direction")
                    val count = curTestObj.getIntValue("line_count")
                    val inCorrect = curTestObj.getIntValue("line_incorrect")
                    val correct = curTestObj.getIntValue("line_correct")
                    val curDirectionCorrect = curTestObj.getIntValue("cur_direction_correct")
                    val linePass = curTestObj.getIntValue("line_pass")
                    val type = curTestObj.getString("type")
                    val item = curTestObj.getString("item")
                    val currentVisionLevel = (line * 10f).toInt()
                    setVisionBg(currentVisionLevel)
                    showRealTimeEyeValue(currentVisionLevel, direction, correct, inCorrect, count)
                    val distance = payloadObj.getFloatValue("distance")
                    tvDistance.setText(distance.toString() + "米")
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
                val currentVisionLevel = (line * 10f).toInt()

                //当前测试项目
                setCurEye(item)

                //handle event
                if ("line" == event) {
                    setVisionBg(currentVisionLevel)
                    showRealTimeEyeValue(currentVisionLevel, direction, correct, inCorrect, count)
                } else if ("direction" == event) {
                    if (curDirectionCorrect == 1) {
                        playSound(R.raw.correct)
                    } else {
                        playSound(R.raw.error)
                    }
                    if (linePass == 1) {
                        //change green
                        setVisionColor(currentVisionLevel, true)
                    } else if (linePass == 2) {
                        //change red
                        setVisionColor(currentVisionLevel, false)
                    }
                } else if ("cur_result" == event) {
                    if ("vision_od" == item) {
                        tvVisionRight.text = "裸眼右\n$line"
                    } else if ("vision_os" == item) {
                        tvVisionLeft.text = "裸眼左\n$line"
                    } else if ("glass_od" == item) {
                        tvGlassRight.text = "戴镜右\n$line"
                    } else if ("glass_os" == item) {
                        tvGlassLeft.text = "戴镜左\n$line"
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
                    } else if ("vision_os" == item) {
                        tvVisionRight.text = "裸眼右\n${if (visionOd == -1f) "" else visionOd}"
                        tvVisionLeft.text = "裸眼左\n${if (visionOs == -1f) "" else visionOs}"
                    } else if ("glass_od" == item) {
                        tvVisionRight.text = "裸眼右\n${if (visionOd == -1f) "" else visionOd}"
                        tvVisionLeft.text = "裸眼左\n${if (visionOs == -1f) "" else visionOs}"
                        tvGlassRight.text = "戴镜右\n${if (glassOd == -1f) "" else glassOd}"
                    } else if ("glass_os" == item) {
                        tvVisionRight.text = "裸眼右\n${if (visionOd == -1f) "" else visionOd}"
                        tvVisionLeft.text = "裸眼左\n${if (visionOs == -1f) "" else visionOs}"
                        tvGlassRight.text = "戴镜右\n${if (glassOd == -1f) "" else glassOd}"
                        tvGlassLeft.text = "戴镜左\n${if (glassOs == -1f) "" else glassOs}"
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

    private fun showRealTimeEyeValue(eyeValue: Int, direction: String?, correct: Int, incorrect: Int, eCount: Int) {
        if ("up" == direction) {
            tvEyeValue.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.icon_up, 0)
        } else if ("down" == direction) {
            tvEyeValue.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.icon_down, 0)
        } else if ("left" == direction) {
            tvEyeValue.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.icon_left, 0)
        } else {
            tvEyeValue.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.icon_right, 0)
        }
        tvEyeValue.text = ""+eyeValue / 10f
        val txt = "(<font color='#247CB7'>$correct</font> | <font color='#F44336'>$incorrect</font>)/$eCount"
        tvTxt.text = Html.fromHtml(txt)
    }


    private fun setVisionBg(eyeValue: Int) {
        val dataList = getEyeChartData()
        val visionValue = dataList[getPosition(eyeValue)]
        visionValue.value = "" + (eyeValue / 10f)
        visionValue.check = true
        visionValue.color = false
        mAdapter.setNewInstance(dataList)
    }

    private fun setVisionColor(eyeValue: Int, isOk: Boolean){
        val dataList = getEyeChartData()
        val visionValue = dataList[getPosition(eyeValue)]
        visionValue.value = "" + (eyeValue / 10f)
        visionValue.check = false
        visionValue.color = isOk
        mAdapter.setNewInstance(dataList)
    }

    private fun recoveryVisionColor() {
        mAdapter.setNewInstance(getEyeChartData())
    }


    private fun exitTest() {
        //ToastUtils.showShort("已退出视力测试");
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

    private fun getPosition(eyeValue: Int): Int {
        return when(eyeValue) {
            40 -> 0
            41 -> 1
            42 -> 2
            43 -> 3
            44 -> 4
            45 -> 5
            46 -> 6
            47 -> 7
            48 -> 8
            49 -> 9
            50 -> 10
            51 -> 11
            52 -> 12
            53 -> 13
            else -> 0
        }
    }
}