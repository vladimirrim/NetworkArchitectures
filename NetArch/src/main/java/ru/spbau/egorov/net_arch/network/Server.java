package ru.spbau.egorov.net_arch.network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public interface Server {
    void start();

    void shutdown();

    default ArrayList<Integer> sort(List<Integer> array) {
        ArrayList<Integer> sortedArray = new ArrayList<>(array);
        for (int i = 0; i < sortedArray.size(); i++) {
            for (int j = i + 1; j < sortedArray.size(); j++) {
                if (sortedArray.get(i) > sortedArray.get(j)) {
                    Collections.swap(sortedArray, i, j);
                }
            }
        }
        return sortedArray;
    }
}
