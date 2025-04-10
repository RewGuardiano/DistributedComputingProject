import java.net.*;
import java.io.*;
import javax.net.ssl.*;
import java.security.*;
import java.io.FileInputStream;

public class EchoClientHelper2 {
   private final MyStreamSocket mySocket;

    EchoClientHelper2(String hostName, String portNum) throws Exception {
       InetAddress serverHost = InetAddress.getByName(hostName);
        int serverPort = Integer.parseInt(portNum);

      // Initialize SSL context
      SSLSocketFactory sslSocketFactory = getSSLSocketFactory();
      SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(serverHost, serverPort);

      this.mySocket = new MyStreamSocket(sslSocket);
      System.out.println("Connected to SMP Server with SSL.");
   }

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
         // Exit the loop if the response is a termination message
         if (line.startsWith("302 NO MESSAGES") || line.startsWith("Message Downloaded")) {
            break;
         }
      }
      return response.toString();
   }

   private SSLSocketFactory getSSLSocketFactory() throws Exception {
      // Load the truststore
      char[] truststorePassword = "admin123".toCharArray(); // Replace with your truststore password
      KeyStore trustStore = KeyStore.getInstance("JKS");
      trustStore.load(new FileInputStream("clienttruststore.jks"), truststorePassword);

      // Initialize TrustManagerFactory
      TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      trustManagerFactory.init(trustStore);

      // Initialize SSLContext
      SSLContext sslContext = SSLContext.getInstance("TLS");
      sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

      return sslContext.getSocketFactory();
   }

   public void done() throws IOException {
      mySocket.close();
   }
}