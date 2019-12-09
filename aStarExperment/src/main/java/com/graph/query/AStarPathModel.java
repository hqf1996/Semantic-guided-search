package com.graph.query;

import java.util.List;

/**
 * @Author: yhj
 * @Description: 路径的模式
 * @Date: Created in 2019/1/21.
 */
public class AStarPathModel {
    List<String> model;
    int count;
    int qald = 0;
    double simValue;

    public AStarPathModel(List<String> model, int count){
        this.model = model;
        this.count = count;
    }

    public List<String> getModel() {
        return model;
    }

    public AStarPathModel(List<String> model, int count, int qald) {
        this.model = model;
        this.count = count;
        this.qald = qald;
    }

    public void setModel(List<String> model) {
        this.model = model;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getQald() {
        return qald;
    }

    public void setQald(int qald) {
        this.qald = qald;
    }


    public double getSimValue() {
        return simValue;
    }

    public void setSimValue(double simValue) {
        this.simValue = simValue;
    }

    @Override
    public int hashCode() {
        return model.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof AStarPathModel){
            return this.model.equals(((AStarPathModel)obj).model);
        }
        return false;
    }
}
