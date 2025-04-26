import javafx.application.Application;
import javafx.application.Platform;
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
            switch (data.type) {
                case TEXT:
                    break;
                case NEWUSER:
                Platform.runLater(() -> {
                    listUsers.getItems().add(String.valueOf(data.recipient));
                    listItems.getItems().add(displayName + " has joined!");
                    refreshAccountTable();
                });
                break;
                case DISCONNECT:
                Platform.runLater(() -> {
                    System.out.println("Check: " + data.senderName + "is Disconnected");
                    listItems.getItems().add(data.senderName + " has disconnected!!!!!!!");
                    listUsers.getItems().remove(data.recipient);
                    refreshAccountTable();
                });
                break;
            }
            
        });
    }

    private Scene createMainScene() {
        Label label1 = new Label("Server: Connect4");
        label1.setFont(Font.font("Arial", 20));
        label1.setPadding(new Insets(0,0,0,30));
        Button accountButton = new Button("Account");
        accountButton.setPrefSize(100,40);
        Button messageButton = new Button("User Status");
        messageButton.setPrefSize(100,40);
 
        messageButton.setOnAction(e -> {
            primaryStage.setScene(messageScene);
        });

        accountButton.setOnAction(e -> {
            refreshAccountTable();
            primaryStage.setScene(accountScene);
        });

        HBox buttonBox = new HBox(20, accountButton, messageButton);
        VBox u8 = new VBox(20, label1, buttonBox);
        
        BorderPane mainPane = new BorderPane(u8);
        u8.setPadding(new Insets(140,0,0,190));
        mainPane.setStyle("-fx-background-color: lightblue; -fx-font-family: 'serif';");
        
        return new Scene(mainPane, 600, 400);
    }

    private Scene createMessageScene() {
        Label label = new Label("User Status");
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
        messagePane.setStyle("-fx-background-color: lightblue; -fx-font-family: 'serif';");
        messagePane.setCenter(messageLayout);

        return new Scene(messagePane, 600, 400);
    }


    private Scene createAccountScene() {
        Label label = new Label("Registered Accounts");
        label.setFont(Font.font("Serif", 18));
    
        // Four Listview username, password, rate, total
        ListView<String> usernameList = new ListView<>();
        ListView<String> passwordList = new ListView<>();
        ListView<String> winRateList = new ListView<>();
        ListView<String> totalGameList = new ListView<>();
    
        // refressh
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
    
        // iternal refresh loading
        refreshAccounts.run();
    
        // Frame
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


    private void refreshAccountTable() { // refresh
        if (accountTable != null) {
            accountTable.getItems().clear();
            accountTable.getItems().addAll(LoginHandler.getAllUsers().entrySet());
        }
    }
}
