package com.qpsoft.cdc.constant

enum class Type(var value: String) {
    //验光仪
    OPTOMETRY("optometry"),
    //焦度计
    PYROMETER("pyrometer"),
    //眼压计
    TONOMETER("tonometer"),
    //身高体重称
    HEIGHTWEIGHT("heightWeight"),
    //血压
    BLOODPRESSURE("bloodpressure"),
    //血糖
    BLOODSUGAR("bloodsugar"),
    //肺活量
    VITALCAPACITY("vitalcapacity"),
    //身份证
    IDCARD("idcard")
}