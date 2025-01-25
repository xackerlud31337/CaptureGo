package Network.Client;

import Game.Player;
import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class CaptureGoClientTUI {

    private CaptureGoClient client;

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
        System.out.println("  move <index>  - Make a move at the specified board index (0-48).");
        System.out.println("  quit          - Disconnect from the server and exit.");
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
            try{
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
