package Backend.Accounts;

import java.io.Serializable;
import java.time.LocalDateTime;

import Backend.Database.DatabaseManager;

public class Message implements Serializable {
    private User sender;
    private User receiver;
    private String content;
    private LocalDateTime sentTime;
    
    public Message(User sender, User receiver, String content) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.sentTime = LocalDateTime.now();
    }
    
    // Getters and Setters
    public User getSender() {
        return sender;
    }
    
    public void setSender(User sender) {
        this.sender = sender;
    }
    
    public User getReceiver() {
        return receiver;
    }
    
    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public LocalDateTime getSentTime() {
        return sentTime;
    }
    
    public void setSentTime(LocalDateTime sentTime) {
        this.sentTime = sentTime;
    }
    public void saveMessage(){
        DatabaseManager.saveMessage(this);
    }
}