import java.io.*;

public class EchoClient2 {
   static final String endMessage = "LOGOFF";

   public static void main(String[] args) {
      InputStreamReader is = new InputStreamReader(System.in);
      BufferedReader br = new BufferedReader(is);

      try {
         System.out.println("Welcome to the SMP Client.\n" +
                 "Enter server hostname (default: localhost):");
         String hostName = br.readLine();
         if (hostName.isEmpty()) hostName = "localhost";

         System.out.println("Enter server port (default: 12345):");
         String portNum = br.readLine();
         if (portNum.isEmpty()) portNum = "12345";

         EchoClientHelper2 helper = new EchoClientHelper2(hostName, portNum);

         System.out.println("Enter username to login:");
         String username = br.readLine();
         System.out.println(helper.sendRequest("LOGIN " + username));

         boolean done = false;
         while (!done) {
            System.out.println("\nOptions: 1. Upload | 2. Download | 3. Logoff");
            String option = br.readLine();

            switch (option) {
               case "1":
                  System.out.println("Enter message to upload:");
                  String message = br.readLine();
                  System.out.println(helper.sendRequest("UPLOAD " + message));
                  break;
               case "2":
                  System.out.println(helper.sendRequest("DOWNLOAD"));
                  break;
               case "3":
                  System.out.println(helper.sendRequest("LOGOFF"));
                  done = true;
                  helper.done();
                  break;
               default:
                  System.out.println("Invalid option. Try again.");
            }
         }
      } catch (Exception ex) {
         ex.printStackTrace();
      }
   }
}
