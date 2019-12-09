package com.graph.query;

import com.graph.util.QuickSort;
import com.graph.util.TwoTuple;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @Author: yhj
 * @Description:
 * @Date: Created in 2018/9/17.
 */
public class AStarQueryNew extends AbstractQuery {
//    Logger logger = LoggerFactory.getLogger(AStarQuery.class);     // 定义类的日志

    List<AStarTask> tasks = new ArrayList<>();                     //存放线程的列表
    ExecutorService exec = Executors.newCachedThreadPool();        //线程池,存放AStarTask
    PriorityNode[][] taskResults;                                  //存放线程结果，具体维度在run中根据线程数决定
    CountDownLatch latch;                                          //计数器的指为tasks中线程数
    List<BoundNode> results;                                       //存放各个线程执行的结果
    List<Map<Integer, Integer>> traversalNodeMaps;                 //存储所有的线程遍历过的节点以及在线程结果列表中的位置
    final int K;                                                   //开方次数,在计算F时使用
    double limitFactor = 1.2;                                      //在寻找实体共同实体的最大上限
    final int limitPath = 50000;                                   //每个线程最大寻找路径数20000
    AtomicInteger maxPath = new AtomicInteger(0);      //线程最大的个数结果
    boolean isThreadFail = false;

    /**
     * 路径类
     * 存放路径点以及之间的谓词
     */
    class Path implements Iterable<TwoTuple<Integer, String>>{
        private int start;
        private Set<Integer> nodes;         //顺序记录路径的点
        private List<String> predicates;    //顺序记录路径之间的谓词

        public Path(int node){
            this.start = node;
            this.nodes = new LinkedHashSet<>();
            this.predicates = new ArrayList<>();
        }

        public Path(Path path){
            this.start = path.getStart();
            this.nodes = new LinkedHashSet<>(path.getNodes());
            this.predicates = new ArrayList<>(path.getPredicates());
        }

        public int size(){
            return nodes.size();
        }

        public int getStart() {
            return start;
        }

        public Set<Integer> getNodes() {
            return Collections.unmodifiableSet(nodes);
        }

        public List<String> getPredicates() {
            return Collections.unmodifiableList(predicates);
        }

        public void add(int node, String predicate){
            nodes.add(node);
            predicates.add(predicate);
        }

        private class Itr implements Iterator<TwoTuple<Integer, String>>{
            Iterator<Integer> nodesIterator = nodes.iterator();
            Iterator<String> predicaetsIterator = predicates.iterator();

            @Override
            public boolean hasNext() {
                return nodesIterator.hasNext() && predicaetsIterator.hasNext();
            }

            @Override
            public TwoTuple<Integer, String> next() {
                return new TwoTuple<>(nodesIterator.next(), predicaetsIterator.next());
            }
        }

        @Override
        public Iterator<TwoTuple<Integer, String>> iterator() {
            return new Itr();
        }

        /**
         * 判断是否该点已经在路径中
         */
        public boolean contains(int node){
            return nodes.contains(node);
        }
    }

    /**
     * A*算法中队列中的元素
     * 记录遍历到的实体id
     * 以及相应的g,h，f的值
     * 以及遍历到该实体的路径
     */
    class PriorityNode {
        int id;                  //节点id
        double g;
        double h;
        double f;
        //        Set<Integer> path;       //最早到达该点的路径
//      List<String> predicates; //记录最早的路径之间的谓词
        Path path;

        public PriorityNode(int id, double g, double h) {
            this.id = id;
            this.h = h;
            this.g = g;
            this.f = Math.pow(g * h, 1.0 / AStarQueryNew.this.K);
            this.path = new Path(id);
        }

//        public PriorityNode(int id, double g, double h, int preNodeIndex, String predicate) {
//            this(id, g, h);
//            this.path.add(preNodeIndex, predicate);
//        }

//        public PriorityNode(int id, double g, double h, PriorityNode preNode) {
//            this(id, g, h, preNode.path, preNode.getId());
//        }
//
//        public PriorityNode(int id, double g, double h, Set<Integer> prePath, int preNodeIndex) {
//            this.id = id;
//            this.g = g;
//            this.h = h;
//            if (g == 0.0 || h == 0.0) {
//                f = 0.0;
//            } else {
//                this.f = Math.pow(g * h, 1.0 / AStarQuery.this.K);
//            }
//            this.path = new LinkedHashSet<>(prePath);
//            this.path.add(preNodeIndex);
//        }

