package com.qpsoft.cdc.utils

import com.alibaba.fastjson.JSON
import com.blankj.utilcode.util.EncodeUtils
import com.blankj.utilcode.util.LogUtils
import com.qpsoft.cdc.constant.Brand
import com.qpsoft.cdc.constant.Model
import com.qpsoft.cdc.constant.Type
import com.qpsoft.cdc.model.Device
import com.qpsoft.cdc.model.DeviceCreate
import com.qpsoft.cdc.thirddevice.bloodpressure.yuwell.YE900DataParser
import com.qpsoft.cdc.thirddevice.diopter.charops.CRK8800DataParser
import com.qpsoft.cdc.thirddevice.diopter.duomei.RC800DataParser
import com.qpsoft.cdc.thirddevice.diopter.duomei.RT6000DataParser
import com.qpsoft.cdc.thirddevice.diopter.faliao.Fr710DataParser
import com.qpsoft.cdc.thirddevice.diopter.faliao.Fr8900DataParser
import com.qpsoft.cdc.thirddevice.diopter.hand.suoer.SW800DataProcess
import com.qpsoft.cdc.thirddevice.diopter.hand.welch.VS100DataProcess
import com.qpsoft.cdc.thirddevice.diopter.nidek.NidekDataParser
import com.qpsoft.cdc.thirddevice.diopter.tianle.KR9800DataParser
import com.qpsoft.cdc.thirddevice.diopter.tianle.Rm9000DataParser
import com.qpsoft.cdc.thirddevice.diopter.topcon.TopConDataParser
import com.qpsoft.cdc.thirddevice.diopter.unicos.URK700DataParser
import com.qpsoft.cdc.thirddevice.diopter.xinyuan.Fa6500DataParser
import com.qpsoft.cdc.thirddevice.diopter.xinyuan.Fa6500KDataParser
import com.qpsoft.cdc.thirddevice.diopter.xiongbo.RMK800DataParser
import com.qpsoft.cdc.thirddevice.glucose.yuwell.B305DataParser
import com.qpsoft.cdc.thirddevice.heightweight.lejia.LJ700DataParser
import com.qpsoft.cdc.thirddevice.heightweight.shanghe.SH01DataParser
import com.qpsoft.cdc.thirddevice.pyrometer.faliao.FL800DataParser
import com.qpsoft.cdc.thirddevice.tonometer.nidek.NT510DataParser
import com.qpsoft.cdc.thirddevice.vitalcapacity.breathhome.B1DataParser
import com.qpsoft.cdc.thirddevice.idcard.Cvr100bDataParser

