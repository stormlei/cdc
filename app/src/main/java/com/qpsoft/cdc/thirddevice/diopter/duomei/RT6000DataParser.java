package com.qpsoft.cdc.thirddevice.diopter.duomei;

import com.qpsoft.cdc.thirddevice.diopter.RefractionData;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/***
 * Modify:
 * caotian caotian@gmail.com 2019-03-03 21:11
 * Description: 验光仪数据解析
 * 测试通过: 交叉线和ble R:3/K:1 Format2 9600
 */
public class RT6000DataParser {
    //CR byte, 换行
    private byte byteCR = 0x0A;


    //解析数据
    public RefractionData parse(byte[] originByteArray) {
        RefractionData result = null;

        if(Objects.isNull(originByteArray)){
            return null;
        }

        ByteBuffer byteBuffer = ByteBuffer.wrap(originByteArray);
        result = new RefractionData();


        //循环读取, 每次读到CR换行为止
        List<Byte> lineBytes2 = null;
        RefractionData.EyeData eyeDataRight = new RefractionData.EyeData();
        RefractionData.EyeData eyeDataLeft = new RefractionData.EyeData();
        String pd = "";

        while ((lineBytes2 = readUntilCR(byteBuffer)) != null){
            String parsedData = parseEyeData(lineBytes2);
            if(parsedData.startsWith("Ref_R*")){
                String[] rData = parsedData.split(",");
                String rS = rData[1];
                String rC = rData[2];
                String rA = rData[3];

                eyeDataRight.s = rS;
                eyeDataRight.c = rC;
                eyeDataRight.a = rA;
            }
            if (parsedData.startsWith("Ref_L*")) {
                String[] rData = parsedData.split(",");
                String lS = rData[1];
                String lC = rData[2];
                String lA = rData[3];

                eyeDataLeft.s = lS;
                eyeDataLeft.c = lC;
                eyeDataLeft.a = lA;
            }
            if (parsedData.startsWith("Kerato_R")) {
                String[] rData = parsedData.split(",");
                String rk = rData[7];

                eyeDataRight.k = rk;
            }
            if (parsedData.startsWith("Kerato_L")) {
                String[] lData = parsedData.split(",");
                String lk = lData[7];

                eyeDataLeft.k = lk;
            }
            if (parsedData.startsWith("PD")) {
                //String[] pdData = parsedData.split(",");
                //pd = pdData[1];
            }
        }

        result.r = eyeDataRight;
        result.l = eyeDataLeft;
        result.od = eyeDataRight;
        result.os = eyeDataLeft;
        result.pd = pd;

        return result;
    }

    /**
     * 解析单眼数据
     * @param lineBytes 行数据
     * @return 单眼数据
     */
    private String parseEyeData(List<Byte> lineBytes){
        int length = lineBytes.size();
        byte[] sData = new byte[length];
        for(int i = 0; i < length; i++){
            sData[i] = lineBytes.get(i);
        }
        return new String(sData);
    }

    //读取字节流, 直到遇到CR
    private List<Byte> readUntilCR(ByteBuffer byteBuffer){
        List<Byte> resultList = new ArrayList<>();

        while (byteBuffer.hasRemaining()){
            byte b = byteBuffer.get();
            if(b == byteCR){
                return resultList;
            }else{
                resultList.add(b);
            }
        }

        return null;
    }
}