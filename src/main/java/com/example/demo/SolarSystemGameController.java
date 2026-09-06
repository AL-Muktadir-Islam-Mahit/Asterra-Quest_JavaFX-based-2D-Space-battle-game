package com.example.demo;

import javafx.animation.AnimationTimer;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.*;
import javafx.scene.text.*;
import javafx.stage.Stage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javafx.application.Platform;
import javafx.fxml.FXML;

import javafx.scene.control.ButtonType;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;


public class SolarSystemGameController {


    //boost system
    // boost system
    boolean boostActive = false;
    double boostTimeLeft = 0;      // seconds left of active boost
    double boostCooldown = 0;      // seconds left before refill
    final double BOOST_DURATION = 30; // 30 seconds boost
    final double BOOST_COOLDOWN = 5; // 30 seconds cooldown


    private boolean paused = false;


    boolean IsBlackholeDestroyed = false;
    // ---- Mission system ----
    boolean missionActive = false;
    boolean missionCompleted = false;
    int currentMission = 0;

    boolean mission1Completed = false;
    boolean mission2Completed = false;
    boolean mission3Completed = false;

    private boolean blackholePopupShown = false;



    // ---- World/grid ----
    static final int TILES = 200;              // 200x200 map
    static final int TILE  = 32;               // each map cell size in pixels
    static final int WORLD_PX = TILES * TILE;  // world size in pixels

    // ---- Screen ----
    static final int VIEW_W = 800;             // window width
    static final int VIEW_H = 500;             // window height

    // ---- Camera / movement ----
    double camX = WORLD_PX / 2.0;              // camera target in world px
    double camY = WORLD_PX / 2.0;
    double vx = 0, vy = 0;                     // velocity (px/s)
    double accel = 400;                        // acceleration (px/s^2)
    double drag = 0.92;                        // damping per frame (scaled by dt)
    double maxSpeed = 900;                     // max speed (px/s)

    // ---- Map data ----
    int[][] map = new int[TILES][TILES];

    // ---- Stars (background) ----
    //star class in other file
    List<Star> stars = new ArrayList<>();

    // ---- Celestial bodies ----
    // ---- Celestial bodies ----
    //body class onno file e

    // floating msg coin collection ! was here


    private List<FloatingMessage> floatingMessages = new ArrayList<>();

    // coin collection msg sesh apatoto


    // blast hobe asteorid er sathe colide korle

    boolean gameOver = false;
    double gameOverTimer = 0;

    boolean shipVisible = true; // blast er por gayeb kore doear jonno

    // onno file e class
    List<BlastEffect> blasts = new ArrayList<>();

    // blast sesh

    List<Body> bodies = new ArrayList<>();


    // ---- Planet info & popup state (added) ----
    Map<Integer, String> planetInfo = new HashMap<>(Map.ofEntries(
            Map.entry(1, "Sun\nThe star at the center of our solar system."),
            Map.entry(2, "Mercury\nSmallest planet, closest to the Sun."),
            Map.entry(3, "Venus\nHot world with a thick atmosphere."),
            Map.entry(4, "Earth\nOur home planet, rich with life."),
            Map.entry(5, "Mars\nThe Red Planet, potential future outpost."),
            Map.entry(6, "Jupiter\nLargest planet with the Great Red Spot."),
            Map.entry(7, "Saturn\nFamous for its stunning rings."),
            Map.entry(8, "Uranus\nIce giant that rotates on its side."),
            Map.entry(9, "Neptune\nDistant, cold, and very windy."),
            Map.entry(88, "Warship\nBiggest defense of solar system"),
            Map.entry(69, "Wormhole\nA wormhole is like a magic tunnel in space. \uD83D\uDE80✨ \na hypothetical tunnel-like structure in spacetime that could act as a shortcut, connecting two distant points in the universe."),
            Map.entry(100, "International Space Station (ISS)\nA habitable satellite in low Earth orbit where astronauts live and work.")
    ));




    Body currentPlanet = null;          // the planet we're currently "in orbit" of
    double orbitEnterPad = 30;          // how close to trigger popup
    double orbitExitPad  = 80;          // hysteresis so it doesn't spam when hovering

    // Input
    Set<KeyCode> keys = new HashSet<>();

    // Map file name (always loads from project root)
    private static final String MAP_WORKDIR = "map.txt";

    // blackhole image

    private Image blackholeImg = new Image(Objects.requireNonNull(
            getClass().getResource("/com/example/demo/Blackhole.gif")).toExternalForm());


    // iss image

    private Image issImg = new Image(Objects.requireNonNull(
            getClass().getResource("/com/example/demo/iss.png")).toExternalForm());


    private Image neptuneImg = new Image(Objects.requireNonNull(
            getClass().getResource("/com/example/demo/neptune.png")).toExternalForm());


    private Image uranusImg = new Image(Objects.requireNonNull(
            getClass().getResource("/com/example/demo/uranus.png")).toExternalForm());


    private Image earthImg = new Image(Objects.requireNonNull(
            getClass().getResource("/com/example/demo/earth.png")).toExternalForm());

    private Image jupiterImg = new Image(Objects.requireNonNull(
            getClass().getResource("/com/example/demo/jupiter.png")).toExternalForm());

    private Image warshipImg = new Image(Objects.requireNonNull(
            getClass().getResource("/com/example/demo/warship.png")).toExternalForm());


    private Image warmholeImg = new Image(Objects.requireNonNull(
            getClass().getResource("/com/example/demo/warmhole.gif")).toExternalForm());







    // ---- Ship images ----
    Map<String, Image> shipImages = new HashMap<>();
    String shipDir = "up";     // current facing direction
    boolean moving = false;    // whether keys are pressed

    // ---- Bullets (added) ----

    // hit effect rmved in other file


    // bullet class also moved

    List<Bullet> bullets = new ArrayList<>();
    List<HitEffect> hitEffects = new ArrayList<>();

    double fireCooldown = 0.10; // seconds between shots when holding SPACE
    double fireTimer = 0.0;



    // add here that thing


    // ---- Loading map ----
    private void loadMapOrThrow() throws IOException {
        File f = new File(MAP_WORKDIR);
        if (f.exists()) {
            System.out.println("Loaded map from working dir: " + f.getAbsolutePath());
            try (InputStream in = new FileInputStream(f)) {
                readMapFromStream(in);
            }
            return;
        }
        throw new FileNotFoundException("Map not found at " + f.getAbsolutePath());
    }

