package Backend.Accounts;
import java.io.Serializable;
import javax.swing.ImageIcon;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class User implements Serializable {
    private String username;
    private String fullName;
    private String password;
    private ImageIcon profilePic;
    private boolean isOnline;
    
    public User(String username, String fullName, String password, ImageIcon profilePic) {
        this.username = username;
        this.fullName = fullName;
        this.password = password;
        this.profilePic = profilePic;
        this.isOnline = false;
    }
    
    // Getters and Setters
    public String getUsername() {
        return username;
    }
    
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public ImageIcon getProfilePic() {
        return profilePic;
    }
    
    public void setProfilePic(ImageIcon profilePic) {
        this.profilePic = profilePic;
    }
    
    public boolean isOnline() {
        return isOnline;
    }
    
    public void setOnline(boolean online) {
        isOnline = online;
    }
}
