package com.qpsoft.cdc.ui.entity

data class DataItem(
    val vision: Vision?,
    val diopter: Diopter?,
    val medicalHistory: DataList?,
    val caries: Caries?,
    val height: Data?,
    val weight: Data?,
    val bloodPressure: BloodPressure?,
    val spine: Spine?,
    val sexuality: Sexuality?,
    val trachoma: Data?,
    val conjunctivitis: Data?,
    val redGreenBlind: Data?,
    val eyeAxis: EyeData?,
    val eyePressure: EyeData?,
    val cornealCurvature: CornealCurvature?,
    val cornealRadius: EyeData?,
    val cj: Cj?,
    val pulse: Data?,
    val vitalCapacity: Data?,
    val bust: Data?,
    val waistline: Data?,
    val hips: Data?,
    val sittingHeight: Data?,
    val grip: Data?,
    val nutrition: Data?,
    val ear: DataList?,
    val nose: DataList?,
    val tonsil: Data?,
    val periodontium: DataList?,
    val hearing: Hearing?,
    val heart: DataList?,
    val lung: Data?,
    val liver: Data?,
    val spleen: Data?,
    val head: Data?,
    val neck: Data?,
    val chest: Data?,
    val limb: Limb?,
    val skin: DataList?,
    val lymphaden: Data?,
    val bcgScar: Data?,
    val hemoglobin: Data?,
    val bloodType: Data?,
    val worm: Data?,
    val pdd: Data?,
    val liverFunction: LiverFunction?,
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
    data class DataList(
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
    data class Data(
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
            val startAge: String?,
            val frequency: String?,
            val duration: String?
        )
        data class NocturnalEmission(
            val whether: Int?,
            val startAge: String?,
        )

    }

    data class EyeData(
            val od: String,
            val os: String
    )

    data class CornealCurvature(
            val ks: EyeData?,
            val kf: EyeData?,
    )

    data class Cj(
            val refractiveError: EyeData?,
            val cjData: EyeData?,
    )

    data class Hearing(
            val leftAbnormal: String?,
            val rightAbnormal: String?,
    )
    data class Limb(
            val lt: MutableList<String>?,
            val rt: MutableList<String>?,
            val lb: MutableList<String>?,
            val rb: MutableList<String>?,
    )
    data class LiverFunction(
            val alt: String?,
            val bc: String?,
    )
}