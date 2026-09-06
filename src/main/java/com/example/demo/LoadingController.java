package com.example.demo;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.util.Duration;

public class LoadingController {

    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label progressText;

    @FXML
    public void initialize() {
        Timeline timeline = new Timeline(); // ekta new timeline banano (animation er jonno)
        final int[] progress = {0}; // progress count 0 theke start

        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(40), e -> { // prottek 40ms e ektu update hobe
            progress[0]++; // progress 1 barabe
            double progressValue = progress[0] / 100.0; // percentage calculating
            progressBar.setProgress(progressValue); // progress bar update
            progressText.setText((progress[0]) + "%"); // percentage text dekhano

            if (progress[0] >= 100) {
                timeline.stop();
                Main.switchScene("story.fxml");
            }
        }));

        timeline.setCycleCount(100); // total 100 bar repeat hobe
        timeline.play(); // animation start
    }

}
