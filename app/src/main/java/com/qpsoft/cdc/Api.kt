package com.qpsoft.cdc

object Api {
    private const val HOST = "https://govdemo.qpsc365.com/api"

    const val LOGIN = "$HOST/auth/v1/client"
    const val CURRENT_PLAN = "$HOST/client/v1/user/current-plan"
    const val SCHOOL = "$HOST/client/v1/school"
    const val GRADE_CLAZZ_LIST = "$HOST/client/v1/student/grade-clazz-list"
}