package com.qpsoft.cdc.ui.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

data class Student(
        val id: String,
        val name: String,
        val py:String,
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
        )