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
        @Ignore var studentId: String?,
        var name: String,
        var py: String,
        var gender: String?,
        var school: School?,
        var grade: String,
        var clazz: String,
        var schoolCategory: String,
        var upload: Int = 0,
        var retest: Int = 0
) : IndexableEntity, Parcelable, RealmObject() {

        var localRecord: String? = null

        var retestTitle: String? = null
        var localRetest: String? = null

        @Ignore var record: Record? = null
        @Ignore var data: DataItem? = null

        constructor() : this("","","","","",null,"","","", 0, 0)

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