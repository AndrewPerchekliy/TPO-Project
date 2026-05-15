/**
 *
 *  @author Percheklii Andrii s33232
 *
 */

package zad1;


import java.io.*;
import java.net.*;
import java.time.LocalTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {
    private int port;
    private ServerSocket serverSocket;
    private Thread serverThread;
    private Map<Socket, PrintWriter> clients = new ConcurrentHashMap<>();
    private StringBuilder log = new StringBuilder();
    private volatile boolean running = false;
    private final Object lock = new Object();

    public ChatServer(int port) {
        this.port = port;
    }

    public void startServer() {
        running = true;
        System.out.println("Server started");
        serverThread = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                while (running) {
                    Socket clientSocket = serverSocket.accept();
                    Thread.ofVirtual().start(() -> handleClient(clientSocket));
                }
            } catch (IOException e) {
            }
        });
        serverThread.start();
    }

    public void stopServer() {
        running = false;
        System.out.println("Server stopped");

        synchronized (lock) {
            LocalTime now = LocalTime.now();
            log.append(now.toString()).append(" ChatServer: chat closed\n");
            for (PrintWriter out : clients.values()) {
                out.println("ChatServer: chat closed");
            }
        }

        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
        }

        for (Socket s : clients.keySet()) {
            try {
                s.close();
            } catch (IOException e) {
            }
        }
    }

    public String getServerLog() {
        return log.toString();
    }

    private void handleClient(Socket socket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            clients.put(socket, out);
            String line;
            String clientId = "Unknown";

            while ((line = in.readLine()) != null) {
                if (line.startsWith("LOGIN ")) {
                    clientId = line.substring(6);
                    addLogAndBroadcast(clientId + " logged in");
                } else if (line.equals("LOGOUT")) {
                    addLogAndBroadcast(clientId + " logged out");
                    break;
                } else {
                    addLogAndBroadcast(clientId + ": " + line);
                }
            }
        } catch (IOException e) {
        } finally {
            clients.remove(socket);
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
    }

    private void addLogAndBroadcast(String event) {
        synchronized (lock) {
            LocalTime now = LocalTime.now();
            log.append(now.toString()).append(" ").append(event).append("\n");
            for (PrintWriter out : clients.values()) {
                out.println(event);
            }
        }
    }
}