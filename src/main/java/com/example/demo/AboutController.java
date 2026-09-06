package com.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.ArrayList;
import java.util.List;

public class AboutController {

    @FXML private Button backButton;
    @FXML private Button leftButton;
    @FXML private Button rightButton;
    @FXML private ImageView imageView;

    private List<Image> images;
    private int currentIndex = 0;

    @FXML
    public void initialize() {
        // Load images from resources
        images = new ArrayList<>();
        images.add(new Image(getClass().getResource("one.jpg").toExternalForm()));
        images.add(new Image(getClass().getResource("two.jpg").toExternalForm()));
        images.add(new Image(getClass().getResource("three.jpg").toExternalForm()));
        images.add(new Image(getClass().getResource("four.jpg").toExternalForm()));
        images.add(new Image(getClass().getResource("six.jpg").toExternalForm()));
        images.add(new Image(getClass().getResource("seven.jpg").toExternalForm()));
        images.add(new Image(getClass().getResource("eight.jpg").toExternalForm()));
        images.add(new Image(getClass().getResource("nine.jpg").toExternalForm()));
        images.add(new Image(getClass().getResource("ten.jpg").toExternalForm()));
        //images.add(new Image(getClass().getResource("about9.jpg").toExternalForm()));


        // Show first image
        imageView.setImage(images.get(currentIndex));

        // Button actions
        leftButton.setOnAction(e -> showPreviousImage());
        rightButton.setOnAction(e -> showNextImage());
    }

    private void showPreviousImage() {
        currentIndex = (currentIndex - 1 + images.size()) % images.size();
        imageView.setImage(images.get(currentIndex));
    }

    private void showNextImage() {
        currentIndex = (currentIndex + 1) % images.size();
        imageView.setImage(images.get(currentIndex));
    }


    @FXML
    private void onBackClick() {
        Main.switchScene("home.fxml");
    }
}
