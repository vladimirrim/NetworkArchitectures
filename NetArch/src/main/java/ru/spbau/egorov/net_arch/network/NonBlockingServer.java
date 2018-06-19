package ru.spbau.egorov.net_arch.network;

import com.google.protobuf.InvalidProtocolBufferException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NonBlockingServer extends Server {

    private final static int MAX_SIZE = 1_000_000;
    private final ConcurrentLinkedQueue<ClientBuffers> clientBuffersReadQueue = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<ClientBuffers> clientBuffersWriteQueue = new ConcurrentLinkedQueue<>();
    private Selector readSelector;
    private Selector writeSelector;
    private ExecutorService threadPool = Executors.newFixedThreadPool(4);

    NonBlockingServer(String hostname, int port) {
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

        try {
            readSelector = Selector.open();
            writeSelector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Thread readThread = new Thread(new ReaderThread());
        readThread.setDaemon(true);
        readThread.start();
        Thread writeThread = new Thread(new WriterThread());
        writeThread.setDaemon(true);
        writeThread.start();
        try (ServerSocketChannel serverChannel = ServerSocketChannel.open()) {
            serverChannel.socket().bind(new InetSocketAddress(addr, port));
            serverChannel.configureBlocking(false);
            while (isRunning) {
                SocketChannel clientChannel = serverChannel.accept();
                if (clientChannel != null) {
                    clientChannel.configureBlocking(false);
                    synchronized (clientBuffersReadQueue) {
                        clientBuffersReadQueue.add(new ClientBuffers(clientChannel, System.currentTimeMillis()));
                        readSelector.wakeup();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ReaderThread implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    synchronized (clientBuffersReadQueue) {
                        while (!clientBuffersReadQueue.isEmpty()) {
                            ClientBuffers client = clientBuffersReadQueue.poll();
                            client.clientChannel.register(readSelector, SelectionKey.OP_READ, client);
                        }
                    }
                    readSelector.select();
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }
                Set<SelectionKey> keys = readSelector.selectedKeys();
                Iterator<SelectionKey> it = keys.iterator();
                while (it.hasNext()) {
                    SelectionKey curKey = it.next();
                    ClientBuffers buffers = (ClientBuffers) curKey.attachment();
                    buffers.read();
                    if (buffers.isReadingDone)
                        curKey.cancel();
                    it.remove();
                }
            }
        }
    }

    private class WriterThread implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    synchronized (clientBuffersWriteQueue) {
                        while (!clientBuffersWriteQueue.isEmpty()) {
                            ClientBuffers client = clientBuffersWriteQueue.poll();
                            client.clientChannel.register(writeSelector, SelectionKey.OP_WRITE, client);
                        }
                    }
                    writeSelector.select();
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }

                Set<SelectionKey> keys = writeSelector.selectedKeys();
                Iterator<SelectionKey> it = keys.iterator();
                while (it.hasNext()) {
                    SelectionKey curKey = it.next();
                    ClientBuffers buffers = (ClientBuffers) curKey.attachment();
                    buffers.write();
                    if (buffers.isWritingDone)
                        curKey.cancel();
                    it.remove();
                }
            }
        }
    }

    private class ClientBuffers {
        private int readBufSize = -1;
        private long serverProcessingTimeStart = -1;
        private long serverProcessingPrevTimeStart = -1;
        private int remainingRequests = -1;

        private ByteBuffer readBuf = ByteBuffer.allocate(MAX_SIZE);
        private ByteBuffer writeBuf = ByteBuffer.allocate(MAX_SIZE);

        private boolean isReadingDone = false;
        private boolean isWritingDone = false;

        private SocketChannel clientChannel;

        private ClientBuffers(SocketChannel clientChannel, long serverProcessingTimeStart) {
            this.serverProcessingTimeStart = serverProcessingTimeStart;
            this.clientChannel = clientChannel;
        }

        private void read() {
            try {
                if (serverProcessingTimeStart == -1) {
                    serverProcessingTimeStart = System.currentTimeMillis();
                }

                int cnt = 1;
                while (cnt > 0)
                    cnt = clientChannel.read(readBuf);

                readBuf.flip();

                if (remainingRequests == -1) {
                    if (readBuf.remaining() > 3) {
                        remainingRequests = readBuf.getInt();
                    } else {
                        readBuf.compact();
                        return;
                    }
                }

                if (readBufSize == -1) {
                    if (readBuf.remaining() > 3) {
                        readBufSize = readBuf.getInt();
                    } else {
                        readBuf.compact();
                        return;
                    }
                }
                if (readBufSize != -1) {
                    if (readBuf.remaining() >= readBufSize) {
                        byte arr[] = new byte[readBufSize];
                        readBuf.get(arr, 0, readBufSize);
                        serverProcessingPrevTimeStart = serverProcessingTimeStart;
                        serverProcessingTimeStart = -1;
                        readBufSize = -1;
                        if (--remainingRequests == 0)
                            isReadingDone = true;
                        threadPool.submit(() -> {
                            try {
                                NetworkArray.Array array = NetworkArray.Array.parseFrom(arr);
                                NetworkArray.Array.Builder builder = NetworkArray.Array.newBuilder();
                                long sortingTimeStart = System.currentTimeMillis();
                                builder.addAllArray(sort(array.getArrayList()));
                                int sortingTime = (int) (System.currentTimeMillis() - sortingTimeStart);
                                NetworkArray.Array sortedArray = builder.build();
                                writeBuf.putInt(sortedArray.toByteArray().length);
                                writeBuf.put(sortedArray.toByteArray());
                                writeBuf.putInt(sortingTime);
                                synchronized (clientBuffersReadQueue) {
                                    clientBuffersWriteQueue.add(ClientBuffers.this);
                                    writeSelector.wakeup();
                                }
                            } catch (InvalidProtocolBufferException e) {
                                e.printStackTrace();
                            }
                        });
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            readBuf.compact();
        }

        private void write() {
            writeBuf.flip();
            while (writeBuf.hasRemaining()) {
                try {
                    clientChannel.write(writeBuf);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (!writeBuf.hasRemaining()) {
                writeBuf.compact();
                writeBuf.putInt((int) (System.currentTimeMillis() - serverProcessingPrevTimeStart));
                writeBuf.flip();
                try {
                    clientChannel.write(writeBuf);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                isWritingDone = true;
            }
            writeBuf.compact();
        }
    }
}
