package UserInterface;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.border.LineBorder;
import java.io.*;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import Backend.Accounts.Message;
import Backend.Accounts.User;
import Backend.Database.DatabaseManager;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

public class ChatUI extends JFrame {
    private User user1;
    private User user2;

    private JPanel chatPanel;
    private JTextField messageField;
    private JButton sendButton;
    private JScrollPane scrollPane;
    private JLabel statusLabel;

    private static final Color CHAT_COLOR = new Color(42, 171, 238);
    private static final Color BACKGROUND_COLOR = new Color(240, 240, 240);
    private static final Color MESSAGE_BUBBLE_COLOR = new Color(230, 230, 230);
    private static final Color SENT_MESSAGE_COLOR = new Color(200, 230, 255);

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 5000;

    public ChatUI(User user1, User user2) {
        this.user1 = user1;
        this.user2 = user2;
        initializeNetworking();
        initializeGUI();
        startMessageListener();
        loadPreviousMessages();
    }

    private void loadPreviousMessages() {
        List<Message> messages = DatabaseManager.getChatHistory(user1.getUsername(), user2.getUsername());
        for (Message message : messages) {
            boolean isSent = message.getSender().getUsername().equals(user1.getUsername());
            addMessage(message.getContent(), message.getSender().getFullName(), isSent, message.getSentTime().toLocalTime());
        }
    }

