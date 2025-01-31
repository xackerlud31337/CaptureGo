package Network.Client;

import Game.Cell;
import Game.Player;
import Network.Server.Protocol.Protocol;
import java.io.IOException;
import java.util.Scanner;

/**
 * The CaptureGoClient class is the main client class for the CaptureGo game.
 * It handles the connection to the server and the game session.
 */
public class CaptureGoClient {
    private final ClientConnection clientConnection;
    private ClientGameSession game;
    private Player ownPlayer;
    private boolean loggedIn = false;
    private boolean inGame = false;

    /**
     * Constructor initializes the client connection.
     */
    public CaptureGoClient(String address, int port) throws IOException {
        clientConnection = new ClientConnection(address, port, this);
        clientConnection.start();
    }

    /**
     * Closes the client connection.
     */
    public void close() {
        clientConnection.close();
    }

    /**
     * Tries to log in to the server with the given username.
     */
    protected void login(String username) {
        Scanner scanner = new Scanner(System.in);
        clientConnection.sendHello(username);
        while (!loggedIn) {
            clientConnection.sendLogin(username);
            try {
                Thread.sleep(500); // Wait for the server to respond
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Interrupted while waiting for login.");
                return;
            }
            if (!loggedIn) {
                System.out.println("Your username is not valid, please enter a new one:");
                username = scanner.nextLine().trim();
            }
        }
    }

    /**
     * Handles commands input by the user.
     */
    protected void sendMove(int move) {
        clientConnection.sendMove(move);
    }

    /**
     * Sends a request to the server to list all players.
     */
    protected void sendList() {
        clientConnection.sendList();
    }

    /**
     * Sends a request to the server to join a game.
     */
    protected void sendQueue() {
        clientConnection.sendQueue();
    }

    /**
     * Handles a response from the server, based on the request to join a game.
     */
    protected void receiveQueue(){
        System.out.println("You are now in the queue!");
    }
    /**
     * Receives a message from the server that the player has successfully joined a game.
     * @param message the message from the server
     */
    public void receiveGameStart(String message){
        String[] parts = message.split("~");
        Player otherPlayer = new Player("", null);
        if (parts[1].equals(ownPlayer.getName())){
            ownPlayer.setStone(Cell.WHITE_O);
            otherPlayer.setStone(Cell.BLUE_O);
            otherPlayer.setName(parts[2]);
            game = new ClientGameSession(ownPlayer, otherPlayer);
        }else{
            ownPlayer.setStone(Cell.BLUE_O);
            otherPlayer.setStone(Cell.WHITE_O);
            otherPlayer.setName(parts[1]);
            game = new ClientGameSession(otherPlayer, ownPlayer);
        }
        System.out.println("The game has started!");
        System.out.println(game.getCurrentPlayer().getName() + "'s turn.");
        game.getBoard().render();
        inGame = true;
    }

    protected void receiveHello(String s) {
        System.out.println("The server sent a hello message: " + s);
    }

    /**
     * Receives a message from the server that the player has successfully logged in.
     */
    protected void receiveLogin() {
        loggedIn = true;
    }

    /**
     * Returns whether the player is currently logged in.
     */
    protected boolean getLoggedIn(){
        return loggedIn;
    }
    /**
     * Returns whether the player is currently in a game.
     */
    protected boolean inGame(){
        return inGame;
    }

    /**
     * Receives a list of players from the server and prints it to the console.
     * @param message the list of players
     */
    protected void receiveList(String message) {
        System.out.println("The list of players is: ");
        String[] parts = message.split("~");
        for (int i = 1; i < parts.length; i++) {
            if (i == parts.length - 1) {
                System.out.print(parts[i]);
            } else {
                System.out.print(parts[i] + ", ");
            }
        }
        System.out.println();
    }

    /**
     * Receives a move from the server and updates the board.
     * The message format should include the move and any captured cells (if applicable).
     * Example format: "MOVE~moveIndex~capturedIndices"
     * @param message the move details sent by the server
     */
    protected void receiveMove(String message) {
        String[] parts = message.split("~");
        int moveIndex = Integer.parseInt(parts[1]);
        int row = moveIndex / 7;
        int col = moveIndex % 7;

        // Place the stone
        game.placeStone(row, col);

        // Process captures (if any)
        if (parts.length > 3 && !parts[2].isEmpty()) {
            String[] capturedIndices = parts[2].split(",");
            String capturerStone = parts[3]; // Capturer's stone color
            for (String captured : capturedIndices) {
                int capturedIndex = Integer.parseInt(captured);
                int capturedRow = capturedIndex / 7;
                int capturedCol = capturedIndex % 7;
                game.getBoard().getCell(capturedRow, capturedCol).setState(capturerStone); // Update state
            }
        }

        // Re-render the board
        game.getBoard().render();
    }

    /**
     * Receives a message from the server that the player is already logged in.
     */
    protected void receiveAlreadyLoggedIn() {
        if (loggedIn) {
            System.out.println("You are already logged in!");
        }
    }

    /**
     * Receives an error message from the server and prints it to the console.
     * @param message the error message
     */
    protected void receiveError(String message) {
        if (message.contains("Illegal move")){
            System.out.println("Please select a move between 0 and 48 again: ");
        } else if (message.contains("not your turn")) {
            System.out.println("Please wait until it's your turn: ");
        }
        System.out.println("An error occurred: " + message);
    }

    /**
     * Sets the player object for the client.
     * @param player the player object to set
     */
    protected void setOwnPlayer(Player player){
        this.ownPlayer = player;
    }

    /**
     * Gets the player object for the client.
     * @return the player object
     */
    public Player getOwnPlayer() {
        return ownPlayer;
    }

    /**
     * Shows the board whenever the user desires it.
     * Supposedly while the player is in a game.
     */
    public void showBoard() {
        if (!inGame) {
            System.out.println("You are not currently in a game. Type 'queue' to join.");
        }else{
            game.getBoard().render();
        }
    }

    protected void receiveGameOver(String result, String winner) {
        if ("VICTORY".equals(result)) {
            System.out.println("Game over! The winner is: " + winner);
        } else if ("DRAW".equals(result)) {
            System.out.println("Game over! It's a draw.");
        } else {
            System.out.println("Game over! Reason: " + result);
        }
        inGame = false; // Mark the game as ended
    }
}
