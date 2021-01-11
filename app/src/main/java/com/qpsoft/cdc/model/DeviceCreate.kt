package com.qpsoft.cdc.model

class DeviceCreate {
        var appId: Long = 0

        var project: String = ""

        var type: String = ""

        var brand: String = ""

        var model: String = ""

        var oriData: String = ""

        var ext: MutableMap<String, String> = mutableMapOf()
}