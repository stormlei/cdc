package com.qpsoft.cdc.thirddevice.pyrometer;

import java.io.Serializable;

/***
 * Modify:
 * caotian caotian@gmail.com 2019-03-03 21:07
 * Description: 抽象的验光仪数据, 目前只有
 */
public class PyroData implements Serializable {
    public EyeData r = new EyeData();
    public EyeData l = new EyeData();
    public EyeData od = new EyeData();
    public EyeData os = new EyeData();

    public static class EyeData implements Serializable {
        //S球镜
        public String sph;
        //C柱镜
        public String cyl;
        //A轴位
        public String axs;

        public String pxi;
        public String pxo;

        public String pyu;
        public String pyd;

        public String ad1;
        public String ad2;

        public String uv;

        public String pd;

        @Override
        public String toString() {
            return "EyeData{" +
                    "sph='" + sph + '\'' +
                    ", cyl='" + cyl + '\'' +
                    ", axs='" + axs + '\'' +
                    ", uv='" + uv + '\'' +
                    ", pd='" + pd + '\'' +
                    '}';
        }

        public String getSph() {
            return sph;
        }

        public void setSph(String sph) {
            this.sph = sph;
        }

        public String getCyl() {
            return cyl;
        }

        public void setCyl(String cyl) {
            this.cyl = cyl;
        }

        public String getAxs() {
            return axs;
        }

        public void setAxs(String axs) {
            this.axs = axs;
        }

        public String getPxi() {
            return pxi;
        }

        public void setPxi(String pxi) {
            this.pxi = pxi;
        }

        public String getPxo() {
            return pxo;
        }

        public void setPxo(String pxo) {
            this.pxo = pxo;
        }

        public String getPyu() {
            return pyu;
        }

        public void setPyu(String pyu) {
            this.pyu = pyu;
        }

        public String getPyd() {
            return pyd;
        }

        public void setPyd(String pyd) {
            this.pyd = pyd;
        }

        public String getAd1() {
            return ad1;
        }

        public void setAd1(String ad1) {
            this.ad1 = ad1;
        }

        public String getAd2() {
            return ad2;
        }

        public void setAd2(String ad2) {
            this.ad2 = ad2;
        }

        public String getUv() {
            return uv;
        }

        public void setUv(String uv) {
            this.uv = uv;
        }

        public String getPd() {
            return pd;
        }

        public void setPd(String pd) {
            this.pd = pd;
        }
    }

    @Override
    public String toString() {
        return "RefractionData{" +
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
