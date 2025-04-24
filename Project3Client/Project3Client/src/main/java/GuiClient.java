import java.util.HashMap;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;



public class GuiClient extends Application {

    TextField c1;
    Button b1, b2;
    Label ratingLabel, gamesLabel, AFresultLabel;
    HashMap<String, Scene> sceneMap;
    VBox clientBox;
    Client clientConnection;
    HBox fields;
    ComboBox<String> listUsers;
    ListView<String> listItems, FriendList;
    private Message lastRoomMessage;
    private String currentUsername;
    private ListView<String> playerListView;
    private java.util.List<String> pendingPlayerList = new java.util.ArrayList<>();
    private Label p1Label;
    private Label p1Stats;
    private Label p2Label;
    private Label p2Stats;

    private String player1Name;
    private String player2Name;


    private int rows = 6;
    private int cols = 7;
    private int[][] board;
    private Button[][] buttons;
    private boolean[] playerTurn;
    private boolean[] gameOver;
    private Label turnLabel;
    Button startbt = new Button("Start");
    Button resetbt = new Button("Reset");
    private boolean[] gameStarted = {false};
    private ListView<String> historyList;



    

    // each scenes
    Stage primaryStage;
    Scene loginScene;
    Scene chatScene;
    Scene lobbyScene;

    TextField usernameField;
    PasswordField passwordField;
    boolean isSignUp = false;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Client Login");

        this.loginScene = buildLoginScene();
        this.chatScene = buildChatScene();
        // this.lobbyScene = buildLobbyScene();

        primaryStage.setScene(loginScene);
        primaryStage.show();

