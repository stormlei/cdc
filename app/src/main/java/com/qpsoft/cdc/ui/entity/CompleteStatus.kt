package com.qpsoft.cdc.ui.entity

data class CompleteStatus(
        val total: String,
        val complateNum: String,
        val unComplateNum: String,
        val gradeComplateMap: MutableMap<String, Int>,
        val gradeTotalMap: MutableMap<String, Int>)