package com.qpsoft.cdc.thirddevice.diopter.faliao;

import com.qpsoft.cdc.thirddevice.diopter.RefractionData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/***
 * Modify: 蓝牙需要交叉线
 * caotian caotian@gmail.com 2019-03-03 21:11
 * Description: 法里奥验光仪数据解析
 * 测试通过: faliao FR8900 母口线(不需要交叉线)，蓝牙（需要母母转接）
 * ble 不需要线直接插
 */
public class Fr8900DataParser {
    //解析数据
    public RefractionData parse(byte[] byteArray) {
        RefractionData result = null;
        RefractionData.EyeData eyeDataRight = null;
        RefractionData.EyeData eyeDataLeft = null;
        String pd = "";

        if (Objects.isNull(byteArray)) {
            return null;
        }

        String obj = new String(byteArray);
        if (obj.contains("R,")) {
            int rIndex = obj.indexOf("R,");
            String rEyeData = obj.substring(rIndex+2, rIndex+2+23);

            Pattern p = Pattern.compile("[-+]?\\d+[\\.]?\\d+");
            Matcher m = p.matcher(rEyeData);
            List<String> rList = new ArrayList<>();
            while (m.find()) {
                String g = m.group();
                rList.add(g);
            }
            String s = rList.get(0);
            if (s.indexOf("0") == 1) {
                s = s.replaceFirst("0", "");
            }
            String c = rList.get(1);
            if (c.indexOf("0") == 1) {
                c = c.replaceFirst("0", "");
            }
            String a = rList.get(2);
            //Log.e("--------R:", "s: "+s+", c:"+c+", a:"+a);

            eyeDataRight = new RefractionData.EyeData();
            eyeDataRight.setS(s);
            eyeDataRight.setC(c);
            eyeDataRight.setA(Integer.parseInt(a)+"");
        }
        if (obj.contains("L,")) {
            int lIndex = obj.indexOf("L,");
            String lEyeData = obj.substring(lIndex+2, lIndex+2+23);

            Pattern p = Pattern.compile("[-+]?\\d+[\\.]?\\d+");
            Matcher m = p.matcher(lEyeData);
            List<String> lList = new ArrayList<>();
            while (m.find()) {
                String g = m.group();
                lList.add(g);
            }
            String s = lList.get(0);
            if (s.indexOf("0") == 1) {
                s = s.replaceFirst("0", "");
            }
            String c = lList.get(1);
            if (c.indexOf("0") == 1) {
                c = c.replaceFirst("0", "");
            }
            String a = lList.get(2);
            //Log.e("--------L:", "s: "+s+", c:"+c+", a:"+a);

            eyeDataLeft = new RefractionData.EyeData();
            eyeDataLeft.setS(s);
            eyeDataLeft.setC(c);
            eyeDataLeft.setA(Integer.parseInt(a)+"");
        }
        if (obj.contains("PD")) {
            int pdIndex = obj.indexOf("PD");
            String pdEyeData = obj.substring(pdIndex, pdIndex+5);

            Pattern p = Pattern.compile("[-+]?\\d+[\\.]?\\d+");
            Matcher m = p.matcher(pdEyeData);
            List<String> pdList = new ArrayList<>();
            while (m.find()) {
                String g = m.group();
                pdList.add(g);
            }
            String pdData = pdList.get(0);

            //Log.e("--------PD:", "pd:"+pdData+"");

            pd = pdData;
        }

        result = new RefractionData();
        result.setR(eyeDataRight==null ? new RefractionData.EyeData() : eyeDataRight);
        result.setL(eyeDataLeft==null ? new RefractionData.EyeData() : eyeDataLeft);
        result.setOd(eyeDataRight==null ? new RefractionData.EyeData() : eyeDataRight);
        result.setOs(eyeDataLeft==null ? new RefractionData.EyeData() : eyeDataLeft);
        result.setPd(pd);

        return result;
    }
}
