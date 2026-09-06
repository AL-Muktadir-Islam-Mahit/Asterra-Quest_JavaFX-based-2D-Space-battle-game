package com.example.demo;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.IntStream;
import javafx.scene.image.Image;

public class SingleShooterController {

    @FXML private Canvas gameCanvas;
    @FXML private AnchorPane rootPane;
    @FXML private Button backButton;

    private GraphicsContext gc;
    private Timeline timeline;

    private static final int WIDTH = 800;
    private static final int HEIGHT = 500;
    private static final int PLAYER_SIZE = 60;
    private static final Random RAND = new Random();


    private Image PLAYER_IMG = new Image(Objects.requireNonNull(
            getClass().getResource("/com/example/demo/player.png")).toExternalForm());
    private Image EXPLOSION_IMG = new Image(Objects.requireNonNull(
            getClass().getResource("/com/example/demo/explosionnnnnnnnn.png")).toExternalForm());
    private Image[] BOMBS_IMG = {
            new Image(Objects.requireNonNull(getClass().getResource("/com/example/demo/1.png")).toExternalForm()),
            new Image(Objects.requireNonNull(getClass().getResource("/com/example/demo/2.png")).toExternalForm()),
            new Image(Objects.requireNonNull(getClass().getResource("/com/example/demo/3.png")).toExternalForm()),
            new Image(Objects.requireNonNull(getClass().getResource("/com/example/demo/4.png")).toExternalForm()),
            new Image(Objects.requireNonNull(getClass().getResource("/com/example/demo/5.png")).toExternalForm()),
            new Image(Objects.requireNonNull(getClass().getResource("/com/example/demo/6.png")).toExternalForm()),
            new Image(Objects.requireNonNull(getClass().getResource("/com/example/demo/7.png")).toExternalForm()),
            new Image(Objects.requireNonNull(getClass().getResource("/com/example/demo/8.png")).toExternalForm()),
            new Image(Objects.requireNonNull(getClass().getResource("/com/example/demo/9.png")).toExternalForm())
    };

    // ✅ Load sounds
    private Media SHOOT_SOUND = new Media(Objects.requireNonNull(
            getClass().getResource("/com/example/demo/shoot.mp3")).toExternalForm());
    private Media BLAST_SOUND = new Media(Objects.requireNonNull(
            getClass().getResource("/com/example/demo/blast.mp3")).toExternalForm());

    private Rocket player;
    private List<Shot> shots;
    private List<Universe> univ;
    private List<Bomb> bombs;
    private int score;
    private boolean gameOver = false;
    private boolean coinsUpdated = false;

    private boolean moveLeft = false;
    private boolean moveRight = false;

