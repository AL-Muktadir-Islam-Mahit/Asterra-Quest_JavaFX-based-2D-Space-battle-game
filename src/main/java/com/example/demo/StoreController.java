package com.example.demo;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.awt.Desktop;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.util.*;

public class StoreController implements Initializable {

    @FXML private Label coinsLabel;
    @FXML private TextField takaField;
    @FXML private Button buyCoinButton;

    @FXML private HBox itemsBox;
    @FXML private VBox item1Box, item2Box, item3Box, item4Box, item5Box;
    @FXML private ImageView img1View, img2View, img3View, img4View, img5View;
    @FXML private Button buyImg1, buyImg2, buyImg3, buyImg4, buyImg5;

    private final int priceImg1 = 50;
    private final int priceImg2 = 70;
    private final int priceImg3 = 90;
    private final int priceImg4 = 120;
    private final int priceImg5 = 200;

    private final String sellerEmail = "sahriorshovon@gmail.com";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        refreshCoinsLabel();

        loadImageIfExists("/com/example/demo/img1.png", img1View);
        loadImageIfExists("/com/example/demo/img2.png", img2View);
        loadImageIfExists("/com/example/demo/img3.png", img3View);
        loadImageIfExists("/com/example/demo/img4.png", img4View);
        loadImageIfExists("/com/example/demo/img5.png", img5View);

        buyCoinButton.setOnAction(e -> onBuyCoin());
        buyImg1.setOnAction(e -> onBuyImage("img1", priceImg1));
        buyImg2.setOnAction(e -> onBuyImage("img2", priceImg2));
        buyImg3.setOnAction(e -> onBuyImage("img3", priceImg3));
        buyImg4.setOnAction(e -> onBuyImage("img4", priceImg4));
        buyImg5.setOnAction(e -> onBuyImage("img5", priceImg5));
    }

    private void loadImageIfExists(String resourcePath, ImageView view) {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is != null) {
                Image img = new Image(is);
                view.setImage(img);
                view.setPreserveRatio(true);
                view.setFitWidth(120);
                view.setFitHeight(120);
            }
        } catch (Exception ignored) {}
    }

    private void refreshCoinsLabel() {
        coinsLabel.setText("Coins: " + UserData.coins);
    }

    private void onBuyCoin() {
        String takaText = takaField.getText().trim();
        if (takaText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Enter amount", "Please enter the amount in Taka to buy coins.");
            return;
        }

        int taka;
        try {
            taka = Integer.parseInt(takaText);
            if (taka <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            showAlert(Alert.AlertType.ERROR, "Invalid amount", "Please enter a valid positive integer for Taka.");
            return;
        }

        int coinsToGive = taka;
        String username = (UserData.username != null && !UserData.username.isEmpty()) ? UserData.username : "Unknown";
        String bkashNumber = "01880392004";

        String content = "To buy " + coinsToGive + " coins (for " + taka + " ৳):\n\n"
                + "Send payment to bKash number: " + bkashNumber + "\n"
                + "Reference: " + username + "\n\n"
                + "After payment, press Confirm Payment to credit coins.";

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("bKash Payment");
        alert.setHeaderText("Send payment to bKash");
        alert.setContentText(content);

        ButtonType confirm = new ButtonType("Confirm Payment (credit coins)", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(confirm, cancel);

        alert.showAndWait().ifPresent(choice -> {
            if (choice == confirm) {
                UserData.coins += coinsToGive;
                updateCoinsInFile(UserData.username, UserData.coins);
                refreshCoinsLabel();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Credited " + coinsToGive + " coins to " + username + ".");
            }
        });
    }

    private void onBuyImage(String imageId, int price) {
        String username = (UserData.username != null && !UserData.username.isEmpty()) ? UserData.username : "Unknown";

        if (UserData.coins < price) {
            showAlert(Alert.AlertType.WARNING, "Not enough coins", "You need " + price + " coins to buy " + imageId + ".\nYour coins: " + UserData.coins);
            return;
        }

        UserData.coins -= price;
        updateCoinsInFile(UserData.username, UserData.coins);
        refreshCoinsLabel();

        try {
            String subject = "Purchase request: " + imageId;
            String body = "Hello,\n\nI would like to purchase " + imageId + ".\n\n"
                    + "Buyer: " + username + "\n"
                    + "Paid with in-game coins: " + price + "\n\nThanks,\n" + username;

            String mailto = String.format("mailto:%s?subject=%s&body=%s",
                    uriEncode(sellerEmail),
                    uriEncode(subject),
                    uriEncode(body));

            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.MAIL)) {
                Desktop.getDesktop().mail(new URI(mailto));
                showAlert(Alert.AlertType.INFORMATION, "Purchase initiated", "Mail client opened to notify the seller.\nCoins deducted: " + price);
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Mail Info",
                        "Send manually to: " + sellerEmail + "\nSubject: " + subject + "\n\n" + body);
            }
        } catch (Exception ex) {
            UserData.coins += price;
            updateCoinsInFile(UserData.username, UserData.coins);
            refreshCoinsLabel();
            showAlert(Alert.AlertType.ERROR, "Error", "Mail client failed: " + ex.getMessage() + "\nCoins refunded.");
        }
    }

    private void updateCoinsInFile(String username, int newCoins) {
        Path filePath = Paths.get("users.txt");
        try {
            List<String> lines = Files.readAllLines(filePath);
            for (int i = 0; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(":");
                if (parts.length == 6 && parts[0].equals(username)) {
                    parts[2] = String.valueOf(newCoins); // coins index = 2
                    lines.set(i, String.join(":", parts));
                    break;
                }
            }
            Files.write(filePath, lines);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "File Error", "Unable to update coins in users.txt\n" + e.getMessage());
        }
    }

    private String uriEncode(String s) {
        return s.replace(" ", "%20").replace("\n", "%0A").replace("&", "%26").replace("?", "%3F");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.showAndWait();
    }

    @FXML
    private void onBack() {
        Main.switchScene("home.fxml");
    }
}
