package Players;

import Game.CaptureGoBoard;
import Game.Cell;

import java.util.*;

public class MonteCarloTreeSearch {
    private static final Random RAND = new Random();

    // -----------------------------
    // CONFIGURABLE CONSTANTS
    // -----------------------------

    private int captureGoal = 3;
    private double captureWeight = 15.0;
    private double libertyWeight = 3.0;
    private double selfCapturePenalty = -200.0;

    // -----------------------------
    // MCTS PARAMETERS, be careful!
    // -----------------------------
    private static final int MAX_DEPTH = 60;
    private double explorationConstant = 1.4;
    private double priorWeight = 0.1;
    private boolean useProgressiveWidening = true;

    public Node runMCTS(Node root, int simulations) {
        List<Cell> rootMoves = root.getLegalMoves();
        if (rootMoves.isEmpty()) {
            return null;
        }

        initializeChildrenPriors(root, rootMoves);

        // Main MCTS loop
        for (int i = 0; i < simulations; i++) {
            // 1. Selection (including partial expansions if we’re using them)
            Node selected = select(root);

            // 2. Expansion
            if (!selected.isTerminal()) {
                selected = expandIfPossible(selected);
            }

            // 3. Simulation (rollout)
            double result = simulate(selected);

            // 4. Backpropagation
            backpropagate(selected, result);
        }

        // Pick the child with the highest visit count (or highest average Q, up to you)
        Node bestChild = null;
        double bestVisits = -1;

        for (Node c : root.getChildren()) {
            if (c.getVisits() > bestVisits) {
                bestVisits = c.getVisits();
                bestChild = c;
            }
        }

        return bestChild;
    }

    // -------------------------------------------------------------
    // SELECTION: navigate down the tree until a leaf/unexpanded node
    // -------------------------------------------------------------
    private Node select(Node node) {
        Node current = node;

        while (!current.getChildren().isEmpty() && !current.isTerminal()) {
            Node best = null;
            double bestUCT = Double.NEGATIVE_INFINITY;

            // Standard loop to find the child with the best UCT (with our new prior-bias)
            for (Node child : current.getChildren()) {
                double uct = uctValue(child, current.getVisits());
                if (uct > bestUCT) {
                    bestUCT = uct;
                    best = child;
                }
            }

            if (best == null) {
                break;
            }
            current = best;
        }

        return current;
    }

    /**
     * Modified UCT formula that includes a small "prior" term.
     *
     * Q = average outcome for the child
     * P = prior for the child (based on evaluateMove)
     * c = exploration constant (explorationConstant)
     * alpha = priorWeight
     *
     * If child has never been visited, we return MAX_VALUE to force exploration.
     */
    private double uctValue(Node child, int parentVisits) {
        double w = child.getWins();
        double n = child.getVisits();

        // If unvisited, explore
        if (n == 0) {
            return Double.MAX_VALUE;
        }

        // Exploitation
        double exploitation = w / n;

        // Exploration
        double exploration = explorationConstant * Math.sqrt(Math.log(parentVisits) / n);

        // Progressive bias: incorporate child’s prior
        // child.getPrior() must be stored in your Node class (see expansions).
        double bias = priorWeight * child.getPrior() / (1.0 + n);

        return exploitation + exploration + bias;
    }

    // -----------------------------------------------
    // EXPANSION: Add new child if we have unexpanded moves
    // -----------------------------------------------
    private Node expandIfPossible(Node selected) {
        List<Cell> moves = selected.getLegalMoves();
        if (moves.isEmpty()) {
            // No moves to expand
            return selected;
        }

        // Optional: Progressive Widening
        if (useProgressiveWidening) {
            int expandedChildren = selected.getChildren().size();
            int maxChildren = maxChildrenToExpand(selected);

            if (expandedChildren < moves.size() && expandedChildren < maxChildren) {
                // Expand a new child
                return expand(selected, moves);
            } else {
                // Return the same node if we don't expand
                return selected;
            }

        } else {
            // If not using progressive widening, always expand one child if possible.
            // (But only if this node has been visited at least once—common check.)
            if (selected.getVisits() > 0 && selected.getChildren().size() < moves.size()) {
                return expand(selected, moves);
            }
            return selected;
        }
    }

    /**
     * For progressive widening.
     * Example: we allow up to floor(sqrt(visits)) children expansions.
     */
    private int maxChildrenToExpand(Node node) {
        return 1 + (int) Math.sqrt(node.getVisits());
    }

