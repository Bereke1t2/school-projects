package UserInterface;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.File;
import java.awt.image.BufferedImage;
import Backend.Database.DatabaseManager;
import Backend.Accounts.User;
import UserInterface.UserList;

public class ChatAppAuth extends JFrame {
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private Color primaryColor = new Color(42, 171, 238);
    private Color backgroundColor = new Color(17, 27, 33);
    private Color textColor = new Color(233, 237, 239);
    private ImageIcon profileImage;
    private JLabel profileImageLabel;

    public ChatAppAuth() {
        setTitle("Chat App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 600);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(backgroundColor);

        JPanel loginPanel = createLoginPanel();
        JPanel signupPanel = createSignupPanel();

        mainPanel.add(loginPanel, "login");
        mainPanel.add(signupPanel, "signup");

        add(mainPanel);
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(17, 27, 33),
                    0, getHeight(), new Color(25, 40, 48)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                g2d.setColor(new Color(42, 171, 238, 30));
                g2d.fillOval(-50, -50, 200, 200);
                g2d.fillOval(getWidth() - 100, getHeight() - 100, 200, 200);
            }
        };
        panel.setBackground(backgroundColor);

        JLabel titleLabel = new JLabel("Welcome Back!");
        titleLabel.setForeground(textColor);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setBounds(100, 80, 200, 40);
        panel.add(titleLabel);

        JTextField usernameField = createStyledTextField("Username");
        usernameField.setBounds(50, 180, 300, 45);
        panel.add(usernameField);

        JPasswordField passwordField = createStyledPasswordField("Password");
        passwordField.setBounds(50, 240, 300, 45);
        panel.add(passwordField);

