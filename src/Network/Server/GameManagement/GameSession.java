package Network.Server.GameManagement;

import Network.Server.ClientHandler;
import Game.CaptureGoBoard;
import Game.Cell;
import Network.Server.Protocol.Protocol;
import Network.Server.ServerImp;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class GameSession {
    private final ClientHandler player1;
    private final ClientHandler player2;
    private final CaptureGoBoard board;
    private boolean isPlayer1Turn;
    private boolean gameOver;
    private final int captureGoal;
    private final BlockingQueue<int[]> moveQueue;
    private final ServerImp server;

    public GameSession(ClientHandler player1, ClientHandler player2, int boardSize, int captureGoal, ServerImp server) {
        this.player1 = player1;
        this.player2 = player2;
        this.board = new CaptureGoBoard(boardSize);
        this.server = server;
        this.isPlayer1Turn = true;
        this.gameOver = false;
        this.captureGoal = captureGoal;
        this.moveQueue = new LinkedBlockingQueue<>();
        player1.setStone(Cell.WHITE_O);
        player2.setStone(Cell.BLUE_O);
    }

    public ClientHandler getPlayer1() {
        return player1;
    }

    public ClientHandler getPlayer2() {
        return player2;
    }

    /**
     * Start the game session. This method will block until the game is over.
     * The game will end when the capture goal is reached or the board is full.
     * The game will also end if the game is interrupted.
     */
    public void playGame() {
        while (!gameOver) {
            //Comment out when finished
            board.render();
            ClientHandler currentPlayer = isPlayer1Turn ? player1 : player2;
            System.out.println(currentPlayer.getUsername() + "'s turn (" + (isPlayer1Turn ? "White" : "Blue") + ").");

            try {
                int[] move = moveQueue.take(); // Wait for the next move
                makeMove(move[0], move[1]);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Game interrupted.");
                break;
            }
        }
        System.out.println("Game over! Final board:");
        //Comment out when finished
        board.render();
    }

    /**
     * Queue a move to be made by the current player. This queue should be empty or at most have one move.
     * @param move the move to queue
     */
    public void queueMove(int move) {
        moveQueue.offer(new int[]{move / 7, move % 7});
    }

    /**
     * Make a move on the board. This method will check if the move is valid and update the board accordingly.
     * @param row the row of the move
     * @param col the column of the move
     */
    public void makeMove(int row, int col) {
        if (gameOver) {
            System.out.println("Game is already over.");
            return;
        }

        ClientHandler currentPlayer = isPlayer1Turn ? player1 : player2;
        if (board.isValidMove(row, col)) {
            placeStone(row, col, currentPlayer);
            isPlayer1Turn = !isPlayer1Turn; // Switch turns
            checkGameOver();
        } else {
            currentPlayer.getConnection().sendMessage(Protocol.formatError("Illegal move"));
        }
    }

    /**
     * Place a stone on the board and check for captures.
     * @param row the row of the move
     * @param col the column of the move
     * @param player the player making the move
     */
    private void placeStone(int row, int col, ClientHandler player) {
        int actualRow = row * 2;
        int actualCol = col * 2;
        Cell[][] boardCopy = board.boardDeepCopy();

        // Validate that the move is on a valid, empty intersection
        if (!board.isValidMove(row, col)) {
            throw new IllegalStateException("Invalid move: Intersection already occupied or out of bounds!");
        }

        // Place the stone on the board
        board.setCell(row, col, player.getStone());

        // Add the cell to the player's occupied cells list
        player.addCell(boardCopy[actualRow][actualCol]);

        // Check for captures for the opponent
        ClientHandler opponent = (player == player1) ? player2 : player1;
        List<Cell> opponentStones = new ArrayList<>(opponent.getOccupiedCells());
        for (Cell opponentStone : opponentStones) {
            // Only attempt to capture if it's still actually the opponent’s stone
            if (opponentStone.getState().equals(opponent.getStone())) {
                captureStones(opponentStone, opponent);
            }
        }
    }

    /**
     * Capture stones of the same color that have no liberties.
     * @param stone the stone to check for capture
     * @param owner the player that owns the stone
     */
    private void captureStones(Cell stone, ClientHandler owner) {
        // Group of stones being checked for capture
        Set<Cell> group = new HashSet<>();
        // Set of liberties (adjacent empty spaces)
        Set<Cell> liberties = new HashSet<>();
        Queue<Cell> queue = new LinkedList<>();
        queue.add(stone);

        // Perform BFS to find the group and its liberties
        while (!queue.isEmpty()) {
            Cell current = queue.poll();

            // Skip if already part of the group
            if (group.contains(current)) continue;

            group.add(current);

            // Check all neighbors
            for (Cell neighbor : board.getNeighbors(current)) {
                if (neighbor.isEmpty()) {
                    liberties.add(neighbor); // Found a liberty
                } else if (neighbor.getState().equals(stone.getState())) {
                    queue.add(neighbor); // Part of the same group
                }
            }
        }

        // If no liberties, capture the entire group
        if (liberties.isEmpty()) {
            // Determine the capturing player
            ClientHandler capturingPlayer = (owner == player1) ? player2 : player1;

            // Update the captured stones to the capturing player's stone type
            for (Cell capturedStone : group) {
                capturedStone.setState(capturingPlayer.getStone()); // Change to capturing player's stone
                owner.removeCell(capturedStone); // Remove from the original owner's list
                capturingPlayer.addCell(capturedStone); // Add to the capturing player's list
            }

            // Update the capturing player's score
            capturingPlayer.addCapturedStones(group.size());
        }
    }

    /**
     * Check if the game is over. The game is over if the board is full or a player has reached the capture goal.
     */
    private void checkGameOver() {
        ClientHandler winner = checkWinner();
        //This should be added to the original logic of the game.
        if (board.isFull()){
            //System.out.println("The board is full! The game is a draw.");
            gameOver = true;
            player1.getConnection().sendMessage(Protocol.formatGameOver(Protocol.GAMEOVER_DRAW, ""));
            player2.getConnection().sendMessage(Protocol.formatGameOver(Protocol.GAMEOVER_DRAW, ""));
            server.removeGameSession(this);
            server.removePlayers(player1, player2);

        }else if (winner != null) {
            //System.out.println("The winner is " + winner.getName() + "!");
            player1.getConnection().sendMessage(Protocol.formatGameOver(Protocol.GAMEOVER_VICTORY, winner.getUsername()));
            player2.getConnection().sendMessage(Protocol.formatGameOver(Protocol.GAMEOVER_VICTORY, winner.getUsername()));
            gameOver = true;
            server.removeGameSession(this);
            server.removePlayers(player1, player2);
        }
    }

    /**
     * Check if a player has reached the capture goal.
     * @return the winning player, or null if no winner yet
     */
    public ClientHandler checkWinner() {
        if (player1.getCapturedStones() >= captureGoal) {
            return player1;
        } else if (player2.getCapturedStones() >= captureGoal) {
            return player2;
        }
        return null; // No winner yet
    }

    /**
     * Get the current turn of the game.
     * @return true if it is player 1's turn, false if it is player 2's turn
     */
    public boolean getTurn() {
        return isPlayer1Turn;
    }

    public CaptureGoBoard getBoard() {
        return board;
    }
}