object DeviceParseUtil {
    fun parse(create: DeviceCreate): Triple<Boolean, String, Any?> {
        val type = create.type
        val brand = create.brand
        val model = create.model
        val oriData = create.oriData
        val oriDataByteArray = EncodeUtils.base64Decode(oriData)
        var result: Any? = null
        when(type) {
            Type.OPTOMETRY.value -> {
                when (brand) {
                    Brand.TIANLE.value -> {
                        when (model) {
                            Model.RM9000.value -> result = Rm9000DataParser().parse(oriDataByteArray)
                            Model.KR9800.value -> result = KR9800DataParser().parse(oriDataByteArray)
                        }
                    }
                    Brand.FALIAO.value -> {
                        when (model) {
                            Model.FR8900.value -> result = Fr8900DataParser().parse(oriDataByteArray)
                            Model.FR710.value -> result = Fr710DataParser().parse(oriDataByteArray)
                        }
                    }
                    Brand.NIDEK.value -> {
                        when (model) {
                            Model.ARK1.value -> result = NidekDataParser().parse(oriDataByteArray)
                        }
                    }
                    Brand.TOPCON.value -> {
                        when (model) {
                            Model.RM8900.value,Model.RM800.value,Model.KR800.value -> result = TopConDataParser().parse(oriDataByteArray)
                        }
                    }
                    Brand.XINYUAN.value -> {
                        when (model) {
                            Model.FA6500.value -> result = Fa6500DataParser().parse(oriDataByteArray)
                            Model.FA6500K.value -> result = Fa6500KDataParser().parse(oriDataByteArray)
                        }
                    }
                    Brand.DUOMEI.value -> {
                        when (model) {
                            Model.RC800.value -> result = RC800DataParser().parse(oriDataByteArray)
                            Model.RT6000.value -> result = RT6000DataParser().parse(oriDataByteArray)
                        }
                    }
                    Brand.XIONGBO.value -> {
                        when (model) {
                            Model.RMK150.value -> result = KR9800DataParser().parse(oriDataByteArray)
                            Model.RMK800.value -> result = RMK800DataParser().parse(oriDataByteArray)
                        }
                    }
                    Brand.CHAROPS.value -> {
                        when (model) {
                            Model.CRK8800.value -> result = CRK8800DataParser().parse(oriDataByteArray)
                        }
                    }
                    Brand.UNICOS.value -> {
                        when (model) {
                            Model.URK700.value -> result = URK700DataParser().parse(oriDataByteArray)
                        }
                    }
                    Brand.SUOER.value -> {
                        when (model) {
                            Model.SW800.value -> result = SW800DataProcess().parse(oriDataByteArray)
                        }
                    }
                    Brand.WELCH.value -> {
                        when (model) {
                            Model.VS100.value -> result = VS100DataProcess().parse(oriDataByteArray)
                        }
                    }
                }
            }
            Type.PYROMETER.value -> {
                when (brand) {
                    Brand.FALIAO.value -> {
                        when (model) {
                            Model.FL800.value -> result = FL800DataParser().parse(oriDataByteArray)
                        }
                    }
                }
            }
            Type.HEIGHTWEIGHT.value -> {
                when (brand) {
                    Brand.SHANGHE.value -> {
                        when (model) {
                            Model.SH01.value -> result = SH01DataParser().parse(oriDataByteArray)
                        }
                    }
                    Brand.LEJIA.value -> {
                        when (model) {
                            Model.LJ700.value -> result = LJ700DataParser().parse(oriDataByteArray)
                        }
                    }
                }
            }
            Type.BLOODPRESSURE.value -> {
                when (brand) {
                    Brand.YUWELL.value -> {
                        when (model) {
                            Model.YE900.value -> result = YE900DataParser().parse(oriDataByteArray)
                        }
                    }
                }
            }
            Type.BLOODSUGAR.value -> {
                when (brand) {
                    Brand.YUWELL.value -> {
                        when (model) {
                            Model.B305.value -> result = B305DataParser().parse(oriDataByteArray)
                        }
                    }
                }
            }
            Type.VITALCAPACITY.value -> {
                when (brand) {
                    Brand.BREATHHOME.value -> {
                        when (model) {
                            Model.B1.value -> result = B1DataParser().parse(oriDataByteArray)
                        }
                    }
                }
            }
            Type.IDCARD.value -> {
                when (brand) {
                    Brand.CHINAVISION.value -> {
                        when (model) {
                            Model.CVR100B.value -> result = Cvr100bDataParser().parse(oriDataByteArray)
                        }
                    }
                }
            }
            Type.TONOMETER.value -> {
                when (brand) {
                    Brand.NIDEK.value -> {
                        when (model) {
                            Model.NT510.value -> result = NT510DataParser().parse(oriDataByteArray)
                        }
                    }
                }
            }
        }

        val deviceModel = Device()
        deviceModel.appId = 0
        deviceModel.project = "erp"
        deviceModel.type = type
        deviceModel.brand = brand
        deviceModel.model = model
        deviceModel.oriData = oriData
        return if (result != null) {
            deviceModel.parData = JSON.toJSONString(result)
            deviceModel.status = 100
            LogUtils.e(deviceModel.toString())
            Triple(true, "", result)
        } else {
            LogUtils.e(deviceModel.toString())
            Triple(false, "解析失败", null)
        }
    }
}