package com.qpsoft.cdc.ui.entity

import com.qpsoft.cdc.constant.Level
import com.qpsoft.cdc.constant.PlanType

data class CurrentPlan(
        val name: String,
        val level: Level,
        val planType: PlanType,
        val itemList: MutableList<CheckItem>)