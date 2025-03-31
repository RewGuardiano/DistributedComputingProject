import java.net.*;
import java.io.*;

/**
 * SMP Client Helper using stream-mode socket.
 */
public class EchoClientHelper2 {
   private MyStreamSocket mySocket;
   private InetAddress serverHost;
   private int serverPort;

   EchoClientHelper2(String hostName, String portNum) throws SocketException, UnknownHostException, IOException {
      this.serverHost = InetAddress.getByName(hostName);
      this.serverPort = Integer.parseInt(portNum);
      this.mySocket = new MyStreamSocket(this.serverHost, this.serverPort);
      System.out.println("Connected to SMP Server.");
   }

   // For single-line responses (e.g., LOGIN, UPLOAD, DOWNLOAD_INDEX, LOGOFF)
   public String sendRequest(String message) throws IOException {
      mySocket.sendMessage(message);
      return mySocket.receiveMessage();
   }

   public String sendRequestMultiLine(String message) throws IOException {
      mySocket.sendMessage(message);
      StringBuilder response = new StringBuilder();
      String line;
      while ((line = mySocket.receiveMessage()) != null) {
         response.append(line).append("\n");
         if (line.startsWith("Message")) {
            break; // Stop reading after the end signal
         }
      }
      return response.toString();
   }

   public void done() throws IOException {
      mySocket.close();
   }
}