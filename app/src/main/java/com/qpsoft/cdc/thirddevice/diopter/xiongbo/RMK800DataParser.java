package com.qpsoft.cdc.thirddevice.diopter.xiongbo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qpsoft.cdc.thirddevice.diopter.RefractionData;
import com.qpsoft.cdc.thirddevice.diopter.tianle.KR9800DataParser;

import java.util.Objects;

/***
 * Modify:
 * caotian caotian@gmail.com 2019-03-03 21:11
 * Description: Xiongbo验光仪数据解析
 * 测试通过: xiongbo RMK800
 * ble 需要交叉线
 */
public class RMK800DataParser {
    //解析数据
    public RefractionData parse(byte[] byteArray) {
        RefractionData result = null;
        RefractionData.EyeData eyeDataRight = null;
        RefractionData.EyeData eyeDataLeft = null;
        String pd = "";

        if (Objects.isNull(byteArray)) {
            return null;
        }

        String str = new String(byteArray);
        if (!str.contains("right_s_avg")&&!str.contains("left_s_avg")) {
            return new KR9800DataParser().parse(byteArray);
        }
        JSONObject jsonObj = JSON.parseObject(str);
        if (jsonObj != null) {
            String rs = jsonObj.getString("right_s_avg");
            String rc = jsonObj.getString("right_c_avg");
            String ra = jsonObj.getString("right_a_avg");
            eyeDataRight = new RefractionData.EyeData();
            eyeDataRight.setS(rs);
            eyeDataRight.setC(rc);
            eyeDataRight.setA(ra);


            String ls = jsonObj.getString("left_s_avg");
            String lc = jsonObj.getString("left_c_avg");
            String la = jsonObj.getString("left_a_avg");
            eyeDataLeft = new RefractionData.EyeData();
            eyeDataLeft.setS(ls);
            eyeDataLeft.setC(lc);
            eyeDataLeft.setA(la);
        }

        result = new RefractionData();
        result.setR(eyeDataRight);
        result.setL(eyeDataLeft);
        result.setOd(eyeDataRight);
        result.setOs(eyeDataLeft);
        result.setPd(pd);

        return result;
    }
}
