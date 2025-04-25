import java.util.HashMap;
import java.util.Map;


public class AccountDatabase {

    private static final Map<String, Integer> winMap = new HashMap<>();
    private static final Map<String, Integer> totalMap = new HashMap<>();
<<<<<<< Updated upstream
=======
    private static Map<String, Integer> gameCounts = new HashMap<>();
    private static Map<String, Integer> winCounts = new HashMap<>();
    private static final Map<String, List<String>> gameHistories = new HashMap<>();
    private static final Set<String> processedHistoryKeys = new HashSet<>();
    private static final Map<String, String> passwordMap = new HashMap<>();


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
    
>>>>>>> Stashed changes

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
<<<<<<< Updated upstream
=======

    public static void addGameResult(String roomCode, String player1, String player2, String winner) {
        String result = String.format("Room %s - %s vs %s: %s Won!!", roomCode, player1, player2, winner);
        if (processedHistoryKeys.contains(result)) return;
        processedHistoryKeys.add(result);
        // 각 플레이어에게 히스토리 기록 추가
        gameHistories.computeIfAbsent(player1, k -> new ArrayList<>()).add(result);
        gameHistories.computeIfAbsent(player2, k -> new ArrayList<>()).add(result);

        System.out.println("[HISTORY] " + result);
        System.out.println("  → " + player1 + ": " + gameHistories.get(player1));
        System.out.println("  → " + player2 + ": " + gameHistories.get(player2));
    }

    public static List<String> getGameHistory(String username) {
        return gameHistories.getOrDefault(username, new ArrayList<>());
    }
    public static void printAllAccountData() {
        System.out.println("[SHUTDOWN] Dumping all account data:");
        for (String user : passwordMap.keySet()) {
            String password = passwordMap.get(user);
            int wins = winMap.getOrDefault(user, 0);
            int total = totalMap.getOrDefault(user, 0);
            List<String> history = gameHistories.getOrDefault(user, new ArrayList<>());
    
            System.out.println("USER: " + user);
            System.out.println("PASS: " + password);
            System.out.println("WINS: " + wins);
            System.out.println("TOTAL: " + total);
            for (String entry : history) {
                System.out.println("HIST: " + entry);
            }
            System.out.println("---");
        }
    }
    public static void setPassword(String username, String password) {
        passwordMap.put(username, password);
    }
    
    
>>>>>>> Stashed changes
}
