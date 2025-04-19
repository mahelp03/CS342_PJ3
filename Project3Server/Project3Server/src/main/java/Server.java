import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.function.Consumer;

public class Server {

    int count = 1;
    ArrayList<ClientThread> clients = new ArrayList<>();
    TheServer server;
    private Consumer<Message> callback;

    Server(Consumer<Message> call) {
        callback = call;
        server = new TheServer();
        server.start();
    }

    public class TheServer extends Thread {
        public void run() {
            try (ServerSocket mysocket = new ServerSocket(5555)) {
                System.out.println("Server is waiting for a client!");
                while (true) {
                    ClientThread c = new ClientThread(mysocket.accept(), count);
                    clients.add(c);
                    c.start();
                    count++;
                }
            } catch (Exception e) {
                callback.accept(new Message(-1, "Server did not launch"));
            }
        }
    }

    class ClientThread extends Thread {

        Socket connection;
        int count;
        ObjectInputStream in;
        ObjectOutputStream out;

        ClientThread(Socket s, int count) {
            this.connection = s;
            this.count = count;
        }

        public void updateClients(Message message) {
            switch (message.type) {
                case TEXT:
                    for (ClientThread t : clients) {
                        if (message.recipient == -1 || message.recipient == t.count) {
                            try {
                                t.out.writeObject(message);
                            } catch (Exception e) {
                                System.err.println("Broadcast Error");
                            }
                        }
                    }
                    break;
                case NEWUSER:
                case DISCONNECT:
                    for (ClientThread t : clients) {
                        try {
                            t.out.writeObject(message);
                        } catch (Exception e) {
                            System.err.println("User Update Error");
                        }
                    }
                    break;
            }
        }

        public void run() {
            try {
                in = new ObjectInputStream(connection.getInputStream());
                out = new ObjectOutputStream(connection.getOutputStream());
                connection.setTcpNoDelay(true);
            } catch (Exception e) {
                System.out.println("Streams not open");
                return;
            }

            System.out.println("Connected client #" + count);

            while (true) {
                try {
                    Message data = (Message) in.readObject();
                    System.out.println("Received message: " + data.message);

                    if (data.message.startsWith("SIGNUP:") || data.message.startsWith("LOGIN:")) {
                        String[] parts = data.message.split(":");
                        if (parts.length == 3) {
                            String type = parts[0];
                            String username = parts[1];
                            String password = parts[2];

                            String result;
                            if (type.equals("SIGNUP")) {
                                result = LoginHandler.signup(username, password);  // String을 그대로 받기
                            } else {
                                result = LoginHandler.login(username, password);   // 역시 String
                            }
                            

                            Message response = new Message(count, result);
                            out.writeObject(response);

                            if (result.equals("SIGNUP_SUCCESS") || result.equals("LOGIN_SUCCESS")) {
                                Message newUser = new Message(count, true); // NEWUSER
                                callback.accept(newUser);
                                updateClients(newUser);
                            }

                            continue;
                        }
                    }

                    callback.accept(data);
                    updateClients(data);

                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Client #" + count + " disconnected due to error.");

                    Message discon = new Message(count, false);  // DISCONNECT
                    callback.accept(discon);
                    updateClients(discon);
                    clients.remove(this);
                    break;
                }
            }
        }
    }
}
