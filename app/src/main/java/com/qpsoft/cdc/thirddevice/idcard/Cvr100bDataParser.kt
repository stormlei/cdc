/*
 * Copyright (c) 2020. QingPai Technology Co.,Ltd. caotian@gmail.com
 */

package com.qpsoft.cdc.thirddevice.idcard

/***
 * 身份证解析服务
 */
class Cvr100bDataParser {
    fun parse(byteArray: ByteArray): IdcardInfo? {
        val input = byteArrayToHex(byteArray)


        if (input.length != 2590) {
            return null
        }

        val idcardInfo = IdcardInfo()

        val startIndex = 28
        val len = 512

        val data = input.substring(startIndex, startIndex + len)

        //姓名
        val nameIndex = 0
        val nameLen = 60
        val nameData = data.substring(nameIndex, nameIndex + nameLen)
        idcardInfo.name = hexToString(nameData)

        //性别
        val genderIndex = 60
        val genderLen = 4
        val genderData = data.substring(genderIndex, genderIndex + genderLen)
        idcardInfo.gender = textToGender(hexToString(genderData))

        //民族
        val nationalityIndex = 64
        val nationalityLen = 8
        val nationalityData = data.substring(nationalityIndex, nationalityIndex + nationalityLen)
        idcardInfo.nationality = textToNationality(hexToString(nationalityData))

        //出生年月
        val birthdayIndex = 72
        val birthdayLen = 32
        val birthdayData = data.substring(birthdayIndex, birthdayIndex + birthdayLen)
        idcardInfo.birthday = hexToString(birthdayData)

        //地址
        val addressIndex = 104
        val addressLen = 140
        val addressData = data.substring(addressIndex, addressIndex + addressLen)
        idcardInfo.address = hexToString(addressData)

        //身份证号
        val cardNumberIndex = 244
        val cardNumberLen = 72
        val cardNumberData = data.substring(cardNumberIndex, cardNumberIndex + cardNumberLen)
        idcardInfo.cardNumber = hexToString(cardNumberData)

        //籍贯
        val nativePlaceCode = hexToString(cardNumberData).substring(0, 6).toInt()
        idcardInfo.nativePlace = NativePlace.getNativePlace(nativePlaceCode)

        //发证机关
        val issuingAuthorityIndex = 316
        val issuingAuthorityLen = 60
        val issuingAuthorityData = data.substring(issuingAuthorityIndex, issuingAuthorityIndex + issuingAuthorityLen)
        idcardInfo.issuingAuthority = hexToString(issuingAuthorityData)

        //有效期开始
        val startValidateDateIndex = 376
        val startValidateDateLen = 32
        val startValidateDateData = data.substring(startValidateDateIndex, startValidateDateIndex + startValidateDateLen)
        idcardInfo.startValidateDate = hexToString(startValidateDateData)

        //有效期结束
        val endValidateDateIndex = 408
        val endValidateDateLen = 32
        val endValidateDateData = data.substring(endValidateDateIndex, endValidateDateIndex + endValidateDateLen)
        idcardInfo.endValidateDate = hexToString(endValidateDateData)

        return idcardInfo
    }

    private fun textToNationality(input: String): String {
        return when(input){
            "01" -> "汉"
            "02" -> "蒙古"
            "03" -> "回"
            "04" -> "藏"
            "05" -> "维吾尔"
            "06" -> "苗"
            "07" -> "彝"
            "08" -> "壮"
            "09" -> "布依"
            "10" -> "朝鲜"
            "11" -> "满"
            "12" -> "侗"
            "13" -> "瑶"
            "14" -> "白"
            "15" -> "土家"
            "16" -> "哈尼"
            "17" -> "哈萨克"
            "18" -> "傣"
            "19" -> "黎"
            "20" -> "傈僳"
            "21" -> "佤"
            "22" -> "畲"
            "23" -> "高山"
            "24" -> "拉祜"
            "25" -> "水"
            "26" -> "东乡"
            "27" -> "纳西"
            "28" -> "景颇"
            "29" -> "柯尔克孜"
            "30" -> "土"
            "31" -> "达斡尔"
            "32" -> "仫佬"
            "33" -> "羌"
            "34" -> "布朗"
            "35" -> "撒拉"
            "36" -> "毛南"
            "37" -> "仡佬"
            "38" -> "锡伯"
            "39" -> "阿昌"
            "40" -> "普米"
            "41" -> "塔吉克"
            "42" -> "怒"
            "43" -> "乌孜别克"
            "44" -> "俄罗斯"
            "45" -> "鄂温克"
            "46" -> "德昂"
            "47" -> "保安"
            "48" -> "裕固"
            "49" -> "京"
            "50" -> "塔塔尔"
            "51" -> "独龙"
            "52" -> "鄂伦春"
            "53" -> "赫哲"
            "54" -> "门巴"
            "55" -> "珞巴"
            "56" -> "基诺"
            "97" -> "其他"
            "98" -> "外国血统中国籍人士"
            else -> "未知"
        }
    }

    private fun textToGender(input: String): String {
        return when(input){
            "1" -> "男"
            "2" -> "女"
            else -> "未知"
        }
    }

    private fun hexToString(hex: String): String {
        return String(hexToByteArray(hex), Charsets.UTF_16LE).trim()
    }

    fun byteArrayToHex(a: ByteArray): String {
        val sb = StringBuilder(a.size * 2)
        for (b in a) sb.append(String.format("%02x", b))
        return sb.toString()
    }

    private fun hexToByteArray(hex: String): ByteArray {
        // initialize the ASCII code string as empty.
        var i = 0
        val byteArray = ByteArray(hex.length / 2)
        while (i < hex.length) {

            // extract two characters from hex string
            val part = hex.substring(i, i + 2)

            // change it into base 16 and typecast as the character
            val ch = part.toInt(16).toByte()

            // add this char to final ASCII string
            byteArray[i/2] = ch
            i += 2
        }
        return byteArray
    }
}