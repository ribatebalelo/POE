package chatapp;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.util.Timer;
import java.util.TimerTask;
import org.json.*;

public class ChatApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame());
    }
}



class LoginFrame extends JFrame {
    public LoginFrame() {
        setTitle("Login / Register");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JTextField phoneField = new JTextField();

        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");

        panel.add(new JLabel("Username:")); panel.add(usernameField);
        panel.add(new JLabel("Password:")); panel.add(passwordField);
        panel.add(new JLabel("Phone Number:")); panel.add(phoneField);
        panel.add(loginButton); panel.add(registerButton);

        loginButton.setBackground(Color.BLUE);
        loginButton.setForeground(Color.WHITE);
        registerButton.setBackground(Color.BLUE);
        registerButton.setForeground(Color.WHITE);

        add(panel, BorderLayout.CENTER);

        loginButton.addActionListener(e -> {
            JSONArray users = JSONDataStore.getUsers();
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            
            boolean found = false;
            for (int i = 0; i < users.length(); i++) {
                JSONObject user = users.getJSONObject(i);
                if (user.getString("username").equals(username) && 
                    user.getString("password").equals(password)) {
                    found = true;
                    break;
                }
            }
            
            if (found) {
                new DashboardFrame(username);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials");
            }
        });

        registerButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String phone = phoneField.getText();

            if (!username.contains("_") || username.length() > 5) {
                JOptionPane.showMessageDialog(this, "Username must contain '_' and be max 5 chars");
                return;
            }

            if (!Pattern.matches("(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,}", password)) {
                JOptionPane.showMessageDialog(this, "Password must be at least 8 characters, with a capital letter, number, and special character.");
                return;
            }

            if (!Pattern.matches("\\+27[6-8][0-9]{8}", phone)) {
                JOptionPane.showMessageDialog(this, "Phone must be a valid South African number with international code");
                return;
            }

            JSONArray users = JSONDataStore.getUsers();
            for (int i = 0; i < users.length(); i++) {
                if (users.getJSONObject(i).getString("username").equals(username)) {
                    JOptionPane.showMessageDialog(this, "Username already exists");
                    return;
                }
            }

            JSONObject newUser = new JSONObject();
            newUser.put("username", username);
            newUser.put("password", password);
            newUser.put("phone", phone);
            
            JSONDataStore.saveUser(newUser);
            JOptionPane.showMessageDialog(this, "Registered successfully!");
        });

        setVisible(true);
    }
}

class DashboardFrame extends JFrame {
    private String username;

    public DashboardFrame(String username) {
        this.username = username;
        setTitle("Dashboard - Welcome " + username);
        setSize(400, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel welcomeLabel = new JLabel("Welcome to Quick Chat", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));

        JButton sendMsgBtn = new JButton("Send Message");
        JButton checkMsgBtn = new JButton("View Message History");
        JButton searchBtn = new JButton("Search Message by ID");
        JButton quickBtn = new JButton("Quit");

        sendMsgBtn.setBackground(Color.BLUE);
        sendMsgBtn.setForeground(Color.WHITE);
        checkMsgBtn.setBackground(Color.BLUE);
        checkMsgBtn.setForeground(Color.WHITE);
        searchBtn.setBackground(Color.BLUE);
        searchBtn.setForeground(Color.WHITE);
        quickBtn.setBackground(Color.BLUE);
        quickBtn.setForeground(Color.WHITE);

        JPanel panel = new JPanel(new GridLayout(5, 1, 10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.add(welcomeLabel);
        panel.add(sendMsgBtn);
        panel.add(checkMsgBtn);
        panel.add(searchBtn);
        panel.add(quickBtn);
        add(panel);

        sendMsgBtn.addActionListener(e -> {
            new UserListFrame(username);
            dispose();
        });

        checkMsgBtn.addActionListener(e -> {
            showMessageHistory();
        });

        searchBtn.addActionListener(e -> {
            searchMessageById();
        });

        quickBtn.addActionListener(e -> System.exit(0));

        setVisible(true);
    }

    private void showMessageHistory() {
        JSONArray messages = JSONDataStore.getMessages();
        StringBuilder history = new StringBuilder();
        
        for (int i = 0; i < messages.length(); i++) {
            JSONObject msg = messages.getJSONObject(i);
            if (msg.getString("from").equals(username)) {
                String status = msg.optBoolean("read", false) ? "✓✓ (read)" : 
                                msg.optBoolean("delivered", false) ? "✓✓" : "✓";
                history.append(String.format("[ID: %d] To: %s | Message: %s | Status: %s | Time: %s\n",
                    i,
                    msg.getString("to"),
                    msg.getString("message").length() > 20 ? 
                        msg.getString("message").substring(0, 20) + "..." : msg.getString("message"),
                    status,
                    new Date(msg.getLong("timestamp"))));
            }
        }
        
        if (history.length() == 0) {
            history.append("No messages found");
        }
        
        JTextArea textArea = new JTextArea(history.toString());
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));
        
        JOptionPane.showMessageDialog(this, scrollPane, "Message History", JOptionPane.INFORMATION_MESSAGE);
    }

