package com.qpsoft.cdc.utils

object SchoolCategoryConvert {
    fun toCh(value: String?): String {
        return when(value) {
            "Kindergarten" -> "幼儿园"
            "PrimarySchool" -> "小学"
            "MiddleSchool" -> "初中"
            "HighSchool" -> "普高"
            "VocationalHighSchool" -> "职业高中"
            "University" -> "大学"
            else -> ""
        }
    }
}