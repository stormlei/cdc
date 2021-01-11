package com.qpsoft.cdc.thirddevice.heightweight.shanghe;

import com.qpsoft.cdc.thirddevice.heightweight.HWData;

public class SH01DataParser {
    //解析数据
    public HWData parse(byte[] byteArray) {
        String orderValue = new String(byteArray);

        HWData hwData = null;
        String growthkit_height = null;
        String growthkit_weight = null;

        if (orderValue.contains("W") && orderValue.contains("H")) {
            growthkit_weight = orderValue.substring(2, 7);
            if (growthkit_weight.indexOf("0") == 0) {
                growthkit_weight = growthkit_weight.replaceFirst("0", "");
            }
            growthkit_height = orderValue.substring(10, 15);
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