    private void searchMessageById() {
        String idStr = JOptionPane.showInputDialog(this, "Enter Message ID:");
        if (idStr == null || idStr.trim().isEmpty()) return;
        
        try {
            int id = Integer.parseInt(idStr);
            JSONArray messages = JSONDataStore.getMessages();
            
            if (id < 0 || id >= messages.length()) {
                JOptionPane.showMessageDialog(this, "Invalid Message ID");
                return;
            }
            
            JSONObject msg = messages.getJSONObject(id);
            if (!msg.getString("from").equals(username)) {
                JOptionPane.showMessageDialog(this, "You can only view your own messages");
                return;
            }
            
            String status = msg.optBoolean("read", false) ? "✓✓ (read)" : 
                          msg.optBoolean("delivered", false) ? "✓✓" : "✓";
            
            String receipt = String.format(
                "Message ID: %d\nTo: %s\nTimestamp: %s\nStatus: %s\n\nMessage Content:\n%s",
                id,
                msg.getString("to"),
                new Date(msg.getLong("timestamp")),
                status,
                msg.getString("message")
            );
            
            JOptionPane.showMessageDialog(this, receipt, "Message Receipt", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number");
        }
    }
}

class UserListFrame extends JFrame {
    private String selectedUser;
    private String selectedPhone;
    private String senderUsername;
    private JList<String> userList;

    public UserListFrame(String senderUsername) {
        this.senderUsername = senderUsername;
        setTitle("Select a User to Message");
        setSize(400, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        DefaultListModel<String> listModel = new DefaultListModel<>();
        
        listModel.addElement("Tebz_ - +27681234567");
        listModel.addElement("Suki_ - +27781234567");
        
        JSONArray users = JSONDataStore.getUsers();
        for (int i = 0; i < users.length(); i++) {
            JSONObject user = users.getJSONObject(i);
            String username = user.getString("username");
            if (!username.equals(senderUsername)) {
                listModel.addElement(username + " - " + user.getString("phone"));
            }
        }

        userList = new JList<>(listModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(userList);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton openChatBtn = new JButton("Open Chat");
        JButton backBtn = new JButton("Back");
        
        openChatBtn.setBackground(Color.BLUE);
        openChatBtn.setForeground(Color.WHITE);
        backBtn.setBackground(Color.BLUE);
        backBtn.setForeground(Color.WHITE);

        buttonPanel.add(backBtn);
        buttonPanel.add(openChatBtn);
        add(buttonPanel, BorderLayout.SOUTH);

        userList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = userList.getSelectedValue();
                if (selected != null) {
                    String[] parts = selected.split(" - ");
                    this.selectedUser = parts[0];
                    this.selectedPhone = parts[1];
                }
            }
        });

        openChatBtn.addActionListener(e -> {
            if (selectedUser == null) {
                JOptionPane.showMessageDialog(this, "Please select a user first");
                return;
            }
            
            String countStr = JOptionPane.showInputDialog(this, "How many messages do you want to send? (Enter 1 for single message)");
            try {
                int count = Integer.parseInt(countStr);
                if (count <= 0) {
                    JOptionPane.showMessageDialog(this, "Please enter a positive number");
                    return;
                }
                
                new MessengerFrame(senderUsername, selectedUser, selectedPhone, count);
                dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid number");
            }
        });

        backBtn.addActionListener(e -> {
            new DashboardFrame(senderUsername);
            dispose();
        });

        setVisible(true);
    }
}

class MessengerFrame extends JFrame {
    JTextArea chatArea;
    Timer timer = new Timer();
    String currentSender;
    String currentReceiver;
    int messageLimit;
    int messagesSent = 0;

