package com.qpsoft.cdc.thirddevice.diopter.duomei;

import com.qpsoft.cdc.thirddevice.diopter.RefractionData;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/***
 * Modify:
 * caotian caotian@gmail.com 2019-03-03 21:11
 * Description: Tomey验光仪数据解析
 * 测试通过:
 * ble 需要交叉线
 */
public class RC800DataParser {
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
            if(parsedData.startsWith("[POWER_R]")){
                String[] rData = parsedData.split(",");
                String rS = rData[2];
                String rC = rData[3];
                String rA = rData[4];

                eyeDataRight.s = rS;
                eyeDataRight.c = rC;
                eyeDataRight.a = rA;
            }
            if (parsedData.startsWith("[POWER_L]")) {
                String[] rData = parsedData.split(",");
                String lS = rData[2];
                String lC = rData[3];
                String lA = rData[4];

                eyeDataLeft.s = lS;
                eyeDataLeft.c = lC;
                eyeDataLeft.a = lA;
            }
            if (parsedData.startsWith("[PD]")) {
                String[] pdData = parsedData.split(",");
                pd = pdData[1];
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
