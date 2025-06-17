package chatapp;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class ChatAppTest {
    private static final String TEST_USERS_FILE = "test_users.json";
    private static final String TEST_MESSAGES_FILE = "test_messages.json";

    @Before
    public void setUp() throws Exception {
        // Redirect to test files
        JSONDataStore.USERS_FILE = TEST_USERS_FILE;
        JSONDataStore.MESSAGES_FILE = TEST_MESSAGES_FILE;
        
        // Clean up test files
        Files.deleteIfExists(Paths.get(TEST_USERS_FILE));
        Files.deleteIfExists(Paths.get(TEST_MESSAGES_FILE));
    }

    @Test
    public void testUserRegistration() throws Exception {
        // Create test user
        JSONObject newUser = new JSONObject();
        newUser.put("username", "test_1");
        newUser.put("password", "Pass123!");
        newUser.put("phone", "+27681234567");
        
        // Save user
        JSONDataStore.saveUser(newUser);
        
        // Verify user was saved
        JSONArray users = JSONDataStore.getUsers();
        assertEquals(1, users.length());
        assertEquals("test_1", users.getJSONObject(0).getString("username"));
        assertEquals("Pass123!", users.getJSONObject(0).getString("password"));
        assertEquals("+27681234567", users.getJSONObject(0).getString("phone"));
    }

    @Test
    public void testMessageOperations() throws Exception {
        // Create test message
        JSONObject message = new JSONObject();
        message.put("from", "user1");
        message.put("to", "user2");
        message.put("message", "Hello");
        message.put("timestamp", System.currentTimeMillis());
        message.put("delivered", false);
        message.put("read", false);
        
        // Save message
        JSONDataStore.saveMessage(message);
        
        // Verify message was saved
        JSONArray messages = JSONDataStore.getMessages();
        assertEquals(1, messages.length());
        
        JSONObject savedMessage = messages.getJSONObject(0);
        assertEquals("user1", savedMessage.getString("from"));
        assertEquals("user2", savedMessage.getString("to"));
        assertEquals("Hello", savedMessage.getString("message"));
        assertFalse(savedMessage.getBoolean("delivered"));
        assertFalse(savedMessage.getBoolean("read"));
        
        // Test message update
        savedMessage.put("delivered", true);
        JSONDataStore.updateMessage(0, savedMessage);
        
        messages = JSONDataStore.getMessages();
        assertTrue(messages.getJSONObject(0).getBoolean("delivered"));
        
        // Test message deletion
        JSONDataStore.deleteMessage(0);
        messages = JSONDataStore.getMessages();
        assertEquals(0, messages.length());
    }

    @After
    public void tearDown() throws Exception {
        // Clean up test files
        Files.deleteIfExists(Paths.get(TEST_USERS_FILE));
        Files.deleteIfExists(Paths.get(TEST_MESSAGES_FILE));
    }
}