package com.graph.query;

import java.io.IOException;
import java.util.*;

/**
 * @Author: yhj
 * @Description:
 * @Date: Created in 2018/8/9.
 */

public class Graph<T, K> {
    VexNode<T>[] vexNodes;                 //存放所有结点的数组
    int vexnum, arcnum;                 //图的当前结点数量和边的数量
    boolean isDirected;                 //是否是有向图

    /**
     * 头结点类
     */
    class VexNode<T> {
        T data;                         //结点信息
        ArcNode firstarc;               //第一条邻接点的位置
        VexNode(T data, ArcNode firstarc){
            this.data = data;
            this.firstarc = firstarc;
        }

        VexNode(T data){
            this(data, null);
        }

        private Iterator<ArcNode> iterator(){
            return new Itr();
        }

        private class Itr implements Iterator<ArcNode>{
            private ArcNode pre;
            private ArcNode current;
            Itr(){
                current = firstarc;
            }
            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            @SuppressWarnings("unchecked")
            public ArcNode next() {
                if(current == null){
                    throw new NoSuchElementException();
                }
                pre = current;
                current = current.next;
                return pre;
            }
        }
    }

    /**
     * 邻接点类K
     */
    public class ArcNode<K> {
        int adjvex;                     //该邻接点在数组中的位置
        ArcNode next;                   //指向下一个邻接点
        K edgeInfo;                     //边的信息，存储边的名
        ArcNode(int adjvex, K edgeInfo, ArcNode next){
            this.adjvex = adjvex;
            this.next = next;
            this.edgeInfo = edgeInfo;
        }

        ArcNode(int adjvex, K edgeInfo){
            this(adjvex, edgeInfo, null);
        }

        public int getAdjvex() {
            return adjvex;
        }

        public K getEdgeInfo() {
            return edgeInfo;
        }
    }

    Graph(){
        this(true);
    }

    Graph(boolean isDirected){
        this.isDirected = isDirected;
    }

    /**
     * 创建图
     * @param vexs
     * @param edges
     * @param edgesInfo
     */
    public void createGraph(T[] vexs, T[][] edges, K[] edgesInfo){
        createVexNode(vexs);
        creeateArcNode(edges, edgesInfo);
    }

    public void createGraph(Collection<T> vexs, Collection<T[]> edges, Collection<K> edgesInfo){
        createVexNode(vexs);
        System.out.println("create node over");
        creeateArcNode(edges, edgesInfo);
    }

    protected void createVexNode(Collection<T> vexs){
        vexNodes = new VexNode[vexs.size()];
        int i = 0;
        for(T vex: vexs){
            vexNodes[i] = new VexNode<>(vex);
            i++;
        }
    }

    protected void createVexNode(T[] vexs){
        vexNodes = new VexNode[vexs.length];
        for(int i = 0; i < vexs.length; i++){
            vexNodes[i] = new VexNode<>(vexs[i]);
        }
    }

    protected void creeateArcNode(Collection<T[]> edges, Collection<K> edgesInfo){
        if(edges.size() != edgesInfo.size()){
            throw new IllegalArgumentException("edge's length not equal edgeInfo's length");
        }
        Iterator<T[]> edgeIterator = edges.iterator();
        Iterator<K> edgeInfoIterator = edgesInfo.iterator();
        while (edgeIterator.hasNext() && edgeInfoIterator.hasNext()){
            insertArc(edgeIterator.next(), edgeInfoIterator.next());
        }
    }

    protected void creeateArcNode(T[][] edges, K[] edgesInfo){
        if(edges.length != edgesInfo.length){
            throw new IllegalArgumentException("edge's length not equal edgeInfo's length");
        }
        for(int i = 0; i < edges.length; i++){
            insertArc(edges[i], edgesInfo[i]);
        }
    }

    public T getNodeData(int index){
        return vexNodes[index].data;
    }

    /**
     * 得到两个节点之间的边的信息
     * @param fromIndex
     * @param toIndex
     * @return
     */
    public K getEdgeInfo(int fromIndex, int toIndex){
        Iterator<ArcNode> iterator = vexNodes[fromIndex].iterator();
        while (iterator.hasNext()){
            ArcNode<K> arcNode = iterator.next();
            if(arcNode.getAdjvex() == toIndex){
                return arcNode.getEdgeInfo();
            }
        }
        return null;
    }

