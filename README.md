<div align="center">

# ✦ Asterra Quest

### A JavaFX-based 2D space-battle adventure

<img src="src/main/resources/com/example/demo/welcome1.gif" alt="Asterra Quest animated space scene" width="760" />

<p>
  <img src="https://img.shields.io/badge/Java-17%2B-ff6b35?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 17 or later" />
  <img src="https://img.shields.io/badge/JavaFX-17-6db33f?style=for-the-badge" alt="JavaFX 17" />
  <img src="https://img.shields.io/badge/Genre-Space%20Adventure-7b61ff?style=for-the-badge" alt="Space adventure game" />
</p>

*Navigate the cosmos, battle hostile ships, and build your legend among the stars.*

</div>

---

## Mission Briefing

**Asterra Quest** is a desktop space adventure built with JavaFX. It combines a cinematic, story-led interface with playable combat, cosmic exploration, social features, and progression systems—all inside one sci-fi universe.

## ✨ Highlights

- **Space combat** — take on single-player and multiplayer shooting challenges.
- **Solar-system exploration** — travel through planetary missions and space-themed levels.
- **Story-driven journey** — move from splash screen to story, login, and a full game hub.
- **Player progression** — profiles, rankings, a store, and game statistics.
- **Interactive extras** — space quizzes, global chat, and an in-game chatbot.
- **Atmospheric presentation** — animated scenes, spacecraft artwork, sound effects, and background music.

## 🚀 Run the Game

### Requirements

- Java Development Kit (JDK) **17 or newer**
- An IDE with JavaFX support, such as IntelliJ IDEA

### Start

1. Clone this repository and open it as a Maven project in your IDE.
2. Make sure your project SDK is set to JDK 17 or newer.
3. Navigate to:

   ```text
   src/main/java/com/example/demo/Main.java
   ```

4. Run the `main` method in `Main.java`.

> `Main.java` is the game entry point. It opens the splash screen and launches the Asterra Quest experience.

## 🔐 Optional Chatbot Setup

The chatbot needs an OpenRouter API key. The key is intentionally not stored in this repository.

Set the `OPENROUTER_API_KEY` environment variable before launching the game:

```text
OPENROUTER_API_KEY=your-openrouter-api-key
```

You can use [`.env.example`](.env.example) as a reference. Keep actual keys private.

## 🗂️ Project Structure

```text
src/
├── main/
│   ├── java/com/example/demo/     # JavaFX application and controllers
│   └── resources/com/example/demo/# FXML views, styles, audio, and game art
├── level1.txt                    # Level data
├── map.txt                       # Map data
└── pom.xml                       # Maven dependencies and build configuration
```

## 🎮 Included Experiences

| Area | What you can do |
| --- | --- |
| Combat | Launch into single-player and multiplayer space battles. |
| Exploration | Discover solar-system missions and cosmic scenes. |
| Progression | Review your profile, rankings, achievements, and store items. |
| Community | Join world chat and use the space-themed assistant. |
| Learning | Test your astronomy knowledge through the quiz mode. |

## 🛡️ Local Data

`users.txt` is deliberately excluded from GitHub because it is local player data. Copy `users.example.txt` to `users.txt` if you need a starting template for local development.

---

<div align="center">
  Built for explorers who look up and wonder what is waiting beyond the next star. ✨
</div>
