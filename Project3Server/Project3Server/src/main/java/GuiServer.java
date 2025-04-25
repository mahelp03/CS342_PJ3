import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.text.Font;

import java.util.Map;

public class GuiServer extends Application {

    Server serverConnection;
    ListView<String> listItems;
    ListView<String> listUsers;
    TableView<Map.Entry<String, String>> accountTable;
    Scene mainScene;
    Scene messageScene;
    Scene accountScene;
    Stage primaryStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("CS342_Project3_Server Monitor");

        this.mainScene = createMainScene();
        this.messageScene = createMessageScene();
        this.accountScene = createAccountScene();

        primaryStage.setScene(mainScene);
        primaryStage.show();

        primaryStage.setOnCloseRequest((WindowEvent t) -> {
            AccountDatabase.saveToFile("account_data.txt");
            System.exit(0);
        });

        

        serverConnection = new Server(data -> {

            String displayName = data.senderName != null ? data.senderName : data.recipient;
            // if (listItems == null || listUsers == null) return;
            switch (data.type) {
                case TEXT:
                    // listItems.getItems().add(displayName);
                    break;
                case NEWUSER:
                    listUsers.getItems().add(String.valueOf(data.recipient));
                    listItems.getItems().add(displayName + " has joined!");
                    refreshAccountTable();
                    break;
                case DISCONNECT:
                    listItems.getItems().add(data.senderName + " has disconnected!!!!!!!");
                    listUsers.getItems().remove(data.recipient);
                    refreshAccountTable();
                    break;
            }
            
        });
    }

    private Scene createMainScene() {
        Label label1 = new Label("Server: Connect4");
        label1.setFont(Font.font("Serif", 20));
        Button accountButton = new Button("Account");
        Button messageButton = new Button("View Messages");
        messageButton.setOnAction(e -> primaryStage.setScene(messageScene));

        accountButton.setOnAction(e -> {
            refreshAccountTable();
            primaryStage.setScene(accountScene);
        });

        VBox buttonBox = new VBox(20, label1, accountButton, messageButton);
        buttonBox.setPadding(new Insets(100, 0, 0, 150));

        BorderPane mainPane = new BorderPane();
        mainPane.setStyle("-fx-background-color: lightgray; -fx-font-family: 'serif';");
        mainPane.setCenter(buttonBox);

        return new Scene(mainPane, 500, 400);
    }

    private Scene createMessageScene() {
        Label label = new Label("Message Scene");
        label.setFont(Font.font("Times New Roman", 18));

        listItems = new ListView<>();
        listUsers = new ListView<>();
        HBox lists = new HBox(10, listUsers, listItems);
        lists.setPadding(new Insets(10));

        Button backBtn = new Button("Back to Main");
        backBtn.setOnAction(e -> primaryStage.setScene(mainScene));

        VBox messageLayout = new VBox(15, label, lists, backBtn);
        messageLayout.setAlignment(Pos.CENTER);
        messageLayout.setPadding(new Insets(20));

        BorderPane messagePane = new BorderPane();
        messagePane.setStyle("-fx-background-color: coral; -fx-font-family: 'serif';");
        messagePane.setCenter(messageLayout);

        return new Scene(messagePane, 600, 400);
    }


    private Scene createAccountScene() {
        Label label = new Label("Registered Accounts");
        label.setFont(Font.font("Serif", 18));
    
        // ✅ 두 개의 ListView: 하나는 유저명, 하나는 비밀번호
        ListView<String> usernameList = new ListView<>();
        ListView<String> passwordList = new ListView<>();
        ListView<String> winRateList = new ListView<>();
        ListView<String> totalGameList = new ListView<>();
    
        // 🔁 갱신 함수
        Runnable refreshAccounts = () -> {
            usernameList.getItems().clear();
            passwordList.getItems().clear();
            winRateList.getItems().clear();
            totalGameList.getItems().clear();

            for (Map.Entry<String, String> entry : LoginHandler.getAllUsers().entrySet()) {
                String username = entry.getKey();
                usernameList.getItems().add(entry.getKey());
                passwordList.getItems().add(entry.getValue());
                winRateList.getItems().add(AccountDatabase.getWinRate(username) + " %");
                totalGameList.getItems().add(AccountDatabase.getGameCount(username) + " games");
            }
        };
    
        // 초기 로딩
        refreshAccounts.run();
    
        // 🔘 레이아웃 구성
        Label usernameLabel = new Label("Username");
        Label passwordLabel = new Label("Password");
        Label winRateLabel = new Label("WinRate");
        Label TotalGameLabel = new Label("Total");
    
        VBox usernameBox = new VBox(5, usernameLabel, usernameList);
        VBox passwordBox = new VBox(5, passwordLabel, passwordList);
        VBox winrateBox = new VBox(5, winRateLabel, winRateList);
        VBox totalgameBox = new VBox(5, TotalGameLabel, totalGameList);
    
        HBox listsBox = new HBox(10, usernameBox, passwordBox, winrateBox, totalgameBox);
        listsBox.setAlignment(Pos.CENTER);
        listsBox.setPadding(new Insets(10));
    
        // ⏪ 버튼
        Button backBtn = new Button("Back to Main");
        backBtn.setOnAction(e -> primaryStage.setScene(mainScene));
    
        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> refreshAccounts.run());
    
        HBox buttonBox = new HBox(10, backBtn, refreshBtn);
        buttonBox.setAlignment(Pos.CENTER);
    
        VBox layout = new VBox(15, label, listsBox, buttonBox);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
    
        BorderPane pane = new BorderPane();
        pane.setStyle("-fx-background-color: lightblue; -fx-font-family: 'serif';");
        pane.setCenter(layout);
    
        return new Scene(pane, 600, 400);
    }
    
    
    // private Scene createAccountScene() {
    //     Label label = new Label("Registered Accounts");
    //     label.setFont(Font.font("Serif", 18));

    //     accountTable = new TableView<>();
    //     TableColumn<Map.Entry<String, String>, String> usernameCol = new TableColumn<>("Username");
    //     usernameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getKey()));
    //     TableColumn<Map.Entry<String, String>, String> passwordCol = new TableColumn<>("Password");
    //     passwordCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getValue()));

    //     accountTable.getColumns().add(usernameCol);
    //     accountTable.getColumns().add(passwordCol);

    //     Button backBtn = new Button("Back to Main");
    //     backBtn.setOnAction(e -> primaryStage.setScene(mainScene));

    //     VBox layout = new VBox(15, label, accountTable, backBtn);
    //     layout.setAlignment(Pos.CENTER);
    //     layout.setPadding(new Insets(20));

    //     BorderPane pane = new BorderPane();
    //     pane.setStyle("-fx-background-color: lightblue; -fx-font-family: 'serif';");
    //     pane.setCenter(layout);

    //     return new Scene(pane, 600, 400);
    // }
    


    private void refreshAccountTable() {
        if (accountTable != null) {
            accountTable.getItems().clear();
            accountTable.getItems().addAll(LoginHandler.getAllUsers().entrySet());
        }
    }
}