        JButton loginButton = createStyledButton("Login");
        loginButton.setBounds(50, 320, 300, 45);
        loginButton.addActionListener(e -> {
            if (DatabaseManager.authenticateUser(usernameField.getText(), new String(passwordField.getPassword()))) {
                User user = DatabaseManager.getUser(usernameField.getText());
                new UserList(user).setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
                });
        panel.add(loginButton);

        JLabel signupLink = new JLabel("Don't have an account? Sign up");
        signupLink.setForeground(primaryColor);
        signupLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        signupLink.setBounds(100, 380, 200, 30);
        signupLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                cardLayout.show(mainPanel, "signup");
            }
        });
        panel.add(signupLink);

        return panel;
    }

    private JPanel createSignupPanel() {
        JPanel panel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(17, 27, 33),
                    0, getHeight(), new Color(25, 40, 48)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                g2d.setColor(new Color(42, 171, 238, 30));
                g2d.fillOval(-50, -50, 200, 200);
                g2d.fillOval(getWidth() - 100, getHeight() - 100, 200, 200);
            }
        };
        panel.setBackground(backgroundColor);

        JLabel titleLabel = new JLabel("Create Account");
        titleLabel.setForeground(textColor);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setBounds(100, 40, 200, 40);
        panel.add(titleLabel);

        profileImageLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (profileImage != null) {
                    Image img = profileImage.getImage();
                    int diameter = Math.min(getWidth(), getHeight());
                    BufferedImage circleBuffer = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2 = circleBuffer.createGraphics();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setClip(new Ellipse2D.Float(0, 0, diameter, diameter));
                    g2.drawImage(img, 0, 0, diameter, diameter, null);
                    g2.dispose();
                    g2d.drawImage(circleBuffer, 0, 0, null);
                } else {
                    g2d.setColor(primaryColor);
                    g2d.fillOval(0, 0, getWidth(), getHeight());
                    g2d.setColor(textColor);
                    g2d.setFont(new Font("Segoe UI", Font.BOLD, 40));
                    FontMetrics fm = g2d.getFontMetrics();
                    String text = "+";
                    int x = (getWidth() - fm.stringWidth(text)) / 2;
                    int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                    g2d.drawString(text, x, y);
                }
            }
        };
        profileImageLabel.setBounds(150, 100, 100, 100);
        profileImageLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        profileImageLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                chooseProfileImage();
            }
        });
        panel.add(profileImageLabel);

        JTextField fullNameField = createStyledTextField("Full Name");
        fullNameField.setBounds(50, 220, 300, 45);
        panel.add(fullNameField);

        JTextField usernameField = createStyledTextField("Username");
        usernameField.setBounds(50, 280, 300, 45);
        panel.add(usernameField);

        JPasswordField passwordField = createStyledPasswordField("Password");
        passwordField.setBounds(50, 340, 300, 45);
        panel.add(passwordField);

        JButton signupButton = createStyledButton("Sign Up");
        signupButton.setBounds(50, 420, 300, 45);
        signupButton.addActionListener(e -> {
            if (DatabaseManager.userExists(usernameField.getText())) {
                JOptionPane.showMessageDialog(this, "Username already exists", "Signup Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }
            else if (usernameField.getText().isEmpty() || fullNameField.getText().isEmpty() || passwordField.getPassword().length == 0) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields", "Signup Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }
            else if (passwordField.getPassword().length < 6) {
                JOptionPane.showMessageDialog(this, "Password must be at least 6 characters long", "Signup Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }
            else{
                JOptionPane.showMessageDialog(this, "Account created successfully", "Signup Successful", JOptionPane.INFORMATION_MESSAGE);
            }
            User user = new User(usernameField.getText(), fullNameField.getText(), new String(passwordField.getPassword()), profileImage);
            DatabaseManager.createUser(user);
            new UserList(user).setVisible(true);
            dispose();
        });
        panel.add(signupButton);

        // Login link
        JLabel loginLink = new JLabel("Already have an account? Login");
        loginLink.setForeground(primaryColor);
        loginLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginLink.setBounds(100, 480, 200, 30);
        loginLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                cardLayout.show(mainPanel, "login");
            }
        });
        panel.add(loginLink);

        return panel;
    }

    private JTextField createStyledTextField(String placeholder) {
        JTextField textField = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().length() == 0) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(new Color(145, 145, 145));
                    g2d.setFont(getFont().deriveFont(Font.PLAIN));
                    g2d.drawString(placeholder, getInsets().left, g.getFontMetrics().getMaxAscent() + getInsets().top);
                }
            }
        };
        textField.setBackground(new Color(30, 42, 48));
        textField.setForeground(textColor);
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textField.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(10, new Color(42, 171, 238)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return textField;
    }

    private JPasswordField createStyledPasswordField(String placeholder) {
        JPasswordField passwordField = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getPassword().length == 0) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(new Color(145, 145, 145));
                    g2d.setFont(getFont().deriveFont(Font.PLAIN));
                    g2d.drawString(placeholder, getInsets().left, g.getFontMetrics().getMaxAscent() + getInsets().top);
                }
            }
        };
        passwordField.setBackground(new Color(30, 42, 48));
        passwordField.setForeground(textColor);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(10, new Color(42, 171, 238)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return passwordField;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2d.setColor(primaryColor.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(primaryColor.brighter());
                } else {
                    g2d.setColor(primaryColor);
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                FontMetrics fm = g2d.getFontMetrics();
                Rectangle2D r = fm.getStringBounds(text, g2d);
                int x = (getWidth() - (int) r.getWidth()) / 2;
                int y = (getHeight() - (int) r.getHeight()) / 2 + fm.getAscent();
                g2d.setColor(Color.WHITE);
                g2d.drawString(text, x, y);
            }
        };
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void chooseProfileImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Image files", "jpg", "jpeg", "png", "gif"));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            profileImage = new ImageIcon(file.getPath());
            Image img = profileImage.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            profileImage = new ImageIcon(img);
            profileImageLabel.repaint();
        }
    }

    private static class RoundedBorder implements Border {
        private int radius;
        private Color color;

        RoundedBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(color);
            g2d.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2d.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(4, 8, 4, 8);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }

}