package Network.Client;

import Game.Player;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * A text-based client UI that relies on the CaptureGoClient and
 * the server’s logic to play Capture Go.
 */
public class CaptureGoClientTUI {

    private CaptureGoClient client;

    /**
     * Constructor creates a client that connects to the server.
     */
    public CaptureGoClientTUI(String host, int port) throws IOException {
        client = new CaptureGoClient(host, port);
    }

    /**
     * Simple main to run the TUI directly.
     */
    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);

            System.out.print("Enter server address: ");
            String host = scanner.nextLine().trim();

            System.out.print("Enter server port: ");
            int port = Integer.parseInt(scanner.nextLine().trim());

            CaptureGoClientTUI tui = new CaptureGoClientTUI(host, port);
            tui.initialize();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Initialize the client: prompt user for name, wait until logged in, then handle commands.
     */
    private void initialize() throws IOException {
        Scanner input = new Scanner(System.in);

        displayWelcomeMessage();

        System.out.print("Enter your name: ");
        String name = input.nextLine().trim();

        // Log in to the server (this sends HELLO and LOGIN messages via client)
        System.out.println("Connecting to the server...");
        client.login(name);

        // Wait until the server acknowledges your login
        while (!client.getLoggedIn()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        System.out.println("Capture Go server connected as '" + name + "'!");

        // Now run the command loop until user types "quit"
        commandLoop(input);
    }

    /**
     * A simple text-based command loop. Type 'quit' to exit.
     */
    private void commandLoop(Scanner input) {
        System.out.println("Available commands: list, queue, move <n>, quit");
        System.out.println("   - list : show who is online");
        System.out.println("   - queue: toggle joining/leaving the matchmaking queue");
        System.out.println("   - move <n>: attempt a move at index n (0..(boardSize^2-1))");
        System.out.println("   - quit : exit the client");

        while (true) {
            System.out.print("> ");
            String line = input.nextLine().trim();
            if (line.equalsIgnoreCase("quit")) {
                // Graceful shutdown
                System.out.println("Closing connection...");
                client.close();
                break;
            }

            // Parse command
            String[] tokens = line.split("\\s+");
            String cmd = tokens[0].toLowerCase();

            switch (cmd) {
                case "list" -> client.sendList();

                case "queue" -> {
                    if (!client.inGame()) {
                        client.sendQueue();
                    } else {
                        System.out.println("You're already in a game!");
                    }
                }

                case "move" -> {
                    if (!client.inGame()) {
                        System.out.println("You are not in a game! Type 'queue' to join the queue.");
                        break;
                    }
                    // Expect 'move <integer>'
                    if (tokens.length < 2) {
                        System.out.println("Usage: move <index>");
                        break;
                    }
                    try {
                        int moveIndex = Integer.parseInt(tokens[1]);
                        if (moveIndex < 0) {
                            System.out.println("Move index must be >= 0.");
                            break;
                        }
                        client.sendMove(moveIndex);
                    } catch (NumberFormatException e) {
                        System.out.println("Usage: move <integer>");
                    }
                }

                default -> System.out.println("Unknown command: " + cmd + ". Type 'list','queue','move','quit'.");
            }
        }
    }

    /**
     * Just a welcome banner to appear once on startup.
     */
    private void displayWelcomeMessage() {
        System.out.println("===================================");
        System.out.println("       Welcome to Capture Go");
        System.out.println("===================================");
        System.out.println("This client relies on the server’s logic.");
        System.out.println("You will log in, join the queue, and the server will pair you for a game.");
        System.out.println("When you're in a game, type 'move <n>' to place a stone in cell index <n>.");
        System.out.println("===================================");
    }

    /**
     * If needed, you can call this from your code to start a local game view
     * after receiving a NEWGAME. But since the server controls logic,
     * you don't really need to do it locally.
     *
     * You already have 'receiveGameStart' in CaptureGoClient that returns
     * a list of Players. If you wanted the TUI to do something with them,
     * you could do so here. For example:
     */
    public void startGame(List<Player> players) {
        System.out.println("Server started a game between: "
                                   + players.get(0).getName() + " and "
                                   + players.get(1).getName());
        // (No local game logic needed, as the server handles it.)
    }
}
