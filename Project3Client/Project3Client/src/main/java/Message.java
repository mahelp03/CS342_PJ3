import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 42L; // ✅ 꼭 명시

    public MessageType type;
    public String message;
    public int recipient;

    public Message(int recipient, String message) {
        this.type = MessageType.TEXT;
        this.recipient = recipient;
        this.message = message;
    }

    public Message(int recipient, boolean connect) {
        this.recipient = recipient;
        if (connect) {
            this.type = MessageType.NEWUSER;
            this.message = "User " + recipient + " has joined!";
        } else {
            this.type = MessageType.DISCONNECT;
            this.message = "User " + recipient + " has disconnected!";
        }
    }

    public Message(String message) {
        this.type = MessageType.TEXT;
        this.message = message;
        this.recipient = -1;
    }
}
