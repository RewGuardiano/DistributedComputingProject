import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread handling a single client session for the SMP server.
 */
class EchoServerThread implements Runnable {
    private final Socket clientSocket;
    private PrintWriter output;
    private static final ConcurrentHashMap<String, List<Message>> userMessages = new ConcurrentHashMap<>();
    private static int messageIdCounter = 0;
    private boolean isLoggedIn; // Track login state for this client session

    EchoServerThread(Socket socket) {
        this.clientSocket = socket;
        this.isLoggedIn = false; // Initialize login state
        System.out.println("New client connected via SSL.");
    }

    public void run() {
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
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
        String[] parts = message.split(" ", 2);
        String command = parts[0].toUpperCase();
        String argument = parts.length > 1 ? parts[1] : "";

        // Allow LOGIN command regardless of login state
        if (command.equals("LOGIN")) {
            String[] loginParts = argument.split(" ", 2);
            if (loginParts.length == 2) {
                handleLogin(loginParts[0], loginParts[1]);
            } else {
                output.println("102 LOGIN FAILED (Missing username or password)");
            }
            return;
        }

        // For all other commands, check if the client is logged in
        if (!isLoggedIn) {
            output.println("103 NOT LOGGED IN");
            return;
        }

        switch (command) {
            case "UPLOAD":
                handleUpload(argument);
                break;
            case "DOWNLOAD":
                handleDownload();
                break;
            case "DOWNLOAD_ID":
                if (argument.isEmpty()) {
                    output.println("403 INVALID REQUEST (Provide message ID)");
                } else if (!argument.matches("d\\d+")) {
                    output.println("405 INVALID MESSAGE ID FORMAT (Expected format: d followed by a number, e.g., d0)");
                } else {
                    handleDownloadSpecific(argument);
                }
                break;
            case "LOGOFF":
                handleLogoff();
                break;
            default:
                output.println("INVALID REQUEST");
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
        output.println("Message Downloaded");
    }

    private void handleLogin(String username, String password) {
        userMessages.putIfAbsent(username, new ArrayList<>());
        isLoggedIn = true; // Set login state to true
        output.println("101 LOGIN SUCCESS");
    }

    private void handleUpload(String message) {
        if (message.isEmpty()) {
            output.println("202 UPLOAD FAILED");
            return;
        }

        userMessages.putIfAbsent("anonymous", new ArrayList<>());
        String messageId = "d" + messageIdCounter++;
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
            isLoggedIn = false; // Reset login state
            clientSocket.close();
        } catch (IOException e) {
            System.err.println("Error closing client socket: " + e.getMessage());
        }
    }
}