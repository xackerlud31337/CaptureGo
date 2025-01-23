package Network.Client;

import Game.Cell;
import Game.CaptureGoBoard;
import Game.Player;
import Players.ComplexAI;
import Players.GoAI;
import Players.NaiveAI;
import Players.SafeAI;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * A TUI-based client that uses composition (NOT extending CaptureGoClient).
 * - Logs in, queues, and lets you do "list"/"queue"/"quit" commands.
 * - On a separate thread, automatically makes AI moves when it becomes your turn.
 */
public class CaptureGoAiClient {

    private final CaptureGoClient client;
    private final GoAI aiPlayer;
    private final int BOARD_SIZE = 7;

    //Used for checking if it's our turn
    private Thread aiThread;
    private volatile boolean stopAiThread = false;

    public CaptureGoAiClient(String address, int port, GoAI ai, String username) throws IOException {
        this.client = new CaptureGoClient(address, port);
        this.aiPlayer = ai;
        client.setOwnPlayer(new Player(username, null));
    }


    public void start() {
        System.out.println("Logging in with username: " + client.getOwnPlayer().getName());
        client.login(client.getOwnPlayer().getName());

        // Wait for successful login
        while (!client.getLoggedIn()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                System.out.println("Interrupted while waiting for login.");
                return;
            }
        }
        System.out.println("Logged in successfully!");

        System.out.println("Joining queue...");
        client.sendQueue();

        // Start AI polling loop in a background thread
        aiThread = new Thread(this::runAiLoop, "AiMoveThread");
        aiThread.start();

        // Now the main TUI command loop
        commandLoop();
    }

    /**
     * The main blocking loop for user commands: list, queue, help, quit.
     */
    private void commandLoop() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Type 'help' for commands.");
        while (true) {
            System.out.print("> ");
            String cmd = scanner.nextLine().trim().toLowerCase();
            switch (cmd) {
                case "quit" -> {
                    System.out.println("Shutting down AI client...");
                    stopAiThread = true; // signal AI thread to stop
                    client.close();      // close underlying connection
                    return;              // exit the command loop
                }
                case "list" -> client.sendList();
                case "queue" -> client.sendQueue();
                case "help" -> printHelp();
                default -> System.out.println("Unknown command. Type 'help' for a list of commands.");
            }
        }
    }

    /**
     * Prints available commands.
     */
    private void printHelp() {
        System.out.println("Available commands:");
        System.out.println("  list   - Request a list of online players");
        System.out.println("  queue  - Join matchmaking queue");
        System.out.println("  help   - Show this help message");
        System.out.println("  quit   - Disconnect and exit");
        System.out.println("\n[AI] Moves are performed automatically on your turn!");
    }

    /**
     * Background thread that periodically checks if it's our turn in a game.
     * If so, we ask the AI for a move and send it to the server.
     */
    private void runAiLoop() {
        while (!stopAiThread) {
            try {
                // If inGame = true and it's our turn, do AI move
                if (client.inGame()) {
                    String myStone = client.getOwnPlayer().getStone();
                    if (myStone != null) {
                        if (isMyTurn()) {
                            doAiMove();
                        }
                    }
                }
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                // If interrupted, we'll just stop
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    /**
     * Determines if our local player is the current player in the session.
     * We do this by reflecting into CaptureGoClient's private "game" field
     * and calling getCurrentPlayer().
     */
    private boolean isMyTurn() {
        ClientGameSession session = getCurrentSession();
        if (session == null) return false;
        if (session.getCurrentPlayer().getName().equals(aiPlayer.getName())) {
            System.out.println(session.getCurrentPlayer().getName()+ "'s turn" );
            return true;
        }else{
            return false;
        }
    }

    /**
     * Actually call the AI to pick a move and send it to the server.
     */
    private void doAiMove() {
        ClientGameSession session = getCurrentSession();
        if (session == null) {
            System.out.println("[AI] No active game session found.");
            return;
        }

        // The AI's chooseMove expects a CaptureGoBoard. We can just use the session's board:
        CaptureGoBoard board = session.getBoard();

        // The AI returns a Cell with row/col
        Cell chosenCell = aiPlayer.chooseMove(board);
        if (chosenCell == null) {
            System.out.println("[AI] No valid moves found. Skipping turn.");
            return;
        }

        // If your AI or board uses different row/col scaling, adjust accordingly
        int row = chosenCell.getRow();
        int col = chosenCell.getCol();
        int moveIndex = row * BOARD_SIZE + col;

        System.out.printf("[AI] Sending move for row=%d, col=%d => index=%d%n", row, col, moveIndex);
        client.sendMove(moveIndex);
    }

    /**
     * Reflection-based getter to read the private `game` field from `CaptureGoClient`.
     */
    private ClientGameSession getCurrentSession() {
        try {
            Field f = CaptureGoClient.class.getDeclaredField("game");
            f.setAccessible(true);
            return (ClientGameSession) f.get(client);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Main entry point: ask user for server info, AI type, username, and run the TUI.
     */
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // Ask for server address
        System.out.print("Server address (e.g. localhost): ");
        String address = sc.nextLine().trim();

        // Ask for port
        int port = -1;
        while (port < 0 || port > 65535) {
            System.out.print("Server port (e.g., 12345): ");
            try {
                port = Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid port. Try again.");
            }
        }

        // Ask which AI
        System.out.print("Which AI? (naive / safe / complex): ");
        String aiType = sc.nextLine().trim().toLowerCase();

        // Ask for username
        System.out.print("Enter AI's username: ");
        String aiName = sc.nextLine().trim();

        // Create the chosen AI
        GoAI aiPlayer;
        switch (aiType) {
            case "safe" -> aiPlayer = new SafeAI(aiName, Cell.BLUE_O);
            case "complex" -> aiPlayer = new ComplexAI(aiName, Cell.BLUE_O, 2000, 1.4);
            default -> aiPlayer = new NaiveAI(aiName, Cell.BLUE_O);
        }

        // Create and start the TUI + AI
        try {
            CaptureGoAiClient aiTui = new CaptureGoAiClient(address, port, aiPlayer, aiName);
            aiTui.start();  // runs until user types 'quit'
        } catch (IOException e) {
            System.out.println("Failed to connect: " + e.getMessage());
        }
    }
}
