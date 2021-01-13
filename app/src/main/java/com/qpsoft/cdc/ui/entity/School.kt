package com.qpsoft.cdc.ui.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class School(
        val id: String,
        val name: String,
        val category: String
        ): Parcelable