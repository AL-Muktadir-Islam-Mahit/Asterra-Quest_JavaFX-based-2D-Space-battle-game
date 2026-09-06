package com.example.demo;

import javafx.fxml.FXML;

public class WelcomeController {

    @FXML
    private void goToLogin() {
        Main.switchScene("login.fxml");
    }
}