    /**
     * 得到结点的位置
     * @param t
     * @return
     */
    protected int getVex(T t){
        for(int i = 0; i < vexNodes.length; i++){
            if(vexNodes[i].data.equals(t)){
                return i;
            }
        }
        return -1;
    }

    /**
     * 插入一个邻接点
     * @param edge
     * @param edgeInfo 边的信息
     */
    private void insertArc(T[] edge, K  edgeInfo){
        int start = getVex(edge[0]);
        int end = getVex(edge[1]);
        /**当有相同边要插入时，进行判断*/
//        Iterator<ArcNode> iteratorStart = iterator(start);
//        Set<Integer> startSet = new HashSet<>();
//        while (iteratorStart.hasNext()) {
//            startSet.add(iteratorStart.next().getAdjvex());
//        }

        if(start == -1 || end == -1){
            throw new NoSuchElementException("no such node: " + edge[0] + " " + edge[1]);
        }
        //拒绝插入自回路
        if(start == end){
            return;
        }
        //单向插入，插入到链表头
        ArcNode<K> newArcNode = new ArcNode(end,edgeInfo);
        ArcNode p = vexNodes[start].firstarc;
        vexNodes[start].firstarc = newArcNode;
        newArcNode.next = p;

        //双向插入
        if(!isDirected){
            newArcNode = new ArcNode(start,edgeInfo);
            p = vexNodes[end].firstarc;
            vexNodes[end].firstarc = newArcNode;
            newArcNode.next = p;
        }

//        //单向插入，插入到链表头
//        if (!startSet.contains(end)) {
//            ArcNode<K> newArcNode = new ArcNode(end,edgeInfo);
//            ArcNode p = vexNodes[start].firstarc;
//            vexNodes[start].firstarc = newArcNode;
//            newArcNode.next = p;
//
//            //双向插入
//            if(!isDirected){
//                newArcNode = new ArcNode(start,edgeInfo);
//                p = vexNodes[end].firstarc;
//                vexNodes[end].firstarc = newArcNode;
//                newArcNode.next = p;
//            }
//        }
    }

    public void BFS(int start){
        Set<Integer> visitNodes = new HashSet<>();  //存储遍历过的节点
        Set<Integer> currentNodes = new HashSet<>(); //存储当前层的节点
        visitNodes.add(start);
        currentNodes.add(start);
        while (!currentNodes.isEmpty()){
            Set<Integer> nextNodes = new HashSet<>();
            for(int currentIndex: currentNodes){
                ArcNode p = vexNodes[currentIndex].firstarc;
                while (p != null){
                    if(!visitNodes.contains(p.adjvex)){
                        nextNodes.add(p.adjvex);
                        visitNodes.add(p.adjvex);
                    }
                    p = p.next;
                }
            }
            currentNodes = nextNodes;
//            System.out.println(nextNodes);
        }
    }

    public void show(){
        for(VexNode vexNode: vexNodes){
            System.out.print(vexNode.data);
            ArcNode p = vexNode.firstarc;
            while (p != null){
                System.out.print("===" + p.edgeInfo + "===>" + vexNodes[p.adjvex].data);
                p = p.next;
            }
            System.out.println();
        }
    }
    public static void main(String[] args) throws IOException{
        Graph<String, Double> graph = new Graph<>(false);
        String[] nodes = {"v1", "v2", "v3", "v4", "v5", "v6", "v7", "v8", "v9"};
        String[][] edges = {{"v1", "v2"},
                            {"v1", "v3"},
                            {"v1", "v4"},
                            {"v2", "v5"},
                            {"v3", "v6"},
                            {"v4", "v6"},
                            {"v6", "v7"},
                            {"v8", "v5"},
                            {"v8", "v7"},
                            {"v8", "v9"}};
        Double[] predicates = {0.9, 0.8, 0.7 , 0.4, 0.4, 0.5, 0.5, 0.5, 0.9, 0.3};
        graph.createGraph(nodes, edges, predicates);
//        graph.show();
        graph.BFS(5);
    }

    public Iterator<ArcNode> iterator(int start){
        return  vexNodes[start].iterator();
    }

}
