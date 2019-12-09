package com.graph.read;

/**
 * Created by yhj on 2018/5/30.
 * 对应数据库的RDF信息
 */
public class EdgeSource extends Object{
    private int id;
    private String entity_id1;
    private String entity_id2;
    private String entity_name1;
    private String predicate_name;
    private String entity_name2;

    public EdgeSource(){}

    public EdgeSource(String entity_id1, String entity_id2,
                      String entity_name1, String predicate_name,
                      String entity_name2){
        this.entity_id1 = entity_id1;
        this.entity_id2 = entity_id2;
        this.entity_name1 = entity_name1;
        this.entity_name2 = entity_name2;
        this.predicate_name = predicate_name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEntity_id1() {
        return entity_id1;
    }

    public void setEntity_id1(String entity_id1) {
        this.entity_id1 = entity_id1;
    }

    public String getEntity_id2() {
        return entity_id2;
    }

    public void setEntity_id2(String entity_id2) {
        this.entity_id2 = entity_id2;
    }

    public String getEntity_name1() {
        return entity_name1;
    }

    public void setEntity_name1(String entity_nam1) {
        this.entity_name1 = entity_nam1;
    }

    public String getPredicate_name() {
        return predicate_name;
    }

    public void setPredicate_name(String predicate_name) {
        this.predicate_name = predicate_name;
    }

    public String getEntity_name2() {
        return entity_name2;
    }

    public void setEntity_name2(String entity_name2) {
        this.entity_name2 = entity_name2;
    }

    @Override
    public boolean equals(Object object) {
        if(object instanceof EdgeSource){
            EdgeSource edgeSource = (EdgeSource)object;
            if(this.getEntity_id1().equals(edgeSource.getEntity_id1())
                    && this.getEntity_id2().equals(edgeSource.getEntity_id2())
                    && this.getPredicate_name().equals(edgeSource.getPredicate_name())){
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (getEntity_id1() + " " + getEntity_id2() + " " + getPredicate_name()).hashCode();
    }

    @Override
    public String toString() {
//        return entity_id1 + " " + entity_id2 + " " + entity_name1 + " " + predicate_name + " " + entity_name2;
        return entity_id1 + "\t" + entity_id2 + "\t" + entity_name1 + "\t" + predicate_name + "\t" + entity_name2;
    }

    public String toString(String s) {
        return entity_id1 + s + entity_id2 + s + entity_name1 + s + predicate_name + s + entity_name2;
    }
}
