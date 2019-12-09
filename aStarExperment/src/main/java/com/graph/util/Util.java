package com.graph.util;

import com.graph.read.FindPath;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by yhj on 2018/6/5.
 */
public class Util {
    final static String lineSeparator = System.lineSeparator();
    /**
     * 读取相对路径的文件
     * @param filePath
     * @return
     */
    public static List<String> readFileRelate(String filePath){
        File file = new File(FindPath.class.getResource(filePath).getFile());
        return  readFile(file);
    }

    /**
     * 读取绝对路径的文件
     * @param filePath
     * @return
     */
    public static List<String> readFileAbsolute(String filePath){
        File file = new File(filePath);
        return readFile(file);
    }

    public static List<String> readFile(File file){
        BufferedReader reader = null;
        List<String> list = new ArrayList<String>();
        try {
            reader = new BufferedReader(new FileReader(file));
            String tmp = null;
            while ((tmp = reader.readLine()) != null){
                list.add(tmp);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(reader != null){
                    reader.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
            return list;
        }
    }

    /**
     * 写入到相对位置的文件
     * @param filePath
     * @param collection
     * @param separator
     * @return
     */
    public static <T> boolean writeCollectionRelate(Class<T> tClass, String filePath, Collection collection, String separator){
        File file = new File(tClass.getResource(filePath).getFile());
        return writeCollection(file, collection, separator);
    }

    public static <T> boolean writeCollectionRelate(Class<T> tClass, String filePath, Collection collection){
        File file = new File(tClass.getResource(filePath).getFile());
        return writeCollection(file, collection, lineSeparator);
    }

    /**
     * 写入到绝对位置的文件
     * @param filePath
     * @param collection
     * @param separator
     * @return
     */
    public static boolean writeCollectionAbsolute(String filePath, Collection collection, String separator){
        File file = new File(filePath);
        return writeCollection(file, collection, separator);
    }

    public static boolean writeCollectionAbsolute(String filePath, Collection collection){
        File file = new File(filePath);
        return writeCollection(file, collection, lineSeparator);
    }

    public static boolean writeCollection(File file, Collection collection, String separator){
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file));
            int count = 0;
            for(Object obj:collection){
//                System.out.println("count " + count);
                count++;
                if (count < collection.size()){
                    writer.write(obj.toString());
                    writer.write(separator);
                }
                else {
                    writer.write(obj.toString());
                }
            }
            return true;
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try {
                if(writer != null){
                    writer.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return false;
    }
}
