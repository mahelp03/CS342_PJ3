import java.util.ArrayList;
import java.util.List;

public class GameRoom {
    private final String roomCode;
    private final String roomName;
    private final List<String> players = new ArrayList<>();

    public GameRoom(String roomCode, String roomName, String creator) {
        this.roomCode = roomCode;
        this.roomName = roomName;
        this.players.add(creator);
    }

    public String getRoomCode() {
        return roomCode;
    }

    public String getRoomName() {
        return roomName;
    }

    public boolean isFull() {
        return players.size() >= 2;
    }

    public void addPlayer(String username) {
        if (!isFull()) players.add(username);
    }

    public List<String> getPlayers() {
        return players;
    }

    public void removePlayer(String username) {
        players.remove(username);
    }

    public boolean hasPlayer(String username) {
        return players.contains(username);
    }
    
}