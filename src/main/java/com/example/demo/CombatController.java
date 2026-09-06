package com.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class CombatController {

    @FXML
    private Button singlePlayerButton;
    @FXML
    private Button multiPlayerButton;
    @FXML
    private Button backButton;

    @FXML
    private void initialize() {

    }

    @FXML
    private void onSinglePlayerClick() {
        Main.switchScene("combatloader.fxml");
    }

    @FXML
    private void onMultiPlayerClick() {

        Main.switchScene("multiplayer_shooter.fxml");

    }

    @FXML
    private void onBackClick() {
        Main.switchScene("home.fxml");
    }
}
