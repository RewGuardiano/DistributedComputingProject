import java.net.*;
import java.io.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import javax.net.ssl.*;
import java.security.*;
import java.io.FileInputStream;

public class EchoServer3 {
   private static final int SERVER_PORT = 12345;
   private static ConcurrentHashMap<String, List<Message>> userMessages = new ConcurrentHashMap<>();

   public static void main(String[] args) {
      try {
         // Initialize SSL context
         SSLServerSocketFactory sslServerSocketFactory = getSSLServerSocketFactory();
         try (SSLServerSocket myConnectionSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(SERVER_PORT)) {
            System.out.println("SMP Server is running on port " + SERVER_PORT + " with SSL");

            while (true) {
               Socket clientSocket = myConnectionSocket.accept();
               System.out.println("New client connected.");
               new Thread(new EchoServerThread(clientSocket)).start();
            }
         }
      } catch (IOException e) {
         e.printStackTrace();
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   private static SSLServerSocketFactory getSSLServerSocketFactory() throws Exception {
      //Load Keystore
      try {
         char[] keystorePassword = "admin123".toCharArray();
         KeyStore keyStore = KeyStore.getInstance("JKS");
         try {
            keyStore.load(new FileInputStream("serverkeystore.jks"), keystorePassword);
         } catch (FileNotFoundException e) {
            throw new Exception("Server keystore file not found: serverkeystore.jks", e);
         } catch (IOException e) {
            throw new Exception("Failed to load server keystore (possible wrong password): " + e.getMessage(), e);
         }

         KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
         keyManagerFactory.init(keyStore, keystorePassword);

         SSLContext sslContext = SSLContext.getInstance("TLS");
         sslContext.init(keyManagerFactory.getKeyManagers(), null, null);

         return sslContext.getServerSocketFactory();
      } catch (Exception e) {
         throw new Exception("Failed to initialize SSL server socket factory: " + e.getMessage(), e);
      }
   }
}