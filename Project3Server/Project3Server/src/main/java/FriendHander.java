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
}