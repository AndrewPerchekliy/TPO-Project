/**
 *
 *  @author Percheklii Andrii s33232
 *
 */

package zad1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class ChatClient {
    private String host;
    private int port;
    private String id;
    private SocketChannel channel;
    private StringBuilder chatView = new StringBuilder();
    private volatile boolean isRunning = true;
    private Thread readerThread;

    public ChatClient(String host, int port, String id) {
        this.host = host;
        this.port = port;
        this.id = id;
        this.chatView.append("=== ").append(id).append(" chat view\n");
    }

    public void login() {
        try {
            channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.connect(new InetSocketAddress(host, port));
            while (!channel.finishConnect()) {
                Thread.sleep(5);
            }

            readerThread = new Thread(() -> {
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                StringBuilder partial = new StringBuilder();
                try {
                    while (isRunning) {
                        int read = channel.read(buffer);
                        if (read > 0) {
                            buffer.flip();
                            byte[] bytes = new byte[buffer.limit()];
                            buffer.get(bytes);
                            partial.append(new String(bytes, StandardCharsets.UTF_8));
                            buffer.clear();

                            int newlineIdx;
                            while ((newlineIdx = partial.indexOf("\n")) != -1) {
                                String line = partial.substring(0, newlineIdx);
                                chatView.append(line).append("\n");
                                partial.delete(0, newlineIdx + 1);
                            }
                        } else if (read == -1) {
                            break;
                        } else {
                            Thread.sleep(10);
                        }
                    }
                } catch (Exception e) {
                    if (isRunning) {
                        chatView.append("*** ").append(e.toString()).append("\n");
                    }
                }
            });
            readerThread.start();

            sendReq(id + "\tLOGIN");
        } catch (Exception e) {
            chatView.append("*** ").append(e.toString()).append("\n");
        }
    }

    public void logout() {
        sendReq(id + "\tLOGOUT");

        long start = System.currentTimeMillis();
        while (isRunning && System.currentTimeMillis() - start < 1500) {
            if (chatView.indexOf(id + " logged out") != -1) {
                break;
            }
            try { Thread.sleep(10); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
        isRunning = false;
        try {
            if (channel != null) channel.close();
        } catch (IOException e) {

        }
    }

    public void send(String req) {
        sendReq(id + "\tMSG\t" + req);
    }

    private void sendReq(String msg) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap((msg + "\n").getBytes(StandardCharsets.UTF_8));
            while (buffer.hasRemaining()) {
                channel.write(buffer);
            }
        } catch (Exception e) {
            chatView.append("*** ").append(e.toString()).append("\n");
        }
    }

    public String getChatView() {
        return chatView.toString();
    }
}