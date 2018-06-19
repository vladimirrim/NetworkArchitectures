package ru.spbau.egorov.net_arch.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static ru.spbau.egorov.net_arch.network.NetworkArray.Array.parseFrom;

public class MultiThreadWriteThreadServer extends Server {

    MultiThreadWriteThreadServer(String hostname, int port) {
        this.hostName = hostname;
        this.port = port;
    }

    private final ExecutorService threadPool = Executors.newFixedThreadPool(4);

    public void start() {
        InetAddress addr;
        try {
            addr = InetAddress.getByName(hostName);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return;
        }
        try (ServerSocket serverSocket = new ServerSocket(port, 0, addr)) {
            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                Runnable worker = () -> {
                    try {
                        DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());
                        DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());
                        int requestsCount = inputStream.readInt();
                        for (int request = 0; request < requestsCount; request++) {
                            int arrSize = inputStream.readInt();
                            byte[] arr = new byte[arrSize];
                            inputStream.readFully(arr);
                            long serverProcessingTimeStart = System.currentTimeMillis();
                            NetworkArray.Array array = parseFrom(arr);
                            threadPool.submit(() -> {
                                        NetworkArray.Array.Builder builder = NetworkArray.Array.newBuilder();
                                        List<Integer> listArray = array.getArrayList();
                                        long sortingTimeStart = System.currentTimeMillis();
                                        NetworkArray.Array sortedArray = builder.addAllArray(sort(listArray)).build();
                                        int sortingTime = (int) (System.currentTimeMillis() - sortingTimeStart);
                                        ExecutorService writeThread = Executors.newSingleThreadExecutor();
                                        writeThread.submit(() -> {
                                            try {
                                                outputStream.writeInt(sortedArray.getSerializedSize());
                                                sortedArray.writeTo(outputStream);
                                                outputStream.writeInt(sortingTime);
                                                outputStream.writeInt((int) (System.currentTimeMillis() - serverProcessingTimeStart));
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        });
                                    }
                            );
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                };
                new Thread(worker).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
