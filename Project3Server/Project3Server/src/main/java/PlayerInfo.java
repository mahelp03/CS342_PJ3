import java.io.Serializable;

public class PlayerInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    public String username;
    public double winRate;
    public int totalGames;

    public PlayerInfo(String username, double winRate, int totalGames) {
        this.username = username;
        this.winRate = winRate;
        this.totalGames = totalGames;
    }
}