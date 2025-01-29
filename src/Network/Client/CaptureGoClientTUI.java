package Network.Client;

import Game.Player;
import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Random;
import java.util.Scanner;

public class CaptureGoClientTUI {

    private CaptureGoClient client;
    private boolean tipsEnabled = false;

    private static final String[] TIPS = {
            "Try to anticipate the opponent’s next move.",
            "Look for stones (yours or the opponent’s) with only one or two liberties left.",
            "Avoid placing stones where they can be easily captured in one move (self-atari).",
            "Don’t forget to keep track of the entire board, not just the last move.",
            "Capturing a single group decides the game in Capture Go, so remain vigilant."
    };

    /**
     * Constructor initializes the TUI client and connects to the server.
     */
    public CaptureGoClientTUI(String address, int port, String username) throws IOException {
        client = new CaptureGoClient(address, port);
        client.setOwnPlayer(new Player(username, null));
    }

    /**
     * Starts the TUI client loop, allowing users to input commands.
     */
    public void start() {
        Scanner scanner = new Scanner(System.in);

        // Log in to the server
        System.out.println("Logging in...");
        client.login(client.getOwnPlayer().getName());

        // Wait until logged in
        while (!client.getLoggedIn()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Interrupted while waiting for login.");
                return;
            }
        }
        System.out.println("Logged in successfully!");

        // Main command loop
        System.out.println("Type 'help' to see available commands.");
        while (true) {
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("quit")) {
                System.out.println("Disconnecting...");
                client.close();
                break;
            }

            handleCommand(input);
        }
    }

    /**
     * Handles commands input by the user.
     */
    private void handleCommand(String input) {
        String[] tokens = input.split("\\s+");
        String command = tokens[0].toLowerCase();

        try {
            switch (command) {
                case "list" -> client.sendList();
                case "queue" -> client.sendQueue();
                case "board" -> client.showBoard();
                case "move" -> handleMoveCommand(tokens);
                case "rules" -> displayRules();
                case "tips" -> handleTipsCommand(tokens);
                case "help" -> displayHelp();
                default -> System.out.println("Unknown command. Type 'help' for available commands.");
            }
        } catch (Exception e) {
            System.out.println("Error processing command: " + e.getMessage());
        }
    }

    /**
     * Handles the "move" command.
     */
    private void handleMoveCommand(String[] tokens) {
        if (!client.inGame()) {
            System.out.println("You are not currently in a game. Type 'queue' to join.");
            return;
        }
        if (tokens.length < 2) {
            System.out.println("Usage: move <index>");
            return;
        }
        try {
            int move = Integer.parseInt(tokens[1]);
            if (move < 0 || move > 48) {
                System.out.println("Invalid move. Please enter a value between 0 and 48.");
                return;
            }
            client.sendMove(move);

            if (tipsEnabled) {
                Random random = new Random();
                if (random.nextInt(100) < 50) { // 50% chance to display a tip
                    displayTip();
                }
            }

        } catch (NumberFormatException e) {
            System.out.println("Invalid move format. Please enter an integer.");
        }
    }

    /**
     * Displays available commands.
     */
    private void displayHelp() {
        System.out.println("Available commands:");
        System.out.println("  list          - List all online players.");
        System.out.println("  queue         - Join the matchmaking queue.");
        System.out.println("  board         - Show current board state.");
        System.out.println("  move <index>  - Make a move at the specified board index (0-48).");
        System.out.println("  rules         - Show a summary of Capture Go rules.");
        System.out.println("  tips <on/off> - Enable or disable tips during the game.");
        System.out.println("  quit          - Disconnect from the server and exit.");
    }

    /**
     * Displays a short summary of Capture Go rules.
     */
    private void displayRules() {
        System.out.println("=== Capture Go Rules (Summary) ===");
        System.out.println("1. Two players take turns placing stones on the board.");
        System.out.println("2. Each player tries to capture the opponent's stones by surrounding them.");
        System.out.println("3. A capture occurs when a stone or a group of stones has no more adjacent");
        System.out.println("   empty points (in all four directions: up, down, left, right).");
        System.out.println("4. In Capture Go, the primary objective is to capture at least one group of");
        System.out.println("   your opponent's stones. The first capture typically decides the game.");
        System.out.println("5. The board is smaller than standard Go to focus on capturing.");
        System.out.println("6. The game ends immediately when one player makes a capture.");
        System.out.println("===================================");
    }

    /**
     * Turns tips on or off based on user input.
     */
    private void handleTipsCommand(String[] tokens) {
        if (tokens.length < 2) {
            System.out.println("Usage: tips <on/off>");
            return;
        }

        String option = tokens[1].toLowerCase();
        if ("on".equals(option)) {
            tipsEnabled = true;
            System.out.println("Tips are now ON.");
        } else if ("off".equals(option)) {
            tipsEnabled = false;
            System.out.println("Tips are now OFF.");
        } else {
            System.out.println("Invalid option. Usage: tips <on/off>");
        }
    }

    /**
     * Displays a short in-game tip (only shown if tipsEnabled == true).
     */
    private void displayTip() {
        Random random = new Random();
        int randomIndex = random.nextInt(TIPS.length);
        System.out.println("[Tip] " + TIPS[randomIndex]);
    }

    /**
     * Main entry point for the TUI client.
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter server address (e.g., localhost): ");
        String address = scanner.nextLine().trim();
        int port = -1;
        while (port < 0 || port > 65535) {
            System.out.print("Enter server port (e.g., 12345): ");
            try {
                port = Integer.parseInt(scanner.nextLine().trim());
            } catch (InputMismatchException e) {
                System.out.println("Invalid port. Please enter a valid port number.");
                scanner.next();
            } catch (NumberFormatException e) {
                System.out.println("Invalid port. Please enter a valid port number.");
            }
        }

        System.out.print("Enter your username: ");
        String username = scanner.nextLine().trim();

        try {
            CaptureGoClientTUI tuiClient = new CaptureGoClientTUI(address, port, username);
            tuiClient.start();
        } catch (IOException e) {
            System.out.println("Failed to connect to server: " + e.getMessage());
            System.out.println("Try again later.");
            System.out.println("Exiting...");
        }
    }
}
