/**
 *
 *  @author Percheklii Andrii s33232
 *
 */

package zad1;

import java.io.*;
import java.net.*;

public class ChatClient {
    private String host;
    private int port;
    private String id;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private StringBuilder chatView = new StringBuilder();
    private Thread readerThread;

    public ChatClient(String host, int port, String id) {
        this.host = host;
        this.port = port;
        this.id = id;
    }

    public void login() {
        try {
            socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            readerThread = new Thread(() -> {
                try {
                    String line;
                    while ((line = in.readLine()) != null) {
                        chatView.append(line).append("\n");
                    }
                } catch (IOException e) {
                }
            });
            readerThread.start();

            send("LOGIN " + id);
        } catch (IOException e) {
        }
    }

    public void logout() {
        send("LOGOUT");
    }

    public void send(String req) {
        if (out != null) {
            out.println(req);
        }
    }

    public String getChatView() {
        return chatView.toString();
    }

    public String getId() {
        return id;
    }
}