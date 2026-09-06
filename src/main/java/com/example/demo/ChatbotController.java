package com.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class ChatbotController {

    @FXML private TextArea chatArea;
    @FXML private TextField userInput;
    @FXML private Button sendButton;
    @FXML private Button backButton;

    private static final String API_KEY = System.getenv("OPENROUTER_API_KEY");
    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";

    @FXML
    private void initialize() {
        chatArea.setText("Hi I am your captain you can ask me anything about space sciecne , astronomy & astrophysics...\n");
    }

    @FXML
    private void onBackClick() {
        Main.switchScene("home.fxml");
    }


    @FXML
    private void onSendClick() {
        String message = userInput.getText().trim();
        if (message.isEmpty()) return;

        if (API_KEY == null || API_KEY.isBlank()) {
            chatArea.appendText("⚠️ Chatbot is unavailable: set the OPENROUTER_API_KEY environment variable.\n");
            return;
        }

        chatArea.appendText("You: " + message + "\n");
        userInput.clear();

        // Send async request
        new Thread(() -> {
            try {
                String requestBody = """
                        {
                          "model": "meta-llama/llama-4-maverick:free",
                          "messages": [
                            {"role": "user", "content": "%s"}
                          ]
                        }
                        """.formatted(message);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(API_URL))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + API_KEY)
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                        .build();

                HttpClient client = HttpClient.newHttpClient();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                // Extract content from response JSON
                String reply = response.body();
                int idx = reply.indexOf("\"content\":\"");
                String botReply = "⚠️ Error!";
                if (idx != -1) {
                    botReply = reply.substring(idx + 11, reply.indexOf("\"", idx + 11));
                    botReply = botReply.replace("\\n", "\n").replace("\\\"", "\"");
                }

                String finalReply = "Captain: " + botReply + "\n";
                javafx.application.Platform.runLater(() -> chatArea.appendText(finalReply));

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> chatArea.appendText("⚠️ Error: " + e.getMessage() + "\n"));
            }
        }).start();
    }
}
