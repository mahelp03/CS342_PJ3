import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class FriendHander {

    private static final Map<String, Set<String>> friends = new HashMap<>();

    public static boolean addFriend(String user, String target) {
        if (user.equals(target)) return false;

        friends.putIfAbsent(user, new HashSet<>());
        friends.putIfAbsent(target, new HashSet<>());

        if (friends.get(user).contains(target)) return false;

        friends.get(user).add(target);
        friends.get(target).add(user);
        return true;
    }

    public static Set<String> getFriends(String user) {
        return friends.getOrDefault(user, new HashSet<>());
    }

    public static boolean areFriends(String user, String target) {
        return friends.getOrDefault(user, new HashSet<>()).contains(target);
    }

     public static void saveFriendsToFile(String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            for (String user : friends.keySet()) {
                Set<String> friendSet = friends.get(user);
                for (String friend : friendSet) {
                    writer.println(user + ":" + friend);
                }
            }
            System.out.println("[SAVE] Friends saved to " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadFriendsFromFile(String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("[LOAD] Friends file not found, starting fresh.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    String user = parts[0];
                    String friend = parts[1];

                    friends.putIfAbsent(user, new HashSet<>());
                    friends.putIfAbsent(friend, new HashSet<>());

                    friends.get(user).add(friend);
                    friends.get(friend).add(user);
                }
            }
            System.out.println("[LOAD] Friends loaded from " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}