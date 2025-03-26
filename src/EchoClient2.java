import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * SMP Client GUI using Swing
 */
public class EchoClient2 {
   private EchoClientHelper2 helper;
   private JFrame frame;
   private JTextField usernameField, passwordField, messageField;
   private JTextArea outputArea;
   private JButton loginButton, uploadButton, downloadButton, logoffButton;

   public EchoClient2() {
      // Create GUI window
      frame = new JFrame("SMP Client");
      frame.setSize(500, 400);
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setLayout(new BorderLayout());

      // Top Panel for Login
      JPanel loginPanel = new JPanel(new GridLayout(3, 2));
      loginPanel.add(new JLabel("Username:"));
      usernameField = new JTextField();
      loginPanel.add(usernameField);
      loginPanel.add(new JLabel("Password:"));
      passwordField = new JPasswordField();
      loginPanel.add(passwordField);
      loginButton = new JButton("Login");
      loginPanel.add(loginButton);

      frame.add(loginPanel, BorderLayout.NORTH);

      // Center Panel for Output
      outputArea = new JTextArea();
      outputArea.setEditable(false);
      frame.add(new JScrollPane(outputArea), BorderLayout.CENTER);

      // Bottom Panel for Upload/Download/Logoff
      JPanel actionPanel = new JPanel(new GridLayout(2, 2));
      messageField = new JTextField();
      actionPanel.add(messageField);
      uploadButton = new JButton("Upload");
      actionPanel.add(uploadButton);
      downloadButton = new JButton("Download");
      actionPanel.add(downloadButton);
      logoffButton = new JButton("Logoff");
      actionPanel.add(logoffButton);

      frame.add(actionPanel, BorderLayout.SOUTH);

      // Event Handlers
      loginButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            login();
         }
      });

      uploadButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            uploadMessage();
         }
      });

      downloadButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            downloadMessages();
         }
      });

      logoffButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            logoff();
         }
      });

      // Show GUI
      frame.setVisible(true);

      // Connect to server
      connectToServer();
   }

   private void connectToServer() {
      try {
         helper = new EchoClientHelper2("localhost", "12345");
         outputArea.append("Connected to SMP Server.\n");
      } catch (Exception e) {
         outputArea.append("Error connecting to server.\n");
      }
   }

   private void login() {
      String username = usernameField.getText();
      String password = new String(((JPasswordField) passwordField).getPassword());
      if (!username.isEmpty() && !password.isEmpty()) {
         try {
            String response = helper.sendRequest("LOGIN " + username + " " + password);
            outputArea.append(response + "\n");
         } catch (IOException e) {
            outputArea.append("Error logging in.\n");
         }
      }
   }

   private void uploadMessage() {
      String message = messageField.getText();
      if (!message.isEmpty()) {
         try {
            String response = helper.sendRequest("UPLOAD " + message);
            outputArea.append(response + "\n");
         } catch (IOException e) {
            outputArea.append("Error uploading message.\n");
         }
      }
   }

   private void downloadMessages() {
      try {
         String response = helper.sendRequest("DOWNLOAD");
         outputArea.append(response + "\n");
      } catch (IOException e) {
         outputArea.append("Error downloading messages.\n");
      }
   }

   private void logoff() {
      try {
         String response = helper.sendRequest("LOGOFF");
         outputArea.append(response + "\n");
         helper.done();
      } catch (IOException e) {
         outputArea.append("Error logging off.\n");
      }
   }

   public static void main(String[] args) {
      new EchoClient2();
   }
}
