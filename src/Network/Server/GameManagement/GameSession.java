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
     * Places a stone, performs captures, checks for game over, and broadcasts the result.
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

        // Place the stone
        board.setCell(row, col, player.getStone());
        Cell placedCell = board.getCell(row, col);
        player.addCell(placedCell);

        // Handle opponent captures
        ClientHandler opponent = (player == player1) ? player2 : player1;
        List<Cell> opponentStones = new ArrayList<>(opponent.getOccupiedCells());
        StringBuilder capturedIndices = new StringBuilder();

        for (Cell oppStone : opponentStones) {
            if (oppStone.getState().equals(opponent.getStone())) {
                List<Cell> capturedGroup = captureStones(oppStone, opponent);
                for (Cell captured : capturedGroup) {
                    int capturedIdx = captured.getRow() * 7 + captured.getCol();
                    capturedIndices.append(capturedIdx).append(",");
                }
            }
        }

        // Handle self-captures (suicide rule)
        List<Cell> yourStones = new ArrayList<>(player.getOccupiedCells());
        for (Cell yourStone : yourStones) {
            if (yourStone.getState().equals(player.getStone())) {
                captureStones(yourStone, player);
            }
        }

        // Check if the game ended
        checkGameOver();

        // Broadcast the move and captures
        int moveIdx = row * 7 + col;
        String message = "MOVE~" + moveIdx + "~" + capturedIndices.toString() + "~" + player.getStone();
        player1.getConnection().sendMessage(message);
        player2.getConnection().sendMessage(message);
    }

    /**
     * Performs BFS to capture stones that have no liberties.
     */
    private List<Cell> captureStones(Cell stone, ClientHandler owner) {
        Set<Cell> group = new HashSet<>();
        Set<Cell> liberties = new HashSet<>();
        Queue<Cell> queue = new LinkedList<>();
        queue.add(stone);

        while (!queue.isEmpty()) {
            Cell current = queue.poll();
            if (!group.add(current)) {
                continue; // Already visited
            }

            synchronized (board) {
                for (Cell neighbor : board.getNeighbors(current)) {
                    if (neighbor.isEmpty()) {
                        liberties.add(neighbor);
                    } else if (neighbor.getState().equals(stone.getState())) {
                        queue.add(neighbor);
                    }
                }
            }
        }

        // If no liberties, capture the group
        if (liberties.isEmpty()) {
            ClientHandler capturingPlayer = (owner == player1) ? player2 : player1;
            for (Cell captured : group) {
                captured.setState(capturingPlayer.getStone());
                owner.removeCell(captured);
                capturingPlayer.addCell(captured);
            }
            capturingPlayer.addCapturedStones(group.size());
            return new ArrayList<>(group);
        }

        return Collections.emptyList();
    }

    /**
     * Checks if the game is over and determines the winner if applicable.
     */
    private void checkGameOver() {
        ClientHandler winner = checkWinner();
        if (winner != null) {
            finishGame();
        } else if (board.isFull()) {
            finishGame();
        }
    }

    /**
     * Determines the winner based on captured stones.
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
     * Ends the game and cleans up the game session.
     */
    private void finishGame() {
        gameOver = true;
        String winner = checkWinner() != null ? checkWinner().getUsername() : "";
        String result = winner.isEmpty() ? Protocol.GAMEOVER_DRAW : Protocol.GAMEOVER_VICTORY;

        player1.getConnection().sendMessage(Protocol.formatGameOver(result, winner));
        player2.getConnection().sendMessage(Protocol.formatGameOver(result, winner));

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