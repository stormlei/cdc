package com.qpsoft.cdc.ui.entity

data class Page<T> (val items: T, val meta: Meta) {
    data class Meta(val page: Int, val size: Int, val pages: Int, val total: Int)
}
