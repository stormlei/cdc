package com.qpsoft.cdc.thirddevice.diopter.nidek;

import com.qpsoft.cdc.thirddevice.diopter.RefractionData;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/***
 * Modify:
 * caotian caotian@gmail.com 2019-03-03 21:11
 * Description: Nidek验光仪数据解析
 * 测试通过: Nidek AR-1 ARK-1  AR-310A(不需要交叉线)
 */
public class NidekDataParser {

    public RefractionData parse(byte[] tempArray) {
        RefractionData result = new RefractionData();
        RefractionData.EyeData eyeDataRight = new RefractionData.EyeData();
        RefractionData.EyeData eyeDataLeft = new RefractionData.EyeData();

        ByteBuffer byteBuffer = ByteBuffer.wrap(tempArray);
        List<Byte> lineBytes = null;
        String lEyeData = "";
        String rEyeData = "";
        String pdEyeData = "";
        while ((lineBytes = readUntilCR(byteBuffer)) != null){
            if (lineBytes.get(0) == 0x4F && lineBytes.get(1) == 0x4C) {
                if ("".equals(lEyeData)) {
                    lEyeData = parseEyeData(lineBytes);
                    String[] leftArray = lEyeData.split(",");
                    String s = leftArray[0];
                    if (s.indexOf("0") == 1) {
                        s = s.replaceFirst("0", "");
                    }
                    eyeDataLeft.s = s;
                    String c = leftArray[1];
                    if (c.indexOf("0") == 1) {
                        c = c.replaceFirst("0", "");
                    }
                    eyeDataLeft.c = c;

                    eyeDataLeft.a = Integer.parseInt(leftArray[2])+"";
                    //Log.e("-------L", lEyeData);
                }
            }
            if (lineBytes.get(0) == 0x4F && lineBytes.get(1) == 0x52) {
                if ("".equals(rEyeData)) {
                    rEyeData = parseEyeData(lineBytes);
                    String[] rightArray = rEyeData.split(",");
                    String s = rightArray[0];
                    if (s.indexOf("0") == 1) {
                        s = s.replaceFirst("0", "");
                    }
                    eyeDataRight.s = s;
                    String c = rightArray[1];
                    if (c.indexOf("0") == 1) {
                        c = c.replaceFirst("0", "");
                    }
                    eyeDataRight.c = c;
                    eyeDataRight.a = Integer.parseInt(rightArray[2])+"";
                    //Log.e("-------R", rEyeData);
                }
            }
            if (lineBytes.get(0) == 0x50 && lineBytes.get(1) == 0x44) {
                pdEyeData = parsePdData(lineBytes);
                //Log.e("-------PD", pdEyeData);
            }
        }

        result.r = eyeDataRight;
        result.l = eyeDataLeft;
        result.od = eyeDataRight;
        result.os = eyeDataLeft;
        result.pd = pdEyeData;

        return result;
    }

    private String parsePdData(List<Byte> lineBytes){
        byte[] pdData = new byte[2];
        for(int i = 0; i < 2; i++){
            pdData[i] = lineBytes.get(i+2);
        }

        return new String(pdData);
    }

    private String parseEyeData(List<Byte> lineBytes){
        //int length = lineBytes.size() - 2;
        byte[] sData = new byte[6];
        for(int i = 0; i < 6; i++){
            sData[i] = lineBytes.get(i+2);
        }

        byte[] cData = new byte[6];
        for(int i = 0; i < 6; i++){
            cData[i] = lineBytes.get(i+2+6);
        }

        byte[] aData = new byte[3];
        for(int i = 0; i < 3; i++){
            aData[i] = lineBytes.get(i+2+6+6);
        }

        return new String(sData)+","+new String(cData)+","+new String(aData);
    }

    //读取字节流, 直到遇到CR
    private List<Byte> readUntilCR(ByteBuffer byteBuffer){
        List<Byte> resultList = new ArrayList<>();

        while (byteBuffer.hasRemaining()){
            byte b = byteBuffer.get();
            if(b == 0x17){
                return resultList;
            }else{
                resultList.add(b);
            }
        }

        return null;
    }
}
