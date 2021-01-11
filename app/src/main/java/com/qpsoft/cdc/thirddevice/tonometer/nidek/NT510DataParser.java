package com.qpsoft.cdc.thirddevice.tonometer.nidek;

import com.qpsoft.cdc.thirddevice.tonometer.IopData;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/***
 * Modify:
 * caotian caotian@gmail.com 2019-03-03 21:11
 * Description: Nidek眼压计数据解析
 * 测试通过: Nidek NT-510
 */
public class NT510DataParser {

    public IopData parse(byte[] tempArray) {
        IopData result = new IopData();
        IopData.EyeData eyeDataRight = new IopData.EyeData();
        IopData.EyeData eyeDataLeft = new IopData.EyeData();

        ByteBuffer byteBuffer = ByteBuffer.wrap(tempArray);
        List<Byte> lineBytes = null;
        String lEyeData = "";
        String rEyeData = "";
        while ((lineBytes = readUntilCR(byteBuffer)) != null){
            if (lineBytes.get(0) == 0x20 && lineBytes.get(1) == 0x4C) {
                if ("".equals(lEyeData)) {
                    lEyeData = parseEyeData(lineBytes);
                    String[] leftArray = lEyeData.split(",");
                    String iop = leftArray[0];
                    eyeDataLeft.iop = iop;
                    //Log.e("-------L", lEyeData);
                }
            }
            if (lineBytes.get(0) == 0x20 && lineBytes.get(1) == 0x52) {
                if ("".equals(rEyeData)) {
                    rEyeData = parseEyeData(lineBytes);
                    String[] rightArray = rEyeData.split(",");
                    String iop = rightArray[0];
                    eyeDataRight.iop = iop;
                    //Log.e("-------R", rEyeData);
                }
            }
        }

        result.r = eyeDataRight;
        result.l = eyeDataLeft;
        result.od = eyeDataRight;
        result.os = eyeDataLeft;

        return result;
    }

    private String parseEyeData(List<Byte> lineBytes){ //AV15.0/2.00
        int length1 = lineBytes.size() - 9;
        byte[] iopData = new byte[4];
        for(int i = 0; i < 4; i++){
            iopData[i] = lineBytes.get(i+length1);
        }

        int length2 = lineBytes.size() - 4;
        byte[] kpaData = new byte[4];
        for(int i = 0; i < 4; i++){
            kpaData[i] = lineBytes.get(i+length2);
        }

        return new String(iopData)+","+new String(kpaData);
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
