package com.qpsoft.cdc.thirddevice.diopter;

import java.io.Serializable;

/***
 * Modify:
 * caotian caotian@gmail.com 2019-03-03 21:07
 * Description: 抽象的验光仪数据, 目前只有
 */
public class RefractionData implements Serializable {
    public EyeData r = new EyeData();
    public EyeData l = new EyeData();
    public EyeData od = new EyeData();
    public EyeData os = new EyeData();
    public String pd = "";

    public static class EyeData implements Serializable {
        //S球镜
        public String s;
        //C柱镜
        public String c;
        //A轴位
        public String a;
        //曲率
        public String k;

        @Override
        public String toString() {
            return "EyeData{" +
                    "s='" + s + '\'' +
                    ", c='" + c + '\'' +
                    ", a='" + a + '\'' +
                    ", k='" + k + '\'' +
                    '}';
        }

        public String getS() {
            return s;
        }

        public void setS(String s) {
            this.s = s;
        }

        public String getC() {
            return c;
        }

        public void setC(String c) {
            this.c = c;
        }

        public String getA() {
            return a;
        }

        public void setA(String a) {
            this.a = a;
        }

        public String getK() {
            return k;
        }

        public void setK(String k) {
            this.k = k;
        }
    }

    @Override
    public String toString() {
        return "diopter.RefractionData{" +
                "r=" + r +
                ", l=" + l +
                ", pd='" + pd + '\'' +
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

    public String getPd() {
        return pd;
    }

    public void setPd(String pd) {
        this.pd = pd;
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
