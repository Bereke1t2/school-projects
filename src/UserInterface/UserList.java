package UserInterface;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import Backend.Accounts.*;
import Backend.Database.DatabaseManager;

public class UserList extends JFrame {
    private User user;
    private JPanel mainPanel;
    private JPanel sidebarPanel;
    private ArrayList<UserPanel> userPanels;
    private JTextField searchField;
    private Color primaryColor = new Color(42, 171, 238);
    private Color backgroundColor = new Color(17, 27, 33);
    private Color textColor = new Color(233, 237, 239);
    private Color onlineColor = new Color(46, 204, 113);
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public UserList(User user) {
        setTitle("User List");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 700);
        this.user = user;

        try {
            Socket socket = new Socket("localhost", 5000);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // Send login credentials
            out.writeObject("LOGIN:" + user.getUsername() + ":" + user.getPassword());
            out.flush();

            // Read server response
            String response = (String) in.readObject();
            if ("LOGIN_SUCCESS".equals(response)) {
                User loggedInUser = (User) in.readObject();
                System.out.println("Logged in as: " + loggedInUser.getFullName());
                new Thread(new ServerListener()).start();
            } else {
                System.out.println("Login failed");
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(backgroundColor);

        JPanel searchPanel = createSearchPanel();
        mainPanel.add(searchPanel, BorderLayout.NORTH);

        sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(backgroundColor);

        JScrollPane scrollPane = new JScrollPane(sidebarPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        userPanels = new ArrayList<>();
        addSampleUsers();

        add(mainPanel);
        setLocationRelativeTo(null);
    }

    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(backgroundColor);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(0, 35));
        searchField.setBackground(new Color(30, 42, 48));
        searchField.setForeground(textColor);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(30, 42, 48)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { search(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { search(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { search(); }
        });

        searchPanel.add(searchField, BorderLayout.CENTER);
        return searchPanel;
    }

    private void search() {
        String searchText = searchField.getText().toLowerCase();
        for (UserPanel userPanel : userPanels) {
            boolean visible = userPanel.getName().toLowerCase().contains(searchText) ||
                            userPanel.getLastMessage().toLowerCase().contains(searchText);
            userPanel.setVisible(visible);
        }
        sidebarPanel.revalidate();
        sidebarPanel.repaint();
    }

    private void addSampleUsers() {
        List<User> users = DatabaseManager.getUsers();
        for (User user2 : users) {
            if (user2.getUsername().equals(this.user.getUsername())) continue;
            Message m = DatabaseManager.getLastMessage(this.user.getUsername(), user2.getUsername());
            UserPanel userPanel = new UserPanel(user2, m != null ? m.getContent() : " ");
            userPanels.add(userPanel);
            sidebarPanel.add(userPanel);
        }
    }

    private class UserPanel extends JPanel {
        private User user;
        private String lastMessage;
        private boolean isOnline;

        public UserPanel(User user, String lastMessage) {
            this.user = user;
            this.lastMessage = lastMessage;
            this.isOnline = user.isOnline();

            setLayout(new BorderLayout());
            setBackground(backgroundColor);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(50, 50, 50)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

            JPanel avatarPanel = new JPanel(new RelativeLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    if (user.getProfilePic() != null) {
                        g2d.drawImage(user.getProfilePic().getImage(), 0, 0, 50, 50, null);
                    } else {
                        g2d.setColor(primaryColor);
                        g2d.fillOval(0, 0, 50, 50);

                        g2d.setColor(Color.WHITE);
                        g2d.setFont(new Font("Arial", Font.BOLD, 20));
                        FontMetrics fm = g2d.getFontMetrics();
                        String initials = user.getFullName().substring(0, 1);
                        int x = (50 - fm.stringWidth(initials)) / 2;
                        int y = ((50 - fm.getHeight()) / 2) + fm.getAscent();
                        g2d.drawString(initials, x, y);
                    }

                    if (isOnline) {
                        g2d.setColor(onlineColor);
                        g2d.fillOval(35, 35, 15, 15);
                        g2d.setColor(backgroundColor);
                        g2d.drawOval(35, 35, 15, 15);
                    }
                }
            };
            avatarPanel.setPreferredSize(new Dimension(50, 50));

            JPanel textPanel = new JPanel();
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
            textPanel.setBackground(backgroundColor);
            textPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

            JLabel nameLabel = new JLabel(user.getFullName());
            nameLabel.setForeground(textColor);
            nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
            nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel messageLabel = new JLabel(lastMessage);
            messageLabel.setForeground(new Color(145, 145, 145));
            messageLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            messageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel onlineLabel = new JLabel(isOnline ? "online" : "offline");
            onlineLabel.setForeground(isOnline ? onlineColor : new Color(145, 145, 145));
            onlineLabel.setFont(new Font("Arial", Font.PLAIN, 11));
            onlineLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            textPanel.add(nameLabel);
            textPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            textPanel.add(messageLabel);
            textPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            textPanel.add(onlineLabel);

            add(avatarPanel, BorderLayout.WEST);
            add(textPanel, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    setBackground(new Color(30, 42, 48));
                    textPanel.setBackground(new Color(30, 42, 48));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setBackground(backgroundColor);
                    textPanel.setBackground(backgroundColor);
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    new ChatUI(UserList.this.user, user).setVisible(true);
                    UserList.this.dispose();
                }
            });
        }

        public String getName() {
            return user.getFullName();
        }

        public String getLastMessage() {
            return lastMessage;
        }

        public void setOnline(boolean isOnline) {
            this.isOnline = isOnline;
            repaint();
        }
    }

    private class ServerListener implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    Object input = in.readObject();
                    if (input instanceof String) {
                        String message = (String) input;
                        if (message.startsWith("USER_ONLINE:")) {
                            String username = message.substring(12);
                            updateUserStatus(username, true);
                        } else if (message.startsWith("USER_OFFLINE:")) {
                            String username = message.substring(13);
                            updateUserStatus(username, false);
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        private void updateUserStatus(String username, boolean isOnline) {
            for (UserPanel userPanel : userPanels) {
                if (userPanel.user.getUsername().equals(username)) {
                    userPanel.setOnline(isOnline);
                    break;
                }
            }
        }
    }

    private class RelativeLayout implements LayoutManager {
        public void addLayoutComponent(String name, Component comp) {}
        public void removeLayoutComponent(Component comp) {}
        public Dimension preferredLayoutSize(Container parent) {
            return new Dimension(50, 50);
        }
        public Dimension minimumLayoutSize(Container parent) {
            return new Dimension(50, 50);
        }
        public void layoutContainer(Container parent) {}
    }
}