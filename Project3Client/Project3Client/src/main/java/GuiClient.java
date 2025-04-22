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
                                String roomCode = data.message.split(":")[1];
                                String username = usernameField.getText();
                                primaryStage.setScene(buildGameScene("Room " + roomCode, username));
                            } else if (data.message.startsWith("JOIN_SUCCESS:")) {
                                String roomCode = data.message.split(":")[1];
                                String username = usernameField.getText();
                                primaryStage.setScene(buildGameScene("Room " + roomCode, username));
                            } else if (data.message.equals("JOIN_FAIL")) {
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
                            } else if (data.message.equals("ADDFRIEND_FAIL")) {
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
        /*enterButton.setOnAction(e -> {
            String roomName = roomInput.getText().trim();
            if (!roomName.isEmpty()) {
                // TODO: Enter game room scene with name `roomName`
                Scene gameScene = buildGameScene(roomName, username);
                Platform.runLater(() -> primaryStage.setScene(gameScene));
                // Placeholder: Replace with actual in-game room logic
                // primaryStage.setScene(buildGameScene(roomName, username));
            }
        });*/

        enterButton.setOnAction(e -> {
            clientConnection.send(new Message("SERVER", "CREATE_ROOM:" + username));
        });        
    
        backButton.setOnAction(e -> {
            primaryStage.setScene(lobbyScene);
        });
    
        HBox buttonRow = new HBox(15, enterButton, backButton);
        layout.getChildren().addAll(label, roomInput, buttonRow);
    
        return new Scene(layout, 400, 300);
    }
    
    private String generateRoomCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int idx = (int)(Math.random() * chars.length());
            code.append(chars.charAt(idx));
        }
        return code.toString();
    }
    
    private Scene buildGameScene(String roomName, String username) {
        String roomCode = generateRoomCode(); // random 6-char room code

        Label header = new Label("Room: " + roomName + " | Code: " + roomCode);
        header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        GridPane gameBoard = new GridPane();
        gameBoard.setPadding(new Insets(10));
        gameBoard.setHgap(5);
        gameBoard.setVgap(5);

        int rows = 6;
        int cols = 7;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Button cell = new Button("");
                cell.setPrefSize(50, 50);
                int finalCol = col;
                cell.setOnAction(e -> {
                    // TODO: dropToken(finalCol);
                    System.out.println(username + " clicked column " + finalCol);
            });
            gameBoard.add(cell, col, row);
        }
    }

    Button backBtn = new Button("Exit Room");
    backBtn.setOnAction(e -> {
        primaryStage.setScene(lobbyScene); // return to lobby
    });

    VBox layout = new VBox(15, header, gameBoard, backBtn);
    layout.setPadding(new Insets(20));
    return new Scene(layout, 500, 450);
    }

    private Scene buildJoinRoomScene(String username) {
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(30));
        layout.setStyle("-fx-background-color: #F0FFF0;");
    
        Label label = new Label("Join Game Room");
        TextField roomCodeField = new TextField();
        roomCodeField.setPromptText("Enter Room Code (e.g. R5S89X)");
    
        Button enterBtn = new Button("Enter");
        Button randomBtn = new Button("Random Join");
        Button backBtn = new Button("Back");
    
        /*enterBtn.setOnAction(e -> {
            String code = roomCodeField.getText().trim().toUpperCase();
            if (!code.isEmpty()) {
                // TODO: validate & join this room if exists
                System.out.println(username + " is joining room with code: " + code);
                primaryStage.setScene(buildGameScene("Room " + code, username)); // placeholder logic
            }
        });
    
        randomBtn.setOnAction(e -> {
            // TODO: find a room with only one user
            String randomCode = findAvailableRoom();
            if (randomCode != null) {
                System.out.println(username + " randomly joining room: " + randomCode);
                primaryStage.setScene(buildGameScene("Room " + randomCode, username));
            } else {
                showErrorMessage("No rooms available for random join.");
            }
        });*/

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
    
    private String findAvailableRoom() {
        // TODO: Replace with actual request to server for available room
        // Simulate with hardcoded return for now
        return "R5S89X"; // Only return if a valid room with 1 player exists
    }
    
}