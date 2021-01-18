package com.qpsoft.cdc.ui.entity

import me.yokeyword.indexablerv.IndexableEntity

data class Student(
        val id: String,
        var name: String,
        var py:String,
        val mobile: String,
        val idcard: String,
        val gender: String,
        val birthday: String,
        val nation: String,
        val birthPlace: String,
        val schoolId: String,
        val grade: String,
        val clazz: String,
        val schoolCategory: String
        ): IndexableEntity {
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