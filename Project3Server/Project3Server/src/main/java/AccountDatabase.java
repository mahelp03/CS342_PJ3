import java.util.HashMap;
import java.util.Map;


public class AccountDatabase {

    private static final Map<String, Integer> winMap = new HashMap<>();
    private static final Map<String, Integer> totalMap = new HashMap<>();
    private static Map<String, Integer> gameCounts = new HashMap<>();
    private static Map<String, Integer> winCounts = new HashMap<>();

    public static void incrementGameCount(String username) {
        int prev = totalMap.getOrDefault(username, 0);
        totalMap.put(username, prev + 1);
        System.out.println("[DEBUG] incrementGameCount - " + username + ": " + (prev + 1));
    }
    
    public static void incrementWinCount(String username) {
        int prev = winMap.getOrDefault(username, 0);
        winMap.put(username, prev + 1);
        System.out.println("[DEBUG] incrementWinCount - " + username + ": " + (prev + 1));
    }
    

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
        return totalMap.getOrDefault(username, 0) /2;
    }

    public static double getWinRate(String username) {
        int total = getGameCount(username);  // 이미 /2 된 값
        if (total == 0) return 0.0;

        int wins = winMap.getOrDefault(username, 0) / 2;  // 👈 여기도 나눠줘야 정확
        return (wins * 100.0) / total;
    }

    public static int getWinCount(String username) {
        return winMap.getOrDefault(username, 0) / 2;  // 👈 여기 추가
    }

    public static int getRawGameCount(String username) {
        return totalMap.getOrDefault(username, 0);
    }
    
    public static double getRawWinRate(String username) {
        int total = getRawGameCount(username);
        if (total == 0) return 0.0;
        return (winMap.getOrDefault(username, 0) * 100.0) / total;
    }
}
