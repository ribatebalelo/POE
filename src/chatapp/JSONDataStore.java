package chatapp;

import org.json.JSONArray;
import org.json.JSONObject;
import java.nio.file.Files;
import java.nio.file.Paths;

public class JSONDataStore {
    // Changed to package-private for testing
    static String USERS_FILE = "users.json";
    static String MESSAGES_FILE = "messages.json";
    
    public static synchronized JSONArray getUsers() {
        try {
            if (!Files.exists(Paths.get(USERS_FILE))) {
                return new JSONArray();
            }
            String content = new String(Files.readAllBytes(Paths.get(USERS_FILE)));
            return new JSONArray(content);
        } catch (Exception e) {
            e.printStackTrace();
            return new JSONArray();
        }
    }
    
    public static synchronized void saveUser(JSONObject user) {
        try {
            JSONArray users = getUsers();
            users.put(user);
            Files.write(Paths.get(USERS_FILE), users.toString().getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static synchronized JSONArray getMessages() {
        try {
            if (!Files.exists(Paths.get(MESSAGES_FILE))) {
                return new JSONArray();
            }
            String content = new String(Files.readAllBytes(Paths.get(MESSAGES_FILE)));
            return new JSONArray(content);
        } catch (Exception e) {
            e.printStackTrace();
            return new JSONArray();
        }
    }
    
    public static synchronized void saveMessage(JSONObject message) {
        try {
            JSONArray messages = getMessages();
            message.put("id", messages.length());
            messages.put(message);
            Files.write(Paths.get(MESSAGES_FILE), messages.toString().getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static synchronized void updateMessage(int index, JSONObject updatedMessage) {
        try {
            JSONArray messages = getMessages();
            messages.put(index, updatedMessage);
            Files.write(Paths.get(MESSAGES_FILE), messages.toString().getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static synchronized void deleteMessage(int index) {
        try {
            JSONArray messages = getMessages();
            JSONArray newMessages = new JSONArray();
            for (int i = 0; i < messages.length(); i++) {
                if (i != index) {
                    newMessages.put(messages.get(i));
                }
            }
            Files.write(Paths.get(MESSAGES_FILE), newMessages.toString().getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
