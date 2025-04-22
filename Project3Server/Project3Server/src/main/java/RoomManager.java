import java.util.Map;
import java.util.HashMap;

public class RoomManager {
    private static final Map<String, GameRoom> rooms = new HashMap<>();

    // public static synchronized String createRoom(String creator) {
    //     String code = generateRoomCode();
    //     GameRoom room = new GameRoom(code, creator);
    //     rooms.put(code, room);
    //     return code;
    // }

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

    // private static String generateRoomCode() {
    //     String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    //     StringBuilder code = new StringBuilder();
    //     for (int i = 0; i < 6; i++) {
    //         int idx = (int) (Math.random() * chars.length());
    //         code.append(chars.charAt(idx));
    //     }
    //     return code.toString();
    // }
}
