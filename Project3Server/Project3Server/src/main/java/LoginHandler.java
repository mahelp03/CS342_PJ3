import java.util.HashMap;
import java.util.Map;

public class LoginHandler {
    private static final Map<String, String> accounts = new HashMap<>();

    public static synchronized String signup(String username, String password) {
        if (accounts.containsKey(username)) return "SIGNUP_FAIL";
        accounts.put(username, password);
        return "OK";
    }

    public static synchronized String login(String username, String password) {
        return accounts.containsKey(username) && accounts.get(username).equals(password)
            ? "OK" : "LOGIN_FAIL";
    }

    public static synchronized Map<String, String> getAllUsers() {
        return accounts;
    }
}

