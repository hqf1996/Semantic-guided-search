package com.graph.query;

/**
 * @Author: yhj
 * @Description:
 * @Date: Created in 2018/8/10.
 */
public class Entity {
    private String id;
    private String name;
    private String type;
    private double value;   //节点访问概率
    private int layer;  //层数

    public Entity(String id, String name, String type){
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public Entity(String id, String name, String type, double value) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public Entity(String id, String name, String type, double value, int layer) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.value = value;
        this.layer = layer;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public int getLayer() {
        return layer;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Entity){
            return ((Entity)obj).id.equals(this.id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public String toString() {
        return getId() + ":" + getName() + ":" + getType();
    }
}