    private void initializeNetworking() {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            out.writeObject("LOGIN:" + user1.getUsername() + ":" + user1.getPassword());
            out.flush();

            String response = (String) in.readObject();
            if ("LOGIN_SUCCESS".equals(response)) {
                user1 = (User) in.readObject();
            } else {
                JOptionPane.showMessageDialog(this, "Login failed!", "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Cannot connect to server!", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void startMessageListener() {
        new Thread(() -> {
            try {
                while (true) {
                    Object input = in.readObject();
                    if (input instanceof Message) {
                        Message message = (Message) input;
                        SwingUtilities.invokeLater(() -> {
                            addMessage(message.getContent(), message.getSender().getFullName(), false, message.getSentTime().toLocalTime());
                        });
                    } else if (input instanceof String) {
                        String command = (String) input;
                        if (command.startsWith("USER_OFFLINE:")) {
                            String username = command.substring(13);
                            if (username.equals(user2.getUsername())) {
                                SwingUtilities.invokeLater(() -> updateUserStatus(false));
                            }
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void initializeGUI() {
        setTitle("Chat");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 600);
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    if (socket != null && !socket.isClosed()) {
                        socket.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND_COLOR);

        JPanel headerPanel = createHeader();
        add(headerPanel, BorderLayout.NORTH);

        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(BACKGROUND_COLOR);

        scrollPane = new JScrollPane(chatPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBackground(CHAT_COLOR);
        headerPanel.setPreferredSize(new Dimension(getWidth(), 70));

        JPanel profilePanel = new CircularProfilePanel(50, user2);
        profilePanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 10));
        headerPanel.add(profilePanel, BorderLayout.WEST);

        JPanel userInfoPanel = new JPanel();
        userInfoPanel.setLayout(new BoxLayout(userInfoPanel, BoxLayout.Y_AXIS));
        userInfoPanel.setBackground(CHAT_COLOR);

        JLabel nameLabel = new JLabel(user2.getFullName());
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        statusLabel = new JLabel(user2.isOnline() ? "Online" : "Offline");
        Timer timer = new Timer(1000, e -> {
            user2.setOnline(DatabaseManager.getUser(user2.getUsername()).isOnline());
            statusLabel.setText(user2.isOnline() ? "Online" : "Offline");
        });
        timer.start();
        statusLabel.setForeground(new Color(200, 255, 200));
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        userInfoPanel.add(nameLabel);
        userInfoPanel.add(statusLabel);
        headerPanel.add(userInfoPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(CHAT_COLOR);
        JButton videoCallButton = createVideoCallButton();
        videoCallButton.addActionListener(e -> {
            UserList userListUI = new UserList(user1);
            userListUI.setVisible(true);
            this.dispose();
        });
        buttonPanel.add(videoCallButton);
        headerPanel.add(buttonPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JButton createVideoCallButton() {
        JButton videoCallButton = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CHAT_COLOR);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);

                int[] xPoints = {5, 15, 15, 5};
                int[] yPoints = {7, 7, 17, 17};
                g2.fillPolygon(xPoints, yPoints, 4);

                int[] triangleX = {15, 20, 15};
                int[] triangleY = {10, 12, 14};
                g2.fillPolygon(triangleX, triangleY, 3);

                g2.dispose();
            }
        };
        videoCallButton.setPreferredSize(new Dimension(25, 25));
        videoCallButton.setBorderPainted(false);
        videoCallButton.setContentAreaFilled(false);
        videoCallButton.setFocusPainted(false);

        return videoCallButton;
    }

    private static class CircularProfilePanel extends JPanel {
        private final int size;
        private final User user;
        private Color profileColor = new Color(100, 100, 100);

        public CircularProfilePanel(int size, User user) {
            this.size = size;
            this.user = user;
            setPreferredSize(new Dimension(size, size));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(profileColor);
            g2.fill(new Ellipse2D.Double(0, 0, size, size));

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, size / 3));
            FontMetrics fm = g2.getFontMetrics();
            String initials = user.getFullName().substring(0, 2).toUpperCase();
            int textWidth = fm.stringWidth(initials);
            int textHeight = fm.getHeight();
            g2.drawString(initials, 
                         (size - textWidth) / 2,
                         (size + textHeight / 3) / 2);

            g2.dispose();
        }
    }

    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout(10, 0));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        messageField = new JTextField();
        messageField.setFont(new Font("Arial", Font.PLAIN, 14));
        messageField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Color.LIGHT_GRAY, 1, true),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));

        sendButton = new JButton("Send") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CHAT_COLOR);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int textWidth = fm.stringWidth(getText());
                int textHeight = fm.getHeight();
                g2.drawString(getText(), 
                    (getWidth() - textWidth) / 2,
                    (getHeight() + textHeight / 2) / 2 - 2);
                g2.dispose();
            }
        };
        sendButton.setPreferredSize(new Dimension(70, 30));
        sendButton.setBorderPainted(false);
        sendButton.setContentAreaFilled(false);
        sendButton.setFocusPainted(false);
        sendButton.setFont(new Font("Arial", Font.BOLD, 14));

        bottomPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        ActionListener sendAction = e -> sendMessage();
        sendButton.addActionListener(sendAction);
        messageField.addActionListener(sendAction);

        return bottomPanel;
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            try {
                Message msg = new Message(user1, user2, message);
                msg.saveMessage();
                out.writeObject(msg);
                out.flush();
                addMessage(message, "Me", true,LocalTime.now());
                messageField.setText("");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void addMessage(String message, String username, boolean isSent,LocalTime sentTime) {
        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new BorderLayout());
        messagePanel.setBackground(BACKGROUND_COLOR);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(BACKGROUND_COLOR);

        JLabel usernameLabel = new JLabel(username);
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 12));
        usernameLabel.setForeground(CHAT_COLOR);
        contentPanel.add(usernameLabel, BorderLayout.NORTH);

        JTextArea textArea = new JTextArea(message);
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.setOpaque(false);
        textArea.setEditable(false);
        textArea.setFont(new Font("Arial", Font.PLAIN, 14));

        JPanel bubblePanel = new JPanel(new BorderLayout());
        bubblePanel.setBackground(isSent ? SENT_MESSAGE_COLOR : MESSAGE_BUBBLE_COLOR);
        bubblePanel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        bubblePanel.add(textArea);

        JLabel timeLabel = new JLabel(sentTime.format(DateTimeFormatter.ofPattern("HH:mm")));
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        timeLabel.setForeground(Color.GRAY);
        bubblePanel.add(timeLabel, BorderLayout.SOUTH);

        contentPanel.add(bubblePanel, BorderLayout.CENTER);
        messagePanel.add(contentPanel, isSent ? BorderLayout.EAST : BorderLayout.WEST);
        messagePanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        chatPanel.add(messagePanel);
        chatPanel.revalidate();
        chatPanel.repaint();

        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    private void updateUserStatus(boolean isOnline) {
        statusLabel.setText(isOnline ? "Online" : "Offline");
    }
}