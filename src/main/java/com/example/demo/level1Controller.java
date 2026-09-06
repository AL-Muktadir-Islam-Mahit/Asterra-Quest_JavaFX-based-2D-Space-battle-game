package com.example.demo;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.media.AudioClip;


import java.io.BufferedReader;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

public class level1Controller implements Initializable {


    private boolean paused = false;


    @FXML
    private Canvas gameCanvas;

    private GraphicsContext gc;

    // tiles
    private final int TILE_SIZE = 50;
    private String[][] mapGrid = new String[0][];

    // audio clip ( sht, blst )
    private SoundManager shootSound;
    private SoundManager blastSound;

    // mission tracking
    private String missionText = "Mission : Collect antimatter from nebula";
    private boolean atNebula = false;
    private boolean atWormhole = false;






    // spaceship images
    private Image upIdle, downIdle, leftIdle, rightIdle;
    private Image upMoving, downMoving, leftMoving, rightMoving;

    // player state
    private Image playerImage;
    private String playerDirection = "UP";  // default facing
    private boolean isMoving = false;

    // objects
    private Image wormholeImage, pulsarImage, alien1Image, alien2Image;

    // player
    private double playerX;
    private double playerY;
    private final double playerSpeed = 4.0;
    private final double playerRenderW = 40;
    private final double playerRenderH = 40;
    private int playerHP = 70;

    // input
    private final Set<KeyCode> keys = new HashSet<>();

    // bullets
    private final List<Bullet> bullets = new ArrayList<>();
    private final List<Bullet> enemyBullets = new ArrayList<>();
    private long lastShootTime = 0L;
    private final long SHOOT_COOLDOWN_NS = 200_000_000L;

    // stars background
    private final List<Star> stars = new ArrayList<>();
    private final int STAR_COUNT = 21500;

    // camera
    private double cameraX = 0;
    private double cameraY = 0;

    // enemies
    private final List<Enemy> enemies = new ArrayList<>();

    // for blastt
    private final List<Explosion> explosions = new ArrayList<>();

    private boolean gameOver = false;
    private long gameOverTime = 0L;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gc = gameCanvas.getGraphicsContext2D();

        // load spaceship images
        upIdle = loadImageTry("/com/example/demo/up-spaceship.png");
        downIdle = loadImageTry("/com/example/demo/down-spaceship.png");
        leftIdle = loadImageTry("/com/example/demo/left-spaceship.png");
        rightIdle = loadImageTry("/com/example/demo/right-spaceship.png");

        upMoving = loadImageTry("/com/example/demo/up_moving_spaceship.png");
        downMoving = loadImageTry("/com/example/demo/down_moving_spaceship.png");
        leftMoving = loadImageTry("/com/example/demo/left_moving_spaceship.png");
        rightMoving = loadImageTry("/com/example/demo/right_moving_spaceship.png");

        // set initial player image
        playerImage = upIdle;

        // load objects
        wormholeImage = loadImageTry("/com/example/demo/warmhole.gif", "/com/example/demo/wormhole.gif");
        pulsarImage = loadImageTry("/com/example/demo/nebula.gif");
        alien1Image = loadImageTry("/com/example/demo/alienship1.png");
        alien2Image = loadImageTry("/com/example/demo/alienship2.png");

        // load map
        loadMapFromResource("level1.txt");

        // place player at wormhole
        respawnPlayer();

        // build enemies from map
        for (int row = 0; row < mapGrid.length; row++) {
            for (int col = 0; col < mapGrid[row].length; col++) {
                String token = mapGrid[row][col];
                if ("-1".equals(token)) {
                    enemies.add(new Enemy(col * TILE_SIZE, row * TILE_SIZE, 1, 1));
                } else if ("-2".equals(token)) {
                    enemies.add(new Enemy(col * TILE_SIZE, row * TILE_SIZE, 2, 2));
                }
            }
        }

