package ru.spbau.egorov.net_arch.network;

public abstract class ServerFactory {
    static public Server newMultiThreadServer(String hostName, int port) {
        return new MultiThreadServer(hostName, port);
    }

    static public Server newNonBlockingServer(String hostName, int port) {
        return new NonBlockingServer(hostName, port);
    }

    static public Server newMultiThreadWriteThreadServer(String hostName, int port) {
        return new MultiThreadWriteThreadServer(hostName, port);
    }
}
