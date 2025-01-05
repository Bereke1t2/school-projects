import javax.swing.UIManager;

import Backend.Database.DatabaseManager;
import UserInterface.ChatAppAuth;
public class App {
    public static void main(String[] args) throws Exception {
        // DatabaseManager.initializeDatabase();
        new ChatAppAuth().setVisible(true);
    }
}
