package com.qpsoft.cdc.thirddevice.bloodpressure.yuwell;

public class YE900DataParser {
    //解析数据
    public BPData parse(byte[] byteArray) {
        BPData bpData = null;

        String sys = null;
        String dia = null;
        Float sysF = new DataConvert(byteArray).getFloatValue(DataConvert.FORMAT_SFLOAT, 1);
        Float diaF = new DataConvert(byteArray).getFloatValue(DataConvert.FORMAT_SFLOAT, 3);
        sys = String.valueOf(sysF);
        dia = String.valueOf(diaF);

        bpData = new BPData();
        bpData.setSys(sys);
        bpData.setDia(dia);

        return bpData;
    }
}