package com.qpsoft.cdc.ui.entity

import android.os.Parcelable
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import kotlinx.android.parcel.Parcelize
import me.yokeyword.indexablerv.IndexableEntity

@Parcelize
open class Student(
        @PrimaryKey var id: String,
        var studentId: String?,
        var name: String,
        var py: String,
        var gender: String?,
        var schoolId: String,
        var school: School?,
        var grade: String,
        var clazz: String,
        var schoolCategory: String
) : IndexableEntity, Parcelable, RealmObject() {

        @Ignore var record: Record? = null
        @Ignore var data: DataItem? = null

        constructor() : this("","","","","","",null,"","","")

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