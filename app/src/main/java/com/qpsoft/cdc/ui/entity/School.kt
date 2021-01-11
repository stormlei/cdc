package com.qpsoft.cdc.ui.entity

import com.qpsoft.cdc.constant.SchoolCategory

data class School(
        val id: String,
        val name: String,
        val category: SchoolCategory
        )