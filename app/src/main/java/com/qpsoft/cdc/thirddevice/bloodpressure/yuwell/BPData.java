package com.qpsoft.cdc.thirddevice.bloodpressure.yuwell;

import java.io.Serializable;

/***
 * Modify:
 * caotian caotian@gmail.com 2019-03-03 21:07
 * Description:
 */
public class BPData implements Serializable {
    private String sys;
    private String dia;
    private String pl;

    public String getSys() {
        return sys;
    }

    public void setSys(String sys) {
        this.sys = sys;
    }

    public String getDia() {
        return dia;
    }

    public void setDia(String dia) {
        this.dia = dia;
    }

    public String getPl() {
        return pl;
    }

    public void setPl(String pl) {
        this.pl = pl;
    }
}
