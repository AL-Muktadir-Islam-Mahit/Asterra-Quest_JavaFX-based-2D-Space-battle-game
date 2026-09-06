package com.example.demo;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Label;
import javafx.util.Duration;

public class CombatLoaderController {

    @FXML
    private ProgressBar loadingBar;

    @FXML
    private Label loadingLabel;

    private double progress = 0;

    @FXML
    private void initialize() {


        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(100), e -> updateProgress())
        );
        timeline.setCycleCount(120);
        timeline.setOnFinished(e -> onLoadingComplete());
        timeline.play();
    }

    private void updateProgress() {
        progress += 1.0 / 120.0;
        loadingBar.setProgress(progress);
        int percent = (int) Math.min(progress * 100, 100);
        loadingLabel.setText("Loading... " + percent + "%");
    }

    private void onLoadingComplete() {
        loadingLabel.setText("Ready for Launch!");
        Main.switchScene("singleshooter.fxml");
    }
}
