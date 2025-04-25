import java.util.Map;
import java.util.HashMap;

public class RoomManager {
    private static final Map<String, GameRoom> rooms = new HashMap<>();

    public static synchronized GameRoom joinRoom(String code, String player) {
        GameRoom room = rooms.get(code);
        if (room != null && !room.isFull()) {
            room.addPlayer(player);
            return room;
        }
        return null;
    }

    public static synchronized GameRoom getAvailableRoom() {
        for (GameRoom room : rooms.values()) {
            if (!room.isFull()) return room;
        }
        return null;
    }

    public static synchronized boolean roomExists(String code) {
        return rooms.containsKey(code);
    }
}
