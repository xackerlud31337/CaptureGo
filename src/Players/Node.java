package Players;

import Game.CaptureGoBoard;
import Game.Cell;
import java.util.ArrayList;
import java.util.List;

public class Node {

    private final Cell[][] boardState; // Copy of the board at this node
    private final Node parent;
    private final String currentStone; // Which stone is about to move in this node
    private final Cell moveMade;       // The move leading to this node from its parent

    private final List<Node> children;
    private double wins;
    private int visits;

    public Node(Cell[][] boardState, Node parent, String currentStone, Cell moveMade) {
        this.boardState = boardState;
        this.parent = parent;
        this.currentStone = currentStone;
        this.moveMade = moveMade;

        this.children = new ArrayList<>();
        this.wins = 0.0;
        this.visits = 0;
    }

    /**
     * Returns a list of all legal moves for 'currentStone' in this board state.
     */
    public List<Cell> getLegalMoves() {
        // We can interpret the board's logical size from array dimensions
        int size = (boardState.length - 1) / 2;
        CaptureGoBoard tempBoard = new CaptureGoBoard(size);

        // Apply our boardState to tempBoard so that 'isValidMove' can be checked
        applyStateTo(tempBoard);

        List<Cell> legalMoves = new ArrayList<>();
        for (int r = 0; r <= size; r++) {
            for (int c = 0; c <= size; c++) {
                if (tempBoard.isValidMove(r, c)) {
                    // Actual row/col = r*2, c*2
                    Cell cell = new Cell(r*2, c*2);
                    legalMoves.add(cell);
                }
            }
        }
        return legalMoves;
    }

    /**
     * Create a child Node by applying 'move' with 'currentStone' on a copy of the board,
     * and flipping the stone so the child node's 'currentStone' will be the next color.
     */
    public Node createChildNode(Cell move) {
        // 1. Deep-copy the current board
        Cell[][] newState = deepCopy(boardState);

        // 2. Apply the move
        int logicalRow = move.getRow() / 2;
        int logicalCol = move.getCol() / 2;
        newState[move.getRow()][move.getCol()].setState(currentStone);

        // 3. Flip the stone for the next node
        String nextStone = flipStone(currentStone);

        // 4. Create the child Node
        Node child = new Node(newState, this, nextStone, move);
        return child;
    }

    /**
     * Convert the Node's boardState into an actual CaptureGoBoard for potential simulation.
     */
    public CaptureGoBoard toCaptureGoBoard() {
        int size = (boardState.length - 1) / 2;
        CaptureGoBoard tempBoard = new CaptureGoBoard(size);
        applyStateTo(tempBoard);
        return tempBoard;
    }

    /**
     * Helper: apply the 'boardState' array to a CaptureGoBoard so it reflects the Node's state.
     */
    private void applyStateTo(CaptureGoBoard board) {
        for (int i = 0; i < boardState.length; i++) {
            for (int j = 0; j < boardState[i].length; j++) {
                if (boardState[i][j] != null) {
                    // logical coords
                    int logicalRow = i / 2;
                    int logicalCol = j / 2;
                    board.setCell(logicalRow, logicalCol, boardState[i][j].getState());
                }
            }
        }
    }

    /**
     * Deep-copy a 2D array of Cells.
     */
    private Cell[][] deepCopy(Cell[][] original) {
        Cell[][] copy = new Cell[original.length][original[0].length];
        for (int i = 0; i < original.length; i++) {
            for (int j = 0; j < original[i].length; j++) {
                if (original[i][j] != null) {
                    copy[i][j] = new Cell(original[i][j].getRow(), original[i][j].getCol());
                    copy[i][j].setState(original[i][j].getState());
                } else {
                    copy[i][j] = null;
                }
            }
        }
        return copy;
    }

    /**
     * Flip the stone from WHITE to BLUE or BLUE to WHITE.
     */
    private String flipStone(String stone) {
        return (Cell.WHITE_O.equals(stone)) ? Cell.BLUE_O : Cell.WHITE_O;
    }

    // ------------------- MCTS Stats / Accessors -------------------- //

    public Node getParent() {
        return parent;
    }

    public List<Node> getChildren() {
        return children;
    }

    public void addChild(Node child) {
        children.add(child);
    }

    public Cell getMoveMade() {
        return moveMade;
    }

    public String getCurrentStone() {
        return currentStone;
    }

    public double getWins() {
        return wins;
    }

    public int getVisits() {
        return visits;
    }

    public void addWin(double w) {
        this.wins += w;
    }

    public void addVisit() {
        this.visits++;
    }
}
