package ru.spbau.egorov.net_arch.network;

public abstract class ServerFactory {
    static public Server newMultiThreadServer(String hostName, int port){
        return new MultiThreadServer(hostName, port);
    }
}
