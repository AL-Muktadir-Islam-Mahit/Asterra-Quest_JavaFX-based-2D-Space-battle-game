package com.example.demo;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.util.*;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;

public class MultiplayerShooterController {

    @FXML private Canvas gameCanvas;
    @FXML private AnchorPane rootPane;
    @FXML private Button backButton;

    // Invite UI
    @FXML private TextField inviteField;
    @FXML private Button inviteButton;

    private GraphicsContext gc;
    private MultiplayerClient client;
    private Timeline timeline;

    private static final int WIDTH = 800;
    private static final int HEIGHT = 500;
    private static final int PLAYER_SIZE = 60;
    private static final Random RAND = new Random();

    private Image PLAYER_IMG;
    private Image EXPLOSION_IMG;
    private Image[] BOMBS_IMG;

    private Media SHOOT_SOUND;
    private Media BLAST_SOUND;

    private Rocket player;
    private Rocket otherPlayer;
    private List<Shot> shots;
    private List<Universe> univ;
    private List<Bomb> bombs;

    // server broadcast scores: score1 is player1, score2 is player2
    private int score1 = 0;
    private int score2 = 0;

    // store server-known player names if available (player1Name corresponds to score1)
    private String player1Name = "Player1";
    private String player2Name = "Player2";

    // friendly names (local view)
    private final String myName = UserData.username != null ? UserData.username : "You";
    private String otherName = "Player";

    private boolean moveLeft = false;
    private boolean moveRight = false;

    private double myX = WIDTH/2 - PLAYER_SIZE/2;
    private double otherX = WIDTH/2 - PLAYER_SIZE/2;

    private boolean gameStarted = false;
    private long endTime = 0;
    private int myId = 0;             // expected values from server: 1 or 2; 0 until assigned
    private final double speed = 6.0;

    private long lastSpawn = 0;

