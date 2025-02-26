[![Logo](img.png)]()

# Capture Go Game: Client-Server System 🎲
(Resit-7) 

![Java](https://img.shields.io/badge/java-v11+-blue.svg)
![License](https://img.shields.io/badge/license-MIT-green)
![Build](https://img.shields.io/badge/build-passing-brightgreen)

The project was to develop the capture go game with working local version of the game and also a server implementation where the players can play with each other. The server implementation includes options for tips, help menu and to add an ai player to the server if the user wants to test his skill.

## Table of Contents
- [Installation 📦](#installation-)
- [Getting Started 🚀](#getting-started-)
- [Gameplay 🎮](#gameplay-)
- [Technical Details 🔍](#technical-details-)
- [Testing Instructions 🧪](#testing-instructions-)
- [Contributing 🤝](#contributing-)
- [License 📄](#license-)
- [Contact 📧](#contact-)

## Installation 📦

**Prerequisites:**
- Java Development Kit (JDK) 11 or higher
- Network connectivity

**Setup:**
1. Clone the repository.
2. Follow directory-specific instructions for server and client.

## Getting Started 🚀

**Server Setup:**
```bash
cd server
java -jar Server.jar  // Input a port number
```

**Client Setup:**
```bash
cd view
java -jar ClientTUI.jar  // Input server IP, port, and username
```

**Ai Setup:**
```bash
cd view
java -jar AiClientTUI.jar  // Input server IP, port, and Ai you wanna play with
```

## Gameplay 🎮

In-game TUI guides the gameplay process.
We also have a tips option that can assist new players (beginners);
We also have a rules command that explains in detail how the game is played so every player even a 
new one will be able to play the game with no problem.

## Technical Details 🔍

Overview of client-server model, network protocols, and AI integration. Includes a section on testing strategies and quality assurance.

## Testing Instructions 🧪

To ensure the reliability and stability of the Game, comprehensive testing is essential. Here are the steps for running the tests:

1. **Unit Tests:**
   - Clone the gitlab repo;
   - Open the repo in in a suitable editor;
   - Run the tests;

2. **Brake Down for the tests:**
   - CaptureGoBoardTest - is the test for the board behaviour.
   - CaptureGoGameTest - is the test for how the game logic works
   - CaptureGoRandomGameTest - is the test that plays a random game with random moves for debugging.
   - CellTest - tests how the cell works for changing the states of the cells in different game scenario.
   - ClientGameSessionTest - tests the session on the local client
   - ClientHandlerTest - tests the way that the clients are handled
   - PlayerTest - tests if the player works as intended
   - ServerImpTest - tests the way that the server is handling the requests

## Contributing 🤝

There are no contributions needed at the moment.

## License 📄

This project is under the MIT License.
## Contact 📧

For queries or assistance: [Ivan](i.gyunderov@student.utwente.nl) and [Ivan.M](i.mandev@student.utwente.nl).