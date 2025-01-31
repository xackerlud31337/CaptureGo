package Players;

import Game.CaptureGoBoard;
import Game.Cell;

import java.util.*;

public class Node {
    private final Cell[][] boardState;
    private final Node parent;
    private final String currentStone; // which stone is about to move
    private final Cell moveMade;       // move that led to this node

    private int whiteCaptured; // how many stones white has captured so far
    private int blueCaptured;  // how many stones blue has captured so far

    private final List<Node> children = new ArrayList<>();
    private double wins = 0.0;
    private int visits = 0;

    /**
     * A single prior for this node (optional).
     * Typically, this represents the prior for the move that *created* this node.
     */
    private double prior = 0.0;

    /**
     * A map of potential moves -> prior (optional).
     * Can be useful at the root (or any node) to store priors for *all* unexpanded moves.
     */
    private final Map<Cell, Double> movePriors = new HashMap<>();

    public Node(Cell[][] boardState,
                Node parent,
                String currentStone,
                Cell moveMade,
                int whiteCaptured,
                int blueCaptured) {

        this.boardState = boardState;
        this.parent = parent;
        this.currentStone = currentStone;
        this.moveMade = moveMade;
        this.whiteCaptured = whiteCaptured;
        this.blueCaptured = blueCaptured;
    }

    // ----------------------------------------------------------
    // 1) List all legal moves from this node's board position
    // ----------------------------------------------------------
    public List<Cell> getLegalMoves() {
        int size = (boardState.length - 1) / 2;
        CaptureGoBoard tempBoard = new CaptureGoBoard(size);
        applyBoardState(tempBoard);

        List<Cell> moves = new ArrayList<>();
        for (int r = 0; r <= size; r++) {
            for (int c = 0; c <= size; c++) {
                if (tempBoard.isValidMove(r, c)) {
                    // We store the actual intersection as (row*2, col*2)
                    moves.add(new Cell(r * 2, c * 2));
                }
            }
        }
        return moves;
    }

    /**
     * The game should end if there are no legal moves left or if either player has captured *10* stones.
     * The 10 stones capture goal is hard coded for now.
     */
    public boolean isTerminal() {
        return getLegalMoves().isEmpty() || whiteCaptured >= 10 || blueCaptured >= 10;
    }

    // ----------------------------------------------------------
    // 2) Create a child node by making the given 'move'
    // ----------------------------------------------------------
    public Node createChildNode(Cell move) {
        Cell[][] newState = deepCopy(boardState);

        int logicalRow = move.getRow() / 2;
        int logicalCol = move.getCol() / 2;

        // Place our currentStone on the newState
        newState[move.getRow()][move.getCol()].setState(currentStone);

        // Because we’re copying from the parent’s capture counters,
        // we’ll mutate the local copies if we capture any group.
        int newWhiteCaptured = whiteCaptured;
        int newBlueCaptured  = blueCaptured;

        // Convert that 2D array into a CaptureGoBoard so we can do BFS/captures
        int size = (newState.length - 1) / 2;
        CaptureGoBoard tempBoard = new CaptureGoBoard(size);
        applyArrayStateToBoard(newState, tempBoard);

        // Attempt capturing the opponent
        String opponentStone = flipStone(currentStone);

        // Check every intersection for a group of the opponent’s color
        for (int row = 0; row <= size; row++) {
            for (int col = 0; col <= size; col++) {
                Cell cell = tempBoard.getCell(row, col);
                if (cell.getState().equals(opponentStone)) {
                    // If that group has no liberties, flip it to our color
                    if (groupHasNoLiberties(tempBoard, cell, opponentStone)) {
                        List<Cell> group = getGroup(tempBoard, cell, opponentStone);
                        for (Cell st : group) {
                            st.setState(currentStone);
                            // Also update the newState array so it matches
                            newState[st.getRow()][st.getCol()].setState(currentStone);
                        }
                        if (currentStone.equals(Cell.WHITE_O)) {
                            newWhiteCaptured += group.size();
                        } else {
                            newBlueCaptured += group.size();
                        }
                    }
                }
            }
        }

        // Create the new node; next turn is the flipped color
        String nextStone = flipStone(currentStone);
        Node child = new Node(newState, this, nextStone, move, newWhiteCaptured, newBlueCaptured);

        return child;
    }

