package com.qpsoft.cdc.ui.entity

import com.qpsoft.cdc.constant.SchoolCategory

data class CheckItem(
        val key: String,
        val name: String,
        val group: String,
        val optional: Boolean,
        val schoolCategories: MutableList<SchoolCategory>,
        var check: Boolean  //是否选中
        )