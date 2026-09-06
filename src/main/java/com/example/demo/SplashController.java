package com.example.demo;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.util.Duration;

public class SplashController {

    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;

    @FXML
    public void initialize() { // Scene load howar por first run hoy
        playIntroAnimation(); // animation start korar jonno function call
    }

    private void playIntroAnimation() {

        // choto -> boro -> fade ! (eta holo main idea)

        titleLabel.setScaleX(0.1); // title ke horizontal choto kora
        titleLabel.setScaleY(0.1); // title ke vertical choto hbe
        titleLabel.setOpacity(0);  // title ke cmpltly invisible kora

        ScaleTransition scaleTitle = new ScaleTransition(Duration.seconds(2.5), titleLabel); // 2.5 sec e scaling animation create kora
        scaleTitle.setToX(1); // abar normal size e ana (X-axis)
        scaleTitle.setToY(1); // abar normal size e ana (Y-axis)
        scaleTitle.setInterpolator(Interpolator.EASE_OUT); // smoothly animation sesh kora

        FadeTransition fadeTitle = new FadeTransition(Duration.seconds(2.5), titleLabel); // fade animation 2.5 sec e
        fadeTitle.setToValue(1); // fully visible kore deya

        ParallelTransition titleAnim = new ParallelTransition(scaleTitle, fadeTitle); // ekshathe scale + fade animation chalano

        // Subtitle: fade in after title
        subtitleLabel.setOpacity(0); // subtitle ke invisible rakha surute
        FadeTransition fadeSubtitle = new FadeTransition(Duration.seconds(2.2), subtitleLabel); // subtitle fade animation
        fadeSubtitle.setToValue(1); // subtitle fully visible hobe
        fadeSubtitle.setDelay(Duration.seconds(2)); // 2 sec pore subtitle fade start hobe

        // After animations, switch scene
        PauseTransition wait = new PauseTransition(Duration.seconds(2)); // 3 sec wait transition banano
        wait.setOnFinished(e -> Main.switchScene("loading.fxml")); // wait sesh hole next scene e switch hobe

        SequentialTransition sequence = new SequentialTransition(titleAnim, fadeSubtitle, wait); // protome title anim, then subtitle, then wait
        sequence.play(); // pura sequence start kore deya
    }
}
