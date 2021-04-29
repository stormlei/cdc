package com.qpsoft.cdc.ui.entity

import android.os.Parcelable
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import kotlinx.android.parcel.Parcelize
import me.yokeyword.indexablerv.IndexableEntity

@Parcelize
open class School(
        @PrimaryKey var id: String,
        var name: String,
        var py: String,
        var category: String
) : Parcelable, IndexableEntity, RealmObject() {

        constructor() : this("", "", "", "")

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