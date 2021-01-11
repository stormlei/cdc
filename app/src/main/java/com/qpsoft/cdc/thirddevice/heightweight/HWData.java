package com.qpsoft.cdc.thirddevice.heightweight;

import java.io.Serializable;

/***
 * Modify:
 * caotian caotian@gmail.com 2019-03-03 21:07
 * Description:
 */
public class HWData implements Serializable {
    private String h;
    private String w;

    public String getH() {
        return h;
    }

    public void setH(String h) {
        this.h = h;
    }

    public String getW() {
        return w;
    }

    public void setW(String w) {
        this.w = w;
    }
}
