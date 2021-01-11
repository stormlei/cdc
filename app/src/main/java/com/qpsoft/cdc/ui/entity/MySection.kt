package com.qpsoft.cdc.ui.entity

import com.chad.library.adapter.base.entity.SectionEntity

class MySection constructor(isHeader: Boolean, any: Any?) : SectionEntity {
    override var isHeader: Boolean = isHeader
    var any: Any? = any
}