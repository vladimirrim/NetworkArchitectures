package ru.spbau.egorov.net_arch.network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Server {
    protected String hostName;
    protected int port;
    protected volatile boolean isRunning = true;

    public abstract void start();

    public void shutdown() {
        isRunning = false;
    }

    public ArrayList<Integer> sort(List<Integer> array) {
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
