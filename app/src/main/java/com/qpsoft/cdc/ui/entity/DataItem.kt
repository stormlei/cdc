package com.qpsoft.cdc.ui.entity

data class DataItem(
    val vision: Vision?,
    val diopter: Diopter?,
    val medicalHistory: MedicalHistory?,
    val caries: Caries?,
    val height: HW?,
    val weight: HW?,
    val bloodPressure: BloodPressure?,
    val spine: Spine?,
    val sexuality: Sexuality?
) {
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
    data class MedicalHistory(
        val data: MutableList<String>?,

        )
    data class Caries(
        val babyTooth: Tooth?,
        val adultTooth: Tooth?,
    ) {
        data class Tooth(
            val caries: ToothData?,
            val missing: ToothData?,
            val fill: ToothData?
        ) {
            data class ToothData(
                val count: Int,
                val list: MutableList<Int>,
            )
        }

    }
    data class HW(
        val data: String?,
    )
    data class BloodPressure(
        val sbp: String?,
        val dbp: String?,

        )
    data class Spine(
        val sideBend: SideBend?,
        val baBend: SideBend.ScoliosisData?,
    ) {
        data class SideBend(
            val chest: ScoliosisData?,
            val waistChest: ScoliosisData?,
            val waist: ScoliosisData?
        ) {
            data class ScoliosisData(
                val category: String,
                val degree: String,
            )
        }

    }
    data class Sexuality(
        val menstruation: Menstruation?,
        val nocturnalEmission: NocturnalEmission?,
    ) {
        data class Menstruation(
            val whether: Int?,
            val startAge: Int?,
            val frequency: Int?,
            val duration: Int?
        )
        data class NocturnalEmission(
            val whether: Int?,
            val startAge: Int?,
        )

    }
}