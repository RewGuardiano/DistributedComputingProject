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
        String[] parts = message.split(" ", 3); // Now expecting LOGIN username password
        String command = parts[0].toUpperCase();

        switch (command) {
            case "LOGIN":
                if (parts.length == 3) {
                    handleLogin(parts[1], parts[2]);
                } else {
                    output.println("102 LOGIN FAILED"); // Fail if username or password is missing
                }
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

    private void handleLogin(String username, String password) {
        userMessages.putIfAbsent(username, new ArrayList<>());
        output.println("101 LOGIN SUCCESS"); // Password is not verified but accepted
    }

    private void handleUpload(String message) {
        if (message.isEmpty()) {
            output.println("202 UPLOAD FAILED");
            return;
        }

        // Ensure the user has an entry in the message store (for now, it's anonymous)
        userMessages.putIfAbsent("anonymous", new ArrayList<>());
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
        System.out.println("Client has logged off.");
        output.println("401 LOGOFF SUCCESS");
    }
}
