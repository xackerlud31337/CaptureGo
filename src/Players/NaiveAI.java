package Players;

import Game.CaptureGoBoard;
import Game.Cell;

import Game.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NaiveAI extends Player implements GoAI {

    public NaiveAI(String name, String stone) {
        super(name, stone);
    }

    /**
     * Selects a random valid move from the board.
     * @param board the current game board
     * @return the chosen cell for the move, or null if no valid moves are available
     */
    public Cell chooseMove(CaptureGoBoard board) {
        List<Cell> validMoves = new ArrayList<>();

        // Collect all valid moves
        for (int row = 0; row < board.getSize(); row++) {
            for (int col = 0; col < board.getSize(); col++) {
                if (board.isValidMove(row, col)) {
                    validMoves.add(board.getCell(row, col));
                }
            }
        }

        if (validMoves.isEmpty()) {
            return null; // No valid moves available
        }

        // Randomly choose a move
        Random random = new Random();
        return validMoves.get(random.nextInt(validMoves.size()));
    }

    public String getName() {
        return super.getName();
    }
}
