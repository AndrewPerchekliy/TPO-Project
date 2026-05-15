/**
 *
 *  @author Percheklii Andrii s33232
 *
 */

package zad1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ChatServer {
    private String host;
    private int port;
    private Selector selector;
    private ServerSocketChannel serverSocket;
    private Thread serverThread;
    private volatile boolean isRunning = false;
    private StringBuilder serverLog = new StringBuilder();

    private Map<SocketChannel, String> clientIds = new HashMap<>();
    private Map<SocketChannel, StringBuilder> buffers = new HashMap<>();
    private Set<SocketChannel> clients = new HashSet<>();

    public ChatServer(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void startServer() {
        try {
            selector = Selector.open();
            serverSocket = ServerSocketChannel.open();
            serverSocket.bind(new InetSocketAddress(host, port));
            serverSocket.configureBlocking(false);
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);
            isRunning = true;
            System.out.println("Server started\n");

            serverThread = new Thread(() -> {
                try {
                    while (isRunning) {
                        selector.select();
                        if (!isRunning) break;

                        Set<SelectionKey> keys = selector.selectedKeys();
                        Iterator<SelectionKey> iter = keys.iterator();
                        while (iter.hasNext()) {
                            SelectionKey key = iter.next();
                            iter.remove();

                            if (key.isAcceptable()) {
                                SocketChannel client = serverSocket.accept();
                                client.configureBlocking(false);
                                client.register(selector, SelectionKey.OP_READ);
                                clients.add(client);
                                buffers.put(client, new StringBuilder());
                            } else if (key.isReadable()) {
                                SocketChannel client = (SocketChannel) key.channel();
                                ByteBuffer buffer = ByteBuffer.allocate(1024);
                                int bytesRead;
                                try {
                                    bytesRead = client.read(buffer);
                                } catch (IOException e) {
                                    bytesRead = -1;
                                }

                                if (bytesRead == -1) {
                                    client.close();
                                    key.cancel();
                                    clients.remove(client);
                                    buffers.remove(client);
                                    clientIds.remove(client);
                                    continue;
                                }

                                buffer.flip();
                                byte[] bytes = new byte[buffer.limit()];
                                buffer.get(bytes);
                                String chunk = new String(bytes, StandardCharsets.UTF_8);

                                StringBuilder partial = buffers.get(client);
                                partial.append(chunk);

                                int newlineIdx;
                                while ((newlineIdx = partial.indexOf("\n")) != -1) {
                                    String line = partial.substring(0, newlineIdx);
                                    partial.delete(0, newlineIdx + 1);
                                    processCommand(client, line);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            serverThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processCommand(SocketChannel client, String line) {
        String[] parts = line.split("\t");
        if (parts.length < 2) return;

        String id = parts[0];
        String type = parts[1];
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
        String broadcastMsg = null;

        if ("LOGIN".equals(type)) {
            clientIds.put(client, id);
            broadcastMsg = id + " logged in";
        } else if ("LOGOUT".equals(type)) {
            broadcastMsg = id + " logged out";
        } else if ("MSG".equals(type)) {
            if (parts.length >= 3) {
                broadcastMsg = id + ": " + parts[2];
            }
        }

        if (broadcastMsg != null) {
            String logEntry = time + " " + broadcastMsg;
            synchronized (serverLog) {
                serverLog.append(logEntry).append("\n");
            }
            broadcast(broadcastMsg + "\n");
        }
    }

    private void broadcast(String msg) {
        byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
        for (SocketChannel client : clients) {
            if (clientIds.containsKey(client)) {
                try {
                    ByteBuffer buf = ByteBuffer.wrap(bytes);
                    while(buf.hasRemaining()) {
                        client.write(buf);
                    }
                } catch (IOException e) {
                }
            }
        }
    }

    public void stopServer() {
        try {
            isRunning = false;
            if (selector != null) selector.wakeup();
            if (serverThread != null) serverThread.join();
            if (serverSocket != null) serverSocket.close();
            System.out.println("Server stopped");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getServerLog() {
        return serverLog.toString();
    }
}