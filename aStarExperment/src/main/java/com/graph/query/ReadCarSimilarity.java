package com.graph.query;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @Author: yhj
 * @Description:
 * @Date: Created in 2018/9/2.
 */
public class ReadCarSimilarity extends ReadSimilarity<String, Double> {

    public ReadCarSimilarity(String filePath, String center){
        super(filePath, center);
        map.put(center, 1.0);
    }

    @Override
    protected void read(){
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "utf-8"));
            String line;
            int flag = 0;
            while ((line = reader.readLine()) != null){
                if(line.contains("====================")){
                    flag = -1;
                }
                if(line.equals("====================It is " + center  + "======================")){
                    flag = 0;
                }
                if(flag > 0){
                    String s = line.split(" : ")[1];
                    String[] infos = s.split("-------------");
                    map.put(infos[0], Double.parseDouble(infos[1]));
                }
                if(flag >= 0){
                    flag ++;
                }
            }
        }catch (IOException e){
            e.printStackTrace();
            System.out.println(e);
        } finally {
            if(reader != null){
                try {
                    reader.close();
                }catch (IOException e1){
                    e1.printStackTrace();
                }
            }
        }

    }
}
