package Backend.Database;

import java.sql.*;
import java.util.Properties;

import java.sql.*;
import java.time.LocalDateTime;
import javax.swing.ImageIcon;
import java.io.*;

import Backend.Accounts.User;
import Backend.Accounts.Message;

import java.util.List;
import java.util.ArrayList;


public class DatabaseManager {
    private static final String URL = "jdbc:postgresql://localhost:5432/chatapp";
    private static final String USER = "postgres";
    private static final String PASSWORD = "1234";
    
    // Initialize database connection
    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
    
    // Create tables if they don't exist
    public static void initializeDatabase() {
        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                username VARCHAR(50) PRIMARY KEY,
                full_name VARCHAR(100) NOT NULL,
                password VARCHAR(100) NOT NULL,
                profile_pic BYTEA,
                is_online BOOLEAN DEFAULT FALSE
            )""";
            
        String createMessagesTable = """
            CREATE TABLE IF NOT EXISTS messages (
                message_id SERIAL PRIMARY KEY,
                sender_username VARCHAR(50) REFERENCES users(username),
                receiver_username VARCHAR(50) REFERENCES users(username),
                content TEXT NOT NULL,
                sent_time TIMESTAMP NOT NULL
            )""";
            
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createMessagesTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // User Operations
    public static void createUser(User user) {
        String sql = "INSERT INTO users (username, full_name, password, profile_pic, is_online) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getFullName());
            pstmt.setString(3, user.getPassword());
            
            // Convert ImageIcon to byte array
            if (user.getProfilePic() != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(user.getProfilePic());
                byte[] profilePicBytes = baos.toByteArray();
                pstmt.setBytes(4, profilePicBytes);
            } else {
                pstmt.setNull(4, Types.BINARY);
            }
            
            pstmt.setBoolean(5, user.isOnline());
            pstmt.executeUpdate();
            
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }
    
    public static User getUser(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                ImageIcon profilePic = null;
                byte[] profilePicBytes = rs.getBytes("profile_pic");
                
                if (profilePicBytes != null) {
                    ByteArrayInputStream bais = new ByteArrayInputStream(profilePicBytes);
                    ObjectInputStream ois = new ObjectInputStream(bais);
                    profilePic = (ImageIcon) ois.readObject();
                }
                
                User user = new User(
                    rs.getString("username"),
                    rs.getString("full_name"),
                    rs.getString("password"),
                    profilePic
                );
                user.setOnline(rs.getBoolean("is_online"));
                return user;
            }
        } catch (SQLException | IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static void updateUserStatus(String username, boolean isOnline) {
        String sql = "UPDATE users SET is_online = ? WHERE username = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setBoolean(1, isOnline);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static void updateUserProfile(User user) {
        String sql = "UPDATE users SET full_name = ?, password = ?, profile_pic = ? WHERE username = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getFullName());
            pstmt.setString(2, user.getPassword());
            
            if (user.getProfilePic() != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(user.getProfilePic());
                byte[] profilePicBytes = baos.toByteArray();
                pstmt.setBytes(3, profilePicBytes);
            } else {
                pstmt.setNull(3, Types.BINARY);
            }
            
            pstmt.setString(4, user.getUsername());
            pstmt.executeUpdate();
            
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }
    
    // Message Operations
    public static void saveMessage(Message message) {
        String sql = "INSERT INTO messages (sender_username, receiver_username, content, sent_time) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, message.getSender().getUsername());
            pstmt.setString(2, message.getReceiver().getUsername());
            pstmt.setString(3, message.getContent());
            pstmt.setTimestamp(4, Timestamp.valueOf(message.getSentTime()));
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static List<Message> getChatHistory(String user1, String user2) {
        List<Message> messages = new ArrayList<>();
        String sql = """
            SELECT * FROM messages 
            WHERE (sender_username = ? AND receiver_username = ?)
            OR (sender_username = ? AND receiver_username = ?)
            ORDER BY sent_time""";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user1);
            pstmt.setString(2, user2);
            pstmt.setString(3, user2);
            pstmt.setString(4, user1);
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                User sender = getUser(rs.getString("sender_username"));
                User receiver = getUser(rs.getString("receiver_username"));
                String content = rs.getString("content");
                LocalDateTime sentTime = rs.getTimestamp("sent_time").toLocalDateTime();
                
                Message message = new Message(sender, receiver, content);
                message.setSentTime(sentTime);
                messages.add(message);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return messages;
    }
    public static boolean userExists(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return true;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    public static Message getLastMessage(String user1, String user2) {
        String sql = """
            SELECT * FROM messages 
            WHERE (sender_username = ? AND receiver_username = ?)
            OR (sender_username = ? AND receiver_username = ?)
            ORDER BY sent_time DESC
            LIMIT 1""";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user1);
            pstmt.setString(2, user2);
            pstmt.setString(3, user2);
            pstmt.setString(4, user1);
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                User sender = getUser(rs.getString("sender_username"));
                User receiver = getUser(rs.getString("receiver_username"));
                String content = rs.getString("content");
                LocalDateTime sentTime = rs.getTimestamp("sent_time").toLocalDateTime();
                
                Message message = new Message(sender, receiver, content);
                message.setSentTime(sentTime);
                return message;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    public static List<User> getUsers() {
        List<User> Users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                ImageIcon profilePic = null;
                byte[] profilePicBytes = rs.getBytes("profile_pic");
                
                if (profilePicBytes != null) {
                    ByteArrayInputStream bais = new ByteArrayInputStream(profilePicBytes);
                    ObjectInputStream ois = new ObjectInputStream(bais);
                    profilePic = (ImageIcon) ois.readObject();
                }
                
                User user = new User(
                    rs.getString("username"),
                    rs.getString("full_name"),
                    rs.getString("password"),
                    profilePic
                );
                user.setOnline(false);
                Users.add(user);
            }
            
        } catch (SQLException | IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        
        return Users;
    }
    
    public static boolean authenticateUser(String username, String password) {
        String sql = "SELECT password FROM users WHERE username = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("password").equals(password);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
}