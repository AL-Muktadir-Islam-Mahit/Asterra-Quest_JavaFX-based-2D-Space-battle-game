package com.example.demo;

import javafx.fxml.FXML;

public class HowToPlayController {

    @FXML
    private void onBackClick() {
        Main.switchScene("home.fxml");
    }
}
