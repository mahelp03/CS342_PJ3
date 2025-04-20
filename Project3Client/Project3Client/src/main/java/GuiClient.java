import java.util.HashMap;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class GuiClient extends Application {

    TextField c1;
    Button b1;
    HashMap<String, Scene> sceneMap;
    VBox clientBox;
    Client clientConnection;
    HBox fields;
    ComboBox<String> listUsers;
    ListView<String> listItems;

    Stage primaryStage;
    Scene loginScene;
    Scene chatScene;

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

        fields = new HBox(10, listUsers, b1);
        fields.setPadding(new Insets(5));

        b1.setOnAction(e -> {
            clientConnection.send(new Message(listUsers.getValue(), c1.getText()));
            c1.clear();
        });

        clientBox = new VBox(10, c1, fields, listItems);
        clientBox.setPadding(new Insets(10));
        clientBox.setStyle("-fx-background-color: blue; -fx-font-family: 'serif';");

        return new Scene(clientBox, 400, 300);
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
                        if (data.message.equals("LOGIN_SUCCESS") || data.message.equals("SIGNUP_SUCCESS")) {
                            primaryStage.setScene(chatScene);
                            primaryStage.setTitle("Client Chat");
                        } else if (data.message.equals("LOGIN_FAIL") || data.message.equals("SIGNUP_FAIL")) {
                            // ✅ 실패 시 메시지 표시
                            showErrorMessage("Login Failed");
    
                            // ✅ 서버 연결 끊기
                            try {
                                clientConnection.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
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
    
    
}