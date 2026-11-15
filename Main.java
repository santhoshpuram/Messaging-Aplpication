import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length != 1) {
            System.out.println("Usage: java Main <number_of_nodes>");
            System.exit(1);
        }

        int numNodes = Integer.parseInt(args[0]);

        // Start the server
        Server server = new Server(12345);  // Listening on port 12345
        Thread serverThread = new Thread(server);
        serverThread.start();

        // Create and start node clients
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < numNodes; i++) {
            Node node = new Node("Node-" + (i + 1), "localhost", 12345);
            nodes.add(node);
            new Thread(node).start();
        }

        // Wait briefly to ensure all nodes are connected
        Thread.sleep(1000);  // 1 second delay

        // Take custom message from user input
        Scanner scanner = new Scanner(System.in);
        String message;
        do {
            System.out.println("Enter the message to be sent (type 'shutdown' to exit):");
            message = scanner.nextLine();

            // Send the custom message from each node
            for (Node node : nodes) {
                node.sendMessage(message);
            }

            // If the message is "shutdown", stop the server and nodes
            if (message.equalsIgnoreCase("shutdown")) {
                System.out.println("Shutdown command received. Stopping server and nodes...");
                break;
            }

            // Wait for nodes to process messages
            Thread.sleep(1000);

        } while (!message.equalsIgnoreCase("shutdown"));

        // Allow time for shutdown to propagate to all nodes
        Thread.sleep(2000);

        // Stop all nodes
        for (Node node : nodes) {
            node.stop();
        }

        // Shutdown server
        server.shutdown();
        serverThread.join();  // Wait for server to finish
        System.out.println("Server and nodes shut down. Program finished.");
    }
}
