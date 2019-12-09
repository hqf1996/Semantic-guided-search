package com.graph.util;

/**
 * @Author: yhj
 * @Description:
 * @Date: Created in 2019/1/18.
 */
public class TwoTuple<A, B> {
    public final A first;
    public final B second;

    public TwoTuple(A a, B b) {
        this.first = a;
        this.second = b;
    }

    public A getFirst() {
        return first;
    }

    public B getSecond() {
        return second;
    }
}