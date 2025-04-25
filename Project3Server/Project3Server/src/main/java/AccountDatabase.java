import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class AccountDatabase {

    private static final Map<String, Integer> winMap = new HashMap<>();
    private static final Map<String, Integer> totalMap = new HashMap<>();
    private static Map<String, Integer> gameCounts = new HashMap<>();
    private static Map<String, Integer> winCounts = new HashMap<>();
    private static final Map<String, List<String>> gameHistories = new HashMap<>();
    private static final Set<String> processedHistoryKeys = new HashSet<>();

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

    public static void loadFromFile(String filename) {
        File file = new File(filename);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                String username = "";
                String password = "";
                int wins = 0;
                int total = 0;
                List<String> history = new ArrayList<>();

                for (String field : fields) {
                    if (field.startsWith("username:")) username = field.substring(9);
                    else if (field.startsWith("password:")) password = field.substring(9);
                    else if (field.startsWith("wins:")) wins = Integer.parseInt(field.substring(5));
                    else if (field.startsWith("total:")) total = Integer.parseInt(field.substring(6));
                    else if (field.startsWith("history:")) {
                        String[] histItems = field.substring(8).split(";");
                        history = new ArrayList<>(List.of(histItems));
                    }
                }

                LoginHandler.getAllUsers().put(username, password);
                winMap.put(username, wins);
                totalMap.put(username, total);
                gameHistories.put(username, history);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void saveToFile(String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            for (String username : LoginHandler.getAllUsers().keySet()) {
                String password = LoginHandler.getAllUsers().get(username);
                int wins = winMap.getOrDefault(username, 0);
                int total = totalMap.getOrDefault(username, 0);
                List<String> history = gameHistories.getOrDefault(username, new ArrayList<>());
                String historyStr = String.join(";", history);

                writer.printf("username:%s,password:%s,wins:%d,total:%d,history:%s\n",
                        username, password, wins, total, historyStr);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
}
