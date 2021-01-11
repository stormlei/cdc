package com.qpsoft.cdc.thirddevice.diopter.xinyuan;

import com.qpsoft.cdc.thirddevice.diopter.RefractionData;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/***
 * Modify:
 * caotian caotian@gmail.com 2019-03-03 21:11
 * Description: TopCon验光仪数据解析
 * 测试通过: TopCon RM8900
 */
public class Fa6500DataParser {

    //R byte
    private byte byteR = 0x52;

    //L byte
    private byte byteL = 0x4C;

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

        //读出头部两个字节, 舍弃
        List<Byte> headBytes = readUntilCR(byteBuffer);

        if(Objects.isNull(headBytes)){
            return null;
        }
        //当前眼
        byte eyeByte = headBytes.get(0);

        //循环读取, 每次读到CR换行为止
        List<Byte> lineBytes2 = null;
        RefractionData.EyeData eyeDataRight = new RefractionData.EyeData();
        RefractionData.EyeData eyeDataLeft = new RefractionData.EyeData();
        String pd = "";

        while ((lineBytes2 = readUntilCR(byteBuffer)) != null){
            if (lineBytes2.get(0) == byteR) {
                eyeByte = byteR;
                continue;
            }
            if (lineBytes2.get(0) == byteL) {
                eyeByte = byteL;
                continue;
            }
            String parsedData = parseEyeData(lineBytes2);
            if(eyeByte == byteR){
                if (parsedData.contains("S")) {
                    eyeDataRight.s = parsedData.split(" ")[1];
                }
                if (parsedData.contains("C")) {
                    eyeDataRight.c = parsedData.split(" ")[1];
                }
                if (parsedData.contains("A")) {
                    eyeDataRight.a = parsedData.split(" ")[1];
                }
                if (parsedData.contains("PD")) {
                    pd = parsedData.split(" ")[1];
                }
            }
            if (eyeByte == byteL) {
                if (parsedData.contains("S")) {
                    eyeDataLeft.s = parsedData.split(" ")[1];
                }
                if (parsedData.contains("C")) {
                    eyeDataLeft.c = parsedData.split(" ")[1];
                }
                if (parsedData.contains("A")) {
                    eyeDataLeft.a = parsedData.split(" ")[1];
                }
                if (parsedData.contains("PD")) {
                    pd = parsedData.split(" ")[1];
                }
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
