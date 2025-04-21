import java.util.HashMap;
import java.util.Map;


public class AccountDatabase {

    private static final Map<String, Integer> winMap = new HashMap<>();
    private static final Map<String, Integer> totalMap = new HashMap<>();

    public static void initUser(String username) {
        winMap.put(username, 0);
        totalMap.put(username, 0);
    }

    public static void recordGame(String username, boolean win) {
        totalMap.put(username, totalMap.getOrDefault(username, 0) + 1);
        if (win) {
            winMap.put(username, winMap.getOrDefault(username, 0) + 1);
        }
    }

    public static int getGameCount(String username) {
        return totalMap.getOrDefault(username, 0);
    }

    public static double getWinRate(String username) {
        int total = getGameCount(username);
        if (total == 0) return 0.0;
        return (winMap.getOrDefault(username, 0) * 100.0) / total;
    }
}
