package com.qpsoft.cdc.utils

object PlanTypeConvert {
    fun toCh(value: String?): String {
        return when(value) {
            "Vision" -> "视力筛查"
            "CommonDisease" -> "常见病筛查"
            "Checkup" -> "体检筛查"
            else -> ""
        }
    }
}