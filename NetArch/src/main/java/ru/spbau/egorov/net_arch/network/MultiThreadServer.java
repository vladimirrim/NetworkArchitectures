package ru.spbau.egorov.net_arch.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

import static ru.spbau.egorov.net_arch.network.NetworkArray.Array.parseDelimitedFrom;
import static ru.spbau.egorov.net_arch.network.NetworkArray.Array.parseFrom;

public class MultiThreadServer implements Server {

    private String hostName;
    private int port;
    private volatile boolean isRunning = true;

    public MultiThreadServer(String hostname, int port) {
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
        try (ServerSocket serverSocket = new ServerSocket(port,0,addr)) {
            InetAddress address = serverSocket.getInetAddress();
            hostName = address.getHostName();
            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                Runnable worker = () -> {
                    try (DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());
                         DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream())) {
                        NetworkArray.Array array = parseDelimitedFrom(inputStream);
                        NetworkArray.Array.Builder builder = NetworkArray.Array.newBuilder();
                        List<Integer> listArray = array.getArrayList();
                        NetworkArray.Array sortedArray = builder.addAllArray(sort(listArray)).build();
                        sortedArray.writeDelimitedTo(outputStream);
                    } catch (IOException  e) {
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
