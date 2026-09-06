package com.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ProfileController {

    @FXML private Label usernameLabel;
    @FXML private Label passwordLabel;
    @FXML private Label levelLabel;
    @FXML private Label winsLabel;
    @FXML private Label lossesLabel;
    @FXML private Label ratioLabel;

    @FXML
    public void initialize() {
        loadUserData();
    }

    private void loadUserData() {


        String username = UserData.username;

        try (BufferedReader br = new BufferedReader(new FileReader("users.txt"))) {


            String line;
            while ((line = br.readLine()) != null) {

                // username:password:coins:level:win:loss

                String[] parts = line.split(":");
                if (parts.length >= 6 && parts[0].equals(username)) {

                    usernameLabel.setText("" + parts[0]);
                    passwordLabel.setText("" + parts[1]);
                    levelLabel.setText("" + parts[3]);
                    winsLabel.setText("" + parts[4]);
                    lossesLabel.setText("" + parts[5]);


                    int wins = Integer.parseInt(parts[4]);
                    int losses = Integer.parseInt(parts[5]);
                    double ratio = (losses == 0) ? wins : (double) wins / losses;
                    ratioLabel.setText(String.format("%.2f", ratio));
                    break;

                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @FXML
    private void onBackClick() {
        Main.switchScene("home.fxml");
    }

    @FXML
    private void onLogoutClick() {
        Main.switchScene("login.fxml");
    }
}
