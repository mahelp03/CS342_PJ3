import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;

public class Login extends Application {

    private TextField usernameField;
    private PasswordField passwordField;
    private Label statusLabel;
    private CheckBox signupCheckBox;
    private Button signupButton;
    private Button loginButton;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Connect Four - Login");

        // Logo
        ImageView logo = new ImageView(new Image("connect4.png"));
        logo.setFitWidth(100);
        logo.setPreserveRatio(true);

        // Username
        usernameField = new TextField();
        usernameField.setPromptText("Enter your username");

        // Password
        passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");

        // Signup checkbox
        signupCheckBox = new CheckBox("SIGN UP");
        signupCheckBox.setOnAction(e -> signupButton.setDisable(!signupCheckBox.isSelected()));

        // Buttons
        signupButton = new Button("SIGN UP");
        signupButton.setDisable(true);
        signupButton.setOnAction(e -> handleSignup());

        loginButton = new Button("LOGIN");
        loginButton.setOnAction(e -> handleLogin());

        HBox buttonBox = new HBox(10, signupCheckBox, signupButton, loginButton);
        buttonBox.setAlignment(Pos.CENTER);

        statusLabel = new Label();

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Enter your account"), 1, 0);
        grid.add(new Label("Username:"), 0, 1);
        grid.add(usernameField, 1, 1);
        grid.add(new Label("Password:"), 0, 2);
        grid.add(passwordField, 1, 2);
        grid.add(buttonBox, 1, 3);
        grid.add(statusLabel, 1, 4);

        HBox layout = new HBox(20, logo, grid);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 500, 250);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Username and password required.");
            return;
        }

        try {
            socket = new Socket("localhost", 5555);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            out.writeObject("LOGIN");
            out.writeObject(username);
            out.writeObject(password);
            out.flush();

            Object response = in.readObject();
            if (response instanceof String && response.equals("OK")) {
                statusLabel.setText("Login successful!");
                // TODO: Proceed to main scene
            } else {
                statusLabel.setText("Login failed. Check credentials.");
            }

        } catch (Exception e) {
            statusLabel.setText("Login error: " + e.getMessage());
        }
    }

    private void handleSignup() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Username and password required.");
            return;
        }

        try {
            socket = new Socket("localhost", 5555);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            out.writeObject("SIGNUP");
            out.writeObject(username);
            out.writeObject(password);
            out.flush();

            Object response = in.readObject();
            if (response instanceof String && response.equals("OK")) {
                statusLabel.setText("Signup successful!");
            } else {
                statusLabel.setText("Signup failed. Username may be taken.");
            }

        } catch (Exception e) {
            statusLabel.setText("Signup error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
