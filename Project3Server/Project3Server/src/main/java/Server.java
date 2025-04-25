import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
        AccountDatabase.loadFromFile("account_data.txt");
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
            String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"; // IDK
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

                        if (data.message.startsWith("LOGIN:") || data.message.startsWith("SIGNUP:")) {
                            System.out.println("Received login/signup request.");
                        } else {
                            System.out.println("Received message: " + data.message);
                        }

                        // login/sign msg do not broadcast in the other scene
                        if (!data.message.startsWith("LOGIN:") && !data.message.startsWith("SIGNUP:")) {
                            callback.accept(data);
                            updateClients(data);
                        }
                    }



                    // System.out.println("Received message: " + data.message); // log testing
                    if (!(data.message.startsWith("LOGIN:") || data.message.startsWith("SIGNUP:"))) {
                        updateClients(data);
                    }
                    

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
                    }
                    else if (data.message.startsWith("ADDFRIEND:")) {
                        String[] parts = data.message.split(":");
                        String user = parts[1];
                        String friend = parts[2];
                    
                        if (LoginHandler.getAllUsers().containsKey(friend)) {
                            boolean success = FriendHander.addFriend(user, friend);
                    
                            if (success) {
                                // .confirm  whether success or not for adding friend
                                out.writeObject(new Message(user, "ADDFRIEND_SUCCESS"));
                    
                                // refresh friend list : Me
                                Set<String> userFriends = FriendHander.getFriends(user);
                                out.writeObject(new Message(user, "FRIENDLIST:" + user + ":" + String.join(",", userFriends)));
                    
                               // refresh friend list : Friend ver
                                for (ClientThread t : clients) {
                                    if (t.username != null && t.username.equals(friend)) {
                                        Set<String> friendFriends = FriendHander.getFriends(friend);
                                        t.out.writeObject(new Message(friend, "FRIENDLIST:" + friend + ":" + String.join(",", friendFriends)));
                                        break;
                                    }
                                }
                    
                            } else {
                                out.writeObject(new Message(user, "ADDFRIEND_FAIL"));
                            }
                        } else {
                            out.writeObject(new Message(user, "ADDFRIEND_FAIL"));
                        }
                        continue;
                    }
                    
                    else if (data.message.startsWith("GETFRIEND:")) { // when player entered lobby
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
                            roomCode = generateRoomCode(); // prevent duplicating
                        } while (gameRooms.containsKey(roomCode));
                    
                        GameRoom newRoom = new GameRoom(roomCode,roomName, creator);
                        gameRooms.put(roomCode, newRoom);
                    
                        System.out.println("RoomCode: " + roomCode + ", Name: " + roomName + ", Creator: " + creator);
                        System.out.println("Players in room " + roomCode + ":");
                        for (String player : newRoom.getPlayers()) {
                            System.out.println(" - " + player);
                        }


                        double rate1 = AccountDatabase.getWinRate(creator);
                        int games1 = AccountDatabase.getGameCount(creator);
                    
                        String payload = "ROOM_CREATED:" + roomName + ":" + roomCode + ":" + creator + ":" + rate1 + "," + games1;
                        out.writeObject(new Message(creator, payload));
                        // continue;
                        List<String> players = newRoom.getPlayers();
                        StringBuilder playerListMsg = new StringBuilder("PLAYER_LIST:");
                        for (String p : players) {
                            playerListMsg.append(p).append(",");
                        }
                        if (playerListMsg.length() > 0 && playerListMsg.charAt(playerListMsg.length() - 1) == ',') {
                            playerListMsg.deleteCharAt(playerListMsg.length() - 1);
                        }

                        System.out.println("Sending PLAYER_LIST to creator of new room " + roomCode + ":");
                        for (String p : players) {
                            System.out.println(" - " + p);
                        }

                        for (ClientThread t : clients) {
                            if (t.username != null && players.contains(t.username)) {
                                t.out.writeObject(new Message(t.username, playerListMsg.toString()));
                            }
                        }
                    }
                    
                    else if (data.message.startsWith("JOIN_ROOM:")) {
                        String[] parts = data.message.split(":");
                        String username = parts[1];
                        String roomCode = parts[2];
                    
                        GameRoom room = gameRooms.get(roomCode);
                        if (room != null && !room.isFull()) {
                            room.addPlayer(username);

                            System.out.println("User: " + username + " entered RoomCode: " + roomCode);
                            System.out.println("Players in room " + roomCode + ":");
                            for (String player : room.getPlayers()) {
                                System.out.println(" - " + player);
                            }
                    
                            List<String> players = room.getPlayers();
                    
                            // construct list as string
                            StringBuilder playerListMsg = new StringBuilder("PLAYER_LIST:");
                            for (String p : players) {
                                playerListMsg.append(p).append(",");
                            }
                            if (playerListMsg.length() > 0 && playerListMsg.charAt(playerListMsg.length() - 1) == ',') {
                                playerListMsg.deleteCharAt(playerListMsg.length() - 1);
                            }
                    
                            // send every player on same room
                            for (ClientThread t : clients) {
                                if (t.username != null && players.contains(t.username)) {
                                    t.out.writeObject(new Message(t.username, playerListMsg.toString()));
                                }
                            }
                    
                            if (players.size() == 2) { // room capacity limits
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
                    else if (data.message.startsWith("PROFILE_REQUEST:")) {
                        String username = data.message.split(":")[1];
                    
                        double rate = AccountDatabase.getWinRate(username);
                        int games = AccountDatabase.getGameCount(username);
                    
                        String profileInfo = "PROFILE_INFO:" + username + ":" + rate + "," + games;
                        out.writeObject(new Message(username, profileInfo));
                        System.out.println("Sent profile for " + username + ": " + rate + " %, " + games + " games");
                        continue;
                    }
                    else if (data.message.startsWith("JOIN_RANDOM_REQUEST:")) {
                        String username = data.message.split(":")[1];
                        GameRoom joinedRoom = null;
                    
                        for (GameRoom room : gameRooms.values()) {
                            if (!room.isFull()) {
                                room.addPlayer(username);
                                joinedRoom = room;
                    
                                System.out.println("User: " + username + " joined RoomCode: " + room.getRoomCode());
                                System.out.println("Players in room " + room.getRoomCode() + ":");
                                for (String player : room.getPlayers()) {
                                    System.out.println(" - " + player);
                                }
                    
                                break;
                            }
                        }
                    
                        if (joinedRoom != null) {
                            List<String> players = joinedRoom.getPlayers();
                            if (players.size() == 2) {
                                String player1 = players.get(0);
                                String player2 = players.get(1);
                    
                                double rate1 = AccountDatabase.getWinRate(player1);
                                int games1 = AccountDatabase.getGameCount(player1);
                    
                                double rate2 = AccountDatabase.getWinRate(player2);
                                int games2 = AccountDatabase.getGameCount(player2);
                    
                                String payload = "JOIN_SUCCESS:" + joinedRoom.getRoomName() + ":" + joinedRoom.getRoomCode() + ":" +
                                        player1 + ":" + rate1 + "," + games1 + ":" +
                                        player2 + ":" + rate2 + "," + games2;
                    
                                for (ClientThread t : clients) {
                                    if (t.username != null && (t.username.equals(player1) || t.username.equals(player2))) {
                                        t.out.writeObject(new Message(t.username, payload));
                                    }
                                }
                            } else {
                                String player1 = players.get(0);
                                double rate1 = AccountDatabase.getWinRate(player1);
                                int games1 = AccountDatabase.getGameCount(player1);
                                String payload = "ROOM_CREATED:" + joinedRoom.getRoomName() + ":" + joinedRoom.getRoomCode() + ":" + player1 + ":" + rate1 + "," + games1;
                                out.writeObject(new Message(username, payload));
                            }
                        } else {
                            out.writeObject(new Message(username, "JOIN_FAIL"));
                        }
                    }else if (data.message.startsWith("MOVE:")) {
                        String[] parts = data.message.split(":");
                        String roomCode = parts[1];
                        String username = parts[2];
                        int col = Integer.parseInt(parts[3]);
                    
                        // send every player in game
                        GameRoom room = gameRooms.get(roomCode);
                        if (room != null) {
                            for (ClientThread t : clients) {
                                if (room.hasPlayer(t.username)) {
                                    t.out.writeObject(new Message("SERVER", "UPDATE_MOVE:" + username + ":" + col));
                                }
                            }
                        }
                        continue;
                    }
                    
                
                    else if (data.message.startsWith("GET_PLAYERLIST:")) {
                        String roomCode = data.message.split(":")[1];
                        GameRoom room = gameRooms.get(roomCode);
                    
                        if (room != null) {
                            List<String> players = room.getPlayers();
                    
                            StringBuilder playerListMsg = new StringBuilder("PLAYER_LIST:");
                            for (String p : players) {
                                playerListMsg.append(p).append(",");
                            }
                            if (playerListMsg.length() > 0 && playerListMsg.charAt(playerListMsg.length() - 1) == ',') {
                                playerListMsg.deleteCharAt(playerListMsg.length() - 1);
                            }
                    
                            // log testing
                            System.out.println("Sending PLAYER_LIST to clients in room " + roomCode + ":");
                            for (String p : players) {
                                System.out.println(" - " + p);
                            }
                    
                            // sending, maybe??
                            for (ClientThread t : clients) {
                                if (t.username != null && players.contains(t.username)) {
                                    t.out.writeObject(new Message(t.username, playerListMsg.toString()));
                                }
                            }
                        }
                    
                        continue;
                    }
                    else if (data.message.startsWith("MOVE:")) {
                        String[] parts = data.message.split(":");
                        String roomCode = parts[1];
                        String username = parts[2];
                        int col = Integer.parseInt(parts[3]);
                    
                        GameRoom room = gameRooms.get(roomCode);
                        if (room != null) {
                            for (ClientThread t : clients) {
                                if (room.hasPlayer(t.username)) {
                                    t.out.writeObject(new Message("SERVER", "UPDATE_MOVE:" + username + ":" + col));
                                }
                            }
                        }
                        continue;
                    }
                    else if (data.message.startsWith("GAME_RESULT:")) {
                        String[] parts = data.message.split(":");
                        String winner = parts[1];
                        String loser = parts[2];
                    
                        // 전적 업데이트
                        AccountDatabase.incrementGameCount(winner);
                        AccountDatabase.incrementGameCount(loser);
                        AccountDatabase.incrementWinCount(winner);

                        System.out.println("GAME_RESULT processed.");
                        System.out.println("Winner: " + winner + ", Loser: " + loser);
                        System.out.println(winner + " Games: " + AccountDatabase.getGameCount(winner) + ", Wins: " + AccountDatabase.getWinCount(winner));
                        System.out.println(loser + " Games: " + AccountDatabase.getGameCount(loser) + ", Wins: " + AccountDatabase.getWinCount(loser));
                        double winRateW = AccountDatabase.getWinRate(winner);
                        int gamesW = AccountDatabase.getGameCount(winner);
                        Message m1 = new Message(winner, winRateW + "," + gamesW, MessageType.WINRATE_INFO);

                        double winRateL = AccountDatabase.getWinRate(loser);
                        int gamesL = AccountDatabase.getGameCount(loser);
                        Message m2 = new Message(loser, winRateL + "," + gamesL, MessageType.WINRATE_INFO);

                        for (ClientThread t : clients) {
                            if (t.username != null && t.username.equals(winner)) {
                                t.out.writeObject(m1);
                            }
                            if (t.username != null && t.username.equals(loser)) {
                                t.out.writeObject(m2);
                            }
                        }

                        // history send
                        String roomCode = "";
                        for (Map.Entry<String, GameRoom> entry : gameRooms.entrySet()) {
                            if (entry.getValue().hasPlayer(winner) && entry.getValue().hasPlayer(loser)) {
                                roomCode = entry.getKey();
                                break;
                            }
                        }
                        AccountDatabase.addGameResult(roomCode, winner, loser, winner);

                        for (ClientThread t : clients) {
                            if (t.username != null && t.username.equals(winner)) {
                                t.out.writeObject(m1);
                            }
                            if (t.username != null && t.username.equals(loser)) {
                                t.out.writeObject(m2);
                            }
                        }
                        System.out.println("Updated results - Winner: " + winner + ", Loser: " + loser);
                        continue;
                    }
                    else if (data.message.startsWith("GET_HISTORY:")) {
                        String username = data.message.split(":")[1];
                        List<String> history = AccountDatabase.getGameHistory(username);
                    
                        Set<String> seen = new HashSet<>();
                        for (String entry : history) {
                            if (entry != null) {
                                String trimmed = entry.trim();
                                if (!trimmed.isEmpty() && !seen.contains(trimmed)) {
                                    seen.add(trimmed);
                                    out.writeObject(new Message(username, "HISTORY_ENTRY:" + trimmed));
                                }
                            }
                        }   
                        continue;
                    }
                    
                    

                    else if (data.message.startsWith("LEAVE_ROOM:")) {
                        String username = data.message.split(":")[1];
                    
                        for (GameRoom room : gameRooms.values()) {
                            if (room.hasPlayer(username)) {
                                room.removePlayer(username);
                    
                                System.out.println("[ROOM LEFT] " + username + " left room " + room.getRoomCode());
                    
                                // reload player list
                                List<String> players = room.getPlayers();
                                StringBuilder playerListMsg = new StringBuilder("PLAYER_LIST:");
                                for (String p : players) {
                                    playerListMsg.append(p).append(",");
                                }
                                if (playerListMsg.length() > 0 && playerListMsg.charAt(playerListMsg.length() - 1) == ',') {
                                    playerListMsg.deleteCharAt(playerListMsg.length() - 1);
                                }
                    
                                System.out.println("[DEBUG] Broadcasting updated PLAYER_LIST after leave:");
                                for (String p : players) {
                                    System.out.println(" - " + p);
                                }
                    
                                for (ClientThread t : clients) {
                                    if (t.username != null && players.contains(t.username)) {
                                        t.out.writeObject(new Message(t.username, playerListMsg.toString()));
                                    }
                                }
                    
                                break;
                            }
                        }
                    
                        continue;
                    }
                    
                    if (data.type != MessageType.TEXT) {
                        callback.accept(data);
                        updateClients(data);
                    }

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
