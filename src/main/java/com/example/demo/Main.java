package com.example.demo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.io.File;
import java.io.IOException;

public class Main extends Application {
    private static Stage primaryStage;
    private static final double WINDOW_WIDTH = 800;
    private static final double WINDOW_HEIGHT = 500;
    private static MediaPlayer mediaPlayer;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        playBackgroundMusic();
        switchScene("splash.fxml");
        stage.setTitle("AsterraQuest");
        stage.show();


        // Second window (Another Stage)
        /*Stage secondStage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("WorldChat.fxml"));
        Scene secondScene = new Scene(loader.load(), WINDOW_WIDTH, WINDOW_HEIGHT);
        secondStage.setScene(secondScene);
        secondStage.setTitle("AsterraQuest");
        secondStage.show();*/




    }




    //bgm
    private void playBackgroundMusic() {
        if (mediaPlayer == null) {
            String path = new File("src/main/resources/com/example/demo/bgm.mp3").toURI().toString();
            Media media = new Media(path);
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE); // inifnt loop
            mediaPlayer.setVolume(0.06); // cntrl volumwwwww
            mediaPlayer.play();
        }
    }

    public static void switchScene(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource(fxml));
            Scene scene = new Scene(loader.load(), WINDOW_WIDTH, WINDOW_HEIGHT);
            primaryStage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
