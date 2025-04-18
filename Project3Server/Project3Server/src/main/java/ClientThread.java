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
                        String user = (String) in.readObject();
                        String pw = (String) in.readObject();

                        String result = command.equals("SIGNUP")
                                ? LoginHandler.signup(user, pw)
                                : LoginHandler.login(user, pw);

                        out.writeObject(result);
                        out.flush();

                        if ("OK".equals(result)) {
                            this.username = user;
                            System.out.println("[Server] " + user + " authenticated successfully.");
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