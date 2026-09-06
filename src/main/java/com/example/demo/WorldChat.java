package com.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import java.io.*;
import java.net.Socket;

public class WorldChat {

    @FXML
    private TextArea chatArea;
    @FXML
    private TextField messageField;
    @FXML
    private Button backBtn;

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    @FXML
    public void initialize() {
        try {
            socket = new Socket("localhost", 5000);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);


            writer.println(UserData.username + " joined the chat");

            new Thread(() -> {
                String msg;
                try {
                    while ((msg = reader.readLine()) != null) {
                        String finalMsg = msg;
                        chatArea.appendText(finalMsg + "\n");
                    }
                } catch (IOException e) {
                    chatArea.appendText("⚠ Disconnected from server\n");
                }
            }).start();

        } catch (IOException e) {
            chatArea.appendText("⚠ Could not connect to chat server!\n");
        }
    }

    @FXML
    private void onSendClick() {
        String msg = messageField.getText().trim();
        if (!msg.isEmpty()) {
            writer.println(UserData.username + ": " + msg);
            messageField.clear();
        }
    }

    @FXML
    private void onBackClick() {
        Main.switchScene("home.fxml");
    }
}
