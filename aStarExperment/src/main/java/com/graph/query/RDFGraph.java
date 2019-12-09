package com.graph.query;

import java.util.*;

/**
 * @Author: yhj
 * @Description:
 * @Date: Created in 2018/8/10.
 */
public class RDFGraph extends Graph<Entity, List<String>> {
    Map<String, List<Integer>> vexTypeIndex;    //按照类型来建立的结点的索引
    Map<Entity, Integer> vexIndex;              //头结点位置的索引

    public Map<Entity, Integer> getVexIndex() {
        return vexIndex;
    }

    public void setVexIndex(Map<Entity, Integer> vexIndex) {
        this.vexIndex = vexIndex;
    }

    public RDFGraph(){
        this(true);
    }

    public RDFGraph(boolean isDirected){
        super(isDirected);
        vexTypeIndex = new HashMap<>();
        vexIndex = new HashMap<>();
    }

    public Map<String, List<Integer>> getVexTypeIndex() {
        return vexTypeIndex;
    }

    @Override
    protected void createVexNode(Entity[] vexs){
        vexNodes = new VexNode[vexs.length];
        for(int i = 0; i < vexs.length; i++){
            vexNodes[i] = new VexNode<Entity>(vexs[i]);
            //加入根据type类型建立索引的过程
            List<Integer> indexs = vexTypeIndex.get(vexs[i].getType());
            if(indexs == null){
                vexTypeIndex.put(vexs[i].getType(), new ArrayList<>(Arrays.asList(i)));
            }else {
                indexs.add(i);
            }
            vexIndex.put(vexs[i], i);
        }
    }

    @Override
    protected void createVexNode(Collection<Entity> vexs){
        vexNodes = new VexNode[vexs.size()];
        int  i = 0;
        for(Entity vex: vexs){
            vexNodes[i] = new VexNode<Entity>(vex);
            //加入根据type类型建立索引的过程
            List<Integer> indexs = vexTypeIndex.get(vex.getType());
            if(indexs == null){
                vexTypeIndex.put(vex.getType(), new ArrayList<>(Arrays.asList(i)));
            }else {
                indexs.add(i);
            }
            vexIndex.put(vex, i);
            i++;
        }
    }

    /**
     * 根据索引来查找头结点的位置
     * @param t
     * @return
     */
    @Override
    protected int getVex(Entity t){
        Integer index = vexIndex.get(t);
        if(index != null){
            return index;
        }
        System.out.println(t.getId());
        return -1;
    }

    /***
     * 选择某个节点的任意一个邻接点的序号
     */
    public int outNearNodesRandom(int start){
        if (vexNodes[start].firstarc == null){
            System.out.println("No near Nodes!");
            return -1;
        } else{
            //存储所有的邻接点
            List<ArcNode> arcNodes = new ArrayList<>();
            ArcNode cur = vexNodes[start].firstarc;
            while (cur != null){
                arcNodes.add(cur);
                cur = cur.next;
            }
            Random random = new Random();
            //生成一个[0, arcNodes.size())的随机数
            int i = random.nextInt(arcNodes.size());
            return arcNodes.get(i).adjvex;
        }
    }

    public int getVex(String vexId) {
        return  getVex(new Entity(vexId, null, null));
    }
}