        primaryStage.setOnCloseRequest((WindowEvent t) -> {
            System.exit(0);
        });
    }

    private Scene buildLoginScene() {
        VBox loginBox = new VBox(15);
        loginBox.setPadding(new Insets(50));
        loginBox.setStyle("-fx-background-color: lightblue;");

        Label title = new Label("Login");
        usernameField = new TextField();
        usernameField.setPromptText("Enter username");
        passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");

        CheckBox agreeCheck = new CheckBox("I agree to the terms of signup");

        Button loginButton = new Button("Login");
        Button signupButton = new Button("Signup");
        signupButton.setDisable(true);

        agreeCheck.setOnAction(e -> {
            signupButton.setDisable(!agreeCheck.isSelected());
        });

        loginButton.setOnAction(e -> {
            isSignUp = false;
            setupConnection();
        });
        
        signupButton.setOnAction(e -> {
            isSignUp = true;
            setupConnection();
        });
        

        loginBox.getChildren().addAll(title, usernameField, passwordField, agreeCheck, loginButton, signupButton);
        return new Scene(loginBox, 400, 400);
    }

    private Scene buildChatScene() { // 로그인 화면 대신 사용중중
        ComboBox<String> listUsers;
        listUsers = new ComboBox<>();
        listUsers.getItems().add("ALL");
        listUsers.setValue("ALL");

        listItems = new ListView<>();
        c1 = new TextField();
        b1 = new Button("Send");
        b2 = new Button("Back");
        b2.setPadding(new Insets(0,0,0,30));

        fields = new HBox(10, listUsers, b1, b2);
        fields.setPadding(new Insets(5));

        b1.setOnAction(e -> {
            clientConnection.send(new Message(listUsers.getValue(), c1.getText()));
            c1.clear();
        });

        b2.setOnAction(e -> {
            primaryStage.setScene(lobbyScene);
        });

        clientBox = new VBox(10, c1, fields, listItems);
        clientBox.setPadding(new Insets(10));
        clientBox.setStyle("-fx-background-color: blue; -fx-font-family: 'serif';");

        return new Scene(clientBox, 400, 300);
    }

    // lobby
    private Scene buildLobbyScene(String username) {
        // test
        // double winRate = AccountDatabase.getWinRate(username);
        // int totalGames = AccountDatabase.getGameCount(username);
        //test end
        

        VBox leftPane = new VBox(10);
        leftPane.setPadding(new Insets(10));
        leftPane.setStyle("-fx-background-color: #E6E6FA;");

        Label userLabel = new Label("UserName: " + username);
        ratingLabel = new Label("Rating: "); // 초기화 안해주면 조댐댐
        gamesLabel = new Label("Games: ");

        clientConnection.send(new Message("SERVER", "PROFILE_REQUEST:" + username));

        historyList = new ListView<>();
        historyList.getItems().add("Recent Game History");

        VBox profileBox = new VBox(5, userLabel, ratingLabel, gamesLabel);
        profileBox.setStyle("-fx-border-color: blue; -fx-padding: 10");

        Button createRoom = new Button("Create Room");

        createRoom.setOnAction(e -> {
            Scene createRoomScene = buildCreateRoomScene(username);
            primaryStage.setScene(createRoomScene);
        });
        
        Button joinRoom = new Button("Join");

        joinRoom.setOnAction(e -> {
            Scene joinRoomScene = buildJoinRoomScene(username);
            primaryStage.setScene(joinRoomScene);
        });
        

        // ListView<String> historyList = new ListView<>();
        historyList = new ListView<>();
        historyList.getItems().add("Recent Game History");

        leftPane.getChildren().addAll(profileBox, createRoom, joinRoom, historyList);

        Button addFriend = new Button("+ Add Friend");
        Button textMessage = new Button("Text Message");

        textMessage.setOnAction(e -> {
            primaryStage.setScene(chatScene);
        });

        addFriend.setOnAction(e -> { // goto add friend
            String currentUsername = usernameField.getText();
            Scene addFriendScene = buildAddFriendScene(currentUsername);
            primaryStage.setScene(addFriendScene);
        });
        

        FriendList = new ListView<>();
        FriendList.setPrefHeight(200);
        FriendList.setPrefWidth(100);
        HBox hbox1 = new HBox(10, addFriend, textMessage);
        VBox vbox1 = new VBox(10, hbox1, FriendList);
        

        // rightPane.getChildren().addAll(addFriend, textMessage, friendList);

        HBox mainLayout = new HBox(20, leftPane, vbox1 );
        mainLayout.setPadding(new Insets(20));

        return new Scene(mainLayout, 600, 400);
    }

    private Scene buildAddFriendScene(String currentUser) {
        // VBox layout = new VBox(15);
        // layout.setPadding(new Insets(30));
        // layout.setStyle("-fx-background-color: #F5F5DC;");
        AFresultLabel = new Label(); 
    
        TextField friendInput = new TextField();
        friendInput.setPromptText("Enter Username");
    
        Button goButton = new Button("Go");

        
    
        goButton.setOnAction(e -> {
            String friendName = friendInput.getText().trim();
            if (!friendName.isEmpty()) {
                String msg = "ADDFRIEND:" + currentUser + ":" + friendName;
                clientConnection.send(new Message("SERVER", msg));
            }
        });
    
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> primaryStage.setScene(lobbyScene));
        
        HBox hboxaddf = new HBox(15, friendInput, goButton, backButton);
        VBox vboxaddf  = new VBox(10, hboxaddf, AFresultLabel);
        hboxaddf.setPadding(new Insets(20,5,20,5));
        return new Scene(vboxaddf, 400, 300);
    }


    private void showErrorMessage(String message) {
        Label errorLabel = new Label(message);
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        Platform.runLater(() -> {
            VBox loginBox = (VBox) loginScene.getRoot();
            if (!loginBox.getChildren().contains(errorLabel)) {
                loginBox.getChildren().add(errorLabel); // 로그인 창 제일 아래에 띄움
            }
        });
    }

        private void setupConnection() {
            clientConnection = new Client(data -> {
                switch (data.type) {
                    case TEXT:
                        Platform.runLater(() -> {
                            if (data.message.startsWith("FRIENDLIST:")) {
                                String[] parts = data.message.split(":");
                                if (parts.length == 3) {
                                    String[] friends = parts[2].split(",");
                                    FriendList.getItems().setAll(friends);
                                }
                                return;
                            }


                            if (data.message.equals("LOGIN_SUCCESS") || data.message.equals("SIGNUP_SUCCESS")) {
                                // primaryStage.setScene(chatScene); // 테스트용용
                                // primaryStage.setTitle("Client Chat");
                                String username = usernameField.getText();
                                this.currentUsername = username;
                                this.lobbyScene = buildLobbyScene(username);
                                
                                clientConnection.send(new Message("SERVER", "GETFRIEND:" + username));
                                clientConnection.send(new Message("SERVER", "GET_HISTORY:" + username));
                                Platform.runLater(() -> {
                                    primaryStage.setScene(lobbyScene);
                                    primaryStage.setTitle("Game Lobby");
                                });

                            } else if (data.message.equals("LOGIN_FAIL") || data.message.equals("SIGNUP_FAIL")) {
                                showErrorMessage("Login Failed");
                                try {
                                    clientConnection.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }else if(data.message.equals("ADDFRIEND_SUCCESS")){
                                if (AFresultLabel != null)
                                    AFresultLabel.setText("User is added");
                                String username = usernameField.getText();
                                clientConnection.send(new Message("SERVER", "GETFRIEND:" + username));
                            }else if(data.message.equals("ADDFRIEND_FAIL")){
                                if (AFresultLabel != null)
                                    AFresultLabel.setText("User not found or already added.");

                            }else {
                                listItems.getItems().add(data.recipient + ": " + data.message);
                            }

                            if (data.message.startsWith("ROOM_CREATED:")) {
                                
                                lastRoomMessage = data;
                                String[] parts = data.message.split(":");
                                String roomName = parts[1];
                                String roomCode = parts[2];
                                String username = usernameField.getText();
                                primaryStage.setScene(buildGameScene(roomName, username, roomCode)); //fixed
                            }
                            else if (data.message.startsWith("JOIN_SUCCESS:")) {
                                System.out.println("[DEBUG] Received JOIN_SUCCESS: " + data.message);
                                lastRoomMessage = data;
                            
                                String[] parts = data.message.split(":");
                                if (parts.length >= 7) {
                                    String roomName = parts[1];
                                    String roomCode = parts[2];
                                    String player1 = parts[3];
                                    String player2 = parts[5];
                            
                                    // 🔑 저장
                                    player1Name = player1;
                                    player2Name = player2;
                            
                                    System.out.println("[DEBUG] player1Name = " + player1Name);
                                    System.out.println("[DEBUG] player2Name = " + player2Name);
                            
                                    // 🔁 UI 스레드에서 화면 전환
                                    Platform.runLater(() -> {
                                        System.out.println("[DEBUG] Switching to game scene...");
                                        primaryStage.setScene(buildGameScene(roomName, usernameField.getText(), roomCode));
                                    });
                            
                                    // 요청
                                    clientConnection.send(new Message("SERVER", "GET_PLAYERLIST:" + roomCode));
                                    clientConnection.send(new Message("SERVER", "PROFILE_REQUEST:" + player1));
                                    clientConnection.send(new Message("SERVER", "PROFILE_REQUEST:" + player2));
                                }
                            }else if (data.message.startsWith("UPDATE_MOVE:")) {
                                String[] parts = data.message.split(":");
                                String mover = parts[1];
                                int col = Integer.parseInt(parts[2]);
                            
                                Platform.runLater(() -> applyMoveToBoard(mover, col));
                            }
                            else if (data.message.startsWith("MOVE:")) {
                                String[] parts = data.message.split(":");
                                String mover = parts[1];
                                int col = Integer.parseInt(parts[2]);
                            
                                Platform.runLater(() -> applyMoveToBoard(mover, col));
                            }
                            
                            if (data.message.startsWith("START_GAME:")) {
                                Platform.runLater(() -> {
                                    gameStarted[0] = true;
                                    startbt.setDisable(true);
                                    resetbt.setDisable(false);
                                });
                                return;
                            }
                            else if (data.message.startsWith("HISTORY_ENTRY:")) {
                                String entry = data.message.substring("HISTORY_ENTRY:".length());
                                Platform.runLater(() -> {
                                    if (!historyList.getItems().contains(entry)) {  // ✅ 중복 체크
                                        if (historyList.getItems().size() >= 6) {
                                            historyList.getItems().remove(1); // 0번째는 헤더("Recent Game History")니까 1번 인덱스 삭제
                                        }
                                        historyList.getItems().add(entry);
                                    }
                                });
                            }
                            

                            else if (data.message.startsWith("PROFILE_INFO:")) {
                                System.out.println("[DEBUG] Received PROFILE_INFO: " + data.message);  // 콘솔 확인용

                                String[] parts = data.message.split(":");
                                String targetUser = parts[1];
                                String[] stats = parts[2].split(",");

                                String winRate = stats[0];
                                String totalGames = stats[1];

                                System.out.println("[DEBUG] targetUser = '" + targetUser + "'");
                                System.out.println("[DEBUG] player1Name = '" + player1Name + "'");
                                System.out.println("[DEBUG] player2Name = '" + player2Name + "'");

                                Platform.runLater(() -> {
                                    if (targetUser.equals(currentUsername)) {
                                        ratingLabel.setText("Rating: " + winRate + " %");
                                        gamesLabel.setText("Games: " + totalGames + " games");
                                    }
                                    if (targetUser.trim().equals(player1Name)) {
                                        System.out.println("[DEBUG] Updating p1Stats");
                                        p1Stats.setText("Rating: " + winRate + " %\nGames: " + totalGames);
                                    } else if (targetUser.trim().equals(player2Name)) {
                                        System.out.println("[DEBUG] Updating p2Stats");
                                        p2Stats.setText("Rating: " + winRate + " %\nGames: " + totalGames);
                                    } else {
                                        System.out.println("[DEBUG] targetUser didn't match either player name");
                                    }
                                });
                            }
                            
                            

                            else if (data.message.equals("JOIN_FAIL")) {
                                showErrorMessage("Join Failed: Invalid or full room.");
                            } else if (data.message.startsWith("FRIENDLIST:")) {
                                String[] parts = data.message.split(":");
                                if (parts.length == 3) {
                                    String[] friends = parts[2].split(",");
                                    FriendList.getItems().setAll(friends);
                                }
                            } else if (data.message.equals("ADDFRIEND_SUCCESS")) {
                                if (AFresultLabel != null)
                                    AFresultLabel.setText("User is added");
                                String username = usernameField.getText();
                                clientConnection.send(new Message("SERVER", "GETFRIEND:" + username));
                            }
                            else if (data.message.startsWith("PLAYER_LIST:")) {
                                String[] players = data.message.substring("PLAYER_LIST:".length()).split(",");
                            
                                Platform.runLater(() -> {
                                    if (playerListView != null) {
                                        playerListView.setItems(javafx.collections.FXCollections.observableArrayList(players));
                                    } else {
                                        pendingPlayerList.clear();
                                        for (String p : players) pendingPlayerList.add(p);
                                    }
                                    if (players.length < 2) {
                                        if (p2Label != null) p2Label.setText("Player 2");
                                        if (p2Stats != null) p2Stats.setText("Rating: N/A\nGames: N/A");
                                    }

                                });
                            }

                            
                            
                            else if (data.message.equals("ADDFRIEND_FAIL")) {
                                if (AFresultLabel != null)
                                    AFresultLabel.setText("User not found or already added.");
                            } else {
                                listItems.getItems().add(data.recipient + ": " + data.message);
                            }



                        });
                        break;
        
                    case NEWUSER:
                        Platform.runLater(() -> {
                            listUsers.getItems().add(data.recipient);
                            listItems.getItems().add(data.recipient );
                        });
                        break;
        
                    case DISCONNECT:
                        Platform.runLater(() -> {
                            listUsers.getItems().remove(data.recipient); // 로그아웃시 좌측화면에서 접속자 지움움
                            listItems.getItems().add(data.recipient);
                            
                        });
                        break;
                        case WINRATE_INFO:
                        Platform.runLater(() -> {
                            String[] parts = data.message.split(",");
                            String winRate = parts[0];
                            String totalGames = parts[1];

                            System.out.println("[DEBUG] Received updated stats for: " + data.recipient);
                            System.out.println("[DEBUG] winRate = " + winRate + ", totalGames = " + totalGames);

                    
                            if (data.recipient.equals(player1Name)) {
                                p1Stats.setText("Rating: " + winRate + " %\nGames: " + totalGames);
                            } else if (data.recipient.equals(player2Name)) {
                                p2Stats.setText("Rating: " + winRate + " %\nGames: " + totalGames);
                            }
                        });
                        break;
                    
                }
            });
    
        clientConnection.start();
    
        new Thread(() -> {
            try {
                while (clientConnection.out == null) {
                    Thread.sleep(50);
                }
    
                String username = usernameField.getText();
                String password = passwordField.getText();
                String payload = (isSignUp ? "SIGNUP" : "LOGIN") + ":" + username + ":" + password;
    
                clientConnection.send(new Message("ALL", payload));
    
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    
    
    private Scene buildCreateRoomScene(String username) {
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(40));
        layout.setStyle("-fx-background-color: #FAFAD2;");
    
        Label label = new Label("Enter Room Name:");
        TextField roomInput = new TextField();
        roomInput.setPromptText("Room Name");
    
        Button enterButton = new Button("Enter");
        Button backButton = new Button("Back");
        
        // 전꺼
        enterButton.setOnAction(e -> {
            String roomName = roomInput.getText().trim();
            if (!roomName.isEmpty()) {
                clientConnection.send(new Message("SERVER", "CREATE_ROOM:" + username + ":" + roomName));
                // Placeholder: Replace with actual in-game room logic
                // primaryStage.setScene(buildGameScene(roomName, username));
            }
        });     
    
        backButton.setOnAction(e -> {
            primaryStage.setScene(lobbyScene);
        });
    
        HBox buttonRow = new HBox(15, enterButton, backButton);
        layout.getChildren().addAll(label, roomInput, buttonRow);
    
        return new Scene(layout, 400, 300);
    }
    
    // 커넥트4 ui빌드
    private Scene buildGameScene(String roomName, String username, String roomCode) {
        Label header = new Label("Room: " + roomName + " | Code: " + roomCode);
        header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // final int rows = 6;
        // final int cols = 7;
        // int[][] board = new int[rows][cols]; // 0=empty, 1=red, 2=yellow
        // boolean[] playerTurn = {true}; // Player 1: true, Player 2: false
        // Button[][] buttons = new Button[rows][cols];
        // boolean[] gameOver = {false};
        // Label turnLabel = new Label("Turn: Player 1 (Red)");

        GridPane gameBoard = new GridPane();
        gameBoard.setPadding(new Insets(10));
        gameBoard.setHgap(5);
        gameBoard.setVgap(5);

        board = new int[rows][cols];
        buttons = new Button[rows][cols];
        playerTurn = new boolean[] {true};
        gameOver = new boolean[] {false};
        turnLabel = new Label("Turn: Player 1 (Red)");
        boolean[] gameStarted = {false};


        
        // Label turnLabel = new Label("Turn: Player 1 (Red)"); // 현재 턴 표시용
        
        

        // initialize buttons and board (여기서부턴 안바꿈)
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Button cell = new Button();
                cell.setMinSize(50, 50);
                cell.setMaxSize(50, 50);
                cell.setStyle("-fx-background-color: #e0e0e0;");

                int finalCol = col;
                cell.setOnAction(e -> {
                    if (gameOver[0] || !gameStarted[0]) return;
                    boolean isMyTurn = (playerTurn[0] && username.equals(player1Name)) || (!playerTurn[0] && username.equals(player2Name));
                    if (!isMyTurn) {
                        System.out.println("[DEBUG] It's not your turn: " + username);
                        return;
                    }

                    int dropRow = -1;
                    for (int r = rows - 1; r >= 0; r--) {
                        if (board[r][finalCol] == 0) {
                            dropRow = r;
                            break;
                        }
                    }
                    if (dropRow == -1) return;



                    
                    clientConnection.send(new Message("SERVER", "MOVE:" + roomCode + ":" + username + ":" + finalCol));
                    // if (gameOver[0]) return;
                    // int dropRow = -1;
                    // System.out.println(username + " clicked column " + finalCol);
                    // for (int r = rows-1; r >= 0; r--) {
                    //     if (board[r][finalCol] == 0) {
                    //         dropRow = r;
                            
                    //         break;
                    //     }
                        
                    // }
                    // if (dropRow == -1) return; // Column full

                    // int currentPlayer = playerTurn[0] ? 1 : 2;
                    // board[dropRow][finalCol] = currentPlayer;
                    // updateButton(buttons, dropRow, finalCol, currentPlayer);
                    // buttons[dropRow][finalCol].setDisable(true);

                    // if (checkWin(board, dropRow, finalCol, currentPlayer)) {
                    //     gameOver[0] = true;
                    //     turnLabel.setText("Player " + (playerTurn[0] ? "1 (Red)" : "2 (Yellow)") + " wins!");
                    //     showWinDialog("Player " + (playerTurn[0] ? "1 (Red)" : "2 (Yellow)") + " wins!");
                    //     return;
                    // } else if (isDraw(board)) {
                    //     gameOver[0] = true;
                    //     turnLabel.setText("Draw!");
                    //     showWinDialog("It's a draw!");
                    //     return;
                    // }

                    // // Switch turn
                    // playerTurn[0] = !playerTurn[0];
                    // turnLabel.setText("Turn: Player " + (playerTurn[0] ? "1 (Red)" : "2 (Yellow)"));
                });
                gameBoard.add(cell, col, row);
                buttons[row][col] = cell;
            }
        }

        Button backBtn = new Button("Exit Room");
        backBtn.setOnAction(e -> {
            // 서버에 퇴장 요청
            clientConnection.send(new Message("SERVER", "LEAVE_ROOM:" + username));
            
            // 히스토리 초기화 후 갱신 요청
            clientConnection.send(new Message("SERVER", "GET_HISTORY:" + currentUsername));


            // 로비 화면으로 전환 (최우선으로 실행)
            Platform.runLater(() -> {
                primaryStage.setScene(lobbyScene);
                primaryStage.setTitle("Game Lobby");
            });
        });


        VBox gameArea = new VBox(10, header, turnLabel, gameBoard, backBtn);
        gameArea.setPadding(new Insets(10));

        // Player Info Area
        p1Label = new Label("Player 1 (" + player1Name + ")");
        p1Stats = new Label("Rating: N/A\nGames: N/A");

        p2Label = new Label("Player 2 (" + player2Name + ")");
        p2Stats = new Label("Rating: N/A\nGames: N/A");

        Message latestMsg = lastRoomMessage; // buildGameScene() 호출 직전에 저장해두는 구조

        if (latestMsg != null && latestMsg.message.startsWith("ROOM_CREATED:")) {
            String[] parts = latestMsg.message.split(":");
            String creator = parts[3];
            String[] stats = parts[4].split(",");

            player1Name = creator;  // 🔥 여기에 저장
            player2Name = null;

            // p1Label.setText(creator);
            p1Label = new Label("Player 1 ( " + creator + " )");
            p1Stats.setText("Rating: " + stats[0] + " %\nGames: " + stats[1]);

            clientConnection.send(new Message("SERVER", "PROFILE_REQUEST:" + creator));

        } else if (latestMsg != null && latestMsg.message.startsWith("JOIN_SUCCESS:")) {
            String[] parts = latestMsg.message.split(":");
            if (parts.length >= 8) {
                String player1 = parts[3];
                String[] stats1 = parts[4].split(",");
                String player2 = parts[5];
                String[] stats2 = parts[6].split(",");

                player1Name = player1;
                player2Name = player2;


                // p1Label.setText(player1);
                // p2Label.setText(player2);
                p1Label = new Label("Player 1 ( " + player1Name + " )");
                p2Label = new Label("Player 2 ( " + player2Name + " )");
                p1Stats.setText("Rating: " + stats1[0] + " %\nGames: " + stats1[1]);
                
                p2Stats.setText("Rating: " + stats2[0] + " %\nGames: " + stats2[1]);

                clientConnection.send(new Message("SERVER", "PROFILE_REQUEST:" + player1));
                clientConnection.send(new Message("SERVER", "PROFILE_REQUEST:" + player2));
            }
        }
        playerListView = new ListView<>();
        playerListView.setPrefHeight(80);
        playerListView.setPlaceholder(new Label("Waiting for players..."));

        if (!pendingPlayerList.isEmpty()) {
            playerListView.setItems(javafx.collections.FXCollections.observableArrayList(pendingPlayerList));
        }
        // Button startbt = new Button("Start");
        // Button resetbt = new Button("Reset");

        if(pendingPlayerList.size()==2){
            startbt.setDisable(false);
        }

        // boolean[] gameStarted = {false}; // is the game started? it mightbe not

        startbt.setOnAction(e -> {
            // gameStarted[0] = true;
            // startbt.setDisable(true); // disable startbt while playing game
            // resetbt.setDisable(false); // reset bt activated
            // turnLabel.setText("Turn: Player 1 (Red)");

            if (playerListView.getItems().size() == 2) {  // 플레이어 2명일 때만
                clientConnection.send(new Message("SERVER", "START_GAME:" + roomCode));
                gameStarted[0] = true;
                startbt.setDisable(true);
                resetbt.setDisable(false);

                // 추가
                for (int row = 0; row < rows; row++) {
                    for (int col = 0; col < cols; col++) {
                        if (board[row][col] == 0) {
                            buttons[row][col].setDisable(false); // Enable empty cells for a new round
                        }
                    }
                }
                
            }
        
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    int finalCol = col;
                    buttons[row][col].setOnAction(ev -> {
                        if (!gameStarted[0] || gameOver[0]) return;
        
                        if ((playerTurn[0] && username.equals(player1Name)) ||
                            (!playerTurn[0] && username.equals(player2Name))) {
                            clientConnection.send(new Message("SERVER", "MOVE:" + roomCode + ":" + username + ":" + finalCol));
                        }
                    });
                }
            }
        });


        resetbt.setOnAction(e -> {
            gameStarted[0] = false;
            gameOver[0] = false;
            resetbt.setDisable(true);
            startbt.setDisable(false); // start 락풀기
            turnLabel.setText("Game reset. Press Start to begin.");
        
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    board[row][col] = 0;
                    buttons[row][col].setGraphic(null);
                    buttons[row][col].setStyle("-fx-background-color: #e0e0e0;");
                    buttons[row][col].setDisable(true);
                }
            }
        
            playerTurn[0] = true;
        });

        HBox hbox333 = new HBox(20, startbt, resetbt);
        hbox333.setPadding(new Insets(0,0,0,20));

        Label textN = new Label("Click Start, if u ready!!");
        textN.setPadding(new Insets(0,0,0,5));

        VBox playerStats = new VBox(15, p1Label, p1Stats, p2Label, p2Stats, playerListView, hbox333, textN);
        playerStats.setPadding(new Insets(20));
        playerStats.setStyle("-fx-background-color: #F0F8FF; -fx-border-color: black;");
        playerStats.setPrefWidth(180);

        HBox root = new HBox(30, gameArea, playerStats);
        root.setPadding(new Insets(20));

        return new Scene(root, 700, 550);
    }




    private Scene buildJoinRoomScene(String username) {
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(30));
        layout.setStyle("-fx-background-color: #F0FFF0;");
    
        Label label = new Label("Join Game Room");
        TextField roomCodeField = new TextField();
        roomCodeField.setPromptText("Enter Room Code (e.g. R5S89X)");
    
        Button enterBtn = new Button("Enter");
        enterBtn.setOnAction(e -> {
            String code = roomCodeField.getText().trim().toUpperCase();
            if (!code.isEmpty()) {
                clientConnection.send(new Message("SERVER", "JOIN_ROOM:" + username + ":" + code));
            }
        });

        Button randomBtn = new Button("Random Join");
        randomBtn.setOnAction(e -> {
            clientConnection.send(new Message("SERVER", "JOIN_RANDOM_REQUEST:" + username));
        });
        Button backBtn = new Button("Back");

        enterBtn.setOnAction(e -> {
            String code = roomCodeField.getText().trim().toUpperCase();
            if (!code.isEmpty()) {
                clientConnection.send(new Message("SERVER", "JOIN_ROOM:" + username + ":" + code));
            }
        });
        
        randomBtn.setOnAction(e -> {
            clientConnection.send(new Message("SERVER", "JOIN_RANDOM_REQUEST:" + username));
        });
        
    
        backBtn.setOnAction(e -> {
            primaryStage.setScene(lobbyScene);
        });
    
        VBox buttons = new VBox(10, enterBtn, randomBtn, backBtn);
        layout.getChildren().addAll(label, roomCodeField, buttons);
    
        return new Scene(layout, 400, 300);
    }
    
    // 밑에부터 4목 게임 요소들
    // Helper: Set color on button
    private void updateButton(Button[][] buttons, int row, int col, int player) {
        buttons[row][col].setGraphic(new javafx.scene.shape.Circle(18, player == 1 ? javafx.scene.paint.Color.RED : javafx.scene.paint.Color.GOLD));
        buttons[row][col].setStyle("-fx-background-color: white;");
        buttons[row][col].setDisable(true);
    }

    // Win checking
    private boolean checkWin(int[][] board, int row, int col, int player) {
        int[][] dirs = {{1,0},{0,1},{1,1},{1,-1}};
        for (int[] d : dirs) {
            int count = 1;
            count += countDir(board, row, col, d[0], d[1], player);
            count += countDir(board, row, col, -d[0], -d[1], player);
            if (count >= 4) return true;
        }
        return false;
    }

    private int countDir(int[][] board, int row, int col, int dr, int dc, int player) {
        int count = 0, r = row+dr, c = col+dc;
        while (r >= 0 && r < board.length && c >= 0 && c < board[0].length && board[r][c] == player) {
            count++; r += dr; c += dc;
        }
        return count;
    }

    private boolean isDraw(int[][] board) {
        for (int col = 0; col < board[0].length; col++) {
            if (board[0][col] == 0) return false;
        }
        return true;
    }

    private void showWinDialog(String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.setTitle("Game Over");
            alert.showAndWait();
        });
    }

    private void applyMoveToBoard(String mover, int col) {
        if (gameOver[0]) return;
    
        int dropRow = -1;
        for (int r = rows - 1; r >= 0; r--) {
            if (board[r][col] == 0) {
                dropRow = r;
                break;
            }
        }
        if (dropRow == -1) return;
    
        int currentPlayer = mover.equals(player1Name) ? 1 : 2;
        board[dropRow][col] = currentPlayer;
        updateButton(buttons, dropRow, col, currentPlayer);
        buttons[dropRow][col].setDisable(true);
    
        if (checkWin(board, dropRow, col, currentPlayer)) {
            gameOver[0] = true;
            turnLabel.setText("Player " + currentPlayer + " wins!");
            showWinDialog("Player " + currentPlayer + " wins!");
            System.out.println("[DEBUG] Game Over - Winner: " + (currentPlayer == 1 ? player1Name : player2Name));
            
            String winner = (currentPlayer == 1) ? player1Name : player2Name;
            String loser  = (currentPlayer == 1) ? player2Name : player1Name;
            clientConnection.send(new Message("SERVER", "GAME_RESULT:" + winner + ":" + loser));

            // clientConnection.send(new Message("SERVER", "PROFILE_REQUEST:" + currentUsername));
            // clientConnection.send(new Message("SERVER", "GET_HISTORY:" + currentUsername));
            clientConnection.send(new Message("SERVER", "PROFILE_REQUEST:" + player1Name));
            clientConnection.send(new Message("SERVER", "PROFILE_REQUEST:" + player2Name));
            clientConnection.send(new Message("SERVER", "GET_HISTORY:" + player1Name));
            clientConnection.send(new Message("SERVER", "GET_HISTORY:" + player2Name));
            return;
        } else if (isDraw(board)) {
            gameOver[0] = true;
            turnLabel.setText("Draw!");
            showWinDialog("It's a draw!");

            clientConnection.send(new Message("SERVER", "PROFILE_REQUEST:" + player1Name));
            clientConnection.send(new Message("SERVER", "PROFILE_REQUEST:" + player2Name));
            clientConnection.send(new Message("SERVER", "GET_HISTORY:" + player1Name));
            clientConnection.send(new Message("SERVER", "GET_HISTORY:" + player2Name));

        } else {
            playerTurn[0] = !playerTurn[0];
            turnLabel.setText("Turn: Player " + (playerTurn[0] ? "1 (Red)" : "2 (Yellow)"));
        }
    }
    


}