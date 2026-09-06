package com.example.demo;

import javafx.scene.media.AudioClip;

public class SoundManager {
    private static double masterVolume = 1.0; // 0.0 to 1.0
    private final AudioClip clip;

    public SoundManager(String resourcePath) {
        clip = new AudioClip(getClass().getResource(resourcePath).toExternalForm());
    }

    /** play with current master volume */
    public void play() {
        if (clip != null) clip.play(masterVolume);
    }

    /** set global volume for all sounds (0.0–1.0) */
    public static void setMasterVolume(double v) {
        masterVolume = Math.max(0.0, Math.min(1.0, v));
    }

    /** get current volume */
    public static double getMasterVolume() {
        return masterVolume;
    }
}