    /**
     * Actually create a child node with a chosen move from the unexpanded set.
     */
    private Node expand(Node node, List<Cell> moves) {
        // Filter out moves that have already been expanded.
        Set<Cell> expandedMoves = new HashSet<>();
        for (Node ch : node.getChildren()) {
            expandedMoves.add(ch.getMove());
        }

        // Get a list of unexpanded moves
        List<Cell> unexpanded = new ArrayList<>();
        for (Cell m : moves) {
            if (!expandedMoves.contains(m)) {
                unexpanded.add(m);
            }
        }

        // If everything is expanded, just return node (should rarely happen)
        if (unexpanded.isEmpty()) {
            return node;
        }

        // We pick exactly one move to expand now.
        // Let's pick based on the highest prior from evaluateMove, or do a random approach.
        Cell bestMove = pickMoveByPrior(node, unexpanded);

        // Create a child node
        Node child = node.createChildNode(bestMove);
        node.addChild(child);

        // We recalculate or store the prior for that child
        double priorVal = evaluateMove(node.toCaptureGoBoard(), bestMove, node.getCurrentStone());
        child.setPrior(priorVal);

        return child;
    }

    /**
     * Optionally pick the unexpanded move with the highest prior (rather than random).
     * This helps a bit with guiding expansions toward seemingly strong moves.
     */
    private Cell pickMoveByPrior(Node node, List<Cell> unexpanded) {
        CaptureGoBoard boardCopy = node.toCaptureGoBoard();
        String stone = node.getCurrentStone();

        Cell bestCell = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (Cell move : unexpanded) {
            double score = evaluateMove(boardCopy, move, stone);
            if (score > bestScore) {
                bestScore = score;
                bestCell = move;
            }
        }

        return bestCell;
    }

    /**
     * Initialize children’s prior at the root node.
     * This is handy so the root's moves have a prior from the start.
     * (We don’t always need to do this at *every* node if your expand() step sets prior.)
     */
    private void initializeChildrenPriors(Node root, List<Cell> moves) {
        for (Cell move : moves) {
            // Create a "fake" prior so we store it in the Node for later reference
            double p = evaluateMove(root.toCaptureGoBoard(), move, root.getCurrentStone());

            // If the root node hasn’t actually created the child object yet,
            // we can store the prior in some local map or wait until expansion time.
            // For demonstration, let's store it in root for easy reference:
            root.setMovePrior(move, p);
        }
    }

    // -----------------------------------------------
    // SIMULATION: A (mostly) random or heuristic-based playout
    // -----------------------------------------------
    private double simulate(Node node) {
        CaptureGoBoard simBoard = node.toCaptureGoBoard();
        int size = simBoard.getSize();

        int whiteCaptured = node.getWhiteCaptured();
        int blueCaptured = node.getBlueCaptured();

        String stoneToMove = node.getCurrentStone();
        int depth = 0;

        while (depth < MAX_DEPTH) {
            depth++;

            if (whiteCaptured >= captureGoal) {
                return stoneToMove.equals(Cell.WHITE_O) ? 1.0 : 0.0;
            }
            if (blueCaptured >= captureGoal) {
                return stoneToMove.equals(Cell.BLUE_O) ? 1.0 : 0.0;
            }

            List<Cell> possible = validMoves(simBoard);
            if (possible.isEmpty()) {
                // No moves left, treat as draw
                return 0.5;
            }

            // Weighted pick among possible moves
            Cell move = selectBestMove(simBoard, possible, stoneToMove);
            int logicalRow = move.getRow() / 2;
            int logicalCol = move.getCol() / 2;

            simBoard.setCell(logicalRow, logicalCol, stoneToMove);

            int capturesNow = applyCaptures(simBoard, stoneToMove, logicalRow, logicalCol);
            if (stoneToMove.equals(Cell.WHITE_O)) {
                whiteCaptured += capturesNow;
            } else {
                blueCaptured += capturesNow;
            }

            stoneToMove = flipStone(stoneToMove);
        }

        // If we exit the loop, treat it as a draw
        return 0.5;
    }

    // -----------------------------------------------
    // BACKPROPAGATION: Update stats along the path
    // -----------------------------------------------
    private void backpropagate(Node node, double result) {
        Node current = node;
        while (current != null) {
            current.addVisit();
            current.addWin(result);
            current = current.getParent();
        }
    }

    // ------------------------------------------------------------------
    // HELPER METHODS (mostly the same, with minor refinements)
    // ------------------------------------------------------------------

    private List<Cell> validMoves(CaptureGoBoard simBoard) {
        List<Cell> moves = new ArrayList<>();
        int size = simBoard.getSize();
        for (int r = 0; r <= size; r++) {
            for (int c = 0; c <= size; c++) {
                if (simBoard.isValidMove(r, c)) {
                    moves.add(new Cell(r * 2, c * 2));
                }
            }
        }
        return moves;
    }

