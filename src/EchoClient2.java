import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * SMP Client GUI using Swing
 */
public class EchoClient2 {
   private JTextField messageIdField;
   private JButton downloadSpecificButton;
   private JButton getAllMessageIdsButton;
   private EchoClientHelper2 helper;
   private JFrame frame;
   private JTextField usernameField, passwordField, messageField;
   private JTextArea outputArea;
   private JButton loginButton, uploadButton, downloadButton, logoffButton;

   public EchoClient2() {
      frame = new JFrame("SMP Client");
      frame.setSize(600, 600);
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setLayout(new BorderLayout(10, 10));

      JPanel loginPanel = new JPanel(new GridLayout(3, 2, 10, 10));
      loginPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
      loginPanel.add(new JLabel("Username:"));
      usernameField = new JTextField();
      loginPanel.add(usernameField);
      loginPanel.add(new JLabel("Password:"));
      passwordField = new JPasswordField();
      loginPanel.add(passwordField);
      loginButton = new JButton("Login");
      loginPanel.add(loginButton);
      frame.add(loginPanel, BorderLayout.NORTH);

      outputArea = new JTextArea();
      outputArea.setEditable(false);
      outputArea.setPreferredSize(new Dimension(400, 300));
      outputArea.setLineWrap(true);
      outputArea.setWrapStyleWord(true);
      JScrollPane scrollPane = new JScrollPane(outputArea);
      scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
      scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      frame.add(scrollPane, BorderLayout.CENTER);

      JPanel actionPanel = new JPanel(new GridLayout(3, 1, 10, 10));
      actionPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

      JPanel uploadSubPanel = new JPanel(new BorderLayout(5, 5));
      uploadSubPanel.add(new JLabel("Message to Upload:"), BorderLayout.NORTH);
      messageField = new JTextField();
      uploadSubPanel.add(messageField, BorderLayout.CENTER);
      uploadButton = new JButton("Upload");
      uploadSubPanel.add(uploadButton, BorderLayout.EAST);
      actionPanel.add(uploadSubPanel);

      JPanel downloadSpecificSubPanel = new JPanel(new BorderLayout(5, 5));
      downloadSpecificSubPanel.add(new JLabel("Message ID:"), BorderLayout.NORTH);
      messageIdField = new JTextField();
      downloadSpecificSubPanel.add(messageIdField, BorderLayout.CENTER);
      downloadSpecificButton = new JButton("Download Specific");
      downloadSpecificSubPanel.add(downloadSpecificButton, BorderLayout.EAST);
      actionPanel.add(downloadSpecificSubPanel);

      JPanel buttonSubPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
      getAllMessageIdsButton = new JButton("Get All Message IDs");
      buttonSubPanel.add(getAllMessageIdsButton);
      downloadButton = new JButton("Download");
      buttonSubPanel.add(downloadButton);
      logoffButton = new JButton("Logoff");
      buttonSubPanel.add(logoffButton);
      actionPanel.add(buttonSubPanel);

      frame.add(actionPanel, BorderLayout.SOUTH);

      downloadSpecificButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            downloadSpecificMessage();
         }
      });

      getAllMessageIdsButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            getAllMessageIds();
         }
      });

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

      frame.setVisible(true);
      connectToServer();
   }

   private void connectToServer() {
      try {
         helper = new EchoClientHelper2("localhost", "12345");
         outputArea.append("Connected to SMP Server with SSL.\n");
      } catch (Exception e) {
         outputArea.append("Error connecting to server: " + e.getMessage() + "\n");
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

   private void downloadSpecificMessage() {
      String messageId = messageIdField.getText();
      if (!messageId.isEmpty()) {
         try {
            String response = helper.sendRequestMultiLine("DOWNLOAD_ID " + messageId); // Use sendRequestMultiLine for multi-line response
            outputArea.append(response);
         } catch (IOException e) {
            outputArea.append("Error downloading message.\n");
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
         String response = helper.sendRequestMultiLine("DOWNLOAD");
         outputArea.append(response);
      } catch (IOException e) {
         outputArea.append("Error downloading messages.\n");
      }
   }

   private void getAllMessageIds() {
      try {
         String response = helper.sendRequestMultiLine("DOWNLOAD");
         outputArea.append("List of Message IDs and Messages:\n");
         outputArea.append(response);
      } catch (IOException e) {
         outputArea.append("Error retrieving message IDs.\n");
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