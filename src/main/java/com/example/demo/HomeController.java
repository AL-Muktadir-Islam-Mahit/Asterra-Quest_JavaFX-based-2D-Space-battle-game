package com.example.demo;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

public class HomeController {

    @FXML private Button profileButton;
    @FXML private Button storeButton;
    @FXML private Label coinsLabel;
    @FXML private Button openWorldButton;
    @FXML private Button mCombatButton;
    //@FXML private ImageView spaceshipImage;
    @FXML private Button WorldChat;


    @FXML private Button aboutButton;
    @FXML private Button settingsButton;
    @FXML private Button howToPlayButton;
    @FXML private Button Chatbot;
    @FXML private Button ranking;
    @FXML private Button events;

    @FXML
    public void initialize() {
        // Load background image via CSS (already handled)

        // Load spaceship image
        //spaceshipImage.setImage(new Image(getClass().getResource("spaceship.png").toExternalForm()));

        // Set coins from UserData
        coinsLabel.setText("Coins: " + UserData.coins);

        // Floating animation
        //TranslateTransition tt = new TranslateTransition(Duration.seconds(2), spaceshipImage);
        //tt.setByY(-15);
        //tt.setAutoReverse(true);
        //tt.setCycleCount(TranslateTransition.INDEFINITE);
        //tt.play();
    }

    @FXML
    private void onProfileClick() {
        Main.switchScene("profile.fxml");
    }

    @FXML
    private void onPlayQuiz() {
        Main.switchScene("quiz.fxml");
    }


    @FXML
    private void onAboutClick() {
        Main.switchScene("about.fxml");
    }

    @FXML
    private void onSettingsClick() {
        Main.switchScene("settings.fxml");
    }

    @FXML
    private void onHowToPlayClick() {
        Main.switchScene("how_to_play.fxml");
    }

    // add work of open world here !

    // working not cmple
    @FXML
    private void onOpenWorldClick() {
        Main.switchScene("solar_system.fxml");
    }

    @FXML
    private void onWorldChatclick(){
        Main.switchScene("WorldChat.fxml");
    }

    @FXML
    private void onchatbotClick(){
        Main.switchScene("chatbot.fxml");
    }

    @FXML
    private void onrankingclick(){
        Main.switchScene("ranking.fxml");
    }

    @FXML
    private void onCombatButtonClick(){
        Main.switchScene("combat.fxml");
    }

    @FXML
    private void oneventclick(){
        Main.switchScene("cosmic.fxml");
    }

    @FXML
    private void onStoreClick(){
        Main.switchScene("store.fxml");
    }


}
