package com.qpsoft.cdc.thirddevice.pyrometer.faliao;

import com.qpsoft.cdc.thirddevice.pyrometer.PyroData;
import java.util.Objects;

public class FL800DataParser {
    //解析数据
    public PyroData parse(byte[] byteArray) {
        PyroData result = null;
        PyroData.EyeData eyeDataRight = null;
        PyroData.EyeData eyeDataLeft = null;

        if (Objects.isNull(byteArray)) {
            return null;
        }

        String obj = new String(byteArray);
        if (obj.contains("R") && obj.contains("L")) {
            String[] rlData = obj.split(",L,");
            //右眼数据
            String rData = rlData[0];
            String rs = "";
            String rc = "";
            String ra = "";
            if (rData.contains("SPH")) {
                int pos = rData.indexOf("SPH");
                rs = rData.substring(pos+4, pos+10);
                if (rs.indexOf("0") == 1) {
                    rs = rs.replaceFirst("0", "");
                }
            }
            if (rData.contains("CYL")) {
                int pos = rData.indexOf("CYL");
                rc = rData.substring(pos+4, pos+10);
                if (rc.indexOf("0") == 1) {
                    rc = rc.replaceFirst("0", "");
                }
            }
            if (rData.contains("AXS")) {
                int pos = rData.indexOf("AXS");
                ra = rData.substring(pos+4, pos+7);
                if (ra.indexOf("0") == 0) {
                    ra = ra.replaceFirst("0", "");
                }
            }


            System.out.println("--------R:, s: "+rs+", c:"+rc+", a:"+ra);

            eyeDataRight = new PyroData.EyeData();
            eyeDataRight.setSph(rs);
            eyeDataRight.setCyl(rc);
            eyeDataRight.setAxs(ra);



            //左眼数据
            String lData = rlData[1];
            String ls = "";
            String lc = "";
            String la = "";
            if (lData.contains("SPH")) {
                int pos = lData.indexOf("SPH");
                ls = lData.substring(pos+4, pos+10);
                if (ls.indexOf("0") == 1) {
                    ls = ls.replaceFirst("0", "");
                }
            }
            if (lData.contains("CYL")) {
                int pos = lData.indexOf("CYL");
                lc = lData.substring(pos+4, pos+10);
                if (lc.indexOf("0") == 1) {
                    lc = lc.replaceFirst("0", "");
                }
            }
            if (lData.contains("AXS")) {
                int pos = lData.indexOf("AXS");
                la = lData.substring(pos+4, pos+7);
                if (la.indexOf("0") == 0) {
                    la = la.replaceFirst("0", "");
                }
            }

            System.out.println("--------L:, s: "+ls+", c:"+lc+", a:"+la);

            eyeDataLeft = new PyroData.EyeData();
            eyeDataLeft.setSph(ls);
            eyeDataLeft.setCyl(lc);
            eyeDataLeft.setAxs(la);
        }

        result = new PyroData();
        result.setR(eyeDataRight==null ? new PyroData.EyeData() : eyeDataRight);
        result.setL(eyeDataLeft==null ? new PyroData.EyeData() : eyeDataLeft);
        result.setOd(eyeDataRight==null ? new PyroData.EyeData() : eyeDataRight);
        result.setOs(eyeDataLeft==null ? new PyroData.EyeData() : eyeDataLeft);

        return result;
    }
}