    private void readMapFromStream(InputStream in) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            for (int y = 0; y < TILES; y++) {
                String line = br.readLine();
                if (line == null) throw new EOFException("Unexpected EOF at row " + y);
                String[] parts = line.trim().split("\\s+");
                if (parts.length < TILES) throw new IOException("Row " + y + " has only " + parts.length + " columns");

                for (int x = 0; x < TILES; x++) {
                    if (parts[x].equalsIgnoreCase("B")) {
                        map[y][x] = -99; // special code for blackhole
                    } else if(parts[x].equalsIgnoreCase("I")){
                        map[y][x] = 100;
                    }else if(parts[x].equalsIgnoreCase("W")){
                        map[y][x] = 88;  // for war spaceship
                    }else if(parts[x].equalsIgnoreCase("R")){
                        map[y][x] = 69;  // for warmhole
                    }else {
                        map[y][x] = Integer.parseInt(parts[x]);
                    }
                }
            }
        }
    }


    // ---- Body computation ----
    private void computeBodiesFromMap() {
        boolean[][] visited = new boolean[TILES][TILES];
        for (int y = 0; y < TILES; y++) {
            for (int x = 0; x < TILES; x++) {
                int code = map[y][x];
                if (code != 0 && !visited[y][x]) {
                    List<int[]> region = new ArrayList<>();
                    Deque<int[]> dq = new ArrayDeque<>();
                    dq.add(new int[]{x, y});
                    visited[y][x] = true;

                    while (!dq.isEmpty()) {
                        int[] p = dq.poll();
                        int px = p[0], py = p[1];
                        region.add(p);

                        int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
                        for (int[] d : dirs) {
                            int nx = px + d[0], ny = py + d[1];
                            if (nx >= 0 && ny >= 0 && nx < TILES && ny < TILES &&
                                    !visited[ny][nx] && map[ny][nx] == code) {
                                visited[ny][nx] = true;
                                dq.add(new int[]{nx, ny});
                            }
                        }
                    }

                    double sx = 0, sy = 0;
                    for (int[] p : region) {
                        sx += p[0];
                        sy += p[1];
                    }
                    double cx = (sx / region.size() + 0.5) * TILE;
                    double cy = (sy / region.size() + 0.5) * TILE;
                    double r = Math.sqrt(region.size()) * TILE / 2;

                    // ✅ Body creation
                    Body b = new Body();
                    b.code = code;
                    b.cx = cx;
                    b.cy = cy;
                    b.radiusPx = r;

                    // ✅ If asteroid, set HP
                    if (code == -1) {
                        b.hp = 30; // asteroid takes 30 hits
                    }

                    if(code == -99 ){
                        b.hp = 10;
                    }

                    bodies.add(b);
                }
            }
        }
    }


    // ---- Star background ----
    private void generateStars() {
        Random rnd = new Random(0);
        for (int i=0;i<1800;i++) {
            stars.add(new Star(rnd.nextDouble()*WORLD_PX,
                    rnd.nextDouble()*WORLD_PX,
                    0.5+rnd.nextDouble()*1.5,
                    0.3+0.7*rnd.nextDouble()));
        }
    }

    // ---- Update ----
    private void update(double dt) {

        if (paused) return;

        double ax=0,ay=0;
        moving = false;

        // movement using shif start

        if(keys.contains(KeyCode.W)) { ay -= 1; shipDir = "up"; moving = true; }
        if(keys.contains(KeyCode.S)) { ay += 1; shipDir = "down"; moving = true; }
        if(keys.contains(KeyCode.A)) { ax -= 1; shipDir = "left"; moving = true; }
        if(keys.contains(KeyCode.D)) { ax += 1; shipDir = "right"; moving = true; }

// normalize movement direction
        double len = Math.hypot(ax, ay);
        if(len > 0) { ax /= len; ay /= len; }

// check for shift turbo
        // ---- Boost activation ----
        if (keys.contains(KeyCode.SHIFT) && !boostActive && boostCooldown <= 0) {
            boostActive = true;
            boostTimeLeft = BOOST_DURATION;
        }

// ---- Boost timing ----
        if (boostActive) {
            boostTimeLeft -= dt;
            if (boostTimeLeft <= 0) {
                boostActive = false;
                boostCooldown = BOOST_COOLDOWN;
            }
        } else if (boostCooldown > 0) {
            boostCooldown -= dt;
        }

// ---- Apply boost multipliers ----
        double accelBoost = boostActive ? 3.0 : 1.0;    // x3 accel during boost
        double maxSpeedBoost = boostActive ? 2.0 : 1.0; // x2 max speed during boost


// apply acceleration
        vx += ax * accel * accelBoost * dt;
        vy += ay * accel * accelBoost * dt;

// apply drag
        vx *= Math.pow(drag, dt * 60);
        vy *= Math.pow(drag, dt * 60);

// clamp speed
        double spd = Math.hypot(vx, vy);
        if(spd > maxSpeed * maxSpeedBoost) {
            vx = vx / spd * maxSpeed * maxSpeedBoost;
            vy = vy / spd * maxSpeed * maxSpeedBoost;
        }

// update camera
        camX += vx * dt;
        camY += vy * dt;
        camX = clamp(camX, VIEW_W / 2.0, WORLD_PX - VIEW_W / 2.0);
        camY = clamp(camY, VIEW_H / 2.0, WORLD_PX - VIEW_H / 2.0);




        // movement using shif end

        // ---- Automatic fire while holding SPACE (respects cooldown) ----
        fireTimer -= dt;
        if (keys.contains(KeyCode.SPACE) && fireTimer <= 0) {
            fireOnce();
            fireTimer = fireCooldown;
        }

        // ---- Update bullets and cull off-world ----
        // ---- Update bullets, collide with bodies, and cull off-world ----
        for (int i = 0; i < bullets.size(); i++) {
            Bullet b = bullets.get(i);
            b.update(dt);

            boolean removed = false;

            // check segment (px,py)->(x,y) against all bodies
            for (Body body : bodies) {
                if (body.destroyed) continue; // skip dead asteroids

                double r = body.radiusPx;
                if (segmentCircleHit(b.px, b.py, b.x, b.y, body.cx, body.cy, r)) {
                    hitEffects.add(new HitEffect((b.x + b.px) / 2, (b.y + b.py) / 2));

                    // if asteroid, reduce HP
                    if (body.code == -1) {
                        body.hp--;
                        if (body.hp <= 0) {
                            body.destroyed = true;
                        }
                    }

                    // for blackhole
                    if (body.code == -99 && CanDestroyBlackhole) {
                        body.hp--;
                        if (body.hp <= 0) {
                            body.destroyed = true;
                            IsBlackholeDestroyed = true;
                        }
                    }

                    // remove bullet after impact
                    bullets.remove(i);
                    i--;
                    removed = true;
                    break;
                }
            }


            if (removed) continue;

            // off-world cull
            if (b.x < -32 || b.y < -32 || b.x > WORLD_PX + 32 || b.y > WORLD_PX + 32) {
                bullets.remove(i);
                i--;
            }
        }


        // ---- Planet approach detection & popup (added) ----
        // Ship's world position equals camera center.
        double shipX = camX, shipY = camY;

        // If already "in orbit" of a planet, only clear it after we really leave (hysteresis).
        if (currentPlanet != null) {
            double dist = Math.hypot(shipX - currentPlanet.cx, shipY - currentPlanet.cy);
            if (dist > currentPlanet.radiusPx + orbitExitPad) {
                currentPlanet = null;
            }
        } else {
            // Not in orbit → see if we've entered any planet's orbit.
            for (Body b : bodies) {

                if (b.code == -1) continue; // asteroid er jonno pop up window disable korlam !

                double dist = Math.hypot(shipX - b.cx, shipY - b.cy);
                if (dist <= b.radiusPx + orbitEnterPad) {
                    currentPlanet = b;
                    if(!b.destroyed){
                        showPlanetInfo(b);
                    }


                    break;
                }

            }
        }

        // collison with astoroid
        if (!gameOver) {
            for (Body b : bodies) {
                if (b.code == -1 && !b.destroyed) { // only asteroids
                    double dist = Math.hypot(shipX - b.cx, shipY - b.cy);
                    if (dist <= b.radiusPx * 0.8) { //  collision
                        gameOver = true;
                        gameOverTimer = 3.0; // 3 sec delay before restart
                        blasts.add(new BlastEffect(shipX, shipY));

                        shipVisible = false;   //  hide ship

                        // cmnt korlam karon jaate coin collected na hoi !
                        // b.destroyed = true;    // hide asteroid too


                        break;
                    }
                }
            }
        }





        // ---- Coin collection check ----
        // ---- Coin collection check ----
        for (Body b : bodies) {
            if (b.code == -1 && b.destroyed && !b.collected) {
                double dist = Math.hypot(camX - b.cx, camY - b.cy);
                if (dist <= b.radiusPx * 0.8) { // ship touches coin
                    b.collected = true;

                    // add coins to user
                    UserData.coins += 2;

                    // file e update hbe ebar !
                    updateUserCoinsInFile(UserData.username, UserData.coins);

                    // show floating message
                    floatingMessages.add(new FloatingMessage("You got 2 coins!", camX, camY, 2.0));
                }
            }
        }

        for (Body b : bodies) {
            if (b.code == -99 && b.destroyed && !b.collected) {

                //blasts.add(new BlastEffect(b.cx, b.cy));

                double dist = Math.hypot(camX - b.cx, camY - b.cy);
                if (dist <= b.radiusPx * 0.8) { // ship touches coin
                    b.collected = true;

                    // add coins to user
                    UserData.coins += 1000;

                    // file e update hbe ebar !
                    updateUserCoinsInFile(UserData.username, UserData.coins);

                    // show floating message
                    floatingMessages.add(new FloatingMessage("You got 1000 coins!", camX, camY, 2.0));
                }
            }
        }



        for (int i = 0; i < hitEffects.size(); i++) {
            if (hitEffects.get(i).update(dt)) {
                hitEffects.remove(i);
                i--;
            }
        }

        // coin collection update kora hbe ekhane
        for (int i = 0; i < floatingMessages.size(); i++) {
            if (floatingMessages.get(i).update(dt)) {
                floatingMessages.remove(i);
                i--;
            }
        }


        // update blasts
        for (int i = 0; i < blasts.size(); i++) {
            if (blasts.get(i).update(dt)) {
                blasts.remove(i);
                i--;
            }
        }

        // handle game over timer
        if (gameOver) {
            gameOverTimer -= dt;
            if (gameOverTimer <= 0) {
                restartGame(); // reset everything
            }
        }





    }

    // ---- Render ----
    private void render(GraphicsContext g) {
        g.setFill(Color.BLACK);
        g.fillRect(0,0,VIEW_W,VIEW_H);

        double viewLeft=camX-VIEW_W/2, viewTop=camY-VIEW_H/2;

        // stars
        g.setFill(Color.WHITE);
        for(Star s:stars){
            double sx=s.x-viewLeft, sy=s.y-viewTop;
            if(sx>=0&&sy>=0&&sx<VIEW_W&&sy<VIEW_H)
                g.fillOval(sx,sy,s.r*2,s.r*2);
        }

        // add this field in your class

        // antimter colction window after destroying blackhole !

        for (Body b : bodies) {
            if (b.code == -99 && b.destroyed && !b.collected) {



                if (!blackholePopupShown) {
                    blackholePopupShown = true;  // mark as shown

                    javafx.application.Platform.runLater(() -> {
                        javafx.stage.Stage dialog = new javafx.stage.Stage();

                        // Remove OS title bar
                        dialog.initStyle(javafx.stage.StageStyle.UNDECORATED);

                        // Block background but keep focus
                        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
                        if (gameCanvas.getScene() != null) {
                            dialog.initOwner(gameCanvas.getScene().getWindow());
                        }

                        // Prevent ESC or outside clicks from closing
                        dialog.setOnCloseRequest(e -> e.consume());

                        // 🔹 Label with toned-down neon
                        javafx.scene.control.Label label = new javafx.scene.control.Label(
                                "⚠️ Antimatter Finished ⚠️\n\n" +
                                        "You have used too much antimatter.\n" +
                                        "Navigate through the wormhole to the nebula\n" +
                                        "and collect fresh antimatter!"
                        );
                        label.setWrapText(true);
                        label.setStyle(
                                "-fx-font-size: 18px;" +
                                        "-fx-text-fill: #00eaff;" +   // neon cyan
                                        "-fx-font-weight: bold;" +
                                        "-fx-text-alignment: center;" +
                                        "-fx-effect: dropshadow(gaussian, #00eaff, 3, 0.2, 0, 0);" // softer glow
                        );

                        // 🔹 Sci-fi styled Close button
                        javafx.scene.control.Button closeBtn = new javafx.scene.control.Button("✦ CLOSE ✦");
                        closeBtn.setOnAction(e -> dialog.close());
                        closeBtn.setStyle(
                                "-fx-background-color: linear-gradient(to right, #00eaff, #0088ff);" +
                                        "-fx-text-fill: black;" +
                                        "-fx-font-weight: bold;" +
                                        "-fx-font-size: 15px;" +
                                        "-fx-padding: 8px 20px;" +
                                        "-fx-background-radius: 10;" +
                                        "-fx-effect: dropshadow(gaussian, #00eaff, 6, 0.6, 0, 0);" +
                                        "-fx-cursor: hand;"
                        );

                        // 🔹 Hover effect for button
                        closeBtn.setOnMouseEntered(ev -> closeBtn.setStyle(
                                "-fx-background-color: linear-gradient(to right, #0088ff, #00eaff);" +
                                        "-fx-text-fill: black;" +
                                        "-fx-font-weight: bold;" +
                                        "-fx-font-size: 15px;" +
                                        "-fx-padding: 8px 20px;" +
                                        "-fx-background-radius: 10;" +
                                        "-fx-effect: dropshadow(gaussian, #0088ff, 10, 0.8, 0, 0);" +
                                        "-fx-cursor: hand;"
                        ));
                        closeBtn.setOnMouseExited(ev -> closeBtn.setStyle(
                                "-fx-background-color: linear-gradient(to right, #00eaff, #0088ff);" +
                                        "-fx-text-fill: black;" +
                                        "-fx-font-weight: bold;" +
                                        "-fx-font-size: 15px;" +
                                        "-fx-padding: 8px 20px;" +
                                        "-fx-background-radius: 10;" +
                                        "-fx-effect: dropshadow(gaussian, #00eaff, 6, 0.6, 0, 0);" +
                                        "-fx-cursor: hand;"
                        ));

                        // 🔹 Layout with subtle neon glow
                        javafx.scene.layout.VBox layout = new javafx.scene.layout.VBox(20, label, closeBtn);
                        layout.setStyle(
                                "-fx-background-color: rgba(0,0,0,0.85);" +
                                        "-fx-padding: 25px;" +
                                        "-fx-border-color: #00eaff;" +
                                        "-fx-border-width: 2px;" +
                                        "-fx-border-radius: 12;" +
                                        "-fx-background-radius: 12;" +
                                        "-fx-effect: dropshadow(gaussian, #00eaff, 12, 0.6, 0, 0);" // glowing border
                        );
                        layout.setAlignment(javafx.geometry.Pos.CENTER);

                        javafx.scene.Scene scene = new javafx.scene.Scene(layout);
                        dialog.setScene(scene);
                        dialog.setTitle("Antimatter Alert");
                        dialog.show();
                    });
                }






            }
        }


        // bodies
        for(Body b:bodies){
            double cx=b.cx-viewLeft, cy=b.cy-viewTop, r=b.radiusPx;
            if(cx+r<0||cy+r<0||cx-r>VIEW_W||cy-r>VIEW_H) continue;
            switch(b.code){
                case -99:
                    if (b.destroyed && !b.collected) {

                        // okeeeeeeeee







                        String msg2 = "✔ Mission 2 Completed (+100 coins)";



                        //blasts.add(new BlastEffect(b.cx, b.cy));

                        // 🔥 Explosion effect (simple animated burst)
                        long t = System.currentTimeMillis() % 400; // cycle every 400ms
                        double progress = t / 400.0;

                        int particles = 14;
                        for (int i = 0; i < particles; i++) {
                            double angle = 2 * Math.PI * i / particles;
                            double dist = r * 8 * progress;
                            double px = cx + Math.cos(angle) * dist;
                            double py = cy + Math.sin(angle) * dist;

                            g.setGlobalAlpha(1 - progress); // fade out
                            g.setFill(Color.hsb(i * 25, 1, 1));
                            g.fillOval(px, py, 6 * (1 - progress), 6 * (1 - progress));
                        }
                        g.setGlobalAlpha(1);

                        // Then draw the coin
                        drawCoin(g, cx, cy, r * 2.6);
                    } else if (!b.destroyed) {
                        drawBlackhole(g, cx, cy, r * 30.5);
                    }

                    break;

                case 69:
                    drawWarmhole(g, cx, cy, r * 30.5); // blackhole bigger
                    break;

                case 88:
                    drawWarShip(g, cx, cy, r * 20.5); // blackhole bigger
                    break;

                case 100:
                    drawISS(g, cx, cy, r * 5.5); // blackhole bigger
                    break;
                case -1:
                    if (b.destroyed && !b.collected) { // collected hole coin er sign thakbe na !
                        drawCoin(g, cx, cy, r * 0.6); // coin smaller than asteroid
                    } else if (!b.destroyed) {
                        drawAsteroid(g, cx, cy, r);
                    }
                    break;

                case 1: drawSun(g,cx,cy,r); break;
                case 2: drawPlanet(g,cx,cy,r,Color.GRAY,Color.DARKGRAY); break;
                case 3: drawPlanet(g,cx,cy,r,Color.ORANGE,Color.DARKORANGE); break;
                case 4: drawEarth(g, cx, cy, r * 2.5);; break;
                case 5: drawPlanet(g,cx,cy,r,Color.RED,Color.DARKRED); break;
                case 6: drawJupiter(g, cx, cy, r * 2.5); break;
                case 7: drawSaturn(g,cx,cy,r); break;
                case 8: drawuranus(g, cx, cy, r * 10.5); break;
                case 9: drawneptune(g, cx, cy, r * 10.5); break;
            }
        }

        // bullets (screen-space from world)

        if(CanDestroyBlackhole == true){


            for (Bullet b : bullets) {
                double bx = b.x - viewLeft;
                double by = b.y - viewTop;

                if (bx < -20 || by < -20 || bx > VIEW_W + 20 || by > VIEW_H + 20) continue;

                // --- Glow effect (soft outer light) ---
                g.setFill(Color.rgb(0, 200, 255, 0.3)); // neon blue, semi-transparent
                g.fillOval(bx - 15, by - 15, 30, 30);   // bigger than bullet core

                // Bullet core (white)
                g.setFill(Color.WHITE);

                if (Math.abs(b.vx) > Math.abs(b.vy)) {
                    // horizontal shot
                    g.fillRoundRect(bx - 12, by - 2, 24, 4, 2, 2);


                    // Blue highlight
                    g.setStroke(Color.CYAN);
                    g.strokeLine(bx - 5, by, bx - 10, by - 6);
                    g.strokeLine(bx + 5, by, bx + 10, by + 6);

                } else {
                    // vertical shot
                    g.fillRoundRect(bx - 2, by - 12, 4, 24, 2, 2);



                    // Blue highlight
                    g.setStroke(Color.CYAN);
                    g.strokeLine(bx, by - 5, bx - 6, by - 10);
                    g.strokeLine(bx, by + 5, bx + 6, by + 10);
                }

                // --- Neon particles (tiny sparks around bullet) ---
                g.setFill(Color.rgb(0, 255, 255, 0.8)); // bright neon cyan
                for (int i = 0; i < 3; i++) { // 3 particles each frame
                    double px = bx + (Math.random() * 12 - 6); // random offset -6..6
                    double py = by + (Math.random() * 12 - 6);
                    g.fillOval(px, py, 2, 2);
                }
            }


        }else{
            g.setFill(Color.RED);
        }

        for (Bullet b : bullets) {
            double bx = b.x - viewLeft;
            double by = b.y - viewTop;
            if (bx < -20 || by < -20 || bx > VIEW_W + 20 || by > VIEW_H + 20) continue;
            // draw a simple laser
            if (Math.abs(b.vx) > Math.abs(b.vy)) {
                // horizontal shot
                g.fillRoundRect(bx - 12, by - 2, 24, 4, 2, 2);
            } else {
                // vertical shot
                g.fillRoundRect(bx - 2, by - 12, 4, 24, 2, 2);
            }
        }

        for (HitEffect h : hitEffects) {
            double hx = h.x - viewLeft;
            double hy = h.y - viewTop;

            g.setFill(Color.color(1, 0.5, 0, h.alpha())); // orange with fade
            double s = h.size();
            g.fillOval(hx - s/2, hy - s/2, s, s);

            g.setStroke(Color.color(1, 1, 0, h.alpha())); // yellow outline
            g.strokeOval(hx - s/2, hy - s/2, s, s);
        }


        // ship (center of screen)
        //drawShip(g, VIEW_W/2, VIEW_H/2, 24);

        // updated dhakka khabe tai
        if (shipVisible && !gameOver) {
            drawShip(g, VIEW_W/2, VIEW_H/2, 24);
        }


        // arrows
        drawPlanetArrows(g, viewLeft, viewTop);

        // coin collecyion popup msg system
        for (FloatingMessage msg : floatingMessages) {
            double sx = msg.x - camX + VIEW_W / 2;
            double sy = msg.y - camY + VIEW_H / 2;

            g.setFill(Color.GOLD);
            g.setFont(Font.font("Arial", FontWeight.BOLD, 20));
            g.setTextAlign(TextAlignment.CENTER);
            g.setTextBaseline(VPos.CENTER);
            g.fillText(msg.text, sx, sy - 40); // above the ship
        }

        // draw blasts
        for (BlastEffect bl : blasts) {
            double bx = bl.x - viewLeft;
            double by = bl.y - viewTop;

            g.setFill(Color.color(1, 0.3, 0, bl.alpha())); // orange-red
            double s = bl.size();
            g.fillOval(bx - s/2, by - s/2, s, s);
        }

        // if game over
        if (gameOver) {
            g.setFill(Color.RED);
            g.setFont(Font.font("Arial", FontWeight.BOLD, 48));
            g.setTextAlign(TextAlignment.CENTER);
            g.setTextBaseline(VPos.CENTER);
            g.fillText("GAME OVER", VIEW_W/2, VIEW_H/2);
        }


        // ---- Mission HUD ----
        // ---- Mission HUD ----
        g.setFill(Color.LIGHTGREEN);
        g.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        g.setTextAlign(TextAlignment.LEFT);
        g.setTextBaseline(VPos.TOP);

        double y = 20;

        if (missionActive) {
            String title = switch (currentMission) {
                case 1 -> "🚀 Mission 1: Deliver package to Mars";
                case 2 -> "🚀 Mission 2: Deliver package to Jupiter";
                case 3 -> "💥 Mission 3: Destroy the Blackhole";
                default -> "✅ All missions completed!";
            };


            // glo hbe
            g.setFont(Font.font("Orbitron", FontWeight.BOLD, 18));
            g.setFill(Color.CYAN);


            g.setStroke(Color.rgb(0, 255, 255, 0.6));
            g.setLineWidth(2);
            g.strokeText(title, 20, y);
            g.fillText(title, 20, y);

            y += 28;
        }


        if (mission1Completed) {
            String msg1 = "✔ Mission 1 Completed (+100 coins)";
            g.setFont(Font.font("Orbitron", FontWeight.BOLD, 16));


            g.setStroke(Color.rgb(0, 255, 100, 0.6));
            g.setLineWidth(2);
            g.strokeText(msg1, 20, y);
            g.setFill(Color.LIME);
            g.fillText(msg1, 20, y);

            y += 28;
        }

        if (mission2Completed) {
            String msg2 = "✔ Mission 2 Completed (+100 coins)";
            g.setFont(Font.font("Orbitron", FontWeight.BOLD, 16));


            g.setStroke(Color.rgb(180, 0, 255, 0.6));
            g.setLineWidth(2);
            g.strokeText(msg2, 20, y);
            g.setFill(Color.MAGENTA);
            g.fillText(msg2, 20, y);

            y += 28;
        }

        if (mission3Completed) {

            String msg2;

            if (IsBlackholeDestroyed) {
                msg2 = "✔ Mission 3 Completed (+1000 coins)";
            }else{
                msg2 = "Antimatter collected go destroy blackhole";
            }

            g.setFont(Font.font("Orbitron", FontWeight.BOLD, 16));


            g.setStroke(Color.rgb(255, 50, 50, 0.6));
            g.setLineWidth(2);
            g.strokeText(msg2, 20, y);
            g.setFill(Color.MAGENTA);
            g.fillText(msg2, 20, y);

            y += 28;
        }









        // ---- Boost HUD ----
        // ---- Boost HUD (Bar Style) ----
        double barWidth = 200;
        double barHeight = 20;
        double barX = 20;
        double barY = VIEW_H - 40;

// Background (dark gray frame)
        g.setFill(Color.color(0.2, 0.2, 0.2, 0.7));
        g.fillRoundRect(barX, barY, barWidth, barHeight, 10, 10);

// Border
        g.setStroke(Color.WHITE);
        g.setLineWidth(2);
        g.strokeRoundRect(barX, barY, barWidth, barHeight, 10, 10);

        if (boostActive) {
            // Fill based on time left
            double pct = boostTimeLeft / 30.0; // assuming 30s max boost
            g.setFill(Color.LIMEGREEN);
            g.fillRoundRect(barX, barY, barWidth * pct, barHeight, 10, 10);

            g.setFill(Color.WHITE);
            g.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            g.setTextAlign(TextAlignment.CENTER);
            g.setTextBaseline(VPos.CENTER);
            g.fillText("BOOST " + (int)boostTimeLeft + "s", barX + barWidth / 2, barY + barHeight / 2);

        } else if (boostCooldown > 0) {
            // Cooldown fill
            double pct = 1.0 - (boostCooldown / 5.0); // assuming 30s cooldown
            g.setFill(Color.ORANGE);
            g.fillRoundRect(barX, barY, barWidth * pct, barHeight, 10, 10);

            g.setFill(Color.WHITE);
            g.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            g.setTextAlign(TextAlignment.CENTER);
            g.setTextBaseline(VPos.CENTER);
            g.fillText("Refill " + (int)boostCooldown + "s", barX + barWidth / 2, barY + barHeight / 2);

        } else {
            // Ready state (full bar, glowing green)
            g.setFill(Color.LIMEGREEN);
            g.fillRoundRect(barX, barY, barWidth, barHeight, 10, 10);

            g.setFill(Color.BLACK);
            g.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            g.setTextAlign(TextAlignment.CENTER);
            g.setTextBaseline(VPos.CENTER);
            g.fillText("BOOST READY", barX + barWidth / 2, barY + barHeight / 2);
        }








    }



    // ---- Ship drawing ----
    private void drawShip(GraphicsContext g,double cx,double cy,double s){



        String key = shipDir + (moving ? "_moving" : "");
        Image img = shipImages.get(key);

        if (img != null) {
            double drawW = s * 2;
            double drawH = s * 2;
            g.drawImage(img, cx - drawW/2, cy - drawH/2, drawW, drawH);
        } else {
            g.setFill(Color.WHITE);
            double[] xs = {cx, cx - s, cx + s};
            double[] ys = {cy - s, cy + s, cy + s};
            g.fillPolygon(xs, ys, 3);
        }
    }

    // ---- Celestial drawings ----


    // bLack hole dorwing

    private void drawBlackhole(GraphicsContext g, double cx, double cy, double size) {
        if (blackholeImg != null) {
            g.drawImage(blackholeImg, cx - size/2, cy - size/2, size, size);
        } else {
            g.setFill(Color.BLACK);
            g.fillOval(cx - size/2, cy - size/2, size, size);
        }
    }

    private void drawWarmhole(GraphicsContext g, double cx, double cy, double size) {
        if (warmholeImg != null) {
            g.drawImage(warmholeImg, cx - size/2, cy - size/2, size, size);
        } else {
            g.setFill(Color.BLACK);
            g.fillOval(cx - size/2, cy - size/2, size, size);
        }
    }


    //draw war ship

    private void drawWarShip(GraphicsContext g, double cx, double cy, double size) {
        if (warshipImg != null) {
            g.drawImage(warshipImg, cx - size/2, cy - size/2, size, size);
        } else {
            g.setFill(Color.BLACK);
            g.fillOval(cx - size/2, cy - size/2, size, size);
        }
    }


    // draw iss
    private void drawISS(GraphicsContext g, double cx, double cy, double size) {
        if (issImg != null) {
            g.drawImage(issImg, cx - size/2, cy - size/2, size, size);
        } else {
            g.setFill(Color.BLACK);
            g.fillOval(cx - size/2, cy - size/2, size, size);
        }
    }

    private void drawuranus(GraphicsContext g, double cx, double cy, double size) {
        if (uranusImg != null) {
            g.drawImage(uranusImg, cx - size/2, cy - size/2, size, size);
        } else {
            g.setFill(Color.BLACK);
            g.fillOval(cx - size/2, cy - size/2, size, size);
        }
    }

    private void drawneptune(GraphicsContext g, double cx, double cy, double size) {
        if (neptuneImg != null) {
            g.drawImage(neptuneImg, cx - size/2, cy - size/2, size, size);
        } else {
            g.setFill(Color.BLACK);
            g.fillOval(cx - size/2, cy - size/2, size, size);
        }
    }




    private void drawAsteroid(GraphicsContext g, double cx, double cy, double r) {
        // Irregular rocky look
        g.setFill(Color.DARKGRAY);
        g.fillOval(cx - r, cy - r, 2 * r, 2 * r);

        g.setFill(Color.GRAY);
        Random rnd = new Random((long)(cx * cy)); // stable "noise"
        for (int i = 0; i < 6; i++) {
            double rx = (rnd.nextDouble() - 0.5) * r * 1.2;
            double ry = (rnd.nextDouble() - 0.5) * r * 1.2;
            double rr = r * (0.2 + rnd.nextDouble() * 0.3);
            g.fillOval(cx + rx - rr / 2, cy + ry - rr / 2, rr, rr);
        }
    }




    private void drawCoin(GraphicsContext g, double cx, double cy, double r) {
        RadialGradient grad = new RadialGradient(
                0, 0, cx, cy, r, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.GOLD),
                new Stop(1, Color.DARKGOLDENROD)
        );
        g.setFill(grad);
        g.fillOval(cx - r, cy - r, 2 * r, 2 * r);

        g.setStroke(Color.ORANGE);
        g.setLineWidth(2);
        g.strokeOval(cx - r * 0.8, cy - r * 0.8, 1.6 * r, 1.6 * r);

        g.setFill(Color.WHITE);
        g.setFont(Font.font(14));
        g.setTextAlign(TextAlignment.CENTER);
        g.setTextBaseline(VPos.CENTER);
        g.fillText("$", cx, cy);
    }

    // new earth

    private void drawEarth(GraphicsContext g, double cx, double cy, double size) {
        if (earthImg != null) {
            g.drawImage(earthImg, cx - size/2, cy - size/2, size, size);
        } else {
            g.setFill(Color.BLACK);
            g.fillOval(cx - size/2, cy - size/2, size, size);
        }
    }






    private void drawSun(GraphicsContext g,double cx,double cy,double r){
        RadialGradient grad=new RadialGradient(0,0,cx,cy,r,false,CycleMethod.NO_CYCLE,
                new Stop(0,Color.YELLOW), new Stop(1,Color.RED));
        g.setFill(grad);
        g.fillOval(cx-r,cy-r,2*r,2*r);
    }

    private void drawPlanet(GraphicsContext g,double cx,double cy,double r,Color c1,Color c2){
        RadialGradient grad=new RadialGradient(0,0,cx,cy,r,false,CycleMethod.NO_CYCLE,
                new Stop(0,c1), new Stop(1,c2));
        g.setFill(grad);
        g.fillOval(cx-r,cy-r,2*r,2*r);
    }

    // new jupiter

    private void drawJupiter(GraphicsContext g, double cx, double cy, double size) {
        if (jupiterImg != null) {
            g.drawImage(jupiterImg, cx - size/2, cy - size/2, size, size);
        } else {
            g.setFill(Color.BLACK);
            g.fillOval(cx - size/2, cy - size/2, size, size);
        }
    }

    private void drawSaturn(GraphicsContext g,double cx,double cy,double r){
        g.setFill(Color.BEIGE);
        g.fillOval(cx-r,cy-r,2*r,2*r);
        g.setStroke(Color.LIGHTGOLDENRODYELLOW);
        g.setLineWidth(4);
        g.strokeOval(cx-r*1.6, cy-r*0.6, 3.2*r, 1.2*r);
        g.setLineWidth(1);
    }

    private void drawPlanetArrows(GraphicsContext g, double viewLeft, double viewTop) {
        // Prottek body er moddhe iterate koro
        for(Body b : bodies){

            // Asteroid thakle skip koro (code == -1)
            if (b.code == -1) continue;

            // Planet er center (cx, cy) calculate koro view area er relative vabe
            double cx = b.cx - viewLeft, cy = b.cy - viewTop;

            // Jodi planet current view window er modhe thake, tahole skip koro
            if(cx >= 0 && cx <= VIEW_W && cy >= 0 && cy <= VIEW_H) continue;

            // View er center o planet er position er moddhe offset calculate koro
            double dx = cx - VIEW_W/2, dy = cy - VIEW_H/2;

            // atan2 diye angle calculate koro (angle radians e return hobe)
            double ang = Math.atan2(dy, dx);

            // Screen er edge e planet er dike ekta point ber koro
            double edgeX = VIEW_W/2 + Math.cos(ang) * VIEW_W/2 * 0.9;  // X position on edge
            double edgeY = VIEW_H/2 + Math.sin(ang) * VIEW_H/2 * 0.9;  // Y position on edge

            // sada rong er
            g.setFill(Color.WHITE);

            // Arrow er size
            double s = 10;

            // Polygon er 4 point er coordinates
            double[] xs = {edgeX, edgeX - s, edgeX - s, edgeX};
            double[] ys = {edgeY, edgeY - s, edgeY + s, edgeY};

            // Screen e arrow draw koro
            g.fillPolygon(xs, ys, 4);

            // Planet ba object er name determine koro body code onujayi
            String name = switch (b.code) {
                case 1 -> "Sun";
                case 2 -> "Mercury";
                case 3 -> "Venus";
                case 4 -> "Earth";
                case 5 -> "Mars";
                case 6 -> "Jupiter";
                case 7 -> "Saturn";
                case 8 -> "Uranus";
                case 9 -> "Neptune";
                case 100 -> "ISS";
                case 88 -> "Warship";
                case 69 -> "Wormhole";
                case -99 -> "BlackHole";
                default -> "Unknown";
            };

            // Font size o alignment set koro name text er jonno
            g.setFont(Font.font(12));
            g.setTextAlign(TextAlignment.CENTER);
            g.setTextBaseline(VPos.TOP);

            // Object er name show korchi calculted edge position e
            g.fillText(name, edgeX, edgeY + 4);
        }
    }


    // ---- Helpers (updated) ----

    // Returns true if segment P1(x1,y1)->P2(x2,y2) intersects circle C(cx,cy,r)
    private boolean segmentCircleHit(double x1, double y1, double x2, double y2,
                                     double cx, double cy, double r) {
        double dx = x2 - x1, dy = y2 - y1;
        double fx = x1 - cx, fy = y1 - cy;

        // project center onto segment
        double len2 = dx*dx + dy*dy;
        double t = (len2 == 0) ? 0 : -(fx*dx + fy*dy) / len2;
        if (t < 0) t = 0;
        else if (t > 1) t = 1;

        double qx = x1 + t*dx, qy = y1 + t*dy; // closest point on segment
        double dist2 = (qx - cx)*(qx - cx) + (qy - cy)*(qy - cy);
        return dist2 <= r*r;
    }


    // coin update hbe txt file e
    private void updateUserCoinsInFile(String username, int newCoins) {
        File file = new File("users.txt");
        List<String> lines = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length >= 6 && parts[0].equals(username)) {
                    // replace coins (index 2)
                    parts[2] = String.valueOf(newCoins);
                    line = String.join(":", parts);
                }
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            for (String l : lines) {
                bw.write(l);
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // agertai shudhu coin update hossilo ekhn level o update hbe !
    private void updateUserStatsInFile(String username, int coins, int level) {
        File file = new File("users.txt");
        List<String> lines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length >= 6 && parts[0].equals(username)) {
                    // Update coins and level
                    parts[2] = String.valueOf(coins);
                    parts[3] = String.valueOf(level);
                    line = String.join(":", parts);
                }
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String updatedLine : lines) {
                writer.write(updatedLine);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void startMissionWindow(int missionId) {

        paused = true;

        missionActive = true;
        currentMission = missionId;

        javafx.stage.Stage missionStage = new javafx.stage.Stage();
        missionStage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
        missionStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        if (gameCanvas.getScene() != null) missionStage.initOwner(gameCanvas.getScene().getWindow());

        // Root panel with sci-fi glowing border
        javafx.scene.layout.BorderPane root = new javafx.scene.layout.BorderPane();
        root.setStyle(
                "-fx-background-color: rgba(20,20,30,0.92);" +
                        "-fx-padding: 20;" +
                        "-fx-border-color: cyan;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 12;" +
                        "-fx-background-radius: 12;" +
                        "-fx-effect: dropshadow(gaussian, cyan, 25, 0.9, 0, 0);"
        );

        // Close button (top-right)
        javafx.scene.layout.HBox top = new javafx.scene.layout.HBox();
        top.setAlignment(javafx.geometry.Pos.TOP_RIGHT);
        top.setPadding(new javafx.geometry.Insets(6, 6, 0, 0));

        javafx.scene.control.Button closeBtn = new javafx.scene.control.Button("✕");
        closeBtn.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-pref-width: 30px; -fx-pref-height: 30px; " +
                        "-fx-background-radius: 15px; -fx-border-radius: 15px;"
        );
        closeBtn.setCursor(javafx.scene.Cursor.HAND);
        closeBtn.setOnMouseEntered(ev -> closeBtn.setStyle(
                "-fx-background-color: rgba(255,255,255,0.08);" +
                        "-fx-text-fill: cyan; -fx-effect: dropshadow(gaussian, cyan, 12, 0.6, 0, 0);" +
                        "-fx-font-size: 14px; -fx-font-weight: bold; -fx-pref-width: 30px; -fx-pref-height: 30px;"
        ));
        closeBtn.setOnMouseExited(ev -> closeBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-pref-width: 30px; -fx-pref-height: 30px;"
        ));
        closeBtn.setOnAction(ev -> missionStage.close());
        top.getChildren().add(closeBtn);
        root.setTop(top);

        // Mission content
        String msg = switch (missionId) {
            case 1 -> "🛰 The UIU Mars Rover is down with a mechanical glitch — your mission: deliver the package to the Mars checkpoint!";
            case 2 -> "🚀 Deliver the artifact to Jupiter! Beware — the Asteroid Belt lies between Mars and Jupiter. Dodge or risk mission failure!";
            case 3 -> "💥 A supermassive Blackhole threatens the galaxy — your mission: destroy the Blackhole before it consumes everything! you can not destroy a blak hole with normal bullet go to warship to take bullet made with antimatter !";
            default -> "✅ All missions completed! Await further commands.";
        };

        javafx.scene.text.Text title = new javafx.scene.text.Text("MISSION BRIEFING");
        title.setFill(javafx.scene.paint.Color.CYAN);
        title.setStyle("-fx-font-size: 22px; -fx-font-family: 'Orbitron'; -fx-font-weight: bold;");

        javafx.scene.text.Text missionText = new javafx.scene.text.Text(msg);
        missionText.setFill(javafx.scene.paint.Color.LIGHTGRAY);
        missionText.setStyle("-fx-font-size: 14px; -fx-font-family: 'Consolas';");
        missionText.setWrappingWidth(300);

        javafx.scene.control.Button okBtn = new javafx.scene.control.Button("ACCEPT MISSION");
        okBtn.setStyle(
                "-fx-background-color: black;" +
                        "-fx-text-fill: cyan;" +
                        "-fx-font-family: 'Orbitron';" +
                        "-fx-font-size: 16px;" +
                        "-fx-border-color: cyan;" +
                        "-fx-border-width: 2;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-radius: 8;" +
                        "-fx-effect: dropshadow(gaussian, cyan, 20, 0.8, 0, 0);"
        );
        okBtn.setOnAction(e -> missionStage.close());

        javafx.scene.layout.VBox layout = new javafx.scene.layout.VBox(15, title, missionText, okBtn);
        layout.setAlignment(javafx.geometry.Pos.CENTER);
        root.setCenter(layout);

        javafx.scene.Scene scene = new javafx.scene.Scene(root, 380, 280);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        scene.setOnKeyPressed(k -> {
            if (k.getCode() == javafx.scene.input.KeyCode.ESCAPE) missionStage.close();
        });

        missionStage.setOnHidden(ev -> paused = false);

        missionStage.setScene(scene);
        missionStage.showAndWait();
    }




    private void completeMissionWindow(int missionId) {

        paused = true;

        missionActive = false;

        // reward
        UserData.coins += 100;
        UserData.level += 1;
        updateUserCoinsInFile(UserData.username, UserData.coins);
        updateUserStatsInFile(UserData.username, UserData.coins, UserData.level);

        if (missionId == 1) {
            mission1Completed = true;
        } else if (missionId == 2) {
            mission2Completed = true;
        } else if (missionId==3) {
            mission3Completed = true;

        }

        floatingMessages.add(new FloatingMessage("Mission Completed! +100 coins", camX, camY, 3.0));

        javafx.stage.Stage missionStage = new javafx.stage.Stage();
        missionStage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
        missionStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        if (gameCanvas.getScene() != null) missionStage.initOwner(gameCanvas.getScene().getWindow());

        // Sci-fi glowing root panel
        javafx.scene.layout.BorderPane root = new javafx.scene.layout.BorderPane();
        root.setStyle(
                "-fx-background-color: rgba(15,15,25,0.92);" +
                        "-fx-padding: 20;" +
                        "-fx-border-color: lime;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 12;" +
                        "-fx-background-radius: 12;" +
                        "-fx-effect: dropshadow(gaussian, lime, 25, 0.9, 0, 0);"
        );

        // Close button (top-right)
        javafx.scene.layout.HBox top = new javafx.scene.layout.HBox();
        top.setAlignment(javafx.geometry.Pos.TOP_RIGHT);
        top.setPadding(new javafx.geometry.Insets(6, 6, 0, 0));

        javafx.scene.control.Button closeBtn = new javafx.scene.control.Button("✕");
        closeBtn.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-pref-width: 30px; -fx-pref-height: 30px; " +
                        "-fx-background-radius: 15px; -fx-border-radius: 15px;"
        );
        closeBtn.setCursor(javafx.scene.Cursor.HAND);
        closeBtn.setOnMouseEntered(ev -> closeBtn.setStyle(
                "-fx-background-color: rgba(255,255,255,0.08);" +
                        "-fx-text-fill: lime; -fx-effect: dropshadow(gaussian, lime, 12, 0.6, 0, 0);" +
                        "-fx-font-size: 14px; -fx-font-weight: bold; -fx-pref-width: 30px; -fx-pref-height: 30px;"
        ));
        closeBtn.setOnMouseExited(ev -> closeBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-pref-width: 30px; -fx-pref-height: 30px;"
        ));
        closeBtn.setOnAction(ev -> missionStage.close());
        top.getChildren().add(closeBtn);
        root.setTop(top);

        // Mission completion text
        String msg = switch (missionId) {
            case 1 -> "✅ You delivered the package to Mars!\n📡 Next: Head over to Venus — they have a task for you!";
            case 2 -> "✅ You delivered the package to Jupiter!\n🚨 Next: Head over to the ISS — urgent call awaits!";
            case 3 -> "💥 antimatter bullet collected\n🌌 Go hero destroy the black hole and save solar system but beaware dont go to near or it will devour you !";
            default -> "🏆 All missions completed! Await further adventures...";
        };

        javafx.scene.text.Text title = new javafx.scene.text.Text("MISSION Status");
        title.setFill(javafx.scene.paint.Color.LIME);
        title.setStyle("-fx-font-size: 22px; -fx-font-family: 'Orbitron'; -fx-font-weight: bold;");

        javafx.scene.text.Text missionText = new javafx.scene.text.Text(msg);
        missionText.setFill(javafx.scene.paint.Color.LIGHTGRAY);
        missionText.setStyle("-fx-font-size: 14px; -fx-font-family: 'Consolas';");
        missionText.setWrappingWidth(300);

        javafx.scene.control.Button okBtn = new javafx.scene.control.Button("CONTINUE");
        okBtn.setStyle(
                "-fx-background-color: black;" +
                        "-fx-text-fill: lime;" +
                        "-fx-font-family: 'Orbitron';" +
                        "-fx-font-size: 16px;" +
                        "-fx-border-color: lime;" +
                        "-fx-border-width: 2;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-radius: 8;" +
                        "-fx-effect: dropshadow(gaussian, lime, 20, 0.8, 0, 0);"
        );
        okBtn.setOnAction(e -> missionStage.close());

        javafx.scene.layout.VBox layout = new javafx.scene.layout.VBox(15, title, missionText, okBtn);
        layout.setAlignment(javafx.geometry.Pos.CENTER);
        root.setCenter(layout);

        javafx.scene.Scene scene = new javafx.scene.Scene(root, 380, 220);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        scene.setOnKeyPressed(k -> {
            if (k.getCode() == javafx.scene.input.KeyCode.ESCAPE) missionStage.close();
        });

        missionStage.setOnHidden(ev -> paused = false);

        missionStage.setScene(scene);
        missionStage.showAndWait();
    }







    // (Optional) keep backward-compatible wrappers if other code still calls them:
    private void startMissionWindow() { startMissionWindow(1); }
    private void completeMissionWindow() { completeMissionWindow(1); }


    public boolean CanDestroyBlackhole = false;

    private void showPlanetInfo(Body b) {

        paused = true;

        if (b.isshown == 0) {
            String name = switch (b.code) {
                case 1 -> "Sun";
                case 2 -> "Mercury";
                case 3 -> "Venus";
                case 4 -> "Earth";
                case 5 -> "Mars";
                case 6 -> "Jupiter";
                case 7 -> "Saturn";
                case 8 -> "Uranus";
                case 9 -> "Neptune";
                case 100 -> "ISS";
                case 88 -> "Warship";
                case 69 -> "Wormhole";
                case -99 -> "BlackHole";
                default -> "Unknown";
            };

            String bodyInfo = planetInfo.getOrDefault(b.code, "Unknown planet.");

            javafx.stage.Stage dialog = new javafx.stage.Stage();
            dialog.initStyle(javafx.stage.StageStyle.TRANSPARENT);
            if (gameCanvas.getScene() != null) dialog.initOwner(gameCanvas.getScene().getWindow());

            // ==== Root background with sci-fi glow ====
            javafx.scene.layout.BorderPane root = new javafx.scene.layout.BorderPane();
            root.setStyle(
                    "-fx-background-color: rgba(20,0,30,0.9);" +
                            "-fx-padding: 18;" +
                            "-fx-border-color: cyan;" +
                            "-fx-border-width: 2;" +
                            "-fx-border-radius: 12;" +
                            "-fx-background-radius: 12;" +
                            "-fx-effect: dropshadow(gaussian, cyan, 20, 0.8, 0, 0);"
            );

            // ==== Close button top-right ====
            javafx.scene.layout.HBox top = new javafx.scene.layout.HBox();
            top.setAlignment(javafx.geometry.Pos.TOP_RIGHT);
            top.setPadding(new javafx.geometry.Insets(4, 4, 0, 0));

            javafx.scene.control.Button closeBtn = new javafx.scene.control.Button("✕");
            closeBtn.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: white;" +
                            "-fx-font-size: 14px; -fx-font-weight: bold;" +
                            "-fx-pref-width: 30px; -fx-pref-height: 30px;" +
                            "-fx-background-radius: 15px; -fx-border-radius: 15px;"
            );
            closeBtn.setOnMouseEntered(ev -> closeBtn.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.06);" +
                            "-fx-text-fill: white;" +
                            "-fx-effect: dropshadow(gaussian, cyan, 10, 0.6, 0, 0);" +
                            "-fx-font-size: 14px; -fx-font-weight: bold;" +
                            "-fx-pref-width: 30px; -fx-pref-height: 30px;"
            ));
            closeBtn.setOnMouseExited(ev -> closeBtn.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: white;" +
                            "-fx-font-size: 14px; -fx-font-weight: bold;" +
                            "-fx-pref-width: 30px; -fx-pref-height: 30px;"
            ));
            closeBtn.setOnAction(ev -> dialog.close());
            top.getChildren().add(closeBtn);
            root.setTop(top);

            // ==== Center content ====
            javafx.scene.layout.VBox box = new javafx.scene.layout.VBox(14);
            box.setAlignment(javafx.geometry.Pos.CENTER);

            javafx.scene.text.Text title = new javafx.scene.text.Text(name);
            title.setFill(javafx.scene.paint.Color.CYAN);
            title.setStyle("-fx-font-size: 22px; -fx-font-family: 'Orbitron'; -fx-font-weight: bold;");

            javafx.scene.text.Text info = new javafx.scene.text.Text(bodyInfo);
            info.setFill(javafx.scene.paint.Color.LIGHTGRAY);
            info.setStyle("-fx-font-size: 14px; -fx-font-family: 'Consolas';");
            info.setWrappingWidth(300);

            // ==== Neon button style ====
            String neonBtnStyle =
                    "-fx-background-color: black;" +
                            "-fx-text-fill: cyan;" +
                            "-fx-font-family: 'Orbitron';" +
                            "-fx-font-size: 14px;" +
                            "-fx-border-color: cyan;" +
                            "-fx-border-width: 2;" +
                            "-fx-background-radius: 8;" +
                            "-fx-border-radius: 8;" +
                            "-fx-effect: dropshadow(gaussian, cyan, 20, 0.8, 0, 0);" +
                            "-fx-cursor: hand;";

            // ==== Mission buttons ====
            if (b.code == 4 && !missionActive && !mission1Completed) {
                javafx.scene.control.Button missionBtn = new javafx.scene.control.Button("🚀 Start Mission 1");
                missionBtn.setStyle(neonBtnStyle);
                missionBtn.setOnAction(e -> {
                    dialog.close();
                    startMissionWindow(1);
                });
                box.getChildren().add(missionBtn);
            } else if (b.code == 5 && missionActive && currentMission == 1 && !mission1Completed) {
                javafx.scene.control.Button deliverBtn = new javafx.scene.control.Button("📦 Deliver to Mars");
                deliverBtn.setStyle(neonBtnStyle);
                deliverBtn.setOnAction(e -> {
                    dialog.close();
                    completeMissionWindow(1);
                });
                box.getChildren().add(deliverBtn);
            }

            if (mission1Completed) {
                if (b.code == 3 && !missionActive && !mission2Completed) {
                    javafx.scene.control.Button missionBtn2 = new javafx.scene.control.Button("🚀 Start Mission 2");
                    missionBtn2.setStyle(neonBtnStyle);
                    missionBtn2.setOnAction(e -> {
                        dialog.close();
                        startMissionWindow(2);
                    });
                    box.getChildren().add(missionBtn2);
                } else if (b.code == 6 && missionActive && currentMission == 2 && !mission2Completed) {
                    javafx.scene.control.Button deliverBtn2 = new javafx.scene.control.Button("📦 Deliver to Jupiter");
                    deliverBtn2.setStyle(neonBtnStyle);
                    deliverBtn2.setOnAction(e -> {
                        dialog.close();
                        completeMissionWindow(2);
                    });
                    box.getChildren().add(deliverBtn2);
                }
            }


            if (mission2Completed) {
                if (b.code == 100 && !missionActive && !mission3Completed) {
                    javafx.scene.control.Button missionBtn3 = new javafx.scene.control.Button("🚀 Start Mission 3");
                    missionBtn3.setStyle(neonBtnStyle);
                    missionBtn3.setOnAction(e -> {
                        dialog.close();
                        startMissionWindow(3);
                    });
                    box.getChildren().add(missionBtn3);
                } else if (b.code == 88 && missionActive && currentMission == 3 && !mission3Completed) {
                    javafx.scene.control.Button collectAntimatterBtn = new javafx.scene.control.Button("📦 Take antimatter Bullet");
                    collectAntimatterBtn.setStyle(neonBtnStyle);
                    collectAntimatterBtn.setOnAction(e -> {

                        CanDestroyBlackhole = true;

                        dialog.close();
                        completeMissionWindow(3);
                    });
                    box.getChildren().add(collectAntimatterBtn);
                }
            }


            // ==== Wormhole extra buttons ====
            if (b.code == 69) {
                javafx.scene.control.Button btn1 = new javafx.scene.control.Button("🌌 Teleport to Nebula");
                btn1.setStyle(neonBtnStyle);
               /* btn1.setOnAction(e -> {
                    Main.switchScene("level1.fxml"); // then change scene
                }); */

                // problem face was afte switching scene that window has no parent so can not close , now alwys clos when button clikcled

                btn1.setOnAction(e -> {
                    dialog.close();   // close the wormhole window
                    Main.switchScene("level1.fxml"); // then change scene
                });



                javafx.scene.control.Button btn2 = new javafx.scene.control.Button("🛰 Teleport to Voyager");
                btn2.setStyle(neonBtnStyle);

                javafx.scene.control.Button btn3 = new javafx.scene.control.Button("⭐ Teleport to Pulsar");
                btn3.setStyle(neonBtnStyle);

                javafx.scene.control.Button btn4 = new javafx.scene.control.Button("🌠 Teleport to Andromeda");
                btn4.setStyle(neonBtnStyle);

                box.getChildren().addAll(btn1, btn2, btn3, btn4);
            }

            box.getChildren().addAll(title, info);
            root.setCenter(box);

            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);

            dialog.setOnHidden(ev -> paused = false);
            dialog.setScene(scene);
            dialog.show();


            b.isshown = 1;
        }
    }



    // restrt hbe game over er pore
    private void restartGame() {
        gameOver = false;
        blasts.clear();
        bullets.clear();
        hitEffects.clear();
        floatingMessages.clear();
        bodies.clear();

        try {
            loadMapOrThrow();
            computeBodiesFromMap();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // reset ship to Earth
        for (Body b : bodies) {
            if (b.code == 4) {
                camX = b.cx;
                camY = b.cy;
                break;
            }
        }
        vx = vy = 0;

        shipVisible = true; // game over hole dekha jabe na ship tai  eta true korlam


    }


    // ---- Firing helpers (added) ----
    private void fireOnce() {
        // ship is at world (camX, camY)
        double dx = 0, dy = -1; // default up
        switch (shipDir) {
            case "down" -> { dx = 0; dy = 1; }
            case "left" -> { dx = -1; dy = 0; }
            case "right" -> { dx = 1; dy = 0; }
            default -> { dx = 0; dy = -1; }
        }
        bullets.add(new Bullet(camX, camY, dx, dy));
        playShootSound();
    }

    private double clamp(double v,double lo,double hi){return Math.max(lo,Math.min(hi,v));}


    @FXML
    private Canvas gameCanvas;

    @FXML
    private void initialize() {
        GraphicsContext g = gameCanvas.getGraphicsContext2D();

        try {
            loadMapOrThrow();
            computeBodiesFromMap();
            generateStars();

            // 🌍 Center camera on Earth instead of Sun
            for (Body b : bodies) {
                if (b.code == 4) {
                    camX = b.cx;
                    camY = b.cy;
                    break;
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        // load space ship image !
        shipImages.put("up", new Image(Objects.requireNonNull(
                getClass().getResource("/com/example/demo/up-spaceship.png")).toExternalForm()));
        shipImages.put("up_moving", new Image(Objects.requireNonNull(
                getClass().getResource("/com/example/demo/up_moving_spaceship.png")).toExternalForm()));
        shipImages.put("down", new Image(Objects.requireNonNull(
                getClass().getResource("/com/example/demo/down-spaceship.png")).toExternalForm()));
        shipImages.put("down_moving", new Image(Objects.requireNonNull(
                getClass().getResource("/com/example/demo/down_moving_spaceship.png")).toExternalForm()));
        shipImages.put("left", new Image(Objects.requireNonNull(
                getClass().getResource("/com/example/demo/left-spaceship.png")).toExternalForm()));
        shipImages.put("left_moving", new Image(Objects.requireNonNull(
                getClass().getResource("/com/example/demo/left_moving_spaceship.png")).toExternalForm()));
        shipImages.put("right", new Image(Objects.requireNonNull(
                getClass().getResource("/com/example/demo/right-spaceship.png")).toExternalForm()));
        shipImages.put("right_moving", new Image(Objects.requireNonNull(
                getClass().getResource("/com/example/demo/right_moving_spaceship.png")).toExternalForm()));



        // ekhane exit control kora hocche !
        gameCanvas.setFocusTraversable(true);
        gameCanvas.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                // make sure the canvas gets keyboard focus
                Platform.runLater(() -> gameCanvas.requestFocus());

                newScene.setOnKeyPressed(e -> {
                    keys.add(e.getCode());

                    if (e.getCode() == KeyCode.SPACE) {
                        fireOnce();
                    } else if (e.getCode() == KeyCode.ESCAPE) {
                        Alert alert = new Alert(
                                Alert.AlertType.CONFIRMATION,
                                "Are you sure you want to exit?",
                                ButtonType.YES, ButtonType.NO
                        );
                        alert.setHeaderText(null);
                        alert.setTitle("Exit Game");

                        alert.showAndWait().ifPresent(result -> {
                            if (result == ButtonType.YES) {
                                try {
                                    // go back to home.fxml
                                    Main.switchScene("home.fxml");
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    Platform.exit(); // fallback
                                }
                            }
                        });
                    }
                });

                newScene.setOnKeyReleased(e -> keys.remove(e.getCode()));
            }
        });
        // ekhane exit controller sesh !



        // ekhane game loop shuru hocche !
        final long[] last = {System.nanoTime()};
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                double dt = (now - last[0]) / 1_000_000_000.0;
                last[0] = now;
                update(dt);
                render(g);
            }
        }.start();
        //game loop
    }

    private void playShootSound() {
        try {
            String soundPath = getClass().getResource("/com/example/demo/shoot.mp3").toExternalForm();
            Media sound = new Media(soundPath);
            MediaPlayer mediaPlayer = new MediaPlayer(sound);
            mediaPlayer.setVolume(0.05); // adjust volume if needed
            mediaPlayer.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }







}