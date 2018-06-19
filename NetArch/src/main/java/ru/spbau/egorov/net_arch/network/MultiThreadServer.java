package ru.spbau.egorov.net_arch.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

import static ru.spbau.egorov.net_arch.network.NetworkArray.Array.parseFrom;

public class MultiThreadServer extends Server {

    MultiThreadServer(String hostname, int port) {
        this.hostName = hostname;
        this.port = port;
    }

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
                        DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());
                        DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());
                        int requestsCount = inputStream.readInt();
                        for (int request = 0; request < requestsCount; request++) {
                            int arrSize = inputStream.readInt();
                            byte[] arr = new byte[arrSize];
                            inputStream.readFully(arr);
                            NetworkArray.Array array = parseFrom(arr);
                            long processingTimeStart = System.currentTimeMillis();
                            NetworkArray.Array.Builder builder = NetworkArray.Array.newBuilder();
                            List<Integer> listArray = array.getArrayList();
                            long sortingTimeStart = System.currentTimeMillis();
                            NetworkArray.Array sortedArray = builder.addAllArray(sort(listArray)).build();
                            int sortingTime = (int) (System.currentTimeMillis() - sortingTimeStart);
                            outputStream.writeInt(sortedArray.getSerializedSize());
                            sortedArray.writeTo(outputStream);
                            outputStream.writeInt(sortingTime);
                            outputStream.writeInt((int) (System.currentTimeMillis() - processingTimeStart));
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

    public void shutdown() {
        isRunning = false;
    }
}
