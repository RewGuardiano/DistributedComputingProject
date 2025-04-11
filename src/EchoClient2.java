import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * SMP Client GUI using Swing
 */
public class EchoClient2 {
   private final JTextField messageIdField;
   private final JButton downloadSpecificButton;
   private final JButton getAllMessageIdsButton;
   private EchoClientHelper2 helper;
    private final JTextField usernameField;
    private final JTextField passwordField;
    private final JTextField messageField;
   private final JTextArea outputArea;
    private final JButton uploadButton;
    private final JButton downloadButton;
    private final JButton logoffButton;
   private boolean isLoggedIn; // Track login state

   public EchoClient2() {
       JFrame frame = new JFrame("SMP Client");
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
       JButton loginButton = new JButton("Login");
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

      // Initialize login state and disable action buttons by default
      isLoggedIn = false;
      setActionButtonsEnabled(false);

      downloadSpecificButton.addActionListener(e -> downloadSpecificMessage());

      getAllMessageIdsButton.addActionListener(e -> getAllMessageIds());

      loginButton.addActionListener(e -> login());

      uploadButton.addActionListener(e -> uploadMessage());

      downloadButton.addActionListener(e -> downloadMessages());

      logoffButton.addActionListener(e -> logoff());

      frame.setVisible(true);
      connectToServer();
   }

   private void connectToServer() {
      try {
         helper = new EchoClientHelper2("localhost", "12345");
         outputArea.append("Connected to SMP Server with SSL.\n");
      } catch (java.net.ConnectException e) {
         outputArea.append("Error: Server is not running or unreachable on localhost:12345.\n");
      } catch (Exception e) {
         outputArea.append("Error connecting to server: " + e.getMessage() + "\n");
      }
   }

   private void setActionButtonsEnabled(boolean enabled) {
      uploadButton.setEnabled(enabled);
      downloadButton.setEnabled(enabled);
      getAllMessageIdsButton.setEnabled(enabled);
      downloadSpecificButton.setEnabled(enabled);
      logoffButton.setEnabled(enabled);
   }

   private void login() {
      String username = usernameField.getText().trim();
      String password = passwordField.getText().trim();

      // Validate username and password fields
      if (username.isEmpty() || password.isEmpty()) {
         outputArea.append("Error: Username and password cannot be empty.\n");
         return;
      }

      try {
         String response = helper.sendRequest("LOGIN " + username + " " + password);
         outputArea.append(response + "\n");
         if (response.startsWith("101 LOGIN SUCCESS")) {
            isLoggedIn = true;
            setActionButtonsEnabled(true);
         }
      } catch (java.net.SocketException e) {
         outputArea.append("Error: Connection to server lost.\n");
      } catch (IOException e) {
         outputArea.append("Error during login: " + e.getMessage() + "\n");
      }
   }

   private void downloadSpecificMessage() {
      if (!isLoggedIn) {
         outputArea.append("Error: You must log in to download a specific message.\n");
         return;
      }
      String messageId = messageIdField.getText();
      if (messageId.isEmpty()) {
         outputArea.append("Error: Please enter a message ID.\n");
      } if (!messageId.matches("d\\d+")) {
         outputArea.append("Error: Invalid message ID format. Expected format: d followed by a number (e.g., d0).\n");
      } else {
         try {
            String response = helper.sendRequestMultiLine("DOWNLOAD_ID " + messageId);
            outputArea.append(response);
         } catch (java.net.SocketException e) {
            outputArea.append("Error: Connection to server lost.\n");
         } catch (IOException e) {
            outputArea.append("Error downloading message: " + e.getMessage() + "\n");
         }
      }
   }

   private void uploadMessage() {
      if (!isLoggedIn) {
         outputArea.append("Error: You must log in to upload a message.\n");
         return;
      }
      String message = messageField.getText();
      if (!message.isEmpty()) {
         try {
            String response = helper.sendRequest("UPLOAD " + message);
            outputArea.append(response + "\n");
         } catch (java.net.SocketException e) {
            outputArea.append("Error: Connection to server lost.\n");
         } catch (IOException e) {
            outputArea.append("Error uploading message: " + e.getMessage() + "\n");
         }
      }
   }

   private void downloadMessages() {
      if (!isLoggedIn) {
         outputArea.append("Error: You must log in to download messages.\n");
         return;
      }
      try {
         String response = helper.sendRequestMultiLine("DOWNLOAD");
         outputArea.append(response);
      } catch (java.net.SocketException e) {
         outputArea.append("Error: Connection to server lost.\n");
      } catch (IOException e) {
         outputArea.append("Error downloading messages: " + e.getMessage() + "\n");
      }
   }

   private void getAllMessageIds() {
      if (!isLoggedIn) {
         outputArea.append("Error: You must log in to get message IDs.\n");
         return;
      }
      try {
         String response = helper.sendRequestMultiLine("DOWNLOAD");
         outputArea.append("List of Message IDs and Messages:\n");
         outputArea.append(response);
      } catch (java.net.SocketException e) {
         outputArea.append("Error: Connection to server lost.\n");
      } catch (IOException e) {
         outputArea.append("Error retrieving message IDs: " + e.getMessage() + "\n");
      }
   }

   private void logoff() {
      if (!isLoggedIn) {
         outputArea.append("Error: You must log in to log off.\n");
         return;
      }
      try {
         String response = helper.sendRequest("LOGOFF");
         outputArea.append(response + "\n");
         if (response.startsWith("401 LOGOFF SUCCESS")) {
            isLoggedIn = false;
            setActionButtonsEnabled(false);
         }
         helper.done();
      } catch (java.net.SocketException e) {
         outputArea.append("Error: Connection to server lost.\n");
      } catch (IOException e) {
         outputArea.append("Error logging off: " + e.getMessage() + "\n");
      }
   }

   public static void main(String[] args) {
      new EchoClient2();
   }
}