        // stars background
        Random rand = new Random();
        int rows = mapGrid.length;
        int cols = (rows > 0 ? mapGrid[0].length : 16);
        double mapW = cols * TILE_SIZE;
        double mapH = rows * TILE_SIZE;
        double margin = Math.max(2000, Math.max(mapW, mapH));
        Color[] starColors = {
                Color.WHITE, Color.LIGHTBLUE, Color.LIGHTYELLOW,
                Color.ORANGE, Color.SALMON
        };
        for (int i = 0; i < STAR_COUNT; i++) {
            double x = rand.nextDouble() * (mapW + margin) - margin / 2.0;
            double y = rand.nextDouble() * (mapH + margin) - margin / 2.0;
            double size = rand.nextDouble() * 2.5 + 0.5;
            double brightness = 0.4 + rand.nextDouble() * 0.6;
            double depth = 0.3 + rand.nextDouble() * 0.7;
            Color color = starColors[rand.nextInt(starColors.length)];
            stars.add(new Star(x, y, size, brightness, depth, color));
        }

        // keyboard input
        gameCanvas.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyPressed(e -> keys.add(e.getCode()));
                newScene.setOnKeyReleased(e -> keys.remove(e.getCode()));
            }
        });

        // sound volum control
        SoundManager.setMasterVolume(0.1);

        shootSound = new SoundManager("/com/example/demo/shoot.mp3");
        blastSound = new SoundManager("/com/example/demo/blast.mp3");



        // game loop
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                update(now);
                render();
            }
        }.start();
    }

    private void respawnPlayer() {
        for (int row = 0; row < mapGrid.length; row++) {
            for (int col = 0; col < mapGrid[row].length; col++) {
                if ("R".equalsIgnoreCase(mapGrid[row][col])) {
                    playerX = col * TILE_SIZE;
                    playerY = row * TILE_SIZE + TILE_SIZE * 2;
                    playerHP = 70;
                    return;
                }
            }
        }
        playerX = 100;
        playerY = 100;
        playerHP = 70;
    }

    private Image loadImageTry(String... paths) {
        for (String p : paths) {
            try (InputStream is = getClass().getResourceAsStream(p)) {
                if (is != null) return new Image(is);
            } catch (Exception ignored) {}
        }
        return null;
    }

    private void loadMapFromResource(String filePath) {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new java.io.FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.trim().split("\\s+");
                rows.add(parts);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mapGrid = rows.toArray(new String[0][]);
    }

    private void update(long now) {

        if (paused) return;


        if (gameOver) {
            gc.setFont(javafx.scene.text.Font.font(48));
            if (now - gameOverTime > 2_000_000_000L) { // 2 sec
                gameOver = false;
                respawnPlayer();
            }
            return;
        }

        isMoving = false;

        if (keys.contains(KeyCode.W)) {
            playerY -= playerSpeed;
            playerDirection = "UP";
            isMoving = true;
        }
        if (keys.contains(KeyCode.S)) {
            playerY += playerSpeed;
            playerDirection = "DOWN";
            isMoving = true;
        }
        if (keys.contains(KeyCode.A)) {
            playerX -= playerSpeed;
            playerDirection = "LEFT";
            isMoving = true;
        }
        if (keys.contains(KeyCode.D)) {
            playerX += playerSpeed;
            playerDirection = "RIGHT";
            isMoving = true;
        }

        // 🚀 Check collision with Nebula (token "1")
        for (int row = 0; row < mapGrid.length; row++) {
            for (int col = 0; col < mapGrid[row].length; col++) {
                String token = mapGrid[row][col];
                double objX = col * TILE_SIZE;
                double objY = row * TILE_SIZE;

                if ("1".equals(token)) { // nebula
                    if (playerX < objX + 200 && playerX + playerRenderW > objX &&
                            playerY < objY + 200 && playerY + playerRenderH > objY) {
                        if (!atNebula) {
                            atNebula = true;
                            showNebulaWindow();
                        }
                    }
                }

                if ("R".equalsIgnoreCase(token)) { // wormhole
                    boolean inWormholeNow =
                            playerX < objX + 200 && playerX + playerRenderW > objX &&
                                    playerY < objY + 200 && playerY + playerRenderH > objY;

                    if (inWormholeNow && !atWormhole) {
                        atWormhole = true;
                        showWormholeWindow();
                    }
                    if (!inWormholeNow) {
                        atWormhole = false;
                    }
                }

            }
        }


        // choose spaceship image
        switch (playerDirection) {
            case "UP":
                playerImage = isMoving ? upMoving : upIdle;
                break;
            case "DOWN":
                playerImage = isMoving ? downMoving : downIdle;
                break;
            case "LEFT":
                playerImage = isMoving ? leftMoving : leftIdle;
                break;
            case "RIGHT":
                playerImage = isMoving ? rightMoving : rightIdle;
                break;
        }

        if (keys.contains(KeyCode.SPACE)) {
            if (now - lastShootTime >= SHOOT_COOLDOWN_NS) {
                spawnBullet();
                lastShootTime = now;
            }
        }

        // player bullets

        Iterator<Bullet> it = bullets.iterator();
        while (it.hasNext()) {
            Bullet b = it.next();
            b.update();
            if (b.isOffscreen()) {
                it.remove();
            } else {
                // collision with enemies
                for (Enemy e : enemies) {
                    if (e.isAlive() && b.collides(e.x, e.y, TILE_SIZE, TILE_SIZE)) {
                        e.hp--;
                        it.remove();

                        // 🔥 NEW: if enemy destroyed, trigger blast
                        if (e.hp <= 0) {
                            double centerX = e.x + TILE_SIZE / 2.0;
                            double centerY = e.y + TILE_SIZE / 2.0;

                            // add particle explosions
                            for (int i = 0; i < 1; i++) { // tweak count if you want more particles
                                explosions.add(new Explosion(centerX, centerY));
                            }
                            // optional shockwave ring
                            //explosions.add(new Shockwave(centerX, centerY));

                            // play blast sound
                            if (blastSound != null) blastSound.play();
                        }
                        break;
                    }
                }
            }
        }


        // enemy bullets
        Iterator<Bullet> eit = enemyBullets.iterator();
        while (eit.hasNext()) {
            Bullet b = eit.next();
            b.update();
            if (b.isOffscreen()) eit.remove();
            else if (b.collides(playerX, playerY, playerRenderW, playerRenderH)) {
                playerHP--;
                eit.remove();
                if (playerHP <= 0) {
                    gameOver = true;
                    gameOverTime = now;

                    // trigger explosion at player's position
                    double centerX = playerX + playerRenderW / 2;
                    double centerY = playerY + playerRenderH / 2;
                    for (int i = 0; i < 150; i++) {  // massive explosion (was 30)
                        explosions.add(new Explosion(centerX, centerY));
                    }

                    explosions.add(new Shockwave(centerX, centerY));

                    // blast sounf
                    if (blastSound != null) blastSound.play();

                }
            }

        }

        // 🔥 Check collision between player and enemies
        for (Enemy e : enemies) {
            if (!e.isAlive()) continue;

            double ex = e.x;
            double ey = e.y;
            double ew = TILE_SIZE;
            double eh = TILE_SIZE;

            double px = playerX;
            double py = playerY;
            double pw = playerRenderW;
            double ph = playerRenderH;

            if (px < ex + ew && px + pw > ex &&
                    py < ey + eh && py + ph > ey) {

                // making both dead
                e.hp = 0;
                playerHP = 0;
                gameOver = true;
                gameOverTime = now;

                // center for explosion
                double centerX = px + pw / 2;
                double centerY = py + ph / 2;


                for (int i = 0; i < 150; i++) {
                    explosions.add(new Explosion(centerX, centerY));
                }

                // shockwave effect
                explosions.add(new Shockwave(centerX, centerY));

                // play blast sound
                if (blastSound != null) blastSound.play();
            }
        }


        // update explosions
        Iterator<Explosion> expIt = explosions.iterator();
        while (expIt.hasNext()) {
            Explosion ex = expIt.next();
            ex.update();
            if (ex.life <= 0) expIt.remove();
        }



        // shotinh while below alwys at my ship
        for (Enemy e : enemies) {
            if (!e.isAlive()) continue;

            // only shoot if the player is below the alien
            if (playerY > e.y) {
                double dx = playerX - e.x;
                double dy = playerY - e.y;
                double dist = Math.sqrt(dx * dx + dy * dy);

                if (dist < 400 && now - e.lastShot > 800_000_000L) {
                    // spaceship er dike angle kore korbe !
                    enemyBullets.add(new Bullet(
                            e.x + TILE_SIZE / 2,
                            e.y + TILE_SIZE / 2,
                            dx / dist * 5,
                            dy / dist * 5,
                            false
                    ));

                    e.lastShot = now;
                }
            }
        }


        cameraX = playerX - gameCanvas.getWidth() / 2 + playerRenderW / 2;
        cameraY = playerY - gameCanvas.getHeight() / 2 + playerRenderH / 2;
    }

    private void render() {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

        // stars
        for (Star s : stars) {
            double sx = (s.x - cameraX * s.depth);
            double sy = (s.y - cameraY * s.depth);
            gc.setGlobalAlpha(s.brightness);
            gc.setFill(s.color);
            gc.fillOval(sx, sy, s.size, s.size);
        }
        gc.setGlobalAlpha(1.0);

        // map objects
        for (int row = 0; row < mapGrid.length; row++) {
            for (int col = 0; col < mapGrid[row].length; col++) {
                String token = mapGrid[row][col];
                double x = col * TILE_SIZE - cameraX;
                double y = row * TILE_SIZE - cameraY;
                switch (token) {
                    case "R":
                        if (wormholeImage != null) gc.drawImage(wormholeImage, x - 150, y - 150, 300, 300);
                        break;
                    case "1":
                        if (pulsarImage != null) gc.drawImage(pulsarImage, x - 200, y - 200, 400, 400);
                        break;


                }
            }
        }

        // enemies
        for (Enemy e : enemies) {
            if (!e.isAlive()) continue;
            double ex = e.x - cameraX;
            double ey = e.y - cameraY;
            if (e.type == 1 && alien1Image != null)
                gc.drawImage(alien1Image, ex, ey, TILE_SIZE, TILE_SIZE);
            else if (e.type == 2 && alien2Image != null)
                gc.drawImage(alien2Image, ex, ey, TILE_SIZE, TILE_SIZE);
        }

        // player
        double px = gameCanvas.getWidth() / 2 - playerRenderW / 2;
        double py = gameCanvas.getHeight() / 2 - playerRenderH / 2;
        if (playerImage != null)
            gc.drawImage(playerImage, px, py, playerRenderW, playerRenderH);

        // bullets
        gc.setFill(Color.YELLOW);
        for (Bullet b : bullets) gc.fillOval(b.x - cameraX, b.y - cameraY, b.w, b.h);
        gc.setFill(Color.RED);
        for (Bullet b : enemyBullets) gc.fillOval(b.x - cameraX, b.y - cameraY, b.w, b.h);

        // player health bar
        gc.setFill(Color.GRAY);
        gc.fillRect(20, 20, 200, 20);
        gc.setFill(Color.LIMEGREEN);
        gc.fillRect(20, 20, (playerHP / 70.0) * 200, 20);
        gc.setStroke(Color.WHITE);
        gc.strokeRect(20, 20, 200, 20);

        // game over
        if (gameOver) {
            gc.setFill(Color.RED);
            //kisui show korbe na ! khali blast hbe ! empty string
            gc.fillText("  ", gameCanvas.getWidth() / 2 - 40, gameCanvas.getHeight() / 2);
        }

        // draw explosions
        for (Explosion ex : explosions) {
            ex.render(gc, cameraX, cameraY);
        }

        // mission text top-right
        //gc.setFill(Color.WHITE);
        //gc.setFont(javafx.scene.text.Font.font(20));
        //gc.fillText(missionText, gameCanvas.getWidth() - 400, 40);

        gc.setFont(javafx.scene.text.Font.font("Orbitron", 20));

        double x = gameCanvas.getWidth() - 400;
        double y = 40;
        String text = missionText;
        Color glowColor = Color.CYAN; // change to MAGENTA at wormhole if you want

// glow effect layers
        for (int r = 6; r >= 1; r--) {
            gc.setFill(glowColor.deriveColor(0, 1, 1, 0.15 * r));
            gc.fillText(text, x, y);
        }

// main bright text
        gc.setFill(glowColor);
        gc.fillText(text, x, y);

        //text filed for mission end !



    }

    private void showNebulaWindow() {

        paused = true;

        javafx.application.Platform.runLater(() -> {
            javafx.stage.Stage dialog = new javafx.stage.Stage();
            dialog.initStyle(javafx.stage.StageStyle.TRANSPARENT);
            dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            if (gameCanvas.getScene() != null) dialog.initOwner(gameCanvas.getScene().getWindow());

            // Root pane with neon border
            javafx.scene.layout.BorderPane root = new javafx.scene.layout.BorderPane();
            root.setStyle(
                    "-fx-background-color: rgba(10,10,30,0.9);" +
                            "-fx-padding: 18;" +
                            "-fx-border-color: cyan;" +
                            "-fx-border-width: 2;" +
                            "-fx-border-radius: 12;" +
                            "-fx-background-radius: 12;" +
                            "-fx-effect: dropshadow(gaussian, cyan, 20, 0.8, 0, 0);"
            );

            // Top-right close button (the "cross")
            javafx.scene.layout.HBox top = new javafx.scene.layout.HBox();
            top.setAlignment(javafx.geometry.Pos.TOP_RIGHT);
            top.setPadding(new javafx.geometry.Insets(4, 4, 0, 0));

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
            // hover glow
            closeBtn.setOnMouseEntered(ev -> closeBtn.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.06);" +
                            "-fx-text-fill: white; -fx-effect: dropshadow(gaussian, cyan, 10, 0.6, 0, 0);" +
                            "-fx-font-size: 14px; -fx-font-weight: bold; -fx-pref-width: 30px; -fx-pref-height: 30px;"
            ));
            closeBtn.setOnMouseExited(ev -> closeBtn.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-pref-width: 30px; -fx-pref-height: 30px;"
            ));
            closeBtn.setOnAction(ev -> dialog.close());
            top.getChildren().add(closeBtn);
            root.setTop(top);

            // Center content
            javafx.scene.layout.VBox box = new javafx.scene.layout.VBox(12);
            box.setAlignment(javafx.geometry.Pos.CENTER);

            javafx.scene.text.Text title = new javafx.scene.text.Text("🚀 Nebula Reached!");
            title.setFill(javafx.scene.paint.Color.CYAN);
            title.setStyle("-fx-font-size: 22px; -fx-font-family: 'Orbitron';");

            javafx.scene.control.Button collectBtn = new javafx.scene.control.Button("⚡ Collect Antimatter");
            collectBtn.setStyle(
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
            collectBtn.setOnAction(ev -> {
                missionText = "Antimatter collected ✅";
                dialog.close();
                showEndGameWindow();
            });

            box.getChildren().addAll(title, collectBtn);
            root.setCenter(box);

            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            // ESC closes
            scene.setOnKeyPressed(k -> {
                if (k.getCode() == javafx.scene.input.KeyCode.ESCAPE) dialog.close();
            });

            dialog.setScene(scene);

            dialog.setOnHidden(ev -> paused = false);

            dialog.showAndWait();
        });
    }

    private void showEndGameWindow() {

        paused = true;

        javafx.application.Platform.runLater(() -> {
            javafx.stage.Stage dialog = new javafx.stage.Stage();
            dialog.initStyle(javafx.stage.StageStyle.TRANSPARENT);
            dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            if (gameCanvas.getScene() != null)
                dialog.initOwner(gameCanvas.getScene().getWindow());

            // Root panel
            javafx.scene.layout.BorderPane root = new javafx.scene.layout.BorderPane();
            root.setStyle(
                    "-fx-background-color: rgba(0,0,20,0.9);" +
                            "-fx-padding: 25;" +
                            "-fx-border-color: gold;" +
                            "-fx-border-width: 3;" +
                            "-fx-border-radius: 15;" +
                            "-fx-background-radius: 15;" +
                            "-fx-effect: dropshadow(gaussian, gold, 25, 0.9, 0, 0);"
            );

            // ❌ Cross button (top-right)
            javafx.scene.control.Button crossBtn = new javafx.scene.control.Button("✖");
            crossBtn.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-text-fill: red;" +
                            "-fx-font-size: 18px;" +
                            "-fx-font-weight: bold;"
            );
            crossBtn.setOnAction(e -> dialog.close());

            javafx.scene.layout.HBox topBar = new javafx.scene.layout.HBox();
            topBar.setAlignment(javafx.geometry.Pos.TOP_RIGHT);
            topBar.getChildren().add(crossBtn);
            root.setTop(topBar);

            // Center content
            javafx.scene.layout.VBox box = new javafx.scene.layout.VBox(15);
            box.setAlignment(javafx.geometry.Pos.CENTER);

            javafx.scene.text.Text title = new javafx.scene.text.Text("🎉 Congratulations, Captain!");
            title.setFill(javafx.scene.paint.Color.GOLD);
            title.setStyle("-fx-font-size: 26px; -fx-font-family: 'Orbitron'; -fx-font-weight: bold;");

            javafx.scene.text.Text msg = new javafx.scene.text.Text(
                    "Thank you for collecting the antimatter.\n\nThis was the final mission!"
            );
            msg.setFill(javafx.scene.paint.Color.LIGHTYELLOW);
            msg.setStyle("-fx-font-size: 18px; -fx-font-family: 'Orbitron';");

            javafx.scene.text.Text hint = new javafx.scene.text.Text(
                    "➡ Go back to the wormhole to return to your home solar system!"
            );
            hint.setFill(javafx.scene.paint.Color.LIGHTGREEN);
            hint.setStyle("-fx-font-size: 16px; -fx-font-family: 'Orbitron'; -fx-font-style: italic;");

            box.getChildren().addAll(title, msg, hint);
            root.setCenter(box);

            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            dialog.setScene(scene);

            dialog.setOnHidden(ev -> paused = false);


            dialog.showAndWait();
        });
    }






    private void showWormholeWindow() {

        paused = true;

        javafx.application.Platform.runLater(() -> {
            javafx.stage.Stage dialog = new javafx.stage.Stage();
            dialog.initStyle(javafx.stage.StageStyle.TRANSPARENT);
            dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            if (gameCanvas.getScene() != null) dialog.initOwner(gameCanvas.getScene().getWindow());

            javafx.scene.layout.BorderPane root = new javafx.scene.layout.BorderPane();
            root.setStyle(
                    "-fx-background-color: rgba(20,0,30,0.9);" +
                            "-fx-padding: 18;" +
                            "-fx-border-color: magenta;" +
                            "-fx-border-width: 2;" +
                            "-fx-border-radius: 12;" +
                            "-fx-background-radius: 12;" +
                            "-fx-effect: dropshadow(gaussian, magenta, 20, 0.8, 0, 0);"
            );

            // top-right close
            javafx.scene.layout.HBox top = new javafx.scene.layout.HBox();
            top.setAlignment(javafx.geometry.Pos.TOP_RIGHT);
            top.setPadding(new javafx.geometry.Insets(4, 4, 0, 0));

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
                    "-fx-background-color: rgba(255,255,255,0.06);" +
                            "-fx-text-fill: white; -fx-effect: dropshadow(gaussian, magenta, 10, 0.6, 0, 0);" +
                            "-fx-font-size: 14px; -fx-font-weight: bold; -fx-pref-width: 30px; -fx-pref-height: 30px;"
            ));
            closeBtn.setOnMouseExited(ev -> closeBtn.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-pref-width: 30px; -fx-pref-height: 30px;"
            ));
            closeBtn.setOnAction(ev -> dialog.close());
            top.getChildren().add(closeBtn);
            root.setTop(top);

            javafx.scene.layout.VBox box = new javafx.scene.layout.VBox(12);
            box.setAlignment(javafx.geometry.Pos.CENTER);

            javafx.scene.text.Text title = new javafx.scene.text.Text("🌌 Wormhole Reached!");
            title.setFill(javafx.scene.paint.Color.MAGENTA);
            title.setStyle("-fx-font-size: 22px; -fx-font-family: 'Orbitron';");

            javafx.scene.control.Button homeBtn = new javafx.scene.control.Button("Return to Home solar system");
            javafx.scene.control.Button exitBtn = new javafx.scene.control.Button(" Exit The game");
            homeBtn.setStyle(
                    "-fx-background-color: black;" +
                            "-fx-text-fill: magenta;" +
                            "-fx-font-family: 'Orbitron';" +
                            "-fx-font-size: 16px;" +
                            "-fx-border-color: magenta;" +
                            "-fx-border-width: 2;" +
                            "-fx-background-radius: 8;" +
                            "-fx-border-radius: 8;" +
                            "-fx-effect: dropshadow(gaussian, magenta, 20, 0.8, 0, 0);"
            );

            exitBtn.setStyle(
                    "-fx-background-color: black;" +
                            "-fx-text-fill: magenta;" +
                            "-fx-font-family: 'Orbitron';" +
                            "-fx-font-size: 16px;" +
                            "-fx-border-color: magenta;" +
                            "-fx-border-width: 2;" +
                            "-fx-background-radius: 8;" +
                            "-fx-border-radius: 8;" +
                            "-fx-effect: dropshadow(gaussian, magenta, 20, 0.8, 0, 0);"
            );

            exitBtn.setOnAction(ev -> {


                dialog.close();


                Main.switchScene("home.fxml");
            });

            homeBtn.setOnAction(ev -> {
                missionText = "Mission complete! Returning home... 🛸";

                dialog.close();


                Main.switchScene("solar_system.fxml");
            });

            box.getChildren().addAll(title, homeBtn , exitBtn);
            root.setCenter(box);

            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            scene.setOnKeyPressed(k -> {
                if (k.getCode() == javafx.scene.input.KeyCode.ESCAPE) dialog.close();
            });

            dialog.setScene(scene);

            dialog.setOnHidden(ev -> paused = false);

            dialog.showAndWait();
        });
    }




    private void spawnBullet() {
        double startX = playerX + playerRenderW / 2.0 - 3;
        double startY = playerY;

        // Set the bullet's velocity based on the player's direction
        double bulletDx = 0;
        double bulletDy = 0;

        switch (playerDirection) {
            case "UP":
                bulletDx = 0;
                bulletDy = -8;
                break;
            case "DOWN":
                bulletDx = 0;
                bulletDy = 8;
                break;
            case "LEFT":
                bulletDx = -8;
                bulletDy = 0;
                break;
            case "RIGHT":
                bulletDx = 8;
                bulletDy = 0;
                break;
        }

        bullets.add(new Bullet(startX, startY, bulletDx, bulletDy, true));

        //shoot sond
        if (shootSound != null) shootSound.play();
    }


    private class Bullet {
        double x, y;
        double w = 6, h = 12;
        double dx, dy;
        boolean fromPlayer;

        Bullet(double x, double y, double dx, double dy, boolean fromPlayer) {
            this.x = x;
            this.y = y;
            this.dx = dx;
            this.dy = dy;
            this.fromPlayer = fromPlayer;
        }

        void update() {
            x += dx;
            y += dy;
        }

        boolean isOffscreen() {
            return x < cameraX - 100 || y < cameraY - 100 ||
                    x > cameraX + gameCanvas.getWidth() + 100 ||
                    y > cameraY + gameCanvas.getHeight() + 100;
        }

        boolean collides(double ox, double oy, double ow, double oh) {
            return x < ox + ow && x + w > ox &&
                    y < oy + oh && y + h > oy;
        }
    }

    private static class Enemy {
        double x, y;
        int hp;
        int type; // 1 or 2
        long lastShot = 0L;

        Enemy(double x, double y, int hp, int type) {
            this.x = x;
            this.y = y;
            this.hp = hp;
            this.type = type;
        }

        boolean isAlive() {
            return hp > 0;
        }
    }

    private static class Star {
        double x, y, size, brightness, depth;
        Color color;
        Star(double x, double y, double size, double brightness, double depth, Color color) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.brightness = brightness;
            this.depth = depth;
            this.color = color;
        }
    }

    public static class Explosion {
        double x, y, dx, dy;
        double radius;
        double life;

        Explosion(double x, double y) {
            this.x = x;
            this.y = y;
            this.dx = (Math.random() - 0.5) * 20;   // much faster spread
            this.dy = (Math.random() - 0.5) * 20;
            this.radius = 30 + Math.random() * 40;  // huge start size
            this.life = 70; // longer life
        }

        void update() {
            x += dx;
            y += dy;
            radius += 3;   // rapid growth
            life--;
        }

        void render(GraphicsContext gc, double camX, double camY) {
            double alpha = Math.max(0, life / 70.0);
            gc.setGlobalAlpha(alpha);

            // fiery palette
            Color[] colors = { Color.ORANGE, Color.YELLOW, Color.RED };
            gc.setFill(colors[(int)(Math.random() * colors.length)]);

            gc.fillOval(x - camX - radius / 2, y - camY - radius / 2, radius, radius);
            gc.setGlobalAlpha(1.0);
        }
    }

    private static class Shockwave extends Explosion {
        double maxRadius = 500; // size of the shockwave

        Shockwave(double x, double y) {
            super(x, y);
            this.radius = 10;
            this.life = 60;
            this.dx = 0;
            this.dy = 0;
        }

        @Override
        void update() {
            radius += 12;   // grows much faster
            life--;
        }

        @Override
        void render(GraphicsContext gc, double camX, double camY) {
            double alpha = Math.max(0, life / 60.0) * 0.6;
            gc.setGlobalAlpha(alpha);
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(4);
            gc.strokeOval(x - camX - radius / 2, y - camY - radius / 2, radius, radius);
            gc.setGlobalAlpha(1.0);
        }
    }




}
