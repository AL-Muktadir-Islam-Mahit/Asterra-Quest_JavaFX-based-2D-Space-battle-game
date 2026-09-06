package com.example.demo;

class HitEffect {
    double x, y;      // world position
    double life = 0.3; // seconds total
    double t = 0;     // how long it has existed

    HitEffect(double x, double y) {
        this.x = x; this.y = y;
    }
    boolean update(double dt) {
        t += dt;
        return t >= life; // return true if expired
    }
    double alpha() {
        return 1.0 - (t / life); // fade out
    }
    double size() {
        return 20 * (t / life + 0.5); // expand over time
    }
}