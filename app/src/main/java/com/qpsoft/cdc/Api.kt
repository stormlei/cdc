package com.qpsoft.cdc

object Api {
    private const val HOST = "https://govdemo.qpsc365.com/api"

    const val LOGIN = "$HOST/auth/v1/client"
    const val CURRENT_PLAN = "$HOST/client/v1/user/current-plan"
    const val SCHOOL = "$HOST/client/v1/school"
    const val GRADE_CLAZZ_LIST = "$HOST/client/v1/student/grade-clazz-list"
    const val STU_COMPLETE_STATUS = "$HOST/client/v1/student/complate-status"
    const val STUDENT = "$HOST/client/v1/student"
    const val CURRENT_USER = "$HOST/client/v1/user/current-user"
    const val RETEST_TITLE_LIST = "$HOST/client/v1/retest/title-list"
    const val RETEST_SUMMARY_VISION = "$HOST/client/v1/retest/summary-vision"
    const val RECORD_SUBMIT = "$HOST/client/v1/record/submit"
}