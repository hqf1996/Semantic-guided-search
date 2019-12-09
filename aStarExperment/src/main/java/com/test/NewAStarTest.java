package com.test;

import com.configuration.ValidationFile;
import com.graph.query.*;
import com.graph.util.Util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * @Author: yhj
 * @Description:
 * @Date: Created in 2018/9/2.
 */
public class NewAStarTest {
    final static String validationFile = ValidationFile.NEO4J_GERMANY.getName();               //德国的车
    public static void main(String[] args) throws IOException, InterruptedException{

        /**德国的汽车**/
        RDFGraph graph = createGraph("D:\\JavaProject\\aStarExperment\\result\\Automobile\\RDF\\AutoJacobian\\entity.txt",
                "D:\\JavaProject\\aStarExperment\\result\\Automobile\\RDF\\AutoJacobian\\edge.txt");
        String simFile = "D:\\JavaProject\\aStarExperment\\result\\TransEResult\\automobile\\iteration500\\part_0.1_0.4_0.8_200.txt";

        ReadCarSimilarity read = new ReadCarSimilarity(simFile, "assembly");
        Map<String, Double> map = read.getMap();
        QueryThreadInfo queryThreadInfo2 = new QueryThreadInfo("1712537", "assembly", map); //德国
        List<QueryThreadInfo> queryThreadInfos = new LinkedList<>();
        queryThreadInfos.add(queryThreadInfo2);

        aStartTest(graph, queryThreadInfos);
    }

    public static void aStartTest(RDFGraph graph, List<QueryThreadInfo> queryThreadInfos) throws IOException{
        // topK数量设置
        int[] tops = new int[]{20,40,80,100,200,300,400,500,600,700,800};

        for (int j = 0; j < tops.length; j++) {
            System.out.println("-----------------Top" + tops[j] + "-----------------");
            AStarQueryNew aStarQueryNew = new AStarQueryNew(graph, queryThreadInfos, "automobile", tops[j], 4);
            aStarQueryNew.run();
            AStarQueryNew.evaluation(aStarQueryNew, getCountryCarFromFile(validationFile));
        }
    }

    public static RDFGraph createGraph(String entityFile, String edgeFile) throws IOException{
        List<Entity> entities = new ArrayList<>();
        List<Entity[]> edges = new ArrayList<>();
        List<List<String>> edgesInfo = new ArrayList<>();
        readEntity(entityFile, entities);
        readEdges(edgeFile, edges, edgesInfo);
        System.out.println("read is over");
        RDFGraph graph = new RDFGraph(false);
        graph.createGraph(entities, edges, edgesInfo);
//        graph.show();
        return graph;
    }

    /**
     * 读取边的信息
     * @param edgeFile
     * @param edges
     * @param edgesInfo
     * @throws IOException
     */
    private static void readEdges(String edgeFile, List<Entity[]> edges, List<List<String>> edgesInfo) throws IOException{
        BufferedReader reader = null;
        reader = new BufferedReader(new InputStreamReader(new FileInputStream(edgeFile), "utf-8"));
        Map<String, List<String>> map = new HashMap<>();
        String s;
        int i = 0;
        while ((s = reader.readLine()) != null){
            String[] infos = s.split("\t");
            if(!infos[3].equals("type")){
                String entityInfo = infos[0] + "\t" + infos[1] + "\t" + infos[2] + "\t" + infos[4];
                if(map.containsKey(entityInfo)){
                    map.get(entityInfo).add(infos[3]);
                }else {
                    map.put(entityInfo, new ArrayList<>(Arrays.asList(infos[3])));
                }
            }
        }
        for(Map.Entry<String, List<String>> entry: map.entrySet()){
            String[] infos = entry.getKey().split("\t");
            Entity e1 = new Entity(infos[0], infos[2], null);
            Entity e2 = new Entity(infos[1], infos[3], null);
            edges.add(new Entity[]{e1, e2});
            edgesInfo.add(entry.getValue());
        }
        reader.close();
    }

    /**
     * 读取节点信息
     * @param entityFile
     * @return
     * @throws IOException
     */
    private static void readEntity(String entityFile, List<Entity> entities) throws IOException{
        BufferedReader reader = null;
        reader = new BufferedReader(new InputStreamReader(new FileInputStream(entityFile), "utf-8"));
        String s;
        while ((s = reader.readLine()) != null){
            String[] infos = s.split("\t");
            entities.add(new Entity(infos[0], infos[1], infos[2]));
        }
        reader.close();
    }

    /**
     * 从文件读取验证集
     * @return
     * @throws IOException
     */
    public static List<String> getCountryCarFromFile(String path) throws IOException{
        return new ArrayList<>(new HashSet<String>(Util.readFileAbsolute(path)));
    }

}
