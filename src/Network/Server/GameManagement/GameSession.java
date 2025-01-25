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

    /**
     * Constructs a server GameSession that uses the same logic as your local CaptureGoGame.
     */
    public GameSession(ClientHandler player1, ClientHandler player2,
                       int boardSize, int captureGoal, ServerImp server) {
        this.player1 = player1;
        this.player2 = player2;
        this.board = new CaptureGoBoard(boardSize);
        this.server = server;
        this.isPlayer1Turn = true;
        this.gameOver = false;
        this.captureGoal = captureGoal;
        this.moveQueue = new LinkedBlockingQueue<>();

        // Assign stones just like in your local game
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
     * Main server loop: waits for moves until the game ends.
     */
    public void playGame() {
        while (!gameOver) {
            ClientHandler currentPlayer = isPlayer1Turn ? player1 : player2;
            try {
                // Take the next move from the queue
                int[] move = moveQueue.take();
                synchronized (this) {
                    // Perform the move and BFS captures
                    placeStone(move[0], move[1], currentPlayer);
                }
                // Switch turns once the move is done
                isPlayer1Turn = !isPlayer1Turn;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Game interrupted for " + currentPlayer.getUsername());
                break;
            }
        }
    }

    /**
     * Called by the server when a "MOVE X" command is received.
     * Convert that single index into (row, col) for a 7×7 grid if boardSize=6.
     */
    public void queueMove(int move) {
        moveQueue.offer(new int[]{ move / 7, move % 7 });
    }

    /**
     * The **one** method that:
     * 1) places the stone,
     * 2) checks captures on opponent,
     * 3) checks captures on self,
     * 4) checks game over,
     * 5) broadcasts the move.
     *
     * We do NOT re-add the stone afterward, because BFS may have changed ownership.
     */
    private void placeStone(int row, int col, ClientHandler player) {
        if (gameOver) {
            System.out.println("Game is already over; ignoring move.");
            return;
        }

        // Validate
        if (!board.isValidMove(row, col)) {
            throw new IllegalStateException("Invalid move at row=" + row + ", col=" + col);
        }

        // 1) Place on the real board
        board.setCell(row, col, player.getStone());
        // Also track that stone for the player
        Cell placedCell = board.getCell(row, col);
        player.addCell(placedCell);

        // 2) BFS capture: Opponent
        ClientHandler opponent = (player == player1) ? player2 : player1;
        List<Cell> opponentStones = new ArrayList<>(opponent.getOccupiedCells());
        for (Cell oppStone : opponentStones) {
            if (oppStone.getState().equals(opponent.getStone())) {
                captureStones(oppStone, opponent);
            }
        }

        // 3) BFS capture: Self (suicide rule)
        List<Cell> yourStones = new ArrayList<>(player.getOccupiedCells());
        for (Cell yourStone : yourStones) {
            if (yourStone.getState().equals(player.getStone())) {
                captureStones(yourStone, player);
            }
        }

        // 4) Check if the game ended
        checkGameOver();

        // 5) Broadcast the move to both players
        int moveIdx = row * 7 + col;
        player1.getConnection().sendMessage(Protocol.formatMove(moveIdx));
        player2.getConnection().sendMessage(Protocol.formatMove(moveIdx));
    }

    /**
     * BFS-based capture: if a contiguous group has zero liberties,
     * that entire group flips to the other player's ownership.
     */
    private void captureStones(Cell stone, ClientHandler owner) {
        Set<Cell> group = new HashSet<>();
        Set<Cell> liberties = new HashSet<>();
        Queue<Cell> queue = new LinkedList<>();
        queue.add(stone);

        while (!queue.isEmpty()) {
            Cell current = queue.poll();
            if (!group.add(current)) {
                continue; // already visited
            }

            // Check all neighbors
            for (Cell neighbor : board.getNeighbors(current)) {
                if (neighbor.isEmpty()) {
                    liberties.add(neighbor); // Found a liberty
                } else if (neighbor.getState().equals(stone.getState())) {
                    queue.add(neighbor); // Part of the same group
                }
            }
        }

        // If no liberties => capture
        if (liberties.isEmpty()) {
            ClientHandler capturingPlayer = (owner == player1) ? player2 : player1;

            for (Cell capturedStone : group) {
                capturedStone.setState(capturingPlayer.getStone());
                owner.removeCell(capturedStone);
                capturingPlayer.addCell(capturedStone);
            }
            capturingPlayer.addCapturedStones(group.size());
        }
    }

    /**
     * Check if a player reached the capture goal or the board is full => game over.
     */
    private void checkGameOver() {
        ClientHandler winner = checkWinner();
        if (winner != null) {
            // We have a capture-based winner
            player1.getConnection().sendMessage(
                    Protocol.formatGameOver(Protocol.GAMEOVER_VICTORY, winner.getUsername())
            );
            player2.getConnection().sendMessage(
                    Protocol.formatGameOver(Protocol.GAMEOVER_VICTORY, winner.getUsername())
            );
            finishGame();
        } else if (board.isFull()) {
            // It's a draw
            player1.getConnection().sendMessage(
                    Protocol.formatGameOver(Protocol.GAMEOVER_DRAW, "")
            );
            player2.getConnection().sendMessage(
                    Protocol.formatGameOver(Protocol.GAMEOVER_DRAW, "")
            );
            finishGame();
        }
    }

    /**
     * If either player has captured enough stones, return them as winner.
     */
    public ClientHandler checkWinner() {
        if (player1.getCapturedStones() >= captureGoal) {
            return player1;
        } else if (player2.getCapturedStones() >= captureGoal) {
            return player2;
        }
        return null;
    }

    /**
     * Mark game over and unregister from the server.
     */
    private void finishGame() {
        gameOver = true;
        server.removeGameSession(this);
        server.removePlayerInGame(player1);
        server.removePlayerInGame(player2);
    }

    public boolean getTurn() {
        return isPlayer1Turn;
    }

    public CaptureGoBoard getBoard() {
        return board;
    }
}