    public MessengerFrame(String sender, String receiver, String receiverPhone, int messageLimit) {
        this.currentSender = sender;
        this.currentReceiver = receiver;
        this.messageLimit = messageLimit;

        setTitle("Chat with " + receiver + " (" + receiverPhone + ") - " + (messageLimit - messagesSent) + " messages remaining");
        setSize(500, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane chatScroll = new JScrollPane(chatArea);
        
        JTextField inputField = new JTextField();
        JButton sendBtn = new JButton("Send");
        JButton deleteBtn = new JButton("Delete");
        JButton backBtn = new JButton("Back");
        
        sendBtn.setBackground(Color.BLUE);
        sendBtn.setForeground(Color.WHITE);
        deleteBtn.setBackground(Color.RED);
        deleteBtn.setForeground(Color.WHITE);
        backBtn.setBackground(Color.BLUE);
        backBtn.setForeground(Color.WHITE);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(inputField, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
        buttonPanel.add(sendBtn);
        buttonPanel.add(deleteBtn);
        inputPanel.add(buttonPanel, BorderLayout.EAST);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(backBtn, BorderLayout.WEST);
        topPanel.add(new JLabel("Chat with " + receiver, SwingConstants.CENTER), BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(chatScroll, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);

        loadMessages();
        startMessagePolling();

        sendBtn.addActionListener(e -> {
            if (messagesSent >= messageLimit) {
                JOptionPane.showMessageDialog(this, 
                    "You've reached your message limit of " + messageLimit + " messages",
                    "Limit Reached",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            String msg = inputField.getText().trim();
            if (msg.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Message cannot be empty");
                return;
            }
            
            JSONObject message = new JSONObject();
            message.put("from", currentSender);
            message.put("to", currentReceiver);
            message.put("message", msg);
            message.put("timestamp", System.currentTimeMillis());
            message.put("delivered", true);
            message.put("read", false);
            
            JSONDataStore.saveMessage(message);
            
            JSONArray messages = JSONDataStore.getMessages();
            int messageId = messages.length() - 1;
            
            chatArea.append(String.format("[ID:%d] Me: %s ✓\n", messageId, msg));
            inputField.setText("");
            messagesSent++;
            updateTitle();
        });

        deleteBtn.addActionListener(e -> {
            String idStr = JOptionPane.showInputDialog(this, "Enter Message ID to delete:");
            if (idStr == null || idStr.trim().isEmpty()) return;
            
            try {
                int id = Integer.parseInt(idStr);
                JSONArray messages = JSONDataStore.getMessages();
                
                if (id < 0 || id >= messages.length()) {
                    JOptionPane.showMessageDialog(this, "Invalid Message ID");
                    return;
                }
                
                JSONObject msg = messages.getJSONObject(id);
                if (!msg.getString("from").equals(currentSender) || !msg.getString("to").equals(currentReceiver)) {
                    JOptionPane.showMessageDialog(this, "You can only delete your own messages in this chat");
                    return;
                }
                
                int confirm = JOptionPane.showConfirmDialog(
                    this, 
                    "Are you sure you want to delete this message?\n" + msg.getString("message"),
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION
                );
                
                if (confirm == JOptionPane.YES_OPTION) {
                    JSONDataStore.deleteMessage(id);
                    chatArea.setText("");
                    loadMessages();
                    messagesSent--;
                    updateTitle();
                    JOptionPane.showMessageDialog(this, "Message deleted successfully");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid number");
            }
        });

        backBtn.addActionListener(e -> {
            timer.cancel();
            new UserListFrame(currentSender); // Fixed back navigation
            dispose();
        });

        setVisible(true);
    }

    private void updateTitle() {
        setTitle("Chat with " + currentReceiver + " - " + (messageLimit - messagesSent) + " messages remaining");
    }

    private void loadMessages() {
        JSONArray messages = JSONDataStore.getMessages();
        messagesSent = 0;
        
        for (int i = 0; i < messages.length(); i++) {
            JSONObject msg = messages.getJSONObject(i);
            String from = msg.getString("from");
            String to = msg.getString("to");
            String content = msg.getString("message");
            boolean delivered = msg.optBoolean("delivered", true);
            boolean read = msg.optBoolean("read", false);
            
            if (from.equals(currentSender) && to.equals(currentReceiver)) {
                messagesSent++;
                String ticks = read ? " ✓✓ (read)" : delivered ? " ✓✓" : " ✓";
                chatArea.append(String.format("[ID:%d] Me: %s%s\n", i, content, ticks));
            } else if (from.equals(currentReceiver) && to.equals(currentSender) && delivered) {
                chatArea.append(String.format("[ID:%d] %s: %s\n", i, currentReceiver, content));
                
                if (!read) {
                    msg.put("read", true);
                    JSONDataStore.updateMessage(i, msg);
                }
            }
        }
        updateTitle();
    }

    private void startMessagePolling() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                JSONArray messages = JSONDataStore.getMessages();
                for (int i = 0; i < messages.length(); i++) {
                    JSONObject msg = messages.getJSONObject(i);
                    String from = msg.getString("from");
                    String to = msg.getString("to");
                    boolean delivered = msg.optBoolean("delivered", false);
                    
                    if (from.equals(currentReceiver) && to.equals(currentSender) && !delivered) {
                        final String content = msg.getString("message");
                        final int messageId = i;
                        
                        SwingUtilities.invokeLater(() -> {
                            chatArea.append(String.format("[ID:%d] %s: %s\n", messageId, currentReceiver, content));
                        });

                        msg.put("delivered", true);
                        msg.put("read", true);
                        JSONDataStore.updateMessage(i, msg);
                    }
                }
            }
        }, 0, 3000);
    }
}