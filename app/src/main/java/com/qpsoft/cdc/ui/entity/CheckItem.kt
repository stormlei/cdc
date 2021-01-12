package com.qpsoft.cdc.ui.entity


data class CheckItem(
        val key: String,
        val name: String,
        val group: String,
        val optional: Boolean,
        val schoolCategories: MutableList<String>,
        var check: Boolean  //是否选中
        )