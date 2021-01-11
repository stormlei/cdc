package com.qpsoft.cdc.thirddevice.diopter.tianle;

import com.qpsoft.cdc.thirddevice.diopter.RefractionData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/***
 * Modify:
 * caotian caotian@gmail.com 2019-03-03 21:11
 * Description: TopCon验光仪数据解析
 * 测试通过: TopCon RM8900
 */
public class Rm9000DataParser {
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
        String[] rArray = obj.split("E");
        for (int i = 1; i < rArray.length; i++) {
            if (rArray[i].startsWith("S-R-R")) {
                Pattern p = Pattern.compile("[-+]?\\d+[\\.]?\\d+");
                Matcher m = p.matcher(rArray[i]);
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
                String a = rList.get(2).replace("+", "");
                String rpd = rList.get(3);
                //Log.e("--------R:", "s: "+s+", c:"+c+", a:"+a+", pd:"+rpd+"");

                eyeDataRight = new RefractionData.EyeData();
                eyeDataRight.setS(s);
                eyeDataRight.setC(c);
                eyeDataRight.setA(Integer.parseInt(a)+"");
                pd = rpd;
            }
            if (rArray[i].startsWith("S-R-L")) {
                Pattern p = Pattern.compile("[-+]?\\d+[\\.]?\\d+");
                Matcher m = p.matcher(rArray[i]);
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
                String a = lList.get(2).replace("+", "");
                String lpd = lList.get(3);
                //Log.e("--------L:", "s: "+s+", c:"+c+", a:"+a+", pd:"+lpd+"");

                eyeDataLeft = new RefractionData.EyeData();
                eyeDataLeft.setS(s);
                eyeDataLeft.setC(c);
                eyeDataLeft.setA(Integer.parseInt(a)+"");
                pd = lpd;
            }
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
