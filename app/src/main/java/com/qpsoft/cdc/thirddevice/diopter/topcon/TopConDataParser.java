package com.qpsoft.cdc.thirddevice.diopter.topcon;

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
public class TopConDataParser {
    //开始byte
    private byte byteStart = 0x41;

    //R byte
    private byte byteR = 0x52;

    //L byte
    private byte byteL = 0x4c;

    //CR byte, 换行
    private byte byteCR = 0x0d;

    //结束byte
    private byte byteEnd = 0x04;

    //解析数据
    public RefractionData parse(byte[] originByteArray) {
        RefractionData result = null;

        if(Objects.isNull(originByteArray)){
            return null;
        }

        //开始byte不符, 或结束byte不符, 数据不符合要求
        if(originByteArray[0] != byteStart || originByteArray[originByteArray.length - 1] != byteEnd){
            return null;
        }

        ByteBuffer byteBuffer = ByteBuffer.wrap(originByteArray);
        result = new RefractionData();

        //读出头部两个字节, 舍弃
        List<Byte> headBytes = readUntilCR(byteBuffer);

        if(Objects.isNull(headBytes)){
            return null;
        }
        //头部数量必须为1
        if(headBytes.size() != 1){
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
            //如果读取到大于1位的字节,则为数据
            int readSize = lineBytes2.size();

            String parsedData = parseEyeData(lineBytes2);
            switch (readSize){
                case 6:
                    if(eyeByte == byteR){
                        eyeDataRight.s = parsedData;
                    }else{
                        eyeDataLeft.s = parsedData;
                    }
                    break;
                case 5:
                    if(eyeByte == byteR){
                        eyeDataRight.c = parsedData;
                    }else{
                        eyeDataLeft.c = parsedData;
                    }
                    break;
                case 3:
                    if(eyeByte == byteR){
                        eyeDataRight.a = parsedData;
                    }else{
                        eyeDataLeft.a = parsedData;
                    }
                    break;
                case 2:
                    pd = parsedData;
                case 1:
                    //只读出来一个byte, 则为R,L,或者空格
                    byte singleByte = lineBytes2.get(0);
                    if(singleByte == byteR || singleByte == byteL){
                        eyeByte = singleByte;

                    }
                    break;
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
     * 原理: [0-6字节 S] [6-12字节 C] [12-15字节 A] 后续字节作用不详
     * @param lineBytes 行数据
     * @return 单眼数据
     */
    private String parseEyeData(List<Byte> lineBytes){
        int length = lineBytes.size();
        byte[] sData = new byte[length];
        for(int i = 0; i < length; i++){
            sData[i] = lineBytes.get(i);
        }

        return new String(sData).replaceAll("\\s+","");
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
