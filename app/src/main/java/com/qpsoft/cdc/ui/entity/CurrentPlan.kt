package com.qpsoft.cdc.ui.entity

data class CurrentPlan(
        val name: String,
        val level: String,
        val planType: String,
        val itemList: MutableList<CheckItem>)