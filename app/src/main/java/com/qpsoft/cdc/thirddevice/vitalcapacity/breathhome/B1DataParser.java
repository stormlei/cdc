package com.qpsoft.cdc.thirddevice.vitalcapacity.breathhome;

public class B1DataParser {
    //解析数据
    public VCData parse(byte[] byteArray) {
        String result = new String(byteArray);

        VCData vcData = null;
        String vc = null;

        String[] aa = result.split(",");
        if (aa.length > 15) {
            vc = aa[15];
        }

        vcData = new VCData();
        vcData.setVc(vc);

        return vcData;
    }
}