        public PriorityNode(int id, double g, double h, PriorityNode preNode, String predicate) {
            this.id = id;
            this.g = g;
            this.h = h;
            if (g == 0.0 || h == 0.0) {
                f = 0.0;
            } else {
                this.f = Math.pow(g * h, 1.0 / AStarQueryNew.this.K);
//                System.out.println(f);
            }
            this.path = new Path(preNode.getPath());
            path.add(this.id, predicate);
        }

        public int getId() {
            return id;
        }

        public double getG() {
            return g;
        }

        public double getH() {
            return h;
        }

        public double getF() {
            return f;
        }

        public Path getPath() {
            return path;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof PriorityNode) {
                return ((PriorityNode) obj).id == this.id;
            }
            return false;
        }
    }

    class AStarTask implements Runnable {
        int start;
        int taskId;
        String predicate;
        Map<String, Double> similarMap;                 //谓词之间的相似度
        List<PriorityNode> result;                      //存储A*寻找的节点的PriorityNode
        Map<Integer, Integer> threadTraversalSet;       //防止结果出现重复点,用于寻找topK个点,以及记录在result的index

        AStarTask(QueryThreadInfo info, int taskId) {
//            synchronized (this){
//                long start = System.currentTimeMillis();
//                this.start = graph.getVex(info.getSource());
//                System.out.println("寻找索引时间：" + (System.currentTimeMillis() - start));
//            }
            this.start = graph.getVex(info.getSource());
            this.taskId = taskId;
            this.predicate = info.getPredicate();
            this.similarMap = info.getSimilarMap();
            result = new ArrayList<>();
            threadTraversalSet = traversalNodeMaps.get(taskId);
        }

        /**
         * 得到谓词相似度
         *
         * @param edgeInfo
         * @return
         */
        public double getPredicateSim(String edgeInfo) {
            if (similarMap.containsKey(edgeInfo))
                return similarMap.get(edgeInfo);
            else
                return 0.0;
        }

        @Override
        public void run() {
            try {
                process();
            }catch (Exception e){
//                logger.error("get a Exception on query-thread: " + Thread.currentThread().getName() + ": " + BaseService.logExceptionStack(e));
                isThreadFail = true;
            }finally {
                latch.countDown();
            }
        }

        /**
         * 线程运行的主要内容
         */
        private void process(){
            //增加路径长度的判断条件
            Queue<PriorityNode> priorityQueue = new PriorityQueue<>(Comparator.comparing(PriorityNode::getF).reversed().thenComparing(p -> p.getPath().size()));

            //寻找topK条路径
            int countPath = 0;                                  //记录查询过的路径条数
            priorityQueue.offer(new PriorityNode(start, 1, 1));
//            priorityQueue.offer(new PriorityNode(graph.getVex("3997066"), 1, 1));

            outer:
            while (!priorityQueue.isEmpty()) {
                PriorityNode priorityNode = priorityQueue.poll();
//                System.out.println(priorityNode.getF());
//                if (checkType(graph.getNodeData(priorityNode.getId()).getType())) {
//                    if (!threadTraversalSet.containsKey(priorityNode.getId())) {
//                    // 存放节点Id和元素存放在result列表的位置
//                        threadTraversalSet.put(priorityNode.getId(), result.size());
//                        result.add(priorityNode);
//                        if (isFindEnoughNode()) {
////                        logger.info(Thread.currentThread().getName() + " find enough node, then quit...");
//                            break outer;
//                        }
//                    }
//                    continue;
//                }

                Iterator<Graph.ArcNode> itr = (Iterator<Graph.ArcNode>) graph.iterator(priorityNode.getId());
                while (itr.hasNext()) {
                    Graph.ArcNode arcNode = itr.next();
                    // 防止循环路径
                    if (priorityNode.path.contains(arcNode.adjvex)) {
                        continue;
                    }
                    int index = arcNode.adjvex;
                    TwoTuple<String, Double> maxLinkSim = getMaxLinkEdgeSim((List<String>) arcNode.getEdgeInfo());
                    double g = priorityNode.getG() * maxLinkSim.getSecond();
//                    System.out.println(maxLinkSim.getFirst()+ g);
                    double h = getMaxNodeEdgeSim(index, priorityNode.getId());
                    PriorityNode newNode = new PriorityNode(index, g, h, priorityNode, maxLinkSim.getFirst());
                    //节点的属性是否符合要求，符合加入结果集，否则加入优先级队列
                    if (checkType(graph.getNodeData(newNode.getId()).getType())) {
                        //这个if判断是为了防止重复点，如果是对路径取topK,这个需要去掉
                        if (!threadTraversalSet.containsKey(newNode.getId())) {
                            // 存放节点Id和元素存放在result列表的位置
                            threadTraversalSet.put(newNode.getId(), result.size());
                            result.add(newNode);
                            //要求找到足够多的点并且优先级队列中的pss值没有超过当前结果集中的pss
                            if (isFindEnoughNode()){
//                                System.out.println(countPath);
                                break outer;
//                                if (isNoPssMore(priorityQueue, result)){
//                                    break outer;
//                                }
                            }
                        }
                    } else {
                        priorityQueue.add(newNode);
                    }
//                    priorityQueue.add(newNode);
                    //如果遍历的路径条数大于极限值
                    if (++countPath >= limitPath) {
                        System.out.println(countPath);
//                        logger.info(Thread.currentThread().getName() + " count path > limit path, then quit...");
                        break outer;
                    }
                }
            }

            //按照实际路径值从大到小排序
            for (PriorityNode p : result) {
                if (p.getG() > 0.0) {
//                    System.out.println(p.id);
//                    System.out.println((p.getPath().size() - 1));
                    p.g = Math.pow(p.getG(), 1.0 / (p.getPath().size()));
                } else {
                    p.g = 0.0;
                }
            }
            result.sort(Comparator.comparing(PriorityNode::getG).reversed());
            for (int i = 0; i < result.size(); i++) {
                taskResults[i][taskId] = result.get(i);
            }
            // 赋值最大路径数
            maxPath.getAndUpdate(x -> x > result.size() ? x : result.size());
        }

        /**
        * @Author: hqf
        * @Date:
        * @Description: 判断优先级队列中没有pss值超过当前结果集中的pss的点了，
         * 也就是优先级队列中的最大值要小于所有results集合中的pss值才满足情况
        */
        private boolean isNoPssMore(Queue<PriorityNode> priorityQueue, List<PriorityNode> results){
            double maxQueue = 0.0;
            double minResults = 99999.0;
            // 当前优先级队列中最大的那个数
            for (PriorityNode priorityNode : priorityQueue) {
                double CurPss = Math.pow(priorityNode.getG(), 1.0 / (priorityNode.getPath().size()));
                if (CurPss > maxQueue){
                    maxQueue = CurPss;
                }
            }
            //当前结果集中最小的那个数
            for (PriorityNode priorityNode : results){
                double CurPss = Math.pow(priorityNode.getG(), 1.0 / (priorityNode.getPath().size()));
                if (CurPss < minResults){
                    minResults = CurPss;
                }
            }
            if (maxQueue < minResults){
                return true;
            }else
                return false;
        }

        /**
         * 获得该点下一跳的最大边的相似度
         *
         * @param nodeIndex
         * @param preNodeInex
         * @return
         */
        private double getMaxNodeEdgeSim(int nodeIndex, int preNodeInex) {
            double h = 0;
            Iterator<Graph.ArcNode> nextIterator = (Iterator<Graph.ArcNode>) graph.iterator(nodeIndex);
            while (nextIterator.hasNext()) {
                Graph.ArcNode nextArcNode = nextIterator.next();
                if (nextArcNode.adjvex == preNodeInex) {
                    continue;
                }
                double tmp = getMaxLinkEdgeSim((List<String>) nextArcNode.edgeInfo).getSecond();
                if (h < tmp) {
                    h = tmp;
                }
            }
            return h;
        }

        /**
         * 获得两点直连的最大边相似度
         * @param edgeInfos
         * @return
         */
        private TwoTuple<String, Double> getMaxLinkEdgeSim(List<String> edgeInfos) {
//            OptionalDouble result = edgeInfos.parallelStream().mapToDouble(engeInfo -> getPredicateSim(engeInfo)).max();
            Optional<TwoTuple<String, Double>> result = edgeInfos.parallelStream()
                    .map(edgeInfo -> new TwoTuple<>(edgeInfo, getPredicateSim(edgeInfo)))
                    .max(Comparator.comparing(TwoTuple::getSecond));
            if (result.isPresent()) {
                return result.get();
            } else {
                throw new IllegalArgumentException();
            }
        }

    }

    public AStarQueryNew(RDFGraph graph, List<QueryThreadInfo> queryThreadInfos, String type, int topN, final int K) {
        super(graph, queryThreadInfos, type, topN);
        this.K = K;
        if (queryThreadInfos.size() == 1) {
            limitFactor = 1.0;
        }
        traversalNodeMaps = new ArrayList<>(queryThreadInfos.size());
        int i = 0;
        for (QueryThreadInfo info : queryThreadInfos) {
            traversalNodeMaps.add(new ConcurrentHashMap<>());
            tasks.add(new AStarTask(info, i++));
        }
        latch = new CountDownLatch(tasks.size());
    }

    @Override
    public void run(){
        int n = limitPath;
        taskResults = new PriorityNode[n][tasks.size()];
        long startTime = System.currentTimeMillis();
        for (AStarTask task : tasks) {
            exec.execute(task);
        }
        exec.shutdown();

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        long endTime = System.currentTimeMillis();
        // 统计线程遍历的时间
//        logger.info("thread consume time " + (endTime - startTime) + "ms");
        startTime = System.currentTimeMillis();
//        showResult(taskResults);
        /*
        showResult(taskResults);    //显示所有线程返回的结果
        combineTopK(taskResults);
        */
        combineNodeTopK(taskResults);
        endTime = System.currentTimeMillis();
        // 统计TA算法的时间
//        logger.info("TA consume time " + (endTime - startTime) + "ms");
    }

    class BoundNode {
        int isFind;                         //是否在多个属性都找到,int共32位，每一位中1代表找到，否则没有
        int nodeIndex;                      //节点id
        PriorityNode[] certain;             //选定的具体点的信息
        double[] bound = new double[2];     //存储上下限,0 lowBound 1 upperBound

        BoundNode(boolean isFind, int nodeIdex, int lines) {
            this.nodeIndex = nodeIdex;
            certain = new PriorityNode[lines];

            this.isFind = 0;
            if (isFind) {
                for (int i = 0; i < lines; i++) {
                    setIsFind(i);
                }
            }
        }

        BoundNode(int isFind, int nodeIdex, PriorityNode[] certain, double[] bound) {
            this.nodeIndex = nodeIdex;
            this.certain = certain;
            this.bound = bound;
            this.isFind = isFind;
        }

        public void setIsFind(int i) {
            if (i >= 32) {
                throw new IllegalArgumentException("out of 31");
            }
            isFind |= (1 << i);
        }

        BoundNode(int nodeIdex, PriorityNode[] certain, double[] bound) {
            this(0, nodeIdex, certain, bound);
        }

        BoundNode(int nodeIdex, int lines) {
            this(false, nodeIdex, lines);
        }

        public double getUpperBound(PriorityNode[] nodes) {
            double upperBound = 0;
            for (int i = 0; i < nodes.length; i++) {
                if (isFind(i)) {
                    upperBound += certain[i].getG();
                } else {
                    if (nodes[i] != null)
                        upperBound += nodes[i].getG();
                }
            }
            return upperBound;
        }

        public boolean isFind() {
            return isFind == ((1 << certain.length) - 1);
        }

        public boolean isFind(int i) {
            if (i >= 32) {
                throw new IllegalArgumentException("out of 31");
            }
            return !(0 == (isFind & (1 << i)));
        }

        public int getNodeIdex() {
            return nodeIndex;
        }

        /**
         * 根据输入的一行更新自身的上下限
         *
         * @param nodes
         * @return
         */
        private List<Integer> updateBpundInter(PriorityNode[] nodes) {
            bound[0] = 0;
            bound[1] = 0;
            List<Integer> result = new ArrayList<>();
            for (int i = 0; i < nodes.length; i++) {
                PriorityNode node = nodes[i];
                if (certain[i] == null) { //未在该列找到A的值
                    if (node.getId() == nodeIndex) {  //输入的该列为A,添加
                        bound[0] += node.getG();
                        certain[i] = node;
                        setIsFind(i);
                    } else {
                        bound[1] += node.getG();
                    }
                } else {  //已经在该列找到A的值
                    bound[0] += certain[i].getG();
                    if (node.getId() == nodeIndex) {   //输入的该列为A,需要为后续
                        result.add(i);
                    }
                }
            }

            bound[1] += bound[0];
            return result;
        }

        /**
         * 根据一行的输入更新上下限并输出扩展出的结果
         *
         * @param nodes
         * @return
         */
        public List<BoundNode> updateBound(PriorityNode[] nodes) {
            List<Integer> updateIndex = updateBpundInter(nodes);
            List<BoundNode> list = new ArrayList<>();
            list.add(this);
            for (int each : updateIndex) {
                List<BoundNode> next = new ArrayList<>();
                for (BoundNode boundNode : list) {
                    PriorityNode[] tmp = Arrays.copyOf(boundNode.certain, boundNode.certain.length);
                    tmp[each] = nodes[each];
                    double chavalue = boundNode.certain[each].getG() - nodes[each].getG();
                    next.add(
                            new BoundNode(boundNode.isFind, boundNode.nodeIndex, tmp,
                                    new double[]{boundNode.bound[0] - chavalue, boundNode.bound[1] - chavalue}));
                }
                list.addAll(next);
            }
            list.remove(this);
            return list;
        }

        public double getLowBound() {
            return bound[0];
        }

        public double getUpperBound() {
            return bound[1];
        }
    }

    /**
     * topK路径综合排序
     *
     * @param target
     */
    private void combineTopK(PriorityNode[][] target) {
        Set<Integer> visit = new HashSet<>();
        results = new ArrayList<>();
//        int n = topN*4;
        int n = topN;
        for (int i = 0; i < target.length; i++) {
            double unknowBound = 0;
            for (int j = 0; j < target[i].length; j++) {
                unknowBound += target[i][j].getG();
                if (!visit.contains(target[i][j].getId())) {     //如果出现未访问过的点，创建一个新BoundNode
                    results.add(new BoundNode(false, target[i][j].getId(), target[i].length));
                    visit.add(target[i][j].getId());
                }
            }

            //更新上下限并添加产生的新输出，最后按照下限排序
            List<BoundNode> tmp = new ArrayList<>();
            for (BoundNode boundNode : results) {
                List<BoundNode> addNodes = boundNode.updateBound(target[i]);
                if (!addNodes.isEmpty()) {
                    tmp.addAll(addNodes);
                }
            }
            results.addAll(tmp);
            results.sort(Comparator.comparing(BoundNode::getLowBound).reversed());

            if (results.size() < n) {    //如果列表个数不足排序，继续
                continue;
            }
            //得出topK的位置，和topK中的最小下限
            int m = n;
            double topKLowBound = results.get(m - 1).getLowBound();
            if (results.get(m - 1).isFind()) {
                for (; m < results.size(); m++) {
                    if (!results.get(m).isFind() || results.get(m).getUpperBound() != topKLowBound) {
                        break;
                    }
                }
            }

            //判断其他的上限是否有大于topK下限的情况，即判断是否可以结束
            boolean flag = topKLowBound > unknowBound;
            for (int j = m; j < results.size() && flag; j++) {
                if (results.get(j).getUpperBound() > topKLowBound) {
                    flag = false;
                }
            }
            if (flag) {
                n = m;
                break;
            }
        }

        System.out.println("==============================topN result==============================");
        results = results.stream().filter(BoundNode::isFind).collect(Collectors.toList()); //过滤没有同时连接到多端的结果
        results.stream().limit(n).forEach(each -> System.out.println(graph.getNodeData(each.nodeIndex) + " : " + each.getLowBound() + ": " + each.getUpperBound() + each.isFind() + ", "));
        System.out.println();
        System.out.println(results.size());
    }

    /**
     * 针对topK个点综合排序
     *
     * @param target
     */
    private void combineNodeTopK(PriorityNode[][] target) {
        Map<Integer, BoundNode> map = new HashMap<>();
        results = new ArrayList<>();
        int lines = tasks.size();
        out:
        for (int i = 0; i < maxPath.get(); i++) {
            for (int j = 0; j < lines; j++) {
                if (target[i][j] == null) {
                    continue;
                }
                BoundNode tmp = map.get(target[i][j].getId());
                if (tmp == null) {
                    tmp = new BoundNode(target[i][j].getId(), lines);
                    map.put(target[i][j].getId(), tmp);
                }
                tmp.certain[j] = target[i][j];
                tmp.setIsFind(j);
                if (tmp.isFind()) {
                    double tmpBound = 0;
                    for (PriorityNode p : tmp.certain) {
                        tmpBound += p.getG();
                    }
                    tmp.bound[0] = tmp.bound[1] = tmpBound;
                    results.add(tmp);
                    //遍历多个再做一次TA，节省按照lowBound排序和计算UpperBound的时间
                    if (results.size() % topN == 0) {
                        Double[] arr = new Double[results.size()];
                        int k = 0;
                        for (BoundNode b : results) {
                            arr[k++] = b.getLowBound();
                        }

                        double minK = QuickSort.arrKth(arr, topN, Comparator.reverseOrder());
                        boolean flag = true;
                        //判断没有满足条件的是否upperbound是否都比其minK小
                        for (BoundNode b : map.values()) {
                            if (!b.isFind()) {
                                if (b.getUpperBound(target[i]) > minK) {
                                    flag = false;
                                    break;
                                }
                            }
                        }
                        if (flag) {
                            break out;
                        }
                    }
                }

            }
        }
//        System.out.println("==============================topN result==============================");
        results.sort(Comparator.comparing(BoundNode::getLowBound).reversed());
//        results.stream().limit(topN).forEach(each -> System.out.println(graph.getNodeData(each.nodeIndex) + " : " + each.getLowBound() + ": " + each.getUpperBound() + each.isFind() + ", "));
//        System.out.println();
//        System.out.println(results.size());
    }

    private void showResult(PriorityNode[][] results) {
        Map<Double, Integer> countMap = new HashMap<>();
        for (PriorityNode[] result : results) {
            for (PriorityNode priorityNode : result) {
                if (priorityNode != null) {
                    StringBuilder s = new StringBuilder();
                    s.append("[");
                    for (int pathNode : priorityNode.getPath().getNodes()) {
                        s.append(graph.getNodeData(pathNode));
                        s.append(", ");
                    }
                    s.delete(s.length() - 2, s.length());
                    s.append("]");
                    s.append(priorityNode.getId());
                    s.append("  : ");
                    s.append(priorityNode.getG());
                    /**
                    * @Author: hqf
                    * @Date:
                    * @Description: 统计各个pss值的数量
                    */
//                    if (countMap.get(priorityNode.getG()) != null){
//                        countMap.put(priorityNode.getG(), countMap.get(priorityNode.getG()) + 1);
//                    }else{
//                        countMap.put(priorityNode.getG(), 1);
//                    }
                    System.out.println(s);
                }
            }
        }
        /** 统计各个pss值的数量*/
//        for (Map.Entry<Double, Integer> entry : countMap.entrySet()){
//            System.out.println(entry.getKey() + ":" + entry.getValue());
//        }
    }

    public List<BoundNode> getResults() {
        return results;
    }

    /**
     * 组装成返回给前端的对象
     * @return
     */
    public AStarResult getAStarResult() {
        List<AStarPathResult> pathResults = new ArrayList<>();
        int count = 0;
        Map<String, Integer> pathModelMap = new HashMap();
        for (BoundNode each : results) {
            List<List<String>> paths = new ArrayList<>();
            for (PriorityNode priorityNode : each.certain) {
                Entity node = graph.getNodeData(priorityNode.getPath().getStart());
                // 存储路径
                List<String> path = new ArrayList<>();
                // 存储路径模式
                StringBuilder model = new StringBuilder(node.getType());
                path.add(node.getName());
                for (TwoTuple<Integer, String> nodeAndPredicate : priorityNode.getPath()) {
                    node = graph.getNodeData(nodeAndPredicate.getFirst());
                    // 路径拼装
                    path.add(nodeAndPredicate.getSecond());
                    path.add(node.getName());
                    // 路径模式拼装
                    model.append("\t");
                    model.append(nodeAndPredicate.getSecond());
                    model.append("\t");
                    model.append(node.getType());
                }
                paths.add(path);
                pathModelMap.merge(model.toString(), 1, (oldValue, value) -> oldValue+1);
            }

//            pathResults.add(new AStarPathResult(graph.getNodeData(each.getNodeIdex()).getName(), paths));
            pathResults.add(new AStarPathResult(graph.getNodeData(each.getNodeIdex()).getName(), graph.getNodeData(each.getNodeIdex()).getId(), each.getUpperBound(), paths));

            if (++count >= topN) {
                break;
            }
        }
        // 按照路径模式出现频率排序
        List<AStarPathModel> pathModels = new ArrayList<>();
        for(Map.Entry<String, Integer> entry: pathModelMap.entrySet()){
            pathModels.add(new AStarPathModel(Arrays.asList(entry.getKey().split("\t")), entry.getValue()));
        }
        pathModels.sort(Comparator.comparing(AStarPathModel::getCount).reversed());
        return new AStarResult(pathResults, pathModels);
    }

    /**
     * 判断是否找到足够多的节点
     *
     * @return
     */
    private boolean isFindEnoughNode() {
        // 先复制出来
        Set<Integer> interSet = new HashSet<>(traversalNodeMaps.get(0).keySet());
        for (int i = 1; i < traversalNodeMaps.size(); i++) {
            interSet.retainAll(traversalNodeMaps.get(i).keySet());
        }
        return interSet.size() >= limitFactor * topN;
    }

    /**
     * 评估结果，计算准确率和召回率
     */
    public static void evaluation(AStarQueryNew aStarQuery, Collection<String> validation) throws IOException {
        int topN = aStarQuery.topN;
        List<BoundNode> testSet = new ArrayList<>();
        Set<Integer> filterSet = new HashSet<>();
        for (BoundNode boundNode : aStarQuery.getResults()) {
            if (!filterSet.contains(boundNode.getNodeIdex())) {
                testSet.add(boundNode);
                filterSet.add(boundNode.getNodeIdex());
            }
            if (filterSet.size() == topN) {
                break;
            }
        }
        if (filterSet.size() < topN) {
            throw new IllegalStateException(filterSet.size() + " elements to sort is less top k : " + topN);
        }
        Set<BoundNode> set = new LinkedHashSet<>();
        int TP = 0;
        int TPMore = 0;
//        writeBoundNodes(aStarQuery.graph, testSet, topN, "E:\\JavaProjects\\rdf_conputer\\result\\Query.Automobile\\compare\\astar_all.txt");
        for (int i = 0; i < topN; i++) {
            BoundNode each = testSet.get(i);
            boolean flag = true;
            for (String v : validation) {
                String name = aStarQuery.graph.getNodeData(each.getNodeIdex()).getId();
                if (v.equals(name)) {
                    TP++;
                    flag = false;
                    break;
                }
            }
        }
        for (BoundNode each : set) {
//            System.out.println(aStarQuery.graph.getNodeData(each.getNodeIdex()));
//            each.certain[0].getPath().forEach(index -> System.out.print("-->" + aStarQuery.graph.getNodeData(index)));
//            System.out.println();
        }
        double P = (TP + TPMore) * 1.0 / testSet.size();
        double R = (TP + TPMore) * 1.0 / (validation.size() + TPMore);
        System.out.println("TP: " + TP + "\tTPMore: " + TPMore + "\tN: " + topN + "\tM: " + validation.size());
        System.out.println((TP + TPMore) + "/" + topN + "     " + (TP + TPMore) + "/" + (validation.size() + TPMore));
        System.out.println("Precision : " + P + "\n" + "recall : " + R);
    }

    /**
     * 将BoundNode集合写入到文件
     */
    public static void writeBoundNodes(RDFGraph graph, Collection<BoundNode> collection, int maxNumber, String path) throws IOException {
        final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path)));
        int count = 0;
        for (BoundNode each : collection) {
            writer.write(graph.getNodeData(each.getNodeIdex()).toString());
            writer.write("[[");
            each.certain[0].getPath().getNodes().forEach(index -> {
                try {
                    writer.write("-->" + graph.getNodeData(index));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            writer.write("]]");
            writer.write("[" + each.getLowBound() + "," + each.getUpperBound() + "]");
            writer.newLine();
            if (count++ == maxNumber) {
                break;
            }
        }
        writer.close();
    }

    /**
     * 获取路径上的谓词顺序
     * @param graph
     * @param source
     * @return
     */
    private static MultiSourcePredicatesPaths getPredicates(RDFGraph graph, BoundNode source) {
        MultiSourcePredicatesPaths result = new MultiSourcePredicatesPaths();
        for (PriorityNode paths : source.certain) {
            List<List<String>> predicatePath = new ArrayList<>();
            List<Integer> list = new ArrayList<>(paths.getPath().getNodes());
            for (int i = list.size() - 1; i > 0; i--) {
                predicatePath.add(graph.getEdgeInfo(list.get(i), list.get(i - 1)));
            }
            result.addPredicatesPaths(list.get(0), predicatePath);
        }
        return result;
    }


    public static void main(String[] args) {
//        PriorityNode[][] result = new PriorityNode[][]{
//                {new PriorityNode(1, 1.0, 1.0), new PriorityNode(2, 0.8, 0.8),new PriorityNode(4,0.8, 0.8)},
//                {new PriorityNode(3, 0.8, 0.8), new PriorityNode(3, 0.7, 0.7),new PriorityNode(3,0.6, 0.6)},
//                {new PriorityNode(3, 0.5, 0.5), new PriorityNode(3, 0.3, 0.3),new PriorityNode(1,0.2, 0.2)},
//                {new PriorityNode(4, 0.3, 0.3), new PriorityNode(4, 0.2, 0.2),new PriorityNode(5,0.1, 0.1)},
//                {new PriorityNode(5, 0.1, 0.1), new PriorityNode(5, 0.1, 0.1),new PriorityNode(2,0.0, 0.0)}
//        };
//
//        AStarQuery aStarQuery = new AStarQuery(new RDFGraph(false), new HashMap<String, Double>(), "da", 10, 8);
//        aStarQuery.showResult(result);
//        aStarQuery.combineTopK(result);

//        BoundNode boundNode = new BoundNode(2,3);
//        boundNode.setIsFind(0);
//        boundNode.setIsFind(1);
////        boundNode.setIsFind(2);
//        System.out.println(boundNode.isFind());

    }

}