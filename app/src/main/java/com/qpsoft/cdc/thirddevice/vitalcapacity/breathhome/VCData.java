package com.qpsoft.cdc.thirddevice.vitalcapacity.breathhome;

import java.io.Serializable;

/***
 * Modify:
 * caotian caotian@gmail.com 2019-03-03 21:07
 * Description:
 */
public class VCData implements Serializable {
    private String vc;

    public String getVc() {
        return vc;
    }

    public void setVc(String vc) {
        this.vc = vc;
    }
}
