package com.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class QuizController {

    @FXML private Button backButton;
    @FXML private VBox levelBox;
    @FXML private VBox questionBox;
    @FXML private Label questionLabel;
    @FXML private RadioButton option1;
    @FXML private RadioButton option2;
    @FXML private RadioButton option3;
    @FXML private RadioButton option4;
    @FXML private Button nextButton;
    @FXML private Label resultLabel;

    private ToggleGroup optionsGroup; // toggle group for radio buttons

    private List<Question> questions;
    private int currentQuestionIndex = 0;
    private int score = 0;

    private static final int REWARD_COINS = 1000;

    // === Question model ===
    static class Question {
        String question;
        String[] options;
        int correctIndex;

        Question(String question, String[] options, int correctIndex) {
            this.question = question;
            this.options = options;
            this.correctIndex = correctIndex;
        }
    }

    @FXML
    public void initialize() {
        // Create toggle group in code
        optionsGroup = new ToggleGroup();
        option1.setToggleGroup(optionsGroup);
        option2.setToggleGroup(optionsGroup);
        option3.setToggleGroup(optionsGroup);
        option4.setToggleGroup(optionsGroup);

        // Hide quiz/result initially
        questionBox.setVisible(false);
        resultLabel.setVisible(false);
    }

    @FXML
    private void onBackClick() {
        Main.switchScene("home.fxml");
    }

    // === Level buttons ===
    @FXML
    private void onLevel1Click() {
        loadQuestionsForLevel(1);
        levelBox.setVisible(false);
        questionBox.setVisible(true);
        showQuestion();
    }

    @FXML
    private void onLevel2Click() {
        loadQuestionsForLevel(2);
        levelBox.setVisible(false);
        questionBox.setVisible(true);
        showQuestion();
    }

    @FXML
    private void onLevel3Click() {
        loadQuestionsForLevel(3);
        levelBox.setVisible(false);
        questionBox.setVisible(true);
        showQuestion();
    }

    @FXML
    private void onLevel4Click() {
        loadQuestionsForLevel(4);
        levelBox.setVisible(false);
        questionBox.setVisible(true);
        showQuestion();
    }


    // === Question banks ===
    private void loadQuestionsForLevel(int level) {
        questions = new ArrayList<>();

        // === Level 1 Questions ===
        if (level == 1) {
            questions.add(new Question("Which planet is known as the Blue Planet?", new String[]{"Earth", "Mars", "Jupiter", "Saturn"}, 0));
            questions.add(new Question("Which planet is known as the Red Planet?", new String[]{"Earth", "Mars", "Jupiter", "Saturn"}, 1));
            questions.add(new Question("Which planet has the longest day?", new String[]{"Earth", "Mars", "Venus", "Mercury"}, 2));
            questions.add(new Question("Which planet has a surface with the largest volcano?", new String[]{"Earth", "Mars", "Jupiter", "Saturn"}, 1));
            questions.add(new Question("Which planet is closer to the Sun?", new String[]{"Earth", "Mars", "Venus", "Mercury"}, 3));
            questions.add(new Question("What is the average temperature on Mars?", new String[]{"-60°C", "-10°C", "10°C", "30°C"}, 0));
            questions.add(new Question("Which planet has the strongest magnetic field?", new String[]{"Earth", "Mars", "Jupiter", "Saturn"}, 0));
            questions.add(new Question("Which planet has a moon named Phobos?", new String[]{"Earth", "Mars", "Jupiter", "Saturn"}, 1));
            questions.add(new Question("Which planet is known for its blue color due to water and atmosphere?", new String[]{"Earth", "Mars", "Jupiter", "Neptune"}, 0));
            questions.add(new Question("Which planet has a red hue due to iron oxide on its surface?", new String[]{"Earth", "Mars", "Venus", "Saturn"}, 1));

        }

        // === Level 2 Questions ===
        else if (level == 2) {
            questions.add(new Question("Which planet is known as the 'Evening Star'?", new String[]{"Venus", "Mars", "Jupiter", "Saturn"}, 0));
            questions.add(new Question("What is the average surface temperature on Venus?", new String[]{"200°C", "400°C", "600°C", "800°C"}, 1));
            questions.add(new Question("Which planet has the most powerful storms in the Solar System?", new String[]{"Earth", "Venus", "Jupiter", "Saturn"}, 2));
            questions.add(new Question("Where is the Asteroid Belt located?", new String[]{"Between Mars and Jupiter", "Between Venus and Earth", "Between Jupiter and Saturn", "Between Earth and Mars"}, 0));
            questions.add(new Question("What is the primary composition of the Asteroid Belt?", new String[]{"Ice", "Gas", "Rocks and metals", "Plasma"}, 2));
            questions.add(new Question("Approximately what percentage of the Solar System’s asteroids are in the Asteroid Belt?", new String[]{"30%", "50%", "70%", "90%"}, 3));
            questions.add(new Question("Which planet is the largest in the Solar System?", new String[]{"Venus", "Earth", "Jupiter", "Saturn"}, 2));
            questions.add(new Question("Which planet has the Great Red Spot?", new String[]{"Venus", "Earth", "Jupiter", "Mars"}, 2));
            questions.add(new Question("Which planet has a thick atmosphere made mostly of carbon dioxide?", new String[]{"Earth", "Venus", "Mars", "Jupiter"}, 1));
            questions.add(new Question("Which planet has the most moons?", new String[]{"Earth", "Mars", "Jupiter", "Venus"}, 2));

        }

        // === Level 3 Questions ===
        else if (level == 3) {
            questions.add(new Question("Which planet is famous for its ring system?", new String[]{"Earth", "Saturn", "Mars", "Jupiter"}, 1));
            questions.add(new Question("Where is the International Space Station (ISS) located?", new String[]{"In Earth's orbit", "On the Moon", "Between Earth and Mars", "On Mars"}, 0));
            questions.add(new Question("What is a black hole?", new String[]{"A giant star", "A region of space with strong gravitational pull", "A type of galaxy", "A massive planet"}, 1));
            questions.add(new Question("What is a nebula?", new String[]{"A cloud of gas and dust in space", "A dying black hole", "A galaxy cluster", "A comet's tail"}, 0));
            questions.add(new Question("What is a wormhole?(Einstein Rogen Bridge)", new String[]{"A type of black hole", "A tunnel connecting two distant parts of space-time", "A fast-moving asteroid", "A giant star"}, 1));
            questions.add(new Question("What is antimatter?", new String[]{"Matter with negative charge", "The opposite of matter, with opposite properties", "A type of energy", "The matter in black holes"}, 1));
            questions.add(new Question("Which planet has the largest moon system?", new String[]{"Earth", "Saturn", "Jupiter", "Mars"}, 2));
            questions.add(new Question("Which of these is not a type of black hole?", new String[]{"Stellar", "Supermassive", "Intermediate", "Quantum"}, 3));
            questions.add(new Question("Which nebula is famous for its horse-like shape?", new String[]{"Orion Nebula", "Horsehead Nebula", "Crab Nebula", "Eagle Nebula"}, 1));
            questions.add(new Question("How is a nebula born?", new String[]{"From the explosion of a dying star", "From collisions of asteroids", "From black hole evaporation", "From the fusion of galaxies"}, 0));
            questions.add(new Question("What would happen if you fell into a black hole?", new String[]{"You would be crushed instantly", "Time would slow down for you", "You would pass through a wormhole", "You would escape the event horizon"}, 1));
        }

        // === Level 4: Kardashev Scale ===
        else if (level == 4) {
            questions.add(new Question("Who proposed the Kardashev Scale?", new String[]{"Carl Sagan", "Stephen Hawking", "Nikolai Kardashev", "Albert Einstein"}, 2));
            questions.add(new Question("What does the Kardashev Scale measure?", new String[]{"A planet's temperature", "A civilization's energy usage", "A galaxy's age", "A star's brightness"}, 1));
            questions.add(new Question("How many main types does the original Kardashev Scale have?", new String[]{"2", "3", "4", "5"}, 1));
            questions.add(new Question("What does a Type I civilization use?", new String[]{"Energy of its planet", "Energy of its star", "Energy of its galaxy", "Energy of the universe"}, 0));
            questions.add(new Question("What does a Type II civilization use?", new String[]{"Energy of its planet", "Energy of its star", "Energy of its galaxy", "Energy of multiple galaxies"}, 1));
            questions.add(new Question("What does a Type III civilization control?", new String[]{"Energy of its planet", "Energy of its galaxy", "Energy of its solar system", "Energy of the universe"}, 1));
            questions.add(new Question("What is humanity's approximate level on the Kardashev Scale?", new String[]{"Type 0.1", "Type 0.5", "Type 0.73", "Type I"}, 2));
            questions.add(new Question("Which of these could represent a Type II civilization’s technology?", new String[]{"Wind turbines", "Nuclear plants", "Dyson sphere", "Hydroelectric dam"}, 2));
            questions.add(new Question("What would it mean if a civilization reached Type I?", new String[]{"It can control planetary energy completely", "It can travel faster than light", "It can destroy black holes", "It can manipulate gravity"}, 0));
            questions.add(new Question("What is humanity still lacking to become Type I?", new String[]{"Planetary unity and efficient global energy use", "Knowledge of galaxies", "Understanding of dark matter", "Interstellar travel"}, 0));
        }



        score = 0;
        currentQuestionIndex = 0;
    }

    // === Show current question ===
    private void showQuestion() {
        if (currentQuestionIndex < questions.size()) {
            Question q = questions.get(currentQuestionIndex);
            questionLabel.setText((currentQuestionIndex + 1) + ". " + q.question);
            option1.setText(q.options[0]);
            option2.setText(q.options[1]);
            option3.setText(q.options[2]);
            option4.setText(q.options[3]);
            optionsGroup.selectToggle(null); // clear selection
        } else {
            showResult();
        }
    }

    // === Handle next button ===
    @FXML
    private void onNextClick() {
        RadioButton selected = (RadioButton) optionsGroup.getSelectedToggle();
        if (selected != null) {
            int selectedIndex = -1;
            if (selected == option1) selectedIndex = 0;
            else if (selected == option2) selectedIndex = 1;
            else if (selected == option3) selectedIndex = 2;
            else if (selected == option4) selectedIndex = 3;

            if (selectedIndex == questions.get(currentQuestionIndex).correctIndex) {
                score++;
            }
        }

        currentQuestionIndex++;
        showQuestion();
    }

    // === Show result ===
    private void showResult() {
        questionBox.setVisible(false);
        resultLabel.setVisible(true);
        if (score >= 8) {
            resultLabel.setText("🎉 You Passed! Score: " + score + "/10");

            // Use the logged-in username from UserData
            String loggedInUsername = (UserData.username == null) ? "" : UserData.username.trim();

            if (loggedInUsername.isEmpty()) {
                System.out.println("⚠️ No logged-in username found (UserData.username is empty). Coins not added.");
                // optional: inform user via GUI
                Alert a = new Alert(Alert.AlertType.ERROR, "Internal error: no logged-in user found. Coins were not added.");
                a.showAndWait();
            } else {
                boolean success = updateCoinsForUser(loggedInUsername, REWARD_COINS);
                if (success) {
                    // Update in-memory value too
                    UserData.coins = UserData.coins + REWARD_COINS;
                    Alert a = new Alert(Alert.AlertType.INFORMATION, "You earned " + REWARD_COINS + " coins!");
                    a.showAndWait();
                } else {
                    Alert a = new Alert(Alert.AlertType.ERROR, "Could not update coins. Check the console for details.");
                    a.showAndWait();
                }
            }

        } else {
            resultLabel.setText("❌ You Failed! Score: " + score + "/10");
        }
    }

    private boolean updateCoinsForUser(String username, int coinsToAdd) {
        File file = new File("users.txt");
        List<String> lines = new ArrayList<>();
        boolean userFound = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");

                System.out.println("Checking line: " + line);

                if (parts.length >= 6) {
                    if (parts[0].trim().equals(username.trim())) {
                        int coins = Integer.parseInt(parts[2].trim()) + coinsToAdd;
                        line = parts[0].trim() + ":" +
                                parts[1].trim() + ":" +
                                coins + ":" +
                                parts[3].trim() + ":" +
                                parts[4].trim() + ":" +
                                parts[5].trim();
                        userFound = true;
                        System.out.println("✅ Updated coins for user " + username + " to " + coins);
                    }
                }
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false; // failed
        }

        if (userFound) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                for (String updatedLine : lines) {
                    writer.write(updatedLine);
                    writer.newLine();
                }
                System.out.println("✅ File successfully updated.");
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            System.out.println("❌ User not found in file: " + username);
            return false;
        }
    }


}

