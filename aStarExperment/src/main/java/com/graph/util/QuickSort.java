package com.graph.util;

import java.util.Comparator;
import java.util.List;

/**
 * @Author: yhj
 * @Description:
 * @Date: Created in 2018/11/2.
 */
public class QuickSort {
    public static <T> T listKth(List<T> list, int k, Comparator<T> cp){
        T[] arr = (T[])list.toArray();
        return arrKth(arr, k, cp);
    }

    public static <T> T arrKth(T[] arr, int k, Comparator<T> cp){
        if(arr == null || arr.length <= 0){
            return null;
        }
        if(k <= 0 || k > arr.length){
            return null;
        }
        k--;
        int start = 0, end = arr.length-1;
        int partition = partition(arr, start, end, cp);
        while (partition != k){
            if(partition > k){
                end = partition - 1;
            }else {
                start = partition + 1;
            }
            partition = partition(arr, start, end, cp);
        }
        return arr[k];
    }

    private static <T> int partition(T[] arr, int start, int end, Comparator<T> cp){
        T tmp = arr[start];
        while (start < end){
            while (start < end && cp.compare(arr[end], tmp) >= 0){
                end--;
            }
            arr[start] = arr[end];
            while (start < end && cp.compare(arr[start], tmp) < 0){
                start ++;
            }
            arr[end] = arr[start];
        }
        arr[start] = tmp;
        return start;
    }

    public static void main(String[] args) {
        Double[] list =new Double[]{2.0, 1.797352494195635, 1.4997637295700896, 1.4997637295700896, 1.353240681623923, 1.353240681623923, 1.353240681623923, 1.4997637295700896, 1.4997637295700896};
        double a = QuickSort.arrKth(list, 1, Comparator.reverseOrder());
        System.out.println(a);
    }
}
