package com.example.demo;

//astroid er sathe dhakka lege blast

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

class BlastEffect {
    double x, y;
    double t = 0;
    double life = 1.5; // seconds

    BlastEffect(double x, double y) {
        this.x = x;
        this.y = y;
        playBlastSound();  // play sound when blast is created
    }

    private void playBlastSound() {
        try {
            String soundPath = getClass().getResource("/com/example/demo/blast.mp3").toExternalForm();
            Media sound = new Media(soundPath);
            MediaPlayer mediaPlayer = new MediaPlayer(sound);
            mediaPlayer.setVolume(1);
            mediaPlayer.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    boolean update(double dt) {
        t += dt;
        return t >= life;
    }

    double alpha() { return 1.0 - t / life; }
    double size() { return 30 + 120 * (t / life); }
}
