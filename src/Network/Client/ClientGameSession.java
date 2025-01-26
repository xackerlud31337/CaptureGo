package Network.Client;

import Game.CaptureGoBoard;
import Game.Cell;
import Game.Player;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class ClientGameSession {
    private final CaptureGoBoard board;
    private final Player player1;
    private final Player player2;
    private boolean player1Turn;

    public ClientGameSession(Player player1, Player player2){
        this.board = new CaptureGoBoard(6);
        this.player1 = player1;
        this.player2 = player2;
        this.player1Turn = true;
    }

    public synchronized void placeStone(int row, int col) {
        Player currentPlayer = player1Turn ? player1 : player2;
        if (board.isValidMove(row, col)) {
            // Place the stone
            board.setCell(row, col, currentPlayer.getStone());

            // Perform capture checks
            Player opponent = player1Turn ? player2 : player1;
            captureStones(opponent);
            captureStones(currentPlayer); // Check self for suicide

            // Switch turns
            player1Turn = !player1Turn;
        } else {
            throw new IllegalArgumentException("Invalid move: Intersection already taken or out of bounds!");
        }
    }

    private void captureStones(Player owner) {
        // Iterate through all stones of the given player
        for (Cell stone : owner.getOccupiedCells()) {
            Set<Cell> group = new HashSet<>();
            Set<Cell> liberties = new HashSet<>();
            Queue<Cell> queue = new LinkedList<>();
            queue.add(stone);

            while (!queue.isEmpty()) {
                Cell current = queue.poll();
                if (!group.add(current)) continue;

                for (Cell neighbor : board.getNeighbors(current)) {
                    if (neighbor.isEmpty()) {
                        liberties.add(neighbor);
                    } else if (neighbor.getState().equals(stone.getState())) {
                        queue.add(neighbor);
                    }
                }
            }

            // If no liberties, capture the entire group
            if (liberties.isEmpty()) {
                Player capturingPlayer = (owner == player1) ? player2 : player1;
                for (Cell captured : group) {
                    captured.setState(capturingPlayer.getStone()); // Change to capturing player's stone
                    owner.removeCell(captured);                   // Remove from original owner's stones
                    capturingPlayer.addCell(captured);            // Add to capturing player's stones
                }
            }
        }
    }

    public synchronized CaptureGoBoard getBoard(){
        return board;
    }

    public synchronized Cell[][] deepCopyBoard(){
        return board.boardDeepCopy();
    }

    public synchronized Player getCurrentPlayer(){
        return player1Turn ? player1 : player2;
    }

    public synchronized boolean isPlayer1Turn(){
        return player1Turn;
    }
}