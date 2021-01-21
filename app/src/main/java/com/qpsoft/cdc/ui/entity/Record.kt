package com.qpsoft.cdc.ui.entity

data class Record(val data: Data) {
        data class Data(
                val vision: Vision?,
                val diopter: Diopter?) {
                data class Vision(
                        val nakedDegree: Degree?,
                        val glassType: String,
                        val glassDegree: Degree?,
                        val spectacles: Degree?,
                        val eyeAbnormal: Boolean,
                ) {
                        data class Degree(
                                val od: String,
                                val os: String
                        )

                }
                data class Diopter(
                        val sph: Degree?,
                        val cyl: Degree?,
                        val axle: Degree?,
                        val optometryFile: String?,
                        val eyeAbnormal: Boolean,
                ) {
                        data class Degree(
                                val od: String,
                                val os: String
                        )

                }
        }
}