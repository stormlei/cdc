package com.qpsoft.cdc.model


data class Device(
        var appId: Long = 0,
        var project: String = "", //eyetest erp
        var type: String = "",
        var brand: String = "",
        var model: String = "",
        var oriData: String = "",
        var parData: String = "",
        var status: Int = 0  //100 ok  0 error
)