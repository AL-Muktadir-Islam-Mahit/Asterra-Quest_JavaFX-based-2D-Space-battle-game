package com.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    private final File userFile = new File("users.txt"); // user data file

    @FXML
    private void handleLogin(ActionEvent event) { // login btn action
        String username = usernameField.getText().trim(); // get username
        String password = passwordField.getText().trim(); // get password

        if (username.isEmpty() || password.isEmpty()) { // empty check
            showAlert(Alert.AlertType.ERROR, "Error", "All fields are required."); // error msg
            return; // stop here
        }

        // file nai check

        if (!userFile.exists()) { // file jodi na thake
            showAlert(Alert.AlertType.ERROR, "Error", "No accounts found. Please sign up first."); // no account msg
            return;
        }

        // file paisi

        try {
            List<String> lines = Files.readAllLines(userFile.toPath()); // read file
            for (String line : lines) { // loop each line
                String[] parts = line.split(":"); // split data
                if (parts.length == 6) { // 6 part check
                    if (parts[0].equalsIgnoreCase(username) && parts[1].equals(password)) { // match check

                        // user data load
                        UserData.username = parts[0];
                        UserData.password = parts[1];
                        UserData.coins = Integer.parseInt(parts[2]);
                        UserData.level = Integer.parseInt(parts[3]);
                        UserData.wins = Integer.parseInt(parts[4]);
                        UserData.losses = Integer.parseInt(parts[5]);

                        // login success
                        Main.switchScene("home.fxml"); // go home scene
                        return;
                    }
                }
            }

            showAlert(Alert.AlertType.ERROR, "Error", "Invalid username or password."); // invalid msg

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not read accounts."); // read error
            e.printStackTrace(); // print error
        }
    }

    @FXML
    private void goToSignup(ActionEvent event) { // signup btn
        Main.switchScene("signup.fxml"); // go signup scene
    }

    @FXML
    private void goBack() { // back btn
        Main.switchScene("story.fxml"); // go story scene
    }

    private void showAlert(Alert.AlertType type, String title, String message) { // alert box
        Alert alert = new Alert(type); // new alert
        alert.setTitle(title); // set title
        alert.setHeaderText(null); // no header
        alert.setContentText(message); // set msg
        alert.showAndWait(); // show alert
    }
}

