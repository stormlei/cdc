package com.qpsoft.cdc.thirddevice.diopter.xinyuan;

import com.qpsoft.cdc.thirddevice.diopter.RefractionData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/***
 * Modify:
 * caotian caotian@gmail.com 2019-03-03 21:11
 * Description: 新缘验光仪数据解析
 * 测试通过: xinyuan fa6500K机器上是母口 传统蓝牙直接插
 */
public class Fa6500KDataParser {
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
        if (obj.contains("R") && obj.contains("L") && obj.contains("PD")) {
            Pattern p = Pattern.compile("[-+]?\\d+[\\.]?\\d*");
            Matcher m = p.matcher(obj);
            List<String> list = new ArrayList<>();
            while (m.find()) {
                String g = m.group();
                list.add(g);
            }
            String rs = list.get(0);
            String rc = list.get(1);
            String ra = list.get(2);
            //Log.e("--------R:", "s: "+rs+", c:"+rc+", a:"+ra);

            eyeDataRight = new RefractionData.EyeData();
            eyeDataRight.setS(rs);
            eyeDataRight.setC(rc);
            eyeDataRight.setA(Integer.parseInt(ra)+"");



            String ls = list.get(3);
            String lc = list.get(4);
            String la = list.get(5);
            //Log.e("--------L:", "s: "+ls+", c:"+lc+", a:"+la);

            eyeDataLeft = new RefractionData.EyeData();
            eyeDataLeft.setS(ls);
            eyeDataLeft.setC(lc);
            eyeDataLeft.setA(Integer.parseInt(la)+"");


            String pdData = list.get(6);

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