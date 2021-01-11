package com.qpsoft.cdc.thirddevice.heightweight.lejia;

import com.qpsoft.cdc.thirddevice.heightweight.HWData;

public class LJ700DataParser {
    //解析数据
    public HWData parse(byte[] byteArray) {
        String orderValue = new String(byteArray);

        HWData hwData = null;
        String growthkit_height = null;
        String growthkit_weight = null;
        if (orderValue.length() >= 14) {
            growthkit_weight = orderValue.substring(1, 4) + "." + orderValue.substring(4, 5);
            if (growthkit_weight.contains(" ")) {
                growthkit_weight = growthkit_weight.trim();
            }
            growthkit_height = orderValue.substring(7, 10) + "." + orderValue.substring(10, 11);
            if (growthkit_height.indexOf("0") == 0) {
                growthkit_height = growthkit_height.replaceFirst("0", "");
            }
        }

        hwData = new HWData();
        hwData.setH(growthkit_height);
        hwData.setW(growthkit_weight);

        return hwData;
    }
}