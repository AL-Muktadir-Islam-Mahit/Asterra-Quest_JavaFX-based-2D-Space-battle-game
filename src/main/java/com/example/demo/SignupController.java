package com.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;

import java.io.*;
import java.nio.file.Files;
import java.util.List;

public class SignupController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    private final File userFile = new File("users.txt"); // user data file

    @FXML
    private void handleSignup(ActionEvent event) { // signup btn
        String username = usernameField.getText().trim(); // get username
        String password = passwordField.getText().trim(); // get password
        String confirmPassword = confirmPasswordField.getText().trim(); // get confirm

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) { // empty check
            showAlert(Alert.AlertType.ERROR, "Error", "All fields are required."); // error msg
            return;
        }

        if (!password.equals(confirmPassword)) { // password match check
            showAlert(Alert.AlertType.ERROR, "Error", "Passwords do not match."); // mismatch msg
            return;
        }

        try {
            if (!userFile.exists()) { // jodi file na thake
                userFile.createNewFile(); // notun file create
            }

            // already account check
            List<String> lines = Files.readAllLines(userFile.toPath()); // pura file line by line pora
            for (String line : lines) { // each line loop
                String[] parts = line.split(":"); // split data
                if (parts.length > 0 && parts[0].equalsIgnoreCase(username)) { // username match check
                    showAlert(Alert.AlertType.ERROR, "Error", "Username already exists."); // duplicate msg
                    return;
                }
            }

            // username:password:coins:level:wins:losses  format
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(userFile, true))) {
                // BufferedWriter -> ekdhap e onek data likhar jonno fast way
                // FileWriter(userFile, true) -> true mane append mode (notun line add hobe, purono thakbe)
                writer.write(username + ":" + password + ":0:1:0:0"); // new user data write
                writer.newLine(); // ekta notun line dibe
            }

            showAlert(Alert.AlertType.INFORMATION, "Success", "Account created successfully!"); // success msg
            Main.switchScene("login.fxml"); // go login page

        } catch (IOException e) { // error hole
            showAlert(Alert.AlertType.ERROR, "Error", "Could not save account."); // error msg
            e.printStackTrace(); // error print
        }
    }

    @FXML
    private void goToLogin(ActionEvent event) { // go login btn
        Main.switchScene("login.fxml"); // switch login
    }

    private void showAlert(Alert.AlertType type, String title, String message) { // alert method
        Alert alert = new Alert(type); // new alert
        alert.setTitle(title); // set title
        alert.setHeaderText(null); // no header
        alert.setContentText(message); // set msg
        alert.showAndWait(); // show alert
    }
}
