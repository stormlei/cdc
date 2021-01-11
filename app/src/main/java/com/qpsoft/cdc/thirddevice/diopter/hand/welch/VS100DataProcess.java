package com.qpsoft.cdc.thirddevice.diopter.hand.welch;


import com.qpsoft.cdc.thirddevice.diopter.hand.RefractionData;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Modify:
 * caotian caotian@qq.com 2018-12-09 13:50
 * Description:
 */
public class VS100DataProcess {

    //CR byte, 换行
    private byte byteCR = 0x0A;

    private List<String> dataList = new ArrayList<>();


    //解析数据
    public RefractionData parse(byte[] originByteArray) {

        if(Objects.isNull(originByteArray)){
            return null;
        }

        ByteBuffer byteBuffer = ByteBuffer.wrap(originByteArray);

        //循环读取, 每次读到CR换行为止
        List<Byte> lineBytes2 = null;

        while ((lineBytes2 = readUntilCR(byteBuffer)) != null){
            String parsedData = parseEyeData(lineBytes2);
            dataList.add(parsedData);
        }

        return parse(dataList);
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

    public static RefractionData parse(List<String> dataList) {
        if(dataList.size() == 0){
            return null;
        }

        int index = 0;
        RefractionData refractionData = new RefractionData();
        RefractionData.EyeData od = new RefractionData.EyeData();
        RefractionData.EyeData os = new RefractionData.EyeData();

        for(String str : dataList){
            //第3行提取年龄范围
            if(index == 3){
                Pattern ds = Pattern.compile("[0-9]+[ym]-[0-9]+[ym]");
                Matcher matcher = ds.matcher(str);

                String result = "";
                List<String> ageRangeData = new ArrayList<>();
                while (matcher.find()){
                    ageRangeData.add(matcher.group());
                }

                if(ageRangeData.size() == 1){
                    refractionData.setAgeRange(ageRangeData.get(0));
                }
            }

            //第5行提取OD
            if(index == 5){
                //DS
                Pattern ds = Pattern.compile("[+-]?[0-9]+(\\.[0-9]+)? DS");
                Matcher matcher = ds.matcher(str);

                String result = "";
                List<String> odData = new ArrayList<>();
                while (matcher.find()){
                    odData.add(matcher.group());
                }
                if(odData.size() > 0){
                    od.setS(odData.get(0).replace(" DS", ""));
                }

                //DC
                ds = Pattern.compile("[+-]?[0-9]+(\\.[0-9]+)? DC");
                matcher = ds.matcher(str);

                odData.clear();
                while (matcher.find()){
                    odData.add(matcher.group());
                }
                if(odData.size() > 0){
                    od.setC(odData.get(0).replace(" DC", ""));
                }

                //AX
                ds = Pattern.compile("@[0-9]+");
                matcher = ds.matcher(str);

                odData.clear();
                while (matcher.find()){
                    odData.add(matcher.group());
                }

                if(odData.size() > 0){
                    od.setA(odData.get(0).replace("@", ""));
                }

                //SE
                ds = Pattern.compile("[+-]?[0-9]+(\\.[0-9]+)? SE");
                matcher = ds.matcher(str);

                odData.clear();
                while (matcher.find()){
                    odData.add(matcher.group());
                }
                if(odData.size() > 0){
                    od.setSe(odData.get(0).replace(" SE", ""));
                }
            }

            //第6行提取OS
            if(index == 6){
                //DS
                Pattern ds = Pattern.compile("[+-]?[0-9]+(\\.[0-9]+)? DS");
                Matcher matcher = ds.matcher(str);

                String result = "";
                List<String> osData = new ArrayList<>();
                while (matcher.find()){
                    osData.add(matcher.group());
                }
                if(osData.size() > 0){
                    os.setS(osData.get(0).replace(" DS", ""));
                }

                //DC
                ds = Pattern.compile("[+-]?[0-9]+(\\.[0-9]+)? DC");
                matcher = ds.matcher(str);

                osData.clear();
                while (matcher.find()){
                    osData.add(matcher.group());
                }
                if(osData.size() > 0){
                    os.setC(osData.get(0).replace(" DC", ""));
                }

                //AX
                ds = Pattern.compile("@[0-9]+");
                matcher = ds.matcher(str);

                osData.clear();
                while (matcher.find()){
                    osData.add(matcher.group());
                }

                if(osData.size() > 0){
                    os.setA(osData.get(0).replace("@", ""));
                }

                //SE
                ds = Pattern.compile("[+-]?[0-9]+(\\.[0-9]+)? SE");
                matcher = ds.matcher(str);

                osData.clear();
                while (matcher.find()){
                    osData.add(matcher.group());
                }
                if(osData.size() > 0){
                    os.setSe(osData.get(0).replace(" SE", ""));
                }
            }

            //第7行提取瞳孔大小
            if(index == 7){
                Pattern ds = Pattern.compile("[0-9]+(\\.[0-9]+)?mm");
                Matcher matcher = ds.matcher(str);

                String result = "";
                List<String> psData = new ArrayList<>();
                while (matcher.find()){
                    psData.add(matcher.group());
                }

                if(psData.size() == 2){
                    od.setPs(psData.get(0).replace("mm", ""));
                    os.setPs(psData.get(1).replace("mm", ""));
                }
            }

            //第8行提取瞳距
            if(index == 8){
                Pattern ds = Pattern.compile("[0-9]+(\\.[0-9]+)?mm");
                Matcher matcher = ds.matcher(str);

                String result = "";
                List<String> pdData = new ArrayList<>();
                while (matcher.find()){
                    pdData.add(matcher.group());
                }

                if(pdData.size() == 1){
                    refractionData.setPd(pdData.get(0).replace("mm", ""));
                }
            }

            //第9行提取斜视
            if(index == 9){
                Pattern ds = Pattern.compile("[+-]?[0-9]{1,2}");
                Matcher matcher = ds.matcher(str);

                String result = "";
                List<String> heterData = new ArrayList<>();
                while (matcher.find()){
                    heterData.add(matcher.group());
                }

                if(heterData.size() == 4){
                    od.setHeterH(heterData.get(0));
                    od.setHeterV(heterData.get(1));

                    os.setHeterH(heterData.get(2));
                    os.setHeterV(heterData.get(3));
                }
            }

            //第12行提取时间
            if(index == 12){
                Pattern ds = Pattern.compile("[0-9]{4}-[0-9]{1,2}-[0-9]{1,2} [0-9]{1,2}:[0-9]{1,2} [ap]m");
                Matcher matcher = ds.matcher(str);

                String result = "";
                List<String> dateData = new ArrayList<>();
                while (matcher.find()){
                    dateData.add(matcher.group());
                }

                if(dateData.size() == 1){
                    refractionData.setDate(dateData.get(0));
                }
            }

            if(refractionData.getDate() == null){
                if(index == 13){
                    Pattern ds = Pattern.compile("[0-9]{4}-[0-9]{1,2}-[0-9]{1,2} [0-9]{1,2}:[0-9]{1,2} [ap]m");
                    Matcher matcher = ds.matcher(str);

                    String result = "";
                    List<String> dateData = new ArrayList<>();
                    while (matcher.find()){
                        dateData.add(matcher.group());
                    }

                    if(dateData.size() == 1){
                        refractionData.setDate(dateData.get(0));
                    }
                }
            }

            index++;
        }
        refractionData.setOd(od);
        refractionData.setOs(os);
        refractionData.setR(od);
        refractionData.setL(os);

        return refractionData;
    }
}
