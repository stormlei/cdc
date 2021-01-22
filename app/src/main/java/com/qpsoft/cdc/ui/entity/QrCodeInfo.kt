package com.qpsoft.cdc.ui.entity

import java.io.Serializable

data class QrCodeInfo(
        val bluetooth_name: String,
        val bluetooth_mac: String?,
        val type: String,
        val brand: String,
        val model: String,
        val no: String,
        val name: String
        ): Serializable