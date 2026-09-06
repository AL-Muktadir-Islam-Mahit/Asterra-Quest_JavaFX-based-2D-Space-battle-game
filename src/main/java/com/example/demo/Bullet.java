package com.example.demo;

 class Bullet {
    double x, y;       // current world coords
    double px, py;     // previous world coords (for swept collision)
    double vx, vy;     // normalized direction
    double speed = 500; // px/s

    Bullet(double x, double y, double vx, double vy) {
        this.x = this.px = x;
        this.y = this.py = y;
        this.vx = vx; this.vy = vy;
    }
    void update(double dt) {
        // keep previous before moving
        px = x; py = y;
        x += vx * speed * dt;
        y += vy * speed * dt;
    }
}