    // ----------------------------------------------------------
    // 3) Check if a group has no liberties
    // ----------------------------------------------------------
    private boolean groupHasNoLiberties(CaptureGoBoard board, Cell start, String color) {
        // 1. Get the entire contiguous group of the same color
        List<Cell> group = getGroup(board, start, color);

        // 2. For each stone in that group, check if it has an empty neighbor
        for (Cell stone : group) {
            List<Cell> neighbors = board.getNeighbors(stone);
            for (Cell n : neighbors) {
                if (n.isEmpty()) {
                    return false; // We found a liberty
                }
            }
        }
        // If no neighbor was empty, no liberties
        return true;
    }

    // ----------------------------------------------------------
    // 4) BFS to get all stones in the connected group
    // ----------------------------------------------------------
    private List<Cell> getGroup(CaptureGoBoard board, Cell start, String color) {
        List<Cell> group = new ArrayList<>();
        Set<Cell> visited = new HashSet<>();
        Queue<Cell> queue = new LinkedList<>();
        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            Cell current = queue.poll();
            group.add(current);

            // Check neighbors that match 'color'
            for (Cell neighbor : board.getNeighbors(current)) {
                if (!visited.contains(neighbor) && neighbor.getState().equals(color)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }
        return group;
    }

    // ----------------------------------------------------------
    // Utility: convert boardState[][] into a CaptureGoBoard
    // ----------------------------------------------------------
    private void applyBoardState(CaptureGoBoard board) {
        applyArrayStateToBoard(boardState, board);
    }

    private void applyArrayStateToBoard(Cell[][] state, CaptureGoBoard board) {
        for (int i = 0; i < state.length; i++) {
            for (int j = 0; j < state[i].length; j++) {
                if (state[i][j] != null) {
                    int logicalRow = i / 2;
                    int logicalCol = j / 2;
                    board.setCell(logicalRow, logicalCol, state[i][j].getState());
                }
            }
        }
    }

    // ----------------------------------------------------------
    // 5) Deep-copy of our 2D Cell array
    // ----------------------------------------------------------
    private Cell[][] deepCopy(Cell[][] original) {
        Cell[][] copy = new Cell[original.length][original[0].length];
        for (int i = 0; i < original.length; i++) {
            for (int j = 0; j < original[i].length; j++) {
                if (original[i][j] != null) {
                    copy[i][j] = new Cell(original[i][j].getRow(), original[i][j].getCol());
                    copy[i][j].setState(original[i][j].getState());
                }
            }
        }
        return copy;
    }

    // ----------------------------------------------------------
    // 6) Convert node's boardState back to a CaptureGoBoard object
    // ----------------------------------------------------------
    public CaptureGoBoard toCaptureGoBoard() {
        int size = (boardState.length - 1) / 2;
        CaptureGoBoard board = new CaptureGoBoard(size);
        applyBoardState(board);
        return board;
    }

    // ----------------------------------------------------------
    // 7) Helpers, accessors, standard MCTS fields
    // ----------------------------------------------------------
    public Node getParent() {
        return parent;
    }

    public Cell getMove() {
        return moveMade;
    }

    public void addChild(Node child) {
        children.add(child);
    }

    public List<Node> getChildren() {
        return children;
    }

    public String getCurrentStone() {
        return currentStone;
    }

    public int getWhiteCaptured() {
        return whiteCaptured;
    }

    public int getBlueCaptured() {
        return blueCaptured;
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

    public Cell[][] getState() {
        return boardState;
    }

    // ----------------------------------------------------------
    // 8) "Prior" for the move that produced this node
    // ----------------------------------------------------------
    public double getPrior() {
        return prior;
    }

    public void setPrior(double prior) {
        this.prior = prior;
    }

    // ----------------------------------------------------------
    // 9) Store or retrieve priors for *unexpanded* moves
    // ----------------------------------------------------------
    public void setMovePrior(Cell move, double priorValue) {
        movePriors.put(move, priorValue);
    }

    public double getMovePrior(Cell move) {
        return movePriors.getOrDefault(move, 0.0);
    }

    // ----------------------------------------------------------
    // Flip a stone’s color
    // ----------------------------------------------------------
    private String flipStone(String stone) {
        return stone.equals(Cell.WHITE_O) ? Cell.BLUE_O : Cell.WHITE_O;
    }
}
