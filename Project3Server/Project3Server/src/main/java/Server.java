import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Set;
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

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[SHUTDOWN] Saving account data...");
            // AccountDatabase.saveToFile("account_data.txt");
            AccountDatabase.printAllAccountData(); // 👈 로그 출력
        }));
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
                callback.accept(new Message("", "Server did not launch", MessageType.TEXT));
            }
        }
    }

    class ClientThread extends Thread {

        Socket connection;
        int count;
        ObjectInputStream in;
        ObjectOutputStream out;
        private String username;

        ClientThread(Socket s, int count) {
            this.connection = s;
            this.count = count;
        }

        public void updateClients(Message message) {
            switch (message.type) {
                case TEXT:
                    for (ClientThread t : clients) {
                        if (message.recipient.equals("ALL") || message.recipient.equals(t.username)) {
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
                    // Message data = (Message) in.readObject();
                    Object obj = in.readObject();
                    Message data = null;

                    if (obj instanceof Message) {
                        data = (Message) obj;
                        System.out.println("Received message: " + data.message);
                        callback.accept(data);
                        updateClients(data);
                    }


                    System.out.println("Received message: " + data.message); // 테스토용 로그 출력력

                    if (data.message.startsWith("SIGNUP:") || data.message.startsWith("LOGIN:")) {
                        String[] parts = data.message.split(":");
                        if (parts.length == 3) {
                            String type = parts[0]; // To Server chatting
                            String username = parts[1];
                            String password = parts[2];
                    
                            String result = type.equals("SIGNUP")
                                    ? LoginHandler.signup(username, password)
                                    : LoginHandler.login(username, password);
                    
                            Message response = new Message(username, result.equals("OK") 
                                ? (type + "_SUCCESS") 
                                : (type + "_FAIL"));
                    
                            out.writeObject(response);
                    
                            if (result.equals("OK")) {
                                this.username = username;
                                Message newUser = new Message(username, true);
                                newUser.senderName = username;
                                callback.accept(newUser);
                                updateClients(newUser);

                                double winRate = AccountDatabase.getWinRate(username);
                                int totalGames = AccountDatabase.getGameCount(username);
                                String statMessage = winRate + "," + totalGames;

                                Message statInfo = new Message(username, statMessage, MessageType.WINRATE_INFO);
                                statInfo.senderName = username;
                                out.writeObject(statInfo);
                            }
                    
                            continue;
                        }
                    }else if (data.message.startsWith("ADDFRIEND:")) {
                        String[] parts = data.message.split(":");
                        String user = parts[1];
                        String friend = parts[2];
                        
                        if (LoginHandler.getAllUsers().containsKey(friend)) {
                            boolean success = FriendHander.addFriend(user, friend);
                            String result = success ? "ADDFRIEND_SUCCESS" : "ADDFRIEND_FAIL";
                            out.writeObject(new Message(user, result));
                        } else {
                            out.writeObject(new Message(user, "ADDFRIEND_FAIL"));
                        }
                    
                        continue;
                    }else if (data.message.startsWith("GETFRIEND:")) {
                        String user = data.message.substring("GETFRIEND:".length());
                        Set<String> friends = FriendHander.getFriends(user);
                        String friendStr = String.join(",", friends);
                        out.writeObject(new Message(user, "FRIENDLIST:" + user + ":" + friendStr));
                        continue;
                    }



                    

                    callback.accept(data);
                    updateClients(data);

                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Client #" + username + " disconnected due to error.");

                    Message discon = new Message(username != null ? username : "UNKNOWN", false);
                    discon.senderName = username != null ? username : "UNKNOWN";
                    callback.accept(discon);
                    updateClients(discon);
                    clients.remove(this);
                    break;
                }
            }
        }
    }
}
