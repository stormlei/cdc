package com.qpsoft.cdc.ui.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import me.yokeyword.indexablerv.IndexableEntity

@Parcelize
data class School(
        val id: String,
        var name: String,
        var py:String,
        val category: String
        ): Parcelable, IndexableEntity {
        override fun getFieldIndexBy(): String {
                return name
        }

        override fun setFieldIndexBy(indexField: String) {
                this.name = indexField
        }

        override fun setFieldPinyinIndexBy(pinyin: String) {
                this.py = pinyin
        }
}