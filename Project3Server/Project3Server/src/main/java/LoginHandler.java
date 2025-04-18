import java.util.HashMap;
import java.util.Map;

public class LoginHandler {
    private static final Map<String, String> users = new HashMap<>();

    public static synchronized String signup(String username, String password) {
        if (users.containsKey(username)) return "EXISTS";
        users.put(username, password);
        return "OK";
    }

    public static synchronized String login(String username, String password) {
        if (!users.containsKey(username)) return "NOT_FOUND";
        if (!users.get(username).equals(password)) return "WRONG_PW";
        return "OK";
    }

    public static synchronized boolean hasUser(String username) {
        return users.containsKey(username);
    }

    public static synchronized void printAllUsers() {
        System.out.println("--- Registered Users ---");
        for (Map.Entry<String, String> entry : users.entrySet()) {
            System.out.println("Username: " + entry.getKey() + ", Password: " + entry.getValue());
        }
        System.out.println("------------------------");
    }

    public static synchronized Map<String, String> getAllUsers() {
        return new HashMap<>(users);
    }
} 
