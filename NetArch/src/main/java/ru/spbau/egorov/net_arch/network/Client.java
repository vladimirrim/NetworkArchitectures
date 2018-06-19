package ru.spbau.egorov.net_arch.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static java.lang.Thread.sleep;

public class Client {
    private int port;
    private String hostName;
    private long serverProcessingTime = 0;
    private long serverSortingTime = 0;
    private long clientProcessingTime = 0;

    public Client(String hostName, int port) {
        this.hostName = hostName;
        this.port = port;
    }

    public long getClientProcessingTime() {
        return clientProcessingTime;
    }

    public long getServerProcessingTime() {
        return serverProcessingTime;
    }

    public long getServerSortingTime() {
        return serverSortingTime;
    }

    public void sendArray(int size, int requestCount, int requestDelay) {
        try {
            Socket socket = new Socket(hostName, port);
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.writeInt(requestCount);
            for (int request = 0; request < requestCount; request++) {
                long clientTimeStart = System.currentTimeMillis();
                Random rand = new Random();
                List<Integer> array = new LinkedList<>();
                for (int i = 0; i < size; i++)
                    array.add(rand.nextInt(1000));
                NetworkArray.Array.Builder builder = NetworkArray.Array.newBuilder();
                NetworkArray.Array netArray = builder.addAllArray(array).build();
                outputStream.writeInt(netArray.getSerializedSize());
                outputStream.write(netArray.toByteArray());
                int ansSize = inputStream.readInt();
                byte[] ans = new byte[ansSize];
                inputStream.readFully(ans);
                NetworkArray.Array sortedArray = NetworkArray.Array.parseFrom(ans);
                if (sortedArray.getArrayList().size() != size) {
                    System.out.println("Incorrect array size");
                    throw new IllegalStateException("Incorrect array size");
                }
                for (int i = 0; i < sortedArray.getArrayList().size() - 1; i++)
                    if (sortedArray.getArray(i) > sortedArray.getArray(i + 1)) {
                        System.out.println("Unsorted array");
                        throw new IllegalStateException("Unsorted array");
                    }
                clientProcessingTime += System.currentTimeMillis() - clientTimeStart;
                serverSortingTime += inputStream.readInt();
                serverProcessingTime += inputStream.readInt();
                try {
                    sleep(requestDelay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
