package com.example.demo;

// coin collection msg
class FloatingMessage {
    String text;
    double x, y;
    double timeLeft;

    FloatingMessage(String text, double x, double y, double duration) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.timeLeft = duration;
    }

    boolean update(double dt) {
        timeLeft -= dt;
        return timeLeft <= 0;
    }
}