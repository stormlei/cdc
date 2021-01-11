package com.qpsoft.cdc.thirddevice.glucose.yuwell;

import java.io.Serializable;

/***
 * Modify:
 * caotian caotian@gmail.com 2019-03-03 21:07
 * Description:
 */
public class BSData implements Serializable {
    private String bs;

    public String getBs() {
        return bs;
    }

    public void setBs(String bs) {
        this.bs = bs;
    }
}
