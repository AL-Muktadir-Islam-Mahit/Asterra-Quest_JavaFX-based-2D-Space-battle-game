package com.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.io.File;

public class StoryController {

    @FXML
    private Label storyText;

    @FXML
    private Button nextButton;

    @FXML
    private Button muteButton;

    private MediaPlayer mediaPlayer;
    private int storyIndex = 0;
    private boolean isMuted = false;

    private final String[] storyParagraphs = {
            "the kardashev scale says that there could be type one type two or type three civilizations a type one civilization is maybe a hundred years more advanced than us to maybe a thousand years sort of like Buck Rogers or Flash Gordon they control the weather volcanoes earthquakes anything planetary they control that's type one",
            "then there's type two type two is Stellar they harness the power of an entire star(using dyson sphere) like Star Trek Star Trek would be a typical type 2 civilization where they manipulate entire stars then there's",
            "type three type three is galactic they roam the galactic space Lanes they play with black holes like the Empire of the Star Wars series would be a typical type 3 civilization",
            "This game is set up in a time line of type 3 civilization , you are a astronaut in this era completing several missions across galazy."
    };

    @FXML
    public void initialize() {
        storyText.setText(storyParagraphs[storyIndex]);
        String audioPath = new File("src/main/resources/com/example/demo/storyaudio.mp3").toURI().toString();
        Media media = new Media(audioPath);
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setVolume(1.0);
        mediaPlayer.play();

        // Add hover and cursor styles
        muteButton.setStyle("-fx-font-size: 18px; -fx-background-color: transparent; -fx-text-fill: red; -fx-cursor: hand;");
        muteButton.setOnMouseEntered(e -> {
            if (isMuted) {
                muteButton.setStyle("-fx-background-color: #1E90FF; -fx-text-fill: white; -fx-font-size: 18px; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, #87CEFA, 10, 0, 0, 0);");
            } else {
                muteButton.setStyle("-fx-background-color: rgba(255,0,0,0.1); -fx-text-fill: red; -fx-font-size: 18px; -fx-cursor: hand;");
            }
        });
        muteButton.setOnMouseExited(e -> {
            if (isMuted) {
                muteButton.setStyle("-fx-background-color: blue; -fx-text-fill: white; -fx-font-size: 18px; -fx-cursor: hand;");
            } else {
                muteButton.setStyle("-fx-background-color: transparent; -fx-text-fill: red; -fx-font-size: 18px; -fx-cursor: hand;");
            }
        });
    }

    @FXML
    private void handleNext() {
        storyIndex++;
        if (storyIndex < storyParagraphs.length) {
            storyText.setText(storyParagraphs[storyIndex]);
            if (storyIndex == storyParagraphs.length - 1) {
                nextButton.setText("Finish");
            }
        } else {
            if (mediaPlayer != null) mediaPlayer.stop();
            Main.switchScene("welcome.fxml");
        }
    }

    @FXML
    private void handleMute() {
        //System.out.println("dg");
        if (mediaPlayer != null) {
            isMuted = !isMuted;

            if (isMuted) {
                mediaPlayer.setVolume(0.0);
                muteButton.setStyle("-fx-background-color: blue; -fx-text-fill: white; -fx-font-size: 18px; -fx-cursor: hand;");
                muteButton.setText("🔇");
            } else {
                mediaPlayer.setVolume(1.0);
                muteButton.setStyle("-fx-background-color: transparent; -fx-text-fill: red; -fx-font-size: 18px; -fx-cursor: hand;");
                muteButton.setText("✖");
            }
        }
    }
}
