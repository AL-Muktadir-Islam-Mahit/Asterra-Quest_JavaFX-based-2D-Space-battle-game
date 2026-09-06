package com.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class SettingsController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    private final File userFile = new File("users.txt");

    @FXML
    public void initialize() {
        // Pre-fill the fields with the current username and password
        usernameField.setText(UserData.username);
        passwordField.setText(UserData.password);
    }

    @FXML
    private void handleSaveChanges(ActionEvent event) {
        String newUsername = usernameField.getText().trim();
        String newPassword = passwordField.getText().trim();

        if (newUsername.isEmpty() || newPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "All fields are required.");
            return;
        }

        try {
            List<String> lines = Files.readAllLines(userFile.toPath());
            boolean userFound = false;

            // Loop through users to find the matching one
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                String[] parts = line.split(":");
                if (parts[0].equals(UserData.username)) {
                    // Update the username and password
                    lines.set(i, newUsername + ":" + newPassword + ":" + parts[2] + ":" + parts[3] + ":" + parts[4] + ":" + parts[5]);
                    userFound = true;
                    break;
                }
            }

            if (userFound) {
                // Save changes to file
                Files.write(userFile.toPath(), lines, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
                UserData.username = newUsername;
                UserData.password = newPassword;

                showAlert(Alert.AlertType.INFORMATION, "Success", "Your information has been updated.");
                Main.switchScene("home.fxml"); // Go back to home scene
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "User not found.");
            }

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not save changes.");
            e.printStackTrace();
        }
    }

    @FXML
    private void goBack() {
        Main.switchScene("home.fxml");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
