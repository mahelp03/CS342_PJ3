import java.util.List;

public interface GuiClientCallback {
    void onMessageReceived(Message message);
    void onPlayerInfoListReceived(List<PlayerInfo> infos);
}
