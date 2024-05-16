import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class ClientGUI {
    private JFrame frame;
    private JTextField inputTextField;
    private JTextArea chatTextArea;
    private JTextArea userTextArea; // TextArea to display connected usernames
    private PrintWriter out;
    private Set<String> connectedUsers = new HashSet<>(); // Set to store connected usernames

    public ClientGUI() {
        frame = new JFrame("Client");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        chatTextArea = new JTextArea();
        chatTextArea.setEditable(false);
        JScrollPane chatScrollPane = new JScrollPane(chatTextArea);

        userTextArea = new JTextArea();
        userTextArea.setEditable(false);
        JScrollPane userScrollPane = new JScrollPane(userTextArea);
        userScrollPane.setPreferredSize(new Dimension(150, 0)); // Set width of userTextArea

        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.add(chatScrollPane, BorderLayout.CENTER);

        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.add(new JLabel("Connected Users"), BorderLayout.NORTH);
        userPanel.add(userScrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputTextField = new JTextField();
        inputPanel.add(inputTextField, BorderLayout.CENTER);

        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        inputPanel.add(sendButton, BorderLayout.EAST);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(chatPanel, BorderLayout.CENTER);
        mainPanel.add(userPanel, BorderLayout.EAST);

        frame.add(mainPanel, BorderLayout.CENTER);
        frame.add(inputPanel, BorderLayout.SOUTH);

        frame.setVisible(true);

        try {
            Socket socket = new Socket("localhost", 12345);
            out = new PrintWriter(socket.getOutputStream(), true);

            new Thread(() -> {
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String message;
                    while ((message = in.readLine()) != null) {
                        if (message.startsWith("USERLIST:")) { // Check if message is a user list update
                            updateConnectedUsers(message.substring(9)); // Update connected users
                        } else {
                            chatTextArea.append(message + "\n");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage() {
        String message = inputTextField.getText();
        out.println(message);
        inputTextField.setText("");
    }

    private void updateConnectedUsers(String userList) {
        connectedUsers.clear(); // Clear existing users
        connectedUsers.addAll(Arrays.asList(userList.split(","))); // Add new users
        SwingUtilities.invokeLater(() -> {
            userTextArea.setText(String.join("\n", connectedUsers)); // Update user list display
        });
    }

    public static void main(String[] args) {
        new ClientGUI();
    }
}