    /**
     * Weighted random selection among possible moves using an exponential of the move score.
     * This effectively implements a "softmax" over your evaluateMove heuristic.
     */
    private Cell selectBestMove(CaptureGoBoard board, List<Cell> moves, String stone) {
        Map<Cell, Double> moveScores = new HashMap<>();
        double totalScore = 0.0;

        for (Cell move : moves) {
            double score = evaluateMove(board, move, stone);
            moveScores.put(move, score);
            totalScore += Math.exp(score);
        }

        double randomValue = RAND.nextDouble() * totalScore;
        double cumulativeScore = 0.0;

        for (Map.Entry<Cell, Double> entry : moveScores.entrySet()) {
            cumulativeScore += Math.exp(entry.getValue());
            if (cumulativeScore >= randomValue) {
                return entry.getKey();
            }
        }

        // Fallback (should never happen theoretically)
        return moves.get(RAND.nextInt(moves.size()));
    }

    /**
     * Evaluate a move heuristically.
     * Adjust these scoring factors to see how they affect AI strength.
     */
    private double evaluateMove(CaptureGoBoard board, Cell move, String stone) {
        double score = 0.0;
        int row = move.getRow() / 2;
        int col = move.getCol() / 2;

        // We'll copy the board to test hypothetical captures, suicides, etc.
        CaptureGoBoard tempBoard = new CaptureGoBoard(board.getSize());
        copyBoard(board, tempBoard);

        // Place the stone
        tempBoard.setCell(row, col, stone);

        // 1) Immediate captures this move would produce
        int capturesNow = applyCaptures(tempBoard, stone, row, col);
        score += captureWeight * capturesNow;

        // 2) Check if it’s a suicide move (no liberties for newly placed group)
        int selfCaptured = simulateSelfCapture(tempBoard, move, stone);
        if (selfCaptured > 0) {
            score += selfCapturePenalty * selfCaptured; // large negative
        }

        // 3) Count local liberties. More local liberties = safer move
        int localLiberties = countLiberties(tempBoard, row, col, stone);
        score += libertyWeight * localLiberties;

        // Additional domain-specific heuristics can go here:
        // - Influence
        // - Distance to edges
        // - Patterns, etc.

        return score;
    }

    private int applyCaptures(CaptureGoBoard board, String placedStone, int row, int col) {
        String opponentStone = placedStone.equals(Cell.WHITE_O) ? Cell.BLUE_O : Cell.WHITE_O;
        int capturedStones = 0;

        List<Cell> neighbors = board.getNeighbors(board.getCell(row, col));
        for (Cell neighbor : neighbors) {
            if (neighbor != null && neighbor.getState().equals(opponentStone)) {
                Set<Cell> group = findGroup(board, neighbor, opponentStone);
                if (!hasLiberty(board, group)) {
                    for (Cell captured : group) {
                        captured.setState(placedStone);
                    }
                    capturedStones += group.size();
                }
            }
        }
        return capturedStones;
    }

    private Set<Cell> findGroup(CaptureGoBoard board, Cell start, String color) {
        Set<Cell> group = new HashSet<>();
        Queue<Cell> queue = new LinkedList<>();
        queue.add(start);
        group.add(start);

        while (!queue.isEmpty()) {
            Cell current = queue.poll();
            for (Cell nbr : board.getNeighbors(current)) {
                if (nbr != null && nbr.getState().equals(color) && !group.contains(nbr)) {
                    group.add(nbr);
                    queue.add(nbr);
                }
            }
        }
        return group;
    }

    private boolean hasLiberty(CaptureGoBoard board, Set<Cell> group) {
        for (Cell stone : group) {
            for (Cell nbr : board.getNeighbors(stone)) {
                if (nbr.isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns how many stones you lose by placing your stone on 'move'
     * (i.e., suicide move).
     */
    private int simulateSelfCapture(CaptureGoBoard board, Cell move, String stone) {
        int r = move.getRow() / 2;
        int c = move.getCol() / 2;
        Cell placedCell = board.getCell(r, c);

        Set<Cell> group = findGroup(board, placedCell, stone);
        boolean hasLiberty = hasLiberty(board, group);
        if (!hasLiberty) {
            // entire group is captured
            return group.size();
        }
        return 0;
    }

    private void copyBoard(CaptureGoBoard source, CaptureGoBoard destination) {
        int size = source.getSize();
        for (int r = 0; r <= size; r++) {
            for (int c = 0; c <= size; c++) {
                String state = source.getCell(r, c).getState();
                destination.setCell(r, c, state);
            }
        }
    }

    private int countLiberties(CaptureGoBoard board, int row, int col, String stone) {
        // Simple local liberty count:
        // Just count how many neighbor cells are empty
        int liberties = 0;
        List<Cell> neighbors = board.getNeighbors(board.getCell(row, col));
        for (Cell neighbor : neighbors) {
            if (neighbor.isEmpty()) {
                liberties++;
            }
        }
        return liberties;
    }

    private String flipStone(String stone) {
        return stone.equals(Cell.WHITE_O) ? Cell.BLUE_O : Cell.WHITE_O;
    }
}
