package com.qpsoft.cdc

object Api {
    const val URL = "https://govdemo.qpsc365.com"
    private const val HOST = "$URL/api"

    const val LOGIN = "$HOST/auth/v1/client"
    const val CURRENT_PLAN = "$HOST/client/v1/user/current-plan"
    const val SCHOOL = "$HOST/client/v1/school"
    const val GRADE_CLAZZ_LIST = "$HOST/client/v1/student/grade-clazz-list"
    const val STU_COMPLETE_STATUS = "$HOST/client/v1/student/complate-status"
    const val STUDENT = "$HOST/client/v1/student"
    const val CURRENT_USER = "$HOST/client/v1/user/current-user"
    const val RETEST_TITLE_LIST = "$HOST/client/v1/retest/title-list"
    const val RETEST_SUMMARY_VISION = "$HOST/client/v1/retest/summary-vision"
    const val RETEST_SUMMARY_HEIGHT_WEIGHT = "$HOST/client/v1/retest/summary-height-weight"
    const val RETEST_SUMMARY_ALL = "$HOST/client/v1/retest/summary-all"
    const val RETEST = "$HOST/client/v1/retest"
    const val RETEST_VIEW_BY_STUDENT_ID = "$HOST/client/v1/retest/view-by-student-id"
    const val RECORD_SUBMIT = "$HOST/client/v1/record/submit"
    const val RECORD_BATCH_SUBMIT = "$HOST/client/v1/record/batch-submit"
    const val RETEST_SUBMIT = "$HOST/client/v1/retest/submit"
    const val RETEST_BATCH_SUBMIT = "$HOST/client/v1/retest/batch-submit"
    const val OSS_UPLOAD = "$HOST/client/v1/oss/upload"
    const val OSS_DOWNLOAD = "$HOST/client/v1/oss/download"
}