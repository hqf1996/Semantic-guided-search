package com.graph.query;

import java.util.List;

/**
 * @Author: yhj
 * @Description:
 * @Date: Created in 2019/1/21.
 */
public class AStarPathResult {
    String entity;
    String entityId;
    double pss;
    List<List<String>> paths;

    public AStarPathResult(String entity, List<List<String>> paths){
        this.entity = entity;
        this.paths = paths;
    }

    public AStarPathResult(String entity, double pss, List<List<String>> paths) {
        this.entity = entity;
        this.pss = pss;
        this.paths = paths;
    }

    public AStarPathResult(String entity, String entityId, double pss, List<List<String>> paths) {
        this.entity = entity;
        this.entityId = entityId;
        this.pss = pss;
        this.paths = paths;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public double getPss() {
        return pss;
    }

    public void setPss(double pss) {
        this.pss = pss;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public List<List<String>> getPaths() {
        return paths;
    }

    public void setPaths(List<List<String>> paths) {
        this.paths = paths;
    }
}
