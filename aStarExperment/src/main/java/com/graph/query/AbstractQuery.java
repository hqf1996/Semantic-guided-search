package com.graph.query;

import java.util.*;

/**
 * @Author: yhj
 * @Description:
 * @Date: Created in 2018/9/17.
 */
public abstract class AbstractQuery {
    RDFGraph graph;                                                 //查询图的邻接表结构
    List<QueryThreadInfo> queryThreadInfos;                         //多源查询的锚节点、指定谓词及其相似度信息
    String type;                                                    //查询的指定类型
    int topN;                                                       //查询结果的topK

    public AbstractQuery(RDFGraph graph, List<QueryThreadInfo> queryThreadInfos, String type, int topN) {
        this.graph = graph;
        this.queryThreadInfos = queryThreadInfos;
        this.type = type;
        this.topN = topN;
    }

    abstract public void run();

    public boolean checkType(String type) {
        return this.type.equals(type);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getTopN() {
        return topN;
    }

    public void setTopN(int topN) {
        this.topN = topN;
    }


    /**
     * 根据指定的谓词得到谓词之间的相似度
     * @param predicate
     * @return
     */
    public static Map<String, Double> findPreicateSimilar(String predicate) {
        return null;
    }

    /**
     * 多源的查询结果的谓词顺序组成路径
     */
    public static class MultiSourcePredicatesPaths {
        Map<Integer, Collection<List<List<String>>>> sourceMap = new HashMap<>();

        public Collection<List<List<String>>> getMultiPredicatesPaths(int index) {
            return sourceMap.get(index);
        }

        public Collection<Integer> getSourceKey() {
            return sourceMap.keySet();
        }

        public void setMultiPredicatesPaths(int index, Collection<List<List<String>>> multiPredicatesPaths) {
            sourceMap.put(index, multiPredicatesPaths);
        }

        public void addPredicatesPaths(int index, List<List<String>> predicatesPaths) {
            if (!sourceMap.containsKey(index)) {
                sourceMap.put(index, new ArrayList<>());
            }
            sourceMap.get(index).add(predicatesPaths);
        }
    }

}