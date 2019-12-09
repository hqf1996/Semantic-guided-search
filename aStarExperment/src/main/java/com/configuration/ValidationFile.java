package com.configuration;

/**
 * @Author: yhj
 * @Description:
 * @Date: Created in 2018/10/26.
 */
public enum ValidationFile {
    NEO4J_GERMANY("D:\\JavaProject\\aStarExperment\\result\\validation\\neo4j\\automobile\\germany.txt", 1);

    private String name;
    private int index;
    // 构造方法
    private ValidationFile(String name, int index) {
        this.name = name;
        this.index = index;
    }

    // 普通方法
    public static String getName(int index) {
        for (ValidationFile c : ValidationFile.values()) {
            if (c.getIndex() == index) {
                return c.name;
            }
        }
        return null;
    }

    // get set 方法
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
