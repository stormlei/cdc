package com.qpsoft.cdc.thirddevice.glucose.yuwell;

import com.qpsoft.cdc.thirddevice.bloodpressure.yuwell.DataConvert;

public class B305DataParser {
    //解析数据
    public BSData parse(byte[] byteArray) {
        BSData bsData = null;

        String bs = null;
        Float bsF = new DataConvert(byteArray).getFloatValue(DataConvert.FORMAT_SFLOAT, 10);
        bsF = (bsF != null ? bsF * 1000 : null);
        bs = String.valueOf(bsF);

        bsData = new BSData();
        bsData.setBs(bs);

        return bsData;
    }
}