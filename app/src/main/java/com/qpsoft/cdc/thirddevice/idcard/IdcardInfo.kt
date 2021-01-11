package com.qpsoft.cdc.thirddevice.idcard

/***
 * 身份证信息
 */
class IdcardInfo {
    //姓名
    var name: String? = null

    //性别
    var gender: String? = null

    //民族
    var nationality: String? = null

    //籍贯
    var nativePlace: String? = null

    //出生年月
    var birthday: String? = null

    //地址
    var address: String? = null

    //身份证号
    var cardNumber: String? = null

    //发证机关
    var issuingAuthority: String? = null

    //有效期开始
    var startValidateDate: String? = null

    //有效期结束
    var endValidateDate: String? = null
}