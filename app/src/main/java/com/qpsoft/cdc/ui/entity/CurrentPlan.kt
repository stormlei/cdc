package com.qpsoft.cdc.ui.entity

data class CurrentPlan(
        val id: String,
        val name: String,
        val level: String,
        val planType: String,
        val stationId: String?,
        val itemList: MutableList<CheckItem>)