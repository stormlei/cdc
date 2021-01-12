package com.qpsoft.cdc.utils

object LevelConvert {
    fun toCh(value: String?): String {
        return when(value) {
            "Province" -> "省"
            "City" -> "市"
            "County" -> "县"
            "Station" -> "监测点"
            "School" -> "学校"
            else -> ""
        }
    }
}