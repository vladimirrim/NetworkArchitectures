package ru.spbau.egorov.net_arch;

import ru.spbau.egorov.net_arch.network.NetworkArray;
import ru.spbau.egorov.net_arch.network.Server;
import ru.spbau.egorov.net_arch.network.ServerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

import static java.lang.Thread.sleep;

public class MainApp {
    public static void main(String[] args) throws InterruptedException {
        Server server = ServerFactory.newMultiThreadServer("127.0.0.1", 8888);
        new Thread(server::start).start();
        sleep(200);
        try (Socket socket = new Socket("127.0.0.1", 8888);
             DataInputStream inputStream = new DataInputStream(socket.getInputStream());
             DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream())) {
            List<Integer>  array = Arrays.asList(4,3,2,8,9,7);
            NetworkArray.Array.Builder builder = NetworkArray.Array.newBuilder();
            builder.addAllArray(array).build().writeDelimitedTo(outputStream);
            NetworkArray.Array sortedArray = NetworkArray.Array.parseDelimitedFrom(inputStream);
            for(int i=0;i<sortedArray.getArrayList().size();i++)
                System.out.println(sortedArray.getArray(i));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
