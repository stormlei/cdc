package com.qpsoft.cdc.thirddevice.diopter.faliao;

import com.qpsoft.cdc.thirddevice.diopter.RefractionData;
import java.util.Objects;

public class Fr710DataParser {
    //解析数据
    public RefractionData parse(byte[] byteArray) {
        RefractionData result = null;
        RefractionData.EyeData eyeDataRight = null;
        RefractionData.EyeData eyeDataLeft = null;

        if (Objects.isNull(byteArray)) {
            return null;
        }

        String str = new String(byteArray);
        String[] strArr = str.split("#!>");
        if (str.contains("REFR")) {
            String rStr = strArr[1]+"#!>";
            String s = rStr.substring(26, 32);
            String c = rStr.substring(32, 38);
            String a = rStr.substring(38, 41);
            if (s.indexOf("0") == 1) {
                s = s.replaceFirst("0", "");
            }
            if (c.indexOf("0") == 1) {
                c = c.replaceFirst("0", "");
            }
            if (a.indexOf("0") == 0) {
                a = a.replaceFirst("0", "");
            }

            eyeDataRight = new RefractionData.EyeData();
            eyeDataRight.setS(s);
            eyeDataRight.setC(c);
            eyeDataRight.setA(a);
        }
        if (str.contains("REFL")) {
            String lStr = strArr[2]+"#!>";
            String s = lStr.substring(26, 32);
            String c = lStr.substring(32, 38);
            String a = lStr.substring(38, 41);
            if (s.indexOf("0") == 1) {
                s = s.replaceFirst("0", "");
            }
            if (c.indexOf("0") == 1) {
                c = c.replaceFirst("0", "");
            }
            if (a.indexOf("0") == 0) {
                a = a.replaceFirst("0", "");
            }

            eyeDataLeft = new RefractionData.EyeData();
            eyeDataLeft.setS(s);
            eyeDataLeft.setC(c);
            eyeDataLeft.setA(a);
        }
        


        result = new RefractionData();
        result.setR(eyeDataRight==null ? new RefractionData.EyeData() : eyeDataRight);
        result.setL(eyeDataLeft==null ? new RefractionData.EyeData() : eyeDataLeft);
        result.setOd(eyeDataRight==null ? new RefractionData.EyeData() : eyeDataRight);
        result.setOs(eyeDataLeft==null ? new RefractionData.EyeData() : eyeDataLeft);

        return result;
    }
}