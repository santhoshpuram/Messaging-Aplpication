import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Server implements Runnable {
    private ServerSocket serverSocket;
    private volatile boolean running = true;
    private final Map<Integer, Socket> clients = new ConcurrentHashMap<>();
    private final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();

    public Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    @Override
    public void run() {
        System.out.println("Server is running...");
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                int clientPort = clientSocket.getPort();
                clients.put(clientPort, clientSocket);

                System.out.println("Node connected: " + clientPort);
                new Thread(new ClientHandler(clientSocket, this)).start();

            } catch (IOException e) {
                if (!running) {
                    System.out.println("Server shutting down...");
                } else {
                    e.printStackTrace();
                }
            }
        }
    }

    public void addMessage(String message) {
        messageQueue.add(message);
    }

    public void forwardMessages() {
        while (!messageQueue.isEmpty()) {
            String message = messageQueue.poll();
            for (Socket client : clients.values()) {
                try {
                    PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                    out.println(message);
                } catch (IOException e) {
//                    e.printStackTrace();
                }
            }
            // Check if the message contains "shutdown"
            if (message.trim().equalsIgnoreCase("shutdown")) {
                shutdown();  // Trigger server shutdown
            }
        }
    }

    public void shutdown() {
        running = false;
        try {
            for (Socket client : clients.values()) {
                client.close();  // Close all client connections
            }
            serverSocket.close();
            System.out.println("Server stopped.");
        } catch (IOException e) {
//            e.printStackTrace();
        }
    }

    private class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private final Server server;

        public ClientHandler(Socket clientSocket, Server server) {
            this.clientSocket = clientSocket;
            this.server = server;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
                String input;
                while ((input = in.readLine()) != null) {
                    System.out.println("Received message: " + input + " from Node " + clientSocket.getPort());
                    server.addMessage(input);
                    server.forwardMessages();
                }
            } catch (IOException e) {
//                e.printStackTrace();
            }
        }
    }
}
