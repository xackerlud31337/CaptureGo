package Players;

import Game.CaptureGoBoard;
import Game.Cell;
import Game.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SafeAI extends Player implements GoAI{

    public SafeAI(String name, String stone) {
        super(name, stone);
    }

    /**
     * Chooses a move that is not immediately suicidal.
     * It also checks if the move can capture opponent stones. If multiple moves
     * are safe, it picks randomly among them or among those that capture the most.
     *
     * @param board the current game board
     * @return the chosen cell for the move, or null if no valid moves are available
     */
    public Cell chooseMove(CaptureGoBoard board) {
        // List of all valid intersections
        List<Cell> allValidMoves = collectValidMoves(board);

        if (allValidMoves.isEmpty()) {
            return null; // No moves available
        }

        // We'll separate moves into:
        // - "Capturing moves" that also are safe
        // - "Safe but non-capturing" moves
        List<Cell> capturingMoves = new ArrayList<>();
        List<Cell> safeMoves = new ArrayList<>();

        for (Cell move : allValidMoves) {
            // Check if placing a stone here is safe (not immediately suicidal)
            if (!isMoveSuicidal(board, move.getRow()/2, move.getCol()/2)) {
                // Check if this move captures any opponent's stones
                int capturedCount = howManyWouldCapture(board, move.getRow()/2, move.getCol()/2);
                if (capturedCount > 0) {
                    capturingMoves.add(move);
                } else {
                    safeMoves.add(move);
                }
            }
        }

        // If we found safe capturing moves, prefer them
        if (!capturingMoves.isEmpty()) {
            return pickRandomMove(capturingMoves);
        }

        // Otherwise, pick from safe, non-capturing moves
        if (!safeMoves.isEmpty()) {
            return pickRandomMove(safeMoves);
        }

        // If everything is suicidal, revert to random among all valid moves
        // or simply pass (return null). We'll just pick random among all valid.
        return pickRandomMove(allValidMoves);
    }

    /**
     * Helper method: Collect all valid moves from the board.
     */
    private List<Cell> collectValidMoves(CaptureGoBoard board) {
        List<Cell> validMoves = new ArrayList<>();
        for (int row = 0; row < board.getSize(); row++) {
            for (int col = 0; col < board.getSize(); col++) {
                if (board.isValidMove(row, col)) {
                    validMoves.add(board.getCell(row, col));
                }
            }
        }
        return validMoves;
    }

    /**
     * Helper method: Pick a random cell from a list.
     */
    private Cell pickRandomMove(List<Cell> moves) {
        Random random = new Random();
        return moves.get(random.nextInt(moves.size()));
    }

    /**
     * Checks if placing a stone at (row, col) would be immediately suicidal.
     * That is, if we place a stone and it immediately has no liberties, we consider
     * it "suicidal".
     */
    private boolean isMoveSuicidal(CaptureGoBoard board, int row, int col) {
        // Create a temporary copy of the board
        Cell[][] boardCopy = board.boardDeepCopy();

        // Place our stone on the board copy
        boardCopy[row*2][col*2].setState(this.getStone());

        // Check if the group formed by this newly placed stone has liberties
        return !hasLiberties(boardCopy, row*2, col*2);
    }

    /**
     * Checks if the group that includes the cell at (r, c) has any liberties (empty neighbors).
     * BFS or DFS can be used here. If there's at least one empty neighbor, we have a liberty.
     */
    private boolean hasLiberties(Cell[][] boardCopy, int r, int c) {
        String myStone = boardCopy[r][c].getState();
        boolean[][] visited = new boolean[boardCopy.length][boardCopy[0].length];
        List<Cell> queue = new ArrayList<>();
        queue.add(boardCopy[r][c]);

        while (!queue.isEmpty()) {
            Cell current = queue.remove(queue.size() - 1);
            if (visited[current.getRow()][current.getCol()]) {
                continue;
            }
            visited[current.getRow()][current.getCol()] = true;

            // Get neighbors
            List<Cell> neighbors = getNeighbors(boardCopy, current);
            for (Cell neighbor : neighbors) {
                if (neighbor.isEmpty()) {
                    // Found an empty space => has liberty
                    return true;
                }
                // If neighbor has the same stone, keep searching
                if (neighbor.getState().equals(myStone)) {
                    queue.add(neighbor);
                }
            }
        }
        return false;
    }

    /**
     * Return the neighbors of a cell in the board copy.
     */
    private List<Cell> getNeighbors(Cell[][] boardCopy, Cell cell) {
        List<Cell> neighbors = new ArrayList<>();
        int[][] directions = {{2, 0}, {-2, 0}, {0, 2}, {0, -2}};
        for (int[] dir : directions) {
            int nr = cell.getRow() + dir[0];
            int nc = cell.getCol() + dir[1];
            if (nr >= 0 && nr < boardCopy.length && nc >= 0 && nc < boardCopy[0].length) {
                if (boardCopy[nr][nc] != null) {
                    neighbors.add(boardCopy[nr][nc]);
                }
            }
        }
        return neighbors;
    }

    /**
     * Counts how many stones of the opponent would be captured if we place
     * our stone at (row, col). Returns 0 if no capture, otherwise the number
     * of stones that would be captured.
     */
    private int howManyWouldCapture(CaptureGoBoard board, int row, int col) {
        // We'll do a quick simulation on a copy.
        Cell[][] boardCopy = board.boardDeepCopy();
        boardCopy[row*2][col*2].setState(this.getStone());

        // Identify the opponent's stone
        String opponentStone = (this.getStone().equals(Cell.WHITE_O))
                ? Cell.BLUE_O
                : Cell.WHITE_O;

        // We want to see which opponent stones (or groups) now have zero liberties
        List<Cell> opponentCells = new ArrayList<>();
        // Collect all stones of the opponent from boardCopy
        for (int i = 0; i < boardCopy.length; i++) {
            for (int j = 0; j < boardCopy[i].length; j++) {
                if (boardCopy[i][j] != null && boardCopy[i][j].getState().equals(opponentStone)) {
                    opponentCells.add(boardCopy[i][j]);
                }
            }
        }
        // For each opponent stone, check if that group has zero liberties
        // If so, those would be captured
        int totalCapturable = 0;
        boolean[][] visited = new boolean[boardCopy.length][boardCopy[0].length];

        for (Cell oppCell : opponentCells) {
            if (!visited[oppCell.getRow()][oppCell.getCol()]) {
                List<Cell> group = new ArrayList<>();
                boolean hasLiberty = exploreGroup(boardCopy, oppCell, visited, group);
                if (!hasLiberty) {
                    // group is fully capturable
                    totalCapturable += group.size();
                }
            }
        }
        return totalCapturable;
    }

    /**
     * BFS/DFS for the group of the same color. Returns true if we found at least one liberty.
     * Fills 'group' with all cells in that group.
     */
    private boolean exploreGroup(Cell[][] boardCopy, Cell start, boolean[][] visited, List<Cell> group) {
        String color = start.getState();
        boolean hasLiberty = false;
        List<Cell> stack = new ArrayList<>();
        stack.add(start);

        while (!stack.isEmpty()) {
            Cell current = stack.remove(stack.size() - 1);
            if (visited[current.getRow()][current.getCol()]) continue;
            visited[current.getRow()][current.getCol()] = true;
            group.add(current);

            // Explore neighbors
            List<Cell> neighbors = getNeighbors(boardCopy, current);
            for (Cell nb : neighbors) {
                if (nb.isEmpty()) {
                    hasLiberty = true;
                } else if (nb.getState().equals(color) && !visited[nb.getRow()][nb.getCol()]) {
                    stack.add(nb);
                }
            }
        }
        return hasLiberty;
    }
}
