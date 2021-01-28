package com.qpsoft.cdc.ui.entity

data class RetestSummary(
        val recordCount: Int,
        val needRetestCount: Int,
        val retestCount: Int,
        val allRetestCount: Int,
        val noRetestCount: Int,
        val retestItemCount: Int,
        val retestItem: Int,
        val errorCount: Int,
        )