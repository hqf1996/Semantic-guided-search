package com.graph.query;

import java.util.List;

/**
 * @Author: yhj
 * @Description:
 * @Date: Created in 2019/1/9.
 */
public class AStarResult {
    List<AStarPathResult> pathResults;
    List<AStarPathModel> pathModels;

    long time;
    double Accuracy;            //准确率
    double Recall;           //召回率
    double F1;

    public AStarResult(List<AStarPathResult> pathResults, List<AStarPathModel> pathModels){
        this.pathResults = pathResults;
        this.pathModels = pathModels;
    }

    public AStarResult(List<AStarPathResult> pathResults, List<AStarPathModel> pathModels, double accuracy, double recall, double f1) {
        this.pathResults = pathResults;
        this.pathModels = pathModels;
        this.Accuracy = accuracy;
        this.Recall = recall;
        this.F1 = f1;
    }

    public AStarResult(List<AStarPathResult> pathResults, List<AStarPathModel> pathModels, int time){
        this(pathResults, pathModels);
        this.time = time;
    }

    public AStarResult(List<AStarPathResult> pathResults, List<AStarPathModel> pathModels, long time, double accuracy, double recall, double f1) {
        this(pathResults, pathModels, accuracy, recall, f1);
        this.time = time;

    }

    public List<AStarPathResult> getPathResults() {
        return pathResults;
    }

    public void setPathResults(List<AStarPathResult> pathResults) {
        this.pathResults = pathResults;
    }

    public List<AStarPathModel> getPathModels() {
        return pathModels;
    }

    public void setPathModels(List<AStarPathModel> pathModels) {
        this.pathModels = pathModels;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public double getAccuracy() {
        return Accuracy;
    }

    public void setAccuracy(double accuracy) {
        Accuracy = accuracy;
    }

    public double getRecall() {
        return Recall;
    }

    public void setRecall(double recall) {
        Recall = recall;
    }

    public double getF1() {
        return F1;
    }

    public void setF1(double f1) {
        F1 = f1;
    }
}
