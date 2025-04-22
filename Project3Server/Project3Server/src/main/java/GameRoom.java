import java.util.ArrayList;
import java.util.List;

public class GameRoom {
    private final String roomCode;
    private final List<String> players = new ArrayList<>(2);

    public GameRoom(String code, String creator) {
        this.roomCode = code;
        this.players.add(creator);
    }

    public void addPlayer(String username) {
        if (!isFull() && !players.contains(username)) {
            players.add(username);
        }
    }

    public boolean isFull() {
        return players.size() == 2;
    }

    public List<String> getPlayers() {
        return players;
    }

    public String getRoomCode() {
        return roomCode;
    }
}
