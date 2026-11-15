import java.io.*;
import java.net.*;

public class Node implements Runnable {
    private final String nodeName;
    private final String host;
    private final int port;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private volatile boolean running = true;

    public Node(String nodeName, String host, int port) {
        this.nodeName = nodeName;
        this.host = host;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            // Connect to the server
            socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.println(nodeName + " connected to server.");

            // Keep listening for messages from the server
            while (running) {
                String serverMessage = in.readLine();
                if (serverMessage != null) {
                    System.out.println(nodeName + " received: " + serverMessage);

                    // Check if the received message is "shutdown"
                    if (serverMessage.trim().equalsIgnoreCase("shutdown")) {
                        System.out.println(nodeName + " received shutdown command.");
                        stop();  // Stop the node if shutdown command is received
                    }
                }
            }
        } catch (IOException e) {
//            e.printStackTrace();
        }
    }

    // Method to send a message to the server
    public void sendMessage(String message) {
        if (out != null) {
            out.println(nodeName + ": " + message);
            // Check if the sent message is "shutdown"
            if (message.trim().equalsIgnoreCase("shutdown")) {
                System.out.println(nodeName + " is shutting down...");
                stop();
            }
        } else {
            System.out.println("Error: Cannot send message, not connected to server.");
        }
    }
    public void stop() {
        running = false;
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
