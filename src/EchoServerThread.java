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
    private static ConcurrentHashMap<String, List<String>> userMessages = new ConcurrentHashMap<>();

    EchoServerThread(Socket socket) {
        this.clientSocket = socket;
    }

    public void run() {
        try {
            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            output = new PrintWriter(clientSocket.getOutputStream(), true);

            String clientMessage;
            while ((clientMessage = input.readLine()) != null) {
                handleClientRequest(clientMessage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClientRequest(String message) {
        String[] parts = message.split(" ", 2);
        String command = parts[0].toUpperCase();

        switch (command) {
            case "LOGIN":
                handleLogin(parts.length > 1 ? parts[1] : "anonymous");
                break;
            case "UPLOAD":
                handleUpload(parts.length > 1 ? parts[1] : "");
                break;
            case "DOWNLOAD":
                handleDownload();
                break;
            case "LOGOFF":
                handleLogoff();
                break;
            default:
                output.println("400 INVALID REQUEST");
        }
    }

    private void handleLogin(String username) {
        userMessages.putIfAbsent(username, new ArrayList<>());
        output.println("101 LOGIN SUCCESS");
    }

    private void handleUpload(String message) {
        if (message.isEmpty()) {
            output.println("202 UPLOAD FAILED");
            return;
        }

        // Ensure the user has an entry in the message store
        userMessages.putIfAbsent("anonymous", new ArrayList<>());

        // Add the message to the user's message list
        userMessages.get("anonymous").add(message);

        // Print message on the server console
        System.out.println("Message received from client: " + message);

        output.println("201 UPLOAD SUCCESS");
    }


    private void handleDownload() {
        List<String> messages = userMessages.getOrDefault("anonymous", new ArrayList<>());
        if (messages.isEmpty()) {
            output.println("302 NO MESSAGES");
        } else {
            for (String msg : messages) {
                output.println("301 MESSAGE: " + msg);
            }
        }
    }

    private void handleLogoff() {
        output.println("401 LOGOFF SUCCESS");
    }
}