    @FXML
    private void initialize() {
        gc = gameCanvas.getGraphicsContext2D();

        // focus behaviour
        rootPane.setFocusTraversable(true);
        inviteButton.setFocusTraversable(false);
        backButton.setFocusTraversable(false);

        // load resources
        try {
            PLAYER_IMG = new Image(Objects.requireNonNull(
                    getClass().getResource("/com/example/demo/player.png")).toExternalForm());
            EXPLOSION_IMG = new Image(Objects.requireNonNull(
                    getClass().getResource("/com/example/demo/explosionnnnnnnnn.png")).toExternalForm());
            BOMBS_IMG = new Image[] {
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
            SHOOT_SOUND = new Media(Objects.requireNonNull(
                    getClass().getResource("/com/example/demo/shoot.mp3")).toExternalForm());
            BLAST_SOUND = new Media(Objects.requireNonNull(
                    getClass().getResource("/com/example/demo/blast.mp3")).toExternalForm());
        } catch (Exception e) {
            e.printStackTrace();
        }

        setupLocalGameObjects();

        // connect to server
        try {
            client = new MultiplayerClient("localhost", 5555, this::handleServerMessage);
            if (UserData.username != null && !UserData.username.trim().isEmpty()) {
                client.send("AUTH:" + UserData.username.trim());
            }
        } catch (Exception e) {
            e.printStackTrace();
            showConnectError();
            return;
        }

        inviteButton.setOnAction(ev -> sendInviteAndRefocus());
        inviteField.setOnAction(ev -> sendInviteAndRefocus());

        // keyboard handlers on Scene
        rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyPressed(e -> {
                    if (e.getCode() == KeyCode.A) moveLeft = true;
                    if (e.getCode() == KeyCode.D) moveRight = true;
                    if (e.getCode() == KeyCode.SPACE) {
                        if (!gameStarted) return;
                        if (shots.size() < 20) {
                            shots.add(player.shoot());
                            playShootSound();
                        }
                    }
                });

                newScene.setOnKeyReleased(e -> {
                    if (e.getCode() == KeyCode.A) moveLeft = false;
                    if (e.getCode() == KeyCode.D) moveRight = false;
                });

                Platform.runLater(() -> rootPane.requestFocus());
            }
        });

        timeline = new Timeline(new KeyFrame(Duration.millis(16), e -> gameLoop()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void setupLocalGameObjects() {
        shots = new ArrayList<>();
        univ = new ArrayList<>();
        bombs = new ArrayList<>();
        player = new Rocket((int)myX, HEIGHT - PLAYER_SIZE - 20, PLAYER_SIZE, PLAYER_IMG);
        otherPlayer = new Rocket((int)otherX, HEIGHT - PLAYER_SIZE - 20, PLAYER_SIZE, PLAYER_IMG);
        score1 = 0; score2 = 0;
        // reset server-known names to defaults until server tells us
        player1Name = "Player1";
        player2Name = "Player2";
        otherName = "Player";
        IntStream.range(0, 9).mapToObj(i -> newBomb()).forEach(bombs::add);
    }

    private void showConnectError() {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
        gc.setFill(Color.WHITE);
        gc.fillText("Unable to connect to server", 200, 200);
    }

    @FXML
    private void onBackClick() {
        stopAndClose();
        Main.switchScene("combat.fxml");
    }

    private void stopAndClose() {
        if (timeline != null) timeline.stop();
        if (client != null) client.close();
    }

    private void sendInviteAndRefocus() {
        String target = inviteField.getText();
        if (target == null || target.trim().isEmpty()) {
            Platform.runLater(() -> {
                Alert a = new Alert(Alert.AlertType.INFORMATION, "Enter a username to invite.");
                a.showAndWait();
                Platform.runLater(() -> rootPane.requestFocus());
            });
            return;
        }
        if (client != null) client.send("INVITE:" + target.trim());
        // optimistic: set otherName so HUD shows who invited (will be confirmed on accept)
        otherName = target.trim();
        Platform.runLater(() -> {
            inviteField.clear();
            rootPane.requestFocus();
        });
    }

    private void handleServerMessage(String msg) {
        if (msg == null) return;

        if (msg.startsWith("AUTH_OK")) {
            System.out.println("Authenticated as " + myName);
            return;
        } else if (msg.startsWith("AUTH_FAIL")) {
            Platform.runLater(() -> {
                new Alert(Alert.AlertType.ERROR, "Auth failed: " + msg).showAndWait();
                rootPane.requestFocus();
            });
            return;
        } else if (msg.equals("DISCONNECTED")) {
            Platform.runLater(() -> {
                new Alert(Alert.AlertType.WARNING, "Disconnected from server.").show();
                rootPane.requestFocus();
            });
            gameStarted = false;
            return;
        }

        if (msg.startsWith("ID:")) {
            try {
                myId = Integer.parseInt(msg.split(":")[1]);
            } catch (Exception ignored) {}
            System.out.println("Assigned ID: " + myId);
            return;
        }

        if (msg.equals("START")) {
            gameStarted = true;
            endTime = System.currentTimeMillis() + 30_000;
            Platform.runLater(() -> {
                shots.clear();
                bombs.clear();
                setupLocalGameObjects();
                rootPane.requestFocus();
            });
            return;
        }

        if (msg.equals("END")) {
            // server finished: gameEnded locally — stop spawning but keep endTime so UI shows result
            gameStarted = false;
            Platform.runLater(() -> rootPane.requestFocus());
            return;
        }

        // POS message contains two numbers, but server ordering can vary.
        // We'll detect which value appears to be "us" by comparing to our current myX,
        // then treat the other value as the remote player's position.
        if (msg.startsWith("POS:")) {
            String[] parts = msg.split(":");
            if (parts.length >= 3) {
                try {
                    double a = Double.parseDouble(parts[1]);
                    double b = Double.parseDouble(parts[2]);

                    // decide which is mine (closest to current myX)
                    double distanceA = Math.abs(a - myX);
                    double distanceB = Math.abs(b - myX);

                    double otherVal;
                    // prefer not to override my local myX (preserve local responsiveness)
                    if (distanceA <= distanceB) {
                        // a looks like "me", b looks like "other"
                        otherVal = b;
                    } else {
                        otherVal = a;
                    }

                    otherX = otherVal;

                    Platform.runLater(() -> {
                        otherPlayer.posX = (int) Math.round(otherX);
                        // do not overwrite player.posX from server here
                    });
                } catch (Exception ignored) {}
            }
            return;
        }

        // SCORE message format supported:
        // SCORE:score1:score2
        // or SCORE:score1:score2:player1Name:player2Name
        if (msg.startsWith("SCORE:")) {
            String[] parts = msg.split(":", 5);
            if (parts.length >= 3) {
                try {
                    score1 = Integer.parseInt(parts[1]);
                    score2 = Integer.parseInt(parts[2]);
                } catch (Exception ignored) {}
            }
            if (parts.length >= 5) {
                // server provided names that map to score1 and score2
                player1Name = parts[3] != null && !parts[3].isEmpty() ? parts[3] : player1Name;
                player2Name = parts[4] != null && !parts[4].isEmpty() ? parts[4] : player2Name;
            }
            // update otherName if we can determine who is the other player
            // If we know myName equals player1Name, other is player2Name, and vice versa.
            if (player1Name.equals(myName)) otherName = player2Name;
            else if (player2Name.equals(myName)) otherName = player1Name;
            return;
        }

        // Invite handling: record partner username on accept/send confirmation
        if (msg.startsWith("INVITE_FROM:")) {
            String from = msg.substring("INVITE_FROM:".length());
            Platform.runLater(() -> {
                Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                        "User '" + from + "' invited you to play. Accept?",
                        ButtonType.YES, ButtonType.NO);
                a.setTitle("Game Invite");
                a.showAndWait().ifPresent(bt -> {
                    if (bt == ButtonType.YES) {
                        if (client != null) client.send("INVITE_ACCEPT:" + from);
                        otherName = from; // partner
                    } else {
                        if (client != null) client.send("INVITE_REJECT:" + from);
                    }
                    Platform.runLater(() -> rootPane.requestFocus());
                });
            });
            return;
        }

        if (msg.startsWith("INVITE_SENT:")) {
            String target = msg.substring("INVITE_SENT:".length());
            // optimistic: inviter already set otherName to target in sendInviteAndRefocus
            Platform.runLater(() -> {
                new Alert(Alert.AlertType.INFORMATION, "Invite sent to " + target).show();
                rootPane.requestFocus();
            });
            return;
        }

        if (msg.startsWith("INVITE_FAIL:")) {
            String reason = msg.substring("INVITE_FAIL:".length());
            Platform.runLater(() -> {
                new Alert(Alert.AlertType.INFORMATION, "Invite failed: " + reason).show();
                rootPane.requestFocus();
            });
            return;
        }

        if (msg.startsWith("INVITE_ACCEPTED:")) {
            // server tells both players who accepted; use that to set partner name
            String other = msg.substring("INVITE_ACCEPTED:".length());
            otherName = other != null && !other.isEmpty() ? other : otherName;
            Platform.runLater(() -> {
                new Alert(Alert.AlertType.INFORMATION, otherName + " accepted your invite. Starting game...").show();
                rootPane.requestFocus();
            });
            return;
        }

        if (msg.startsWith("INVITE_REJECTED:")) {
            String other = msg.substring("INVITE_REJECTED:".length());
            Platform.runLater(() -> {
                new Alert(Alert.AlertType.INFORMATION, other + " rejected your invite.").show();
                rootPane.requestFocus();
            });
            return;
        }

        // other messages: log
        System.out.println("Server: " + msg);
    }

    // collisions
    private void shootCheck() {
        boolean destroyedAny = false;
        synchronized (bombs) {
            for (Bomb b : bombs) {
                if (b.destroyed || b.exploding) continue;
                for (Shot s : shots) {
                    if (!s.toRemove && s.collides(b)) {
                        b.explode();
                        s.toRemove = true;
                        destroyedAny = true;
                        playBlastSound();
                    }
                }
            }
        }
        if (destroyedAny && client != null) client.send("DESTROY");
    }

    // main loop
    private void gameLoop() {
        if (moveLeft) myX -= speed;
        if (moveRight) myX += speed;
        myX = Math.max(0, Math.min(WIDTH - PLAYER_SIZE, myX));
        player.posX = (int) Math.round(myX);

        if (client != null && myId != 0) client.send("POS:" + myX);

        long now = System.currentTimeMillis();
        if (now - lastSpawn > 500 && gameStarted) {
            lastSpawn = now;
            bombs.add(newBomb());
        }

        synchronized (bombs) {
            Iterator<Bomb> it = bombs.iterator();
            while (it.hasNext()) {
                Bomb b = it.next();
                b.update();
                if (b.destroyed) it.remove();
            }
        }

        for (int i = shots.size() - 1; i >= 0; i--) {
            Shot s = shots.get(i);
            s.update();
            if (s.posY < 0 || s.toRemove) shots.remove(i);
        }

        shootCheck();
        draw();
    }

    private void draw() {
        double w = WIDTH, h = HEIGHT;
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, w, h);

        // Determine effective local id:
        // prefer server-sent myId if available,
        // otherwise infer from player1Name/player2Name if server provided names,
        // otherwise default to 1.
        int effectiveMyId = 1; // default
        if (myId == 1 || myId == 2) {
            effectiveMyId = myId;
        } else {
            if (player1Name.equals(myName)) effectiveMyId = 1;
            else if (player2Name.equals(myName)) effectiveMyId = 2;
            else effectiveMyId = 1; // fallback
        }

        // now map displayed scores and names accordingly
        int myScoreDisplay = (effectiveMyId == 2) ? score2 : score1;
        int otherScoreDisplay = (effectiveMyId == 2) ? score1 : score2;
        String displayedPlayerName = (effectiveMyId == 2) ? player2Name : player1Name;
        String displayedOtherName = (effectiveMyId == 2) ? player1Name : player2Name;

        // keep otherName consistent if we can deduce it
        if (displayedOtherName != null && !displayedOtherName.isEmpty()) otherName = displayedOtherName;

        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFill(Color.CYAN);
        gc.setFont(Font.font(20));
        gc.fillText(myName + ": " + myScoreDisplay + "   " + otherName + ": " + otherScoreDisplay, 20, 25);

        if (gameStarted) {
            long remaining = Math.max(0, (endTime - System.currentTimeMillis()) / 1000);
            gc.fillText("Time left: " + remaining + "s", w - 160, 25);
        }

        gc.setFont(Font.font(12));
        for (Universe u : univ) u.draw();

        synchronized (bombs) {
            for (Bomb b : bombs) {
                if (!b.destroyed) {
                    if (b.exploding) b.draw();
                    else {
                        Image img = b.img != null ? b.img : BOMBS_IMG[RAND.nextInt(BOMBS_IMG.length)];
                        gc.drawImage(img, b.posX - 20, b.posY - 20, 40, 40);
                    }
                }
            }
        }

        otherPlayer.posX = (int) Math.round(otherX);
        otherPlayer.draw();
        player.draw();

        for (Shot s : shots) s.draw();

        // Show result relative to local player when the game has ended.
        if (!gameStarted && endTime > 0) {
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setFont(Font.font("Orbitron", FontWeight.BOLD, 42));
            gc.setEffect(new DropShadow(10, Color.CYAN));

            String result;
            if (myScoreDisplay > otherScoreDisplay) {
                result = "You Win!";
            } else if (myScoreDisplay < otherScoreDisplay) {
                result = otherName + " Wins!";
            } else {
                result = "Draw!";
            }

            gc.setFill(Color.CYAN);
            gc.fillText(result + "\n" + myName + ":" + myScoreDisplay + "  " + otherName + ":" + otherScoreDisplay, w / 2, h / 2);
            gc.setEffect(null);
        }

        gc.setFont(Font.font(12));
        gc.setFill(Color.WHITE);
        gc.fillText("A / D = move. SPACE = shoot", 10, h - 10);
    }

    private Bomb newBomb() {
        return new Bomb(50 + RAND.nextInt(WIDTH - 100), 0, PLAYER_SIZE,
                BOMBS_IMG[RAND.nextInt(BOMBS_IMG.length)]);
    }

    private void playShootSound() {
        try {
            if (SHOOT_SOUND != null) {
                MediaPlayer mp = new MediaPlayer(SHOOT_SOUND);
                mp.play();
            }
        } catch (Exception ignored){}
    }

    private void playBlastSound() {
        try {
            if (BLAST_SOUND != null) {
                MediaPlayer mp = new MediaPlayer(BLAST_SOUND);
                mp.play();
            }
        } catch (Exception ignored){}
    }

    // -------- Inner Classes (same as before) --------
    public class Rocket {
        int posX, posY, size;
        Image img;
        boolean exploding = false;
        int explosionStep = 0;
        boolean destroyed = false;
        private final int EXPLOSION_DURATION = 20;
        private final double EXPLOSION_SCALE = 1.5;

        Rocket(int x, int y, int size, Image img) {
            posX = x; posY = y; this.size = size; this.img = img;
        }

        public void update() {
            if (exploding) {
                explosionStep++;
                if (explosionStep > EXPLOSION_DURATION) destroyed = true;
            }
        }

        public void draw() {
            if (exploding) {
                int explosionSize = (int)(size * EXPLOSION_SCALE);
                gc.drawImage(EXPLOSION_IMG, posX - (explosionSize - size)/2,
                        posY - (explosionSize - size)/2, explosionSize, explosionSize);
                update();
            } else {
                if (img != null) gc.drawImage(img, posX, posY, size, size);
                else {
                    gc.setFill(Color.LIME);
                    gc.fillRect(posX, posY, size, size);
                }
            }
        }

        public Shot shoot() {
            return new Shot(posX + size / 2 - 3, posY - 10);
        }

        public void explode() { exploding = true; explosionStep = 0; }
    }

    public class Bomb extends Rocket {
        int speed = 3;
        Bomb(int x, int y, int s, Image img) { super(x, y, s, img); }

        public void update() {
            if (!exploding && !destroyed) {
                posY += speed;
                if (posY > HEIGHT + 50) destroyed = true;
            } else {
                super.update();
            }
        }

        @Override
        public void explode() {
            if (!exploding && !destroyed) {
                exploding = true;
                explosionStep = 0;
            }
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
            return Math.sqrt(dx * dx + dy * dy) < r.size / 2 + 6;
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
