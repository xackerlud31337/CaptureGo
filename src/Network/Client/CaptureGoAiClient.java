//package Network.Client;
//
//import Game.Cell;
//import Game.CaptureGoBoard;
//import Game.Player;
//import Players.GoAI;
//import Players.NaiveAI;
//import Players.SafeAI;
//import Players.ComplexAI;
//
//import java.io.IOException;
//import java.util.List;
//import java.util.Scanner;
//
///**
// * An AI-based client that extends CaptureGoClient.
// * It auto-queues for a game, tracks the board locally,
// * and sends moves (MOVE~n) whenever it is its turn.
// */
//public class CaptureGoAiClient{
//
//    private Player aiPlayer;
//    private CaptureGoBoard board;
//    private int moveCount = 0;
//    private String myStone = null;
//    private int boardSize = 7;
//    CaptureGoClient client;
//
//    /**
//     * Constructor calls super(...) to build the underlying client connection,
//     * then sets up the AI player.
//     */
//    public CaptureGoAiClient(String address, int port, Player aiPlayer) throws IOException {
//        client = new CaptureGoClient(address, port);
//        this.aiPlayer = aiPlayer;
//    }
//
//    /**
//     * We override this so we can determine which stone color we are,
//     * reset local board, and get ready for a new game.
//     */
//    @Override
//    public void receiveGameStart(String message) {
//
//
//        // The original code:
//        //   Player player1 = new Player(parts[1], Cell.BLUE_O);
//        //   Player player2 = new Player(parts[2], Cell.WHITE_O);
//        // so players.get(0) is the BLUE player, players.get(1) is the WHITE player.
//
//        Player pBlue = players.get(0);
//        Player pWhite = players.get(1);
//
//        // Check if the AI's username matches pBlue or pWhite:
//        if (pBlue.getName().equalsIgnoreCase(aiPlayer.getName())) {
//            // We are the BLUE player
//            this.myStone = Cell.BLUE_O;
//            // Ensure our internal Player object uses BLUE_O
//            aiPlayer.setStone(Cell.BLUE_O);
//            System.out.println("AI recognized as the BLUE player. (Blue moves first in your setup!)");
//        } else if (pWhite.getName().equalsIgnoreCase(aiPlayer.getName())) {
//            // We are the WHITE player
//            this.myStone = Cell.WHITE_O;
//            aiPlayer.setStone(Cell.WHITE_O);
//            System.out.println("AI recognized as the WHITE player.");
//        } else {
//            // If our AI name doesn't match either, fallback or log an error
//            System.out.println("Warning: AI username didn't match p1 or p2. The AI might not move properly.");
//        }
//
//        // Reset local tracking:
//        this.moveCount = 0;
//        // If your server always uses boardSize=7, that’s fine. Otherwise parse from server if available.
//        this.board = new CaptureGoBoard(boardSize);
//        if (Cell.BLUE_O.equals(myStone)) {
//            // It's moveCount=0 => Blue's turn => we do an AI move:
//            doAIMoveIfMyTurn();
//        }
//
//        return players;
//    }
//
//    /**
//     * Called whenever the server broadcasts a MOVE~<index>.
//     * We'll update our local board and see whose turn it is next.
//     */
//    @Override
//    protected void receiveMove(int moveIndex) {
//        int row = moveIndex / boardSize;
//        int col = moveIndex % boardSize;
//        String color = (moveCount % 2 == 0) ? Cell.BLUE_O : Cell.WHITE_O;
//
//        System.out.println("Coordinates received: " + row + col);
//
//        // Update local board
//        board.setCell(row, col, color);
//
//        // Increment moveCount
//        moveCount++;
//
//        // Now see if it's our turn.
//        doAIMoveIfMyTurn();
//    }
//
//    /**
//     * Checks if it's our turn, and if so, asks the AI for a move and sends it.
//     */
//    private void doAIMoveIfMyTurn() {
//        // If we are the BLUE player, we move on even moveCount.
//        // If we are WHITE, we move on odd moveCount.
//        boolean isBlueTurn = (moveCount % 2 == 0);
//        boolean iAmBlue = Cell.BLUE_O.equals(myStone);
//
//        // Or a simple check:
//        if (iAmBlue && isBlueTurn) {
//            // It's my turn
//            makeAIMove();
//        } else if (!iAmBlue && !isBlueTurn) {
//            // White player's turn
//            makeAIMove();
//        }
//        // Otherwise, it's the opponent's turn, do nothing.
//    }
//
//    /**
//     * The AI picks a move from the local board, and we send MOVE~<index> to the server.
//     */
//    private void makeAIMove() {
//        Cell chosen;
//        if (aiPlayer instanceof GoAI) {
//            chosen = ((GoAI) aiPlayer).chooseMove(board);
//        } else {
//            System.out.println("[AI] The AI player is not an instance of GoAI.");
//            return;
//        }
//        if (chosen == null) {
//            System.out.println("[AI] No valid moves found. Doing nothing.");
//            return;
//        }
//        int row = chosen.getRow() / 2;
//        int col = chosen.getCol() / 2;
//        int moveIndex = row * boardSize + col;
//        System.out.println("[AI] Sending move: row=" + row + ", col=" + col + " => index " + moveIndex);
//        sendMove(moveIndex);
//    }
//
//    // --- MAIN METHOD EXAMPLE ---
//    public static void main(String[] args) {
//        Scanner sc = new Scanner(System.in);
//
//        System.out.print("Server address (e.g. localhost): ");
//        String address = sc.nextLine().trim();
//
//        System.out.print("Server port: ");
//        int port = Integer.parseInt(sc.nextLine().trim());
//
//        // Which AI type?
//        System.out.print("Which AI? (naive / safe / complex): ");
//        String aiType = sc.nextLine().trim().toLowerCase();
//
//        // We'll also ask for a username for the AI on the server
//        System.out.print("Enter AI's username: ");
//        String aiName = sc.nextLine().trim();
//
//        // Build the AI Player object
//        Player aiPlayer;
//        switch (aiType) {
//            case "safe" -> {
//                aiPlayer = new SafeAI(aiName, Cell.BLUE_O);
//            }
//            case "complex" -> {
//                aiPlayer = new ComplexAI(aiName, Cell.BLUE_O, 2000, 1.4);
//            }
//            default -> {
//                aiPlayer = new NaiveAI(aiName, Cell.BLUE_O);
//            }
//        }
//
//        try {
//            // Create AI client
//            CaptureGoAiClient clientAI = new CaptureGoAiClient(address, port, aiPlayer);
//
//            // Connect + start reading from server
//            // (This calls super(...) which starts a ClientConnection thread.)
//            // Now log in to the server
//            clientAI.login(aiName);
//
//            // Wait until 'loggedIn' is true
//            while (!clientAI.getLoggedIn()) {
//                System.out.println("Waiting for server to confirm login...");
//                Thread.sleep(500);
//            }
//            System.out.println("Logged in successfully!");
//
//            // Optionally, auto-join the queue so it can get matched:
//            System.out.println("Joining the queue...");
//            clientAI.sendQueue();
//
//            // Keep running until user decides to stop or server closes.
//            // For a real "headless" AI, you might just block forever.
//            // Here's a small prompt to let the user type "quit".
//            while (true) {
//                System.out.println("Type 'quit' to exit or 'list' to see players, 'queue' to toggle queue:");
//                String cmd = sc.nextLine().trim();
//                if (cmd.equalsIgnoreCase("quit")) {
//                    clientAI.close();
//                    break;
//                } else if (cmd.equalsIgnoreCase("list")) {
//                    clientAI.sendList();
//                } else if (cmd.equalsIgnoreCase("queue")) {
//                    clientAI.sendQueue();
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}
