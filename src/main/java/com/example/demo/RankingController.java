package com.example.demo;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.animation.ParallelTransition;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class RankingController {

    @FXML private ComboBox<String> filterCombo;
    @FXML private TableView<UserEntry> rankingTable;
    @FXML private Button backButton;

    @FXML
    public void initialize() {
        filterCombo.setItems(FXCollections.observableArrayList("By Level", "By Coins", "By W/L Ratio"));
        filterCombo.getSelectionModel().selectFirst();

        // Use a constrained resize policy so there's no visible "filler" extra column
        rankingTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        loadRanking();
        filterCombo.setOnAction(e -> loadRanking());
        backButton.setOnAction(e -> Main.switchScene("home.fxml"));
    }

    private void loadRanking() {
        ObservableList<UserEntry> data = FXCollections.observableArrayList();
        String filter = filterCombo.getValue();

        // read users.txt
        try (BufferedReader br = new BufferedReader(new FileReader("users.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length >= 6) {
                    String name = parts[0];
                    int coins = Integer.parseInt(parts[2]);
                    int level = Integer.parseInt(parts[3]);
                    int wins = Integer.parseInt(parts[4]);
                    int losses = Integer.parseInt(parts[5]);
                    double wl = (losses == 0) ? (wins > 0 ? wins : 0) : (double) wins / losses;
                    data.add(new UserEntry(name, level, coins, wl));
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // clear & create columns dynamically (only the columns we want)
        rankingTable.getColumns().clear();

        TableColumn<UserEntry, String> nameCol = new TableColumn<>("PLAYER");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        nameCol.setStyle("-fx-alignment: CENTER-LEFT;");    // apply alignment for header & cells
        nameCol.setPrefWidth(300);

        rankingTable.getColumns().add(nameCol);

        if ("By Coins".equals(filter)) {
            TableColumn<UserEntry, Integer> coinCol = new TableColumn<>("COINS");
            coinCol.setCellValueFactory(new PropertyValueFactory<>("coins"));
            coinCol.setStyle("-fx-alignment: CENTER;"); // center numeric
            coinCol.setPrefWidth(180);
            rankingTable.getColumns().add(coinCol);

            data.sort(Comparator.comparingInt(UserEntry::getCoins).reversed());
        } else if ("By W/L Ratio".equals(filter)) {
            TableColumn<UserEntry, Double> wlCol = new TableColumn<>("W/L RATIO");
            wlCol.setCellValueFactory(new PropertyValueFactory<>("wlRatio"));
            wlCol.setStyle("-fx-alignment: CENTER;"); // center numeric
            wlCol.setPrefWidth(180);

            // friendly formatting (2 decimals)
            wlCol.setCellFactory(col -> new TableCell<UserEntry, Double>() {
                @Override
                protected void updateItem(Double value, boolean empty) {
                    super.updateItem(value, empty);
                    if (empty || value == null) {
                        setText(null);
                    } else {
                        setText(String.format("%.2f", value));
                    }
                }
            });

            rankingTable.getColumns().add(wlCol);
            data.sort(Comparator.comparingDouble(UserEntry::getWlRatio).reversed());
        } else {
            TableColumn<UserEntry, Integer> levelCol = new TableColumn<>("LEVEL");
            levelCol.setCellValueFactory(new PropertyValueFactory<>("level"));
            levelCol.setStyle("-fx-alignment: CENTER;"); // center numeric
            levelCol.setPrefWidth(180);
            rankingTable.getColumns().add(levelCol);

            data.sort(Comparator.comparingInt(UserEntry::getLevel).reversed());
        }

        rankingTable.setItems(data);

        // small fade + slide animation (gentle)
        rankingTable.setOpacity(0);
        rankingTable.setTranslateY(12);

        FadeTransition fade = new FadeTransition(Duration.millis(480), rankingTable);
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition slide = new TranslateTransition(Duration.millis(480), rankingTable);
        slide.setFromY(12);
        slide.setToY(0);

        ParallelTransition anim = new ParallelTransition(fade, slide);
        anim.play();
    }

    public static class UserEntry {
        private final String username;
        private final int level;
        private final int coins;
        private final double wlRatio;

        public UserEntry(String username, int level, int coins, double wlRatio) {
            this.username = username;
            this.level = level;
            this.coins = coins;
            this.wlRatio = wlRatio;
        }

        public String getUsername() { return username; }
        public int getLevel() { return level; }
        public int getCoins() { return coins; }
        public double getWlRatio() { return wlRatio; }
    }
}
