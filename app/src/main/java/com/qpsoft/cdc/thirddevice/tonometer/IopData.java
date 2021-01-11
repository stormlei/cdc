package com.qpsoft.cdc.thirddevice.tonometer;

import java.io.Serializable;

/***
 * Modify:
 * caotian caotian@gmail.com 2019-03-03 21:07
 * Description: 抽象的验光仪数据, 目前只有
 */
public class IopData implements Serializable {
    public EyeData r = new EyeData();
    public EyeData l = new EyeData();
    public EyeData od = new EyeData();
    public EyeData os = new EyeData();

    public static class EyeData implements Serializable {
        //S球镜
        public String iop;

        @Override
        public String toString() {
            return "EyeData{" +
                    "iop='" + iop + '\'' +
                    '}';
        }

        public String getIop() {
            return iop;
        }

        public void setIop(String iop) {
            this.iop = iop;
        }
    }

    @Override
    public String toString() {
        return "TonoData{" +
                "r=" + r +
                ", l=" + l + '\'' +
                '}';
    }

    public EyeData getR() {
        return r;
    }

    public void setR(EyeData r) {
        this.r = r;
    }

    public EyeData getL() {
        return l;
    }

    public void setL(EyeData l) {
        this.l = l;
    }

    public EyeData getOd() {
        return od;
    }

    public void setOd(EyeData od) {
        this.od = od;
    }

    public EyeData getOs() {
        return os;
    }

    public void setOs(EyeData os) {
        this.os = os;
    }
}
