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
        

        ListView<String> historyList = new ListView<>();
        historyList.getItems().add("History (Recent 3-5 games)");

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


        

    // private void setupConnection() {
    //     clientConnection = new Client(data -> {
    //         switch (data.type) {
    //             case TEXT:
    //                 Platform.runLater(() -> {
    //                     if (data.message.startsWith("ROOM_CREATED:")) {
    //                         String roomCode = data.message.split(":")[1];
    //                         String username = usernameField.getText();
    //                         primaryStage.setScene(buildGameScene("Room " + roomCode, username));
    //                     } else if (data.message.startsWith("JOIN_SUCCESS:")) {
    //                         String roomCode = data.message.split(":")[1];
    //                         String username = usernameField.getText();
    //                         primaryStage.setScene(buildGameScene("Room " + roomCode, username));
    //                     } else if (data.message.equals("JOIN_FAIL")) {
    //                         showErrorMessage("Join Failed: Invalid or full room.");
    //                     } else if (data.message.startsWith("FRIENDLIST:")) {
    //                         String[] parts = data.message.split(":");
    //                         if (parts.length == 3) {
    //                             String[] friends = parts[2].split(",");
    //                             FriendList.getItems().setAll(friends);
    //                         }
    //                     } else if (data.message.equals("ADDFRIEND_SUCCESS")) {
    //                         if (AFresultLabel != null)
    //                             AFresultLabel.setText("User is added");
    //                         String username = usernameField.getText();
    //                         clientConnection.send(new Message("SERVER", "GETFRIEND:" + username));
    //                     } else if (data.message.equals("ADDFRIEND_FAIL")) {
    //                         if (AFresultLabel != null)
    //                             AFresultLabel.setText("User not found or already added.");
    //                     } else {
    //                         listItems.getItems().add(data.recipient + ": " + data.message);
    //                     }
    //                 });
    //                 break;
        
    //             case NEWUSER:
    //                 Platform.runLater(() -> {
    //                     listUsers.getItems().add(data.recipient);
    //                     listItems.getItems().add(data.recipient);
    //                 });
    //                 break;
        
    //             case DISCONNECT:
    //                 Platform.runLater(() -> {
    //                     listUsers.getItems().remove(data.recipient);
    //                     listItems.getItems().add(data.recipient);
    //                 });
    //                 break;
        
    //             case WINRATE_INFO:
    //                 Platform.runLater(() -> {
    //                     if (ratingLabel != null && gamesLabel != null) {
    //                         String[] parts = data.message.split(",");
    //                         String winRate = parts[0];
    //                         String totalGames = parts[1];
    //                         ratingLabel.setText("Rating: " + winRate + " %");
    //                         gamesLabel.setText("Games: " + totalGames + " games");
    //                     }
    //                 });
    //                 break;
    //         }
    //     });
    // }
        
        // 밑에가 형이한거 (위에 replace한건 게임코드로 조인하는거 매니징하면서 바꿔본것)
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
                                // showPopup("User not found or already added.");
                                if (AFresultLabel != null)
                                    AFresultLabel.setText("User not found or already added.");

                            }else {
                                listItems.getItems().add(data.recipient + ": " + data.message);
                            }

                            if (data.message.startsWith("ROOM_CREATED:")) {
                                //String roomCode = data.message.split(":")[1];
                                //String username = usernameField.getText();
                                //primaryStage.setScene(buildGameScene("Room " + roomCode, username));
                                lastRoomMessage = data;
                                String[] parts = data.message.split(":");
                                String roomName = parts[1];
                                String roomCode = parts[2];
                                String username = usernameField.getText();
                                primaryStage.setScene(buildGameScene(roomName, username, roomCode)); //fixed
                            }
                            else if (data.message.startsWith("JOIN_SUCCESS:")) {
                                //String roomCode = data.message.split(":")[1];
                                //String username = usernameField.getText();
                                //primaryStage.setScene(buildGameScene("Room " + roomCode, username));
                                lastRoomMessage = data;
                                String[] parts = data.message.split(":");
                                if (parts.length >= 3) {
                                    String roomName = parts[1];
                                    String roomCode = parts[2];

                                    String username = usernameField.getText();
                                    
                                    Platform.runLater(() -> primaryStage.setScene(buildGameScene(roomName, username, roomCode))); //fixed
                                    clientConnection.send(new Message("SERVER", "GET_PLAYERLIST:" + roomCode));
                                }
                                
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
                            if(ratingLabel != null && gamesLabel != null){
                                String[] parts = data.message.split(",");
                            String winRate = parts[0];
                            String totalGames = parts[1];

                            ratingLabel.setText("Rating: " + winRate + " %");
                            gamesLabel.setText("Games: " + totalGames + " games");
                            }
                            // 받은 메시지 파싱
                            

                            // updateLobbyStats(winRate, totalGames);
        
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

        final int rows = 6;
        final int cols = 7;
        int[][] board = new int[rows][cols]; // 0=empty, 1=red, 2=yellow
        boolean[] playerTurn = {true}; // Player 1: true, Player 2: false
        Button[][] buttons = new Button[rows][cols];
        boolean[] gameOver = {false};
        Label turnLabel = new Label("Turn: Player 1 (Red)");

        GridPane gameBoard = new GridPane();
        gameBoard.setPadding(new Insets(10));
        gameBoard.setHgap(5);
        gameBoard.setVgap(5);

        // initialize buttons and board (여기서부턴 안바꿈)
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Button cell = new Button();
                cell.setMinSize(50, 50);
                cell.setMaxSize(50, 50);
                cell.setStyle("-fx-background-color: #e0e0e0;");

                int finalCol = col;
                cell.setOnAction(e -> {
                    if (gameOver[0]) return;
                    int dropRow = -1;
                    for (int r = rows-1; r >= 0; r--) {
                        if (board[r][finalCol] == 0) {
                            dropRow = r;
                            break;
                        }
                    }
                    if (dropRow == -1) return; // Column full

                    int currentPlayer = playerTurn[0] ? 1 : 2;
                    board[dropRow][finalCol] = currentPlayer;
                    updateButton(buttons, dropRow, finalCol, currentPlayer);
                    buttons[dropRow][finalCol].setDisable(true);

                    if (checkWin(board, dropRow, finalCol, currentPlayer)) {
                        gameOver[0] = true;
                        turnLabel.setText("Player " + (playerTurn[0] ? "1 (Red)" : "2 (Yellow)") + " wins!");
                        showWinDialog("Player " + (playerTurn[0] ? "1 (Red)" : "2 (Yellow)") + " wins!");
                        return;
                    } else if (isDraw(board)) {
                        gameOver[0] = true;
                        turnLabel.setText("Draw!");
                        showWinDialog("It's a draw!");
                        return;
                    }

                    // Switch turn
                    playerTurn[0] = !playerTurn[0];
                    turnLabel.setText("Turn: Player " + (playerTurn[0] ? "1 (Red)" : "2 (Yellow)"));
                });
                gameBoard.add(cell, col, row);
                buttons[row][col] = cell;
            }
        }

        Button backBtn = new Button("Exit Room");
        backBtn.setOnAction(e -> {
            clientConnection.send(new Message("SERVER", "LEAVE_ROOM:" + username));
            Platform.runLater(() -> primaryStage.setScene(lobbyScene));
        });

        VBox gameArea = new VBox(10, header, turnLabel, gameBoard, backBtn);
        gameArea.setPadding(new Insets(10));

        // Player Info Area
        Label p1Label = new Label("Player 1");
        Label p1Stats = new Label("Rating: N/A\nGames: N/A");

        Label p2Label = new Label("Player 2");
        Label p2Stats = new Label("Rating: N/A\nGames: N/A");

        Message latestMsg = lastRoomMessage; // buildGameScene() 호출 직전에 저장해두는 구조
        if (latestMsg != null && latestMsg.message.startsWith("ROOM_CREATED:")) {
            String[] parts = latestMsg.message.split(":");
            String creator = parts[3];
            String[] stats = parts[4].split(",");
            p1Label.setText(creator);
            p1Stats.setText("Rating: " + stats[0] + " %\nGames: " + stats[1]);
        } else if (latestMsg != null && latestMsg.message.startsWith("JOIN_SUCCESS:")) {
            String[] parts = latestMsg.message.split(":");
            if (parts.length >= 8) {
                // p1Label.setText(parts[3]);
                // p1Stats.setText("Rating: " + parts[4].split(",")[0] + " %\nGames: " + parts[4].split(",")[1]);

                // p2Label.setText(parts[5]);
                // p2Stats.setText("Rating: " + parts[6].split(",")[0] + " %\nGames: " + parts[6].split(",")[1]);
                String player1 = parts[3];
                String[] stats1 = parts[4].split(",");
                String player2 = parts[5];
                String[] stats2 = parts[6].split(",");

                p1Label.setText(player1);
                p1Stats.setText("Rating: " + stats1[0] + " %\nGames: " + stats1[1]);
                p2Label.setText(player2);
                p2Stats.setText("Rating: " + stats2[0] + " %\nGames: " + stats2[1]);
            }
        }
        playerListView = new ListView<>();
        playerListView.setPrefHeight(80);
        playerListView.setPlaceholder(new Label("Waiting for players..."));

        if (!pendingPlayerList.isEmpty()) {
            playerListView.setItems(javafx.collections.FXCollections.observableArrayList(pendingPlayerList));
        }
        

        VBox playerStats = new VBox(15, p1Label, p1Stats, p2Label, p2Stats, playerListView);
        playerStats.setPadding(new Insets(20));
        playerStats.setStyle("-fx-background-color: #F0F8FF; -fx-border-color: black;");
        playerStats.setPrefWidth(180);

        HBox root = new HBox(30, gameArea, playerStats);
        root.setPadding(new Insets(20));

        return new Scene(root, 700, 450);
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


}