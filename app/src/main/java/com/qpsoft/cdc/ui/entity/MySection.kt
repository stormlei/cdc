package com.qpsoft.cdc.ui.entity

import com.chad.library.adapter.base.entity.SectionEntity

class MySection constructor(isHeader: Boolean, any: Any?, header: String?) : SectionEntity {
    override var isHeader: Boolean = isHeader
    var any: Any? = any
    var header: String? = header
}