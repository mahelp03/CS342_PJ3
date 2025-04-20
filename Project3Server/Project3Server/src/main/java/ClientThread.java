// ClientThread.java
import java.io.*;
import java.net.Socket;

public class ClientThread extends Thread {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String username;

    public ClientThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            while (true) {
                Object cmd = in.readObject();

                if (cmd instanceof String) {
                    String command = (String) cmd;
                    if (command.equals("SIGNUP") || command.equals("LOGIN")) {
                        String username = (String) in.readObject();
                        String password = (String) in.readObject();

                        String result = command.equals("SIGNUP")
                                ? LoginHandler.signup(username, password)
                                : LoginHandler.login(username, password);

                        out.writeObject(result);
                        out.flush();

                        if ("OK".equals(result)) {
                            this.username = username;
                            System.out.println("[Server] " + username + " authenticated successfully.");
                        }
                    } else if (command.equals("MSG")) {
                        String msg = (String) in.readObject();
                        System.out.println("[" + username + "]: " + msg);
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("Connection dropped: " + username);
        } finally {
            try {
                if (socket != null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}