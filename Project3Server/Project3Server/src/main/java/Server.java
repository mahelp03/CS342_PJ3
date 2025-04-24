import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class Server {

    int count = 1;
    ArrayList<ClientThread> clients = new ArrayList<>();
    TheServer server;
    private Consumer<Message> callback;

    private final HashMap<String, GameRoom> gameRooms = new HashMap<>();


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

        private String generateRoomCode() {
            String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                sb.append(chars.charAt((int)(Math.random() * chars.length())));
            }
            return sb.toString();
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
                    
                    else if (data.message.startsWith("CREATE_ROOM:")) {
                        String[] parts = data.message.split(":");
                        String creator = parts[1];
                        String roomName = parts[2];
                    
                        String roomCode;
                        do {
                            roomCode = generateRoomCode(); // 중복 방지
                        } while (gameRooms.containsKey(roomCode));
                    
                        GameRoom newRoom = new GameRoom(roomCode,roomName, creator);
                        gameRooms.put(roomCode, newRoom);
                    
                        System.out.println("[ROOM CREATED] RoomCode: " + roomCode + ", Name: " + roomName + ", Creator: " + creator);
                        System.out.println("[ROOM STATE] Players in room " + roomCode + ":");
                        for (String player : newRoom.getPlayers()) {
                            System.out.println(" - " + player);
                        }


                        double rate1 = AccountDatabase.getWinRate(creator);
                        int games1 = AccountDatabase.getGameCount(creator);
                    
                        String payload = "ROOM_CREATED:" + roomName + ":" + roomCode + ":" + creator + ":" + rate1 + "," + games1;
                        out.writeObject(new Message(creator, payload));
                        continue;
                    }
                    // else if (data.message.startsWith("JOIN_ROOM:")) {
                    //     String[] parts = data.message.split(":");
                    //     String username = parts[1];
                    //     String roomCode = parts[2];

                    //     GameRoom room = gameRooms.get(roomCode);
                    //     if (room != null && !room.isFull()) {
                    //         room.addPlayer(username);

                    //         List<String> players = room.getPlayers();
                    //         String player1 = players.get(0);
                    //         String player2 = username;

                    //         double rate1 = AccountDatabase.getWinRate(player1);
                    //         int games1 = AccountDatabase.getGameCount(player1);

                    //         String payload = null;

                    //         // double rate2 = AccountDatabase.getWinRate(player2);
                    //         // int games2 = AccountDatabase.getGameCount(player2);

                    //         // String payload = "JOIN_SUCCESS:" + roomCode + ":" +
                    //         //                 player1 + ":" + rate1 + "," + games1 + ":" +
                    //         //                 player2 + ":" + rate2 + "," + games2;
                    //         if (player2 != null){
                    //             double rate2 = AccountDatabase.getWinRate(player2);
                    //             int games2 = AccountDatabase.getGameCount(player2);

                    //             payload = "JOIN_SUCCESS:" + room.getRoomName() + ":" + roomCode + ":" + player1 + ":" + rate1 + "," + games1 + ":" + player2 + ":" + rate2 + "," + games2;


                    //             //out.writeObject(new Message(username, payload));
                    //             for (ClientThread t : clients) {
                    //                 if (t.username != null && (t.username.equals(player1) || t.username.equals(player2))) {
                    //                     t.out.writeObject(new Message(t.username, payload));
                    //                 }
                    //             }
                    //         }else{
                    //             payload = "ROOM_CREATED:" + room.getRoomName() + ":" + roomCode + ":" + player1 + ":" + rate1 + "," + games1;
                    //             out.writeObject(new Message(username, payload));
                    //         }

                            
                            
                    //     } else {
                    //         out.writeObject(new Message(username, "JOIN_FAIL"));
                    //     }
                    //     continue;
                    // }
                    // ✅ PATCH: Server.java > JOIN_ROOM 처리 부분만 수정
                    else if (data.message.startsWith("JOIN_ROOM:")) {
                        String[] parts = data.message.split(":");
                        String username = parts[1];
                        String roomCode = parts[2];
                    
                        GameRoom room = gameRooms.get(roomCode);
                        if (room != null && !room.isFull()) {
                            room.addPlayer(username);

                            System.out.println("[ROOM JOINED] User: " + username + " entered RoomCode: " + roomCode);
                            System.out.println("[ROOM STATE] Players in room " + roomCode + ":");
                            for (String player : room.getPlayers()) {
                                System.out.println(" - " + player);
                            }
                    
                            List<String> players = room.getPlayers();
                    
                            // ✅ 현재 플레이어 목록을 문자열로 구성
                            StringBuilder playerListMsg = new StringBuilder("PLAYER_LIST:");
                            for (String p : players) {
                                playerListMsg.append(p).append(",");
                            }
                            if (playerListMsg.length() > 0 && playerListMsg.charAt(playerListMsg.length() - 1) == ',') {
                                playerListMsg.deleteCharAt(playerListMsg.length() - 1);
                            }
                    
                            // ✅ 방에 있는 유저 모두에게 PLAYER_LIST 메시지 전송
                            for (ClientThread t : clients) {
                                if (t.username != null && players.contains(t.username)) {
                                    t.out.writeObject(new Message(t.username, playerListMsg.toString()));
                                }
                            }
                    
                            // ✅ 기존 JOIN_SUCCESS 로직 유지
                            if (players.size() == 2) {
                                String player1 = players.get(0);
                                String player2 = players.get(1);
                    
                                double rate1 = AccountDatabase.getWinRate(player1);
                                int games1 = AccountDatabase.getGameCount(player1);
                    
                                double rate2 = AccountDatabase.getWinRate(player2);
                                int games2 = AccountDatabase.getGameCount(player2);
                    
                                String payload = "JOIN_SUCCESS:" + room.getRoomName() + ":" + roomCode + ":" +
                                    player1 + ":" + rate1 + "," + games1 + ":" +
                                    player2 + ":" + rate2 + "," + games2;
                    
                                for (ClientThread t : clients) {
                                    if (t.username != null && (t.username.equals(player1) || t.username.equals(player2))) {
                                        t.out.writeObject(new Message(t.username, payload));
                                    }
                                }
                            } else if (players.size() == 1) {
                                String player1 = players.get(0);
                                double rate1 = AccountDatabase.getWinRate(player1);
                                int games1 = AccountDatabase.getGameCount(player1);
                    
                                String payload = "ROOM_CREATED:" + room.getRoomName() + ":" + roomCode + ":" + player1 + ":" + rate1 + "," + games1;
                                out.writeObject(new Message(username, payload));
                            }
                        } else {
                            out.writeObject(new Message(username, "JOIN_FAIL"));
                        }
                        continue;
                    }
                    

                    
                    else if (data.message.startsWith("JOIN_RANDOM_REQUEST:")) {
                        String username = data.message.split(":")[1];
                        GameRoom joinedRoom = null;
                    
                        for (GameRoom room : gameRooms.values()) {
                            if (!room.isFull()) {
                                room.addPlayer(username);
                                joinedRoom = room;

                                System.out.println("[RANDOM JOIN] User: " + username + " joined RoomCode: " + room.getRoomCode());
                                System.out.println("[ROOM STATE] Players in room " + room.getRoomCode() + ":");
                                for (String player : room.getPlayers()) {
                                    System.out.println(" - " + player);
                                }

                                break;
                            }
                        }
                    
                        if (joinedRoom != null) {
                            out.writeObject(new Message(username, "JOIN_SUCCESS:" + joinedRoom.getRoomName() + ":" + joinedRoom.getRoomCode()));
                        } else {
                            out.writeObject(new Message(username, "JOIN_FAIL"));
                        }
                        //continue;
                    }

                    else if (data.message.startsWith("LEAVE_ROOM:")) {
                        String username = data.message.split(":")[1];
                    
                        for (GameRoom room : gameRooms.values()) {
                            if (room.hasPlayer(username)) {
                                room.removePlayer(username);
                                break;
                            }
                        }
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
