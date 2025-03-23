import java.net.*;
import java.io.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.List;

/**
 * Multi-threaded SMP Server handling LOGIN, UPLOAD, DOWNLOAD, and LOGOFF.
 * Keeps messages in memory using ConcurrentHashMap.
 */
public class EchoServer3 {
   private static final int SERVER_PORT = 12345;
   private static ConcurrentHashMap<String, List<String>> userMessages = new ConcurrentHashMap<>();

   public static void main(String[] args) {
      try (ServerSocket myConnectionSocket = new ServerSocket(SERVER_PORT)) {
         System.out.println("SMP Server is running on port " + SERVER_PORT);

         while (true) {
            Socket clientSocket = myConnectionSocket.accept();
            System.out.println("New client connected.");
            new Thread(new EchoServerThread(clientSocket)).start();
         }
      } catch (IOException e) {
         e.printStackTrace();
      }
   }
}
