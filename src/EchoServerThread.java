import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread handling a single client session for the SMP server.
 */
class EchoServerThread implements Runnable {
    private Socket clientSocket;
    private BufferedReader input;
    private PrintWriter output;
    private static ConcurrentHashMap<String, List<Message>> userMessages = new ConcurrentHashMap<>();
    private static int messageIdCounter = 0;


    EchoServerThread(Socket socket) {
        this.clientSocket = socket;
        System.out.println("New client connected via SSL.");
    }

    public void run() {
        try {
            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            output = new PrintWriter(clientSocket.getOutputStream(), true);

            String clientMessage;
            while ((clientMessage = input.readLine()) != null) {
                handleClientRequest(clientMessage);
                if (clientMessage.equalsIgnoreCase("LOGOFF")) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }
    }

    private void handleClientRequest(String message) {
        // Split the message into command and the rest (allowing spaces in the rest)
        String[] parts = message.split(" ", 2); // Split into at most 2 parts: command and the rest
        String command = parts[0].toUpperCase();
        String argument = parts.length > 1 ? parts[1] : ""; // The rest of the message (may contain spaces)

        switch (command) {
            case "LOGIN":
                // LOGIN requires username and password, so split the argument further
                String[] loginParts = argument.split(" ", 2);
                if (loginParts.length == 2) {
                    handleLogin(loginParts[0], loginParts[1]);
                } else {
                    output.println("102 LOGIN FAILED (Missing username or password)");
                }
                break;
            case "UPLOAD":
                handleUpload(argument); // Pass the entire message (including spaces)
                break;
            case "DOWNLOAD":
                handleDownload();
                break;
            case "DOWNLOAD_ID":
                if (!argument.isEmpty()) {
                    handleDownloadSpecific(argument);
                } else {
                    output.println("403 INVALID REQUEST (Provide message ID)");
                }
                break;
            case "LOGOFF":
                handleLogoff();
                break;
            default:
                output.println("400 INVALID REQUEST");
        }
    }

    private void handleDownloadSpecific(String messageId) {
        List<Message> messages = userMessages.getOrDefault("anonymous", new ArrayList<>());
        boolean found = false;

        for (Message msg : messages) {
            if (msg.getId().equals(messageId)) {
                output.println("301 MESSAGE: " + msg.getContent());
                found = true;
                break;
            }
        }

        if (!found) {
            output.println("404 MESSAGE NOT FOUND");
        }
        output.println("Message Downloaded"); // Add this line to indicate download completion
    }

    private void handleLogin(String username, String password) {
        userMessages.putIfAbsent(username, new ArrayList<>());
        output.println("101 LOGIN SUCCESS");
    }

    private void handleUpload(String message) {
        if (message.isEmpty()) {
            output.println("202 UPLOAD FAILED");
            return;
        }

        userMessages.putIfAbsent("anonymous", new ArrayList<>());
        String messageId = "m" + messageIdCounter++;
        Message newMessage = new Message(messageId, message);
        userMessages.get("anonymous").add(newMessage);

        System.out.println("Message received from client: " + newMessage);
        output.println("201 UPLOAD SUCCESS (Message ID: " + messageId + ")");
    }

    private void handleDownload() {
        List<Message> messages = userMessages.getOrDefault("anonymous", new ArrayList<>());
        if (messages.isEmpty()) {
            output.println("302 NO MESSAGES");
        } else {
            for (Message msg : messages) {
                output.println("301 MESSAGE (ID: " + msg.getId() + "): " + msg.getContent());
            }
            output.println("Message Downloaded");
        }
    }

    private void handleLogoff() {
        try {
            System.out.println("Client has logged off.");
            output.println("401 LOGOFF SUCCESS");
            clientSocket.close();
        } catch (IOException e) {
            System.err.println("Error closing client socket: " + e.getMessage());
        }
    }
}