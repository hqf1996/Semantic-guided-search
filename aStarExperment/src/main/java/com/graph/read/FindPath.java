package com.graph.read;

import com.graph.util.Util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by yhj on 2018/5/29.
 * 找到所有的n跳之内的所有路径，用于观察该领域下的图的大致构造
 */
public class FindPath {
    private int plies;
    private List<List<String>> paths;
    private BaseOperate graph;
    private BufferedWriter writer;
    private Map<String, Integer> countMap;
    public FindPath(int plies, String filePath) {
        this.plies = plies;
        graph = new GraphTableOperate("edge_no_string_complete_source");
        paths = new ArrayList<List<String>>();
        countMap = new HashMap<String, Integer>();
        try {
            writer = new BufferedWriter(new FileWriter(new File(filePath)));
        }catch (IOException e){
            e.printStackTrace();
            closeWrite();
        }

    }

    public void clear(){
        paths.clear();
    }

    public List<List<String>> find(Collection<String> c) throws SQLException {
        for(String node: c){
            //递归取前n条的路径
            findNext(new ArrayList<String>(Arrays.asList(node)), 0);
        }
        closeWrite();
        return this.paths;
    }

    public Map<String, Integer> getCountMap(){
        return countMap;
    }

    private void writePath(List<String> path){
        try {
            List<String> edges = new ArrayList<String>();
            for(int i = 1;i < path.size(); i = i + 2){
                edges.add(path.get(i));
            }
            String pathStr = edges.toString();
            if(countMap.containsKey(pathStr)){
                countMap.put(pathStr, countMap.get(pathStr) + 1);
            }else {
                countMap.put(pathStr, 1);
            }
            writer.write(pathStr);
            writer.write("\t" + path.toString() + "\n");
            writer.flush();
        }catch (IOException e){
            e.printStackTrace();
            closeWrite();
        }
    }

    private void closeWrite(){
        try {
            if(writer != null){
                writer.close();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void findNext(List<String> path, int n) throws SQLException {
        if(n == plies) {
            writePath(path);
            return;
        }
        String limit = "where predicate_name != 'type' and entity_id1 = '" + path.get(path.size()-1).replace("'", "\\'") + "'";
        List<EdgeSource> list = (List<EdgeSource>)graph.selectLimit(limit);
        if(list.size() <= 0){
            writePath(path);
            return;
        }
        for(EdgeSource each: list){
            List<String> new_path = new ArrayList<String>(path);
            new_path.add(each.getPredicate_name());
            new_path.add(each.getEntity_id2());
            findNext(new_path, n + 1);
        }
    }

    public static void main(String[] args) throws SQLException, IOException {
        FindPath findPath = new FindPath(4, "E:\\JavaProjects\\rdf_conputer\\result\\Astronaut\\four_all_path.txt");
        List<String> list = Util.readFileRelate("/astronauts_id.txt");
        List<List<String>> paths = findPath.find(new ArrayList<String>(list));
        findPath.closeWrite();
    }
}