    @FXML
    private void initialize() {
        gc = gameCanvas.getGraphicsContext2D();
        setupGame();

        timeline = new Timeline(new KeyFrame(Duration.millis(16), e -> runGame()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyPressed(e -> {
                    if (e.getCode() == KeyCode.A) moveLeft = true;
                    if (e.getCode() == KeyCode.D) moveRight = true;
                    if (e.getCode() == KeyCode.SPACE) {
                        if (gameOver) {
                            gameOver = false;
                            coinsUpdated = false;
                            setupGame();
                        } else if (shots.size() < 20) {
                            shots.add(player.shoot());
                            playShootSound();
                        }
                    }
                });

                newScene.setOnKeyReleased(e -> {
                    if (e.getCode() == KeyCode.A) moveLeft = false;
                    if (e.getCode() == KeyCode.D) moveRight = false;
                });

                rootPane.requestFocus();
            }
        });
    }

    @FXML
    private void onBackClick() {
        timeline.stop();
        Main.switchScene("combat.fxml");
    }

    private void setupGame() {
        univ = new ArrayList<>();
        shots = new ArrayList<>();
        bombs = new ArrayList<>();
        player = new Rocket(WIDTH / 2, HEIGHT - PLAYER_SIZE - 20, PLAYER_SIZE, PLAYER_IMG);
        score = 0;
        IntStream.range(0, 10).mapToObj(i -> newBomb()).forEach(bombs::add);
    }

    private void runGame() {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFill(Color.CYAN);
        gc.setFont(Font.font(20));
        gc.fillText("Score: " + score, 20, 25);

        if (gameOver) {
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setFont(Font.font("Orbitron", FontWeight.BOLD, 50));

            DropShadow neonGlow = new DropShadow();
            neonGlow.setColor(Color.CYAN);
            neonGlow.setRadius(9);
            neonGlow.setSpread(0.1);

            gc.setEffect(neonGlow);
            gc.setFill(Color.CYAN);
            gc.fillText("GAME OVER\nScore: " + score + "\nPress SPACE to Restart", WIDTH / 2, HEIGHT / 2);
            gc.setEffect(null);

            if (!coinsUpdated) {
                updateUserCoins();
                coinsUpdated = true;
            }

            return;
        }

        if (moveLeft && player.posX > 0) player.posX -= 6;
        if (moveRight && player.posX + PLAYER_SIZE < WIDTH) player.posX += 6;

        univ.forEach(Universe::draw);
        player.update();
        player.draw();

        for (Bomb bomb : bombs) {
            bomb.update();
            bomb.draw();
            if (player.collides(bomb) && !player.exploding) {
                player.explode();
                playBlastSound(); // ✅ play explosion sound
            }
        }

        for (int i = shots.size() - 1; i >= 0; i--) {
            Shot s = shots.get(i);
            s.update();
            s.draw();
            if (s.posY < 0) shots.remove(i);
            else for (Bomb b : bombs) {
                if (s.collides(b) && !b.exploding) {
                    score++;
                    b.explode();
                    s.toRemove = true;
                    playBlastSound(); // ✅ play explosion sound
                }
            }
        }

        shots.removeIf(s -> s.toRemove);
        bombs.removeIf(b -> b.destroyed);
        while (bombs.size() < 10) bombs.add(newBomb());
        if (RAND.nextInt(10) > 2) univ.add(new Universe());
        univ.removeIf(u -> u.posY > HEIGHT);

        gameOver = player.destroyed;
    }

    private void updateUserCoins() {
        try {
            Path filePath = Paths.get("users.txt");
            List<String> lines = Files.readAllLines(filePath);
            List<String> updatedLines = new ArrayList<>();
            boolean found = false;

            for (String line : lines) {
                if (line.startsWith(UserData.username + ":")) {
                    String[] parts = line.split(":");
                    int oldCoins = Integer.parseInt(parts[2]);
                    int newCoins = oldCoins + score;
                    parts[2] = String.valueOf(newCoins);
                    updatedLines.add(String.join(":", parts));
                    UserData.coins = newCoins;
                    found = true;
                } else updatedLines.add(line);
            }

            if (!found) {
                String newUser = UserData.username + ":" + UserData.password + ":" + score + ":1:0:0";
                updatedLines.add(newUser);
            }

            Files.write(filePath, updatedLines, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("Coins updated successfully for " + UserData.username);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Bomb newBomb() {
        return new Bomb(50 + RAND.nextInt(WIDTH - 100), 0, PLAYER_SIZE,
                BOMBS_IMG[RAND.nextInt(BOMBS_IMG.length)]);
    }

    // ✅ Helper sound methods
    private void playShootSound() {
        MediaPlayer player = new MediaPlayer(SHOOT_SOUND);
        player.play();
    }

    private void playBlastSound() {
        MediaPlayer player = new MediaPlayer(BLAST_SOUND);
        player.play();
    }

    // -------- Inner Classes --------
    public class Rocket {
        int posX, posY, size;
        Image img;
        boolean exploding = false, destroyed = false;
        int explosionStep = 0;
        private final int EXPLOSION_DURATION = 20;
        private final double EXPLOSION_SCALE = 1.5;

        Rocket(int x, int y, int size, Image img) {
            posX = x; posY = y; this.size = size; this.img = img;
        }

        public void update() {
            if (exploding) explosionStep++;
            destroyed = explosionStep > EXPLOSION_DURATION;
        }

        public void draw() {
            if (exploding) {
                int explosionSize = (int)(size * EXPLOSION_SCALE);
                gc.drawImage(EXPLOSION_IMG, posX - (explosionSize - size)/2,
                        posY - (explosionSize - size)/2, explosionSize, explosionSize);
            } else gc.drawImage(img, posX, posY, size, size);
        }

        public Shot shoot() {
            return new Shot(posX + size / 2 - 3, posY - 10);
        }

        public boolean collides(Rocket other) {
            int dx = posX - other.posX;
            int dy = posY - other.posY;
            return Math.sqrt(dx * dx + dy * dy) < (size / 2 + other.size / 2);
        }

        public void explode() { exploding = true; explosionStep = 0; }
    }

    public class Bomb extends Rocket {
        int speed = (score / 5) + 2;
        Bomb(int x, int y, int s, Image img) { super(x, y, s, img); }
        public void update() {
            super.update();
            if (!exploding && !destroyed) posY += speed;
            if (posY > HEIGHT) destroyed = true;
        }
    }

    public class Shot {
        int posX, posY, speed = 10;
        boolean toRemove = false;

        Shot(int x, int y) { posX = x; posY = y; }

        void update() { posY -= speed; }

        void draw() {
            gc.setFill(Color.LIME);
            gc.fillOval(posX, posY, 6, 6);
        }

        boolean collides(Rocket r) {
            int dx = posX - r.posX;
            int dy = posY - r.posY;
            return Math.sqrt(dx * dx + dy * dy) < r.size / 2;
        }
    }

    public class Universe {
        int posX = RAND.nextInt(WIDTH);
        int posY = 0;
        int w = RAND.nextInt(4) + 1;
        int h = RAND.nextInt(4) + 1;
        Color color = Color.rgb(100 + RAND.nextInt(65),
                100 + RAND.nextInt(155),
                100 + RAND.nextInt(155),
                RAND.nextDouble() * 0.7 + 0.3);
        void draw() {
            gc.setFill(color);
            gc.fillOval(posX, posY, w, h);
            posY += 3;
        }
    }
}
