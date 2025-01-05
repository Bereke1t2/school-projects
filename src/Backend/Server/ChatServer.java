package Backend.Server;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import Backend.Accounts.Message;
import Backend.Accounts.User;
import Backend.Database.DatabaseManager;

public class ChatServer {
    private static final int PORT = 5000;
    private static Map<String, ClientHandler> onlineClients = new ConcurrentHashMap<>();
    private static DatabaseManager dbManager;

    public static void main(String[] args) {
        // Initialize database
        dbManager = new DatabaseManager();
        dbManager.initializeDatabase();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Chat Server is running on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress().getHostAddress());
                
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private User user;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                while (true) {
                    Object input = in.readObject();
                    
                    if (input instanceof String) {
                        String command = (String) input;
                        if (command.startsWith("LOGIN:")) {
                            handleLogin(command.substring(6));
                        }
                    } else if (input instanceof User) {
                        // Handle new user registration
                        User newUser = (User) input;
                        handleRegistration(newUser);
                    } else if (input instanceof Message) {
                        // Handle message
                        Message message = (Message) input;
                        handleMessage(message);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                handleDisconnection();
            }
        }

        private void handleLogin(String credentials) {
            try {
                String[] parts = credentials.split(":");
                String username = parts[0];
                String password = parts[1];

                if (dbManager.authenticateUser(username, password)) {
                    this.user = dbManager.getUser(username);
                    this.user.setOnline(true);
                    dbManager.updateUserStatus(username, true);
                    onlineClients.put(username, this);
                    
                    // Send success response
                    out.writeObject("LOGIN_SUCCESS");
                    out.writeObject(user);
                    
                    // Broadcast online status to other users
                    broadcastUserStatus(username, true);
                    
                    // Send online users list to the newly connected user
                    sendOnlineUsersList();
                } else {
                    out.writeObject("LOGIN_FAILED");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void handleRegistration(User newUser) {
            try {
                if (dbManager.getUser(newUser.getUsername()) == null) {
                    dbManager.createUser(newUser);
                    out.writeObject("REGISTRATION_SUCCESS");
                } else {
                    out.writeObject("REGISTRATION_FAILED:Username already exists");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void handleMessage(Message message) {
            try {
                // Save message to database
                dbManager.saveMessage(message);
                
                // Send to recipient if online
                ClientHandler recipient = onlineClients.get(message.getReceiver().getUsername());
                if (recipient != null) {
                    recipient.out.writeObject(message);
                    recipient.out.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void handleDisconnection() {
            if (user != null) {
                String username = user.getUsername();
                onlineClients.remove(username);
                dbManager.updateUserStatus(username, false);
                broadcastUserStatus(username, false);
            }
            
            try {
                if (socket != null) socket.close();
                if (in != null) in.close();
                if (out != null) out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void broadcastUserStatus(String username, boolean isOnline) {
            String statusMessage = isOnline ? "USER_ONLINE:" + username : "USER_OFFLINE:" + username;
            for (ClientHandler client : onlineClients.values()) {
                try {
                    if (!client.user.getUsername().equals(username)) {
                        client.out.writeObject(statusMessage);
                        client.out.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void sendOnlineUsersList() {
            try {
                List<User> onlineUsers = new ArrayList<>();
                for (String username : onlineClients.keySet()) {
                    if (!username.equals(user.getUsername())) {
                        User onlineUser = dbManager.getUser(username);
                        onlineUsers.add(onlineUser);
                    }
                }
                out.writeObject("ONLINE_USERS");
                out.writeObject(onlineUsers);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}