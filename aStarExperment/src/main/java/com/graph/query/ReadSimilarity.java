package com.graph.query;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: yhj
 * @Description:
 * @Date: Created in 2018/9/2.
 */
public abstract class ReadSimilarity<K, V> {
    protected Map<K, V> map = new HashMap<>();
    protected String filePath;
    protected K center;
    public ReadSimilarity(String filePath, K center){
        this.filePath = filePath;
        this.center = center;
        read();
    }

    protected abstract void read();

    public Map<K, V> getMap() {
        return map;
    }

    public V getSimilarity(K object){
        return map.get(object);
    }
}
