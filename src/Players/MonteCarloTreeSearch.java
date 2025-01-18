package Players;

import Game.CaptureGoBoard;
import Game.Cell;
import java.util.*;

public class MonteCarloTreeSearch {
    private static final Random RAND = new Random();
    private static final int MAX_DEPTH = 60; // or so, to prevent endless loops in large boards

    private int captureGoal = 5; // example
    private double captureWeight = 15.0;
    private double libertyWeight = 2.0;
    private double selfCapturePenalty = -200.0;

    public Node runMCTS(Node root, int simulations) {
        List<Cell> rootMoves = root.getLegalMoves();
        if (rootMoves.isEmpty()) {
            return null;
        }

        for (int i = 0; i < simulations; i++) {
            // 1. Selection
            Node selected = select(root);

            // 2. Expansion
            if (selected.getVisits() > 0) {
                List<Cell> moves = selected.getLegalMoves();
                if (!moves.isEmpty()) {
                    Node child = expand(selected, moves);
                    selected = child;
                }
            }

            // 3. Simulation
            double result = simulate(selected);

            // 4. Backpropagation
            backpropagate(selected, result);
        }

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

    private Node select(Node node) {
        Node current = node;
        while (!current.getChildren().isEmpty()) {
            Node best = null;
            double bestUCT = Double.NEGATIVE_INFINITY;

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

    private double uctValue(Node child, int parentVisits) {
        double w = child.getWins();
        double n = child.getVisits();
        if (n == 0) {
            return Double.MAX_VALUE;
        }
        double exploration = Math.sqrt(2 * Math.log(parentVisits) / n);
        return (w / n) + exploration;
    }

    private Node expand(Node node, List<Cell> moves) {
        Cell bestMove = selectBestMove(node.toCaptureGoBoard(), moves, node.getCurrentStone());
        Node child = node.createChildNode(bestMove);
        node.addChild(child);
        return child;
    }

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
                return (stoneToMove.equals(Cell.WHITE_O)) ? 1.0 : 0.0;
            }
            if (blueCaptured >= captureGoal) {
                return (stoneToMove.equals(Cell.BLUE_O)) ? 1.0 : 0.0;
            }

            List<Cell> possible = validMoves(simBoard);
            if (possible.isEmpty()) {
                return 0.5;
            }

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

        return 0.5;
    }

    private void backpropagate(Node node, double result) {
        Node current = node;
        while (current != null) {
            current.addVisit();
            current.addWin(result);
            current = current.getParent();
        }
    }

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

    private int applyCaptures(CaptureGoBoard board, String placedStone, int row, int col) {
        String opponentStone = placedStone.equals(Cell.WHITE_O) ? Cell.BLUE_O : Cell.WHITE_O;
        int capturedStones = 0;

        List<Cell> neighbors = board.getNeighbors(board.getCell(row, col));
        for (Cell neighbor : neighbors) {
            // Make sure neighbor is still the opponent's stone
            if (neighbor != null && neighbor.getState().equals(opponentStone)) {
                // BFS/DFS to get the entire group
                Set<Cell> group = findGroup(board, neighbor, opponentStone);

                // Check if group has liberties
                if (!hasLiberty(board, group)) {
                    // Capture them
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

    private String flipStone(String stone) {
        return stone.equals(Cell.WHITE_O) ? Cell.BLUE_O : Cell.WHITE_O;
    }

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

        return moves.get(RAND.nextInt(moves.size()));
    }

    private double evaluateMove(CaptureGoBoard board, Cell move, String stone) {
        double score = 0.0;
        int row = move.getRow() / 2;
        int col = move.getCol() / 2;

        // Example tweak:
        // Instead of always awarding +15 for having an opponent neighbor,
        // only give a bigger bonus if that neighbor can be captured.

        // 1) Check neighbors
        List<Cell> neighbors = board.getNeighbors(board.getCell(row, col));
        for (Cell neighbor : neighbors) {
            if (neighbor.getState().equals(flipStone(stone))) {
                // see if placing a stone here *would* actually capture that neighbor’s group
                // or at least put it in atari. For now, we just keep it simple:
                score += 8.0;
            } else if (neighbor.isEmpty()) {
                score += 2.0;
            }
        }

        // 2) Avoid suicides (self-capturing)
        int capturedByOpponent = simulateSelfCapture(board, move, stone);
        if (capturedByOpponent > 0) {
            score -= 100.0;
        }

        return score;
    }

    /**
     * Checks if placing 'stone' at 'move' on the given 'board' results in
     * immediately capturing the newly placed stone's group (i.e. a "suicide move").
     *
     * @param board the current board (we will make a copy inside).
     * @param move  the cell where we attempt to place our stone.
     * @param stone the color of the stone being placed.
     * @return the number of stones self-captured if this move kills its own group, or 0 otherwise.
     */
    private int simulateSelfCapture(CaptureGoBoard board, Cell move, String stone) {
        // 1) Make a temporary copy of the board, so we don't modify the real one
        int size = board.getSize();
        CaptureGoBoard tempBoard = new CaptureGoBoard(size);

        // Copy all cells from 'board' into 'tempBoard'
        copyBoard(board, tempBoard);

        // 2) Place the stone on the tempBoard
        int logicalRow = move.getRow() / 2;
        int logicalCol = move.getCol() / 2;
        tempBoard.setCell(logicalRow, logicalCol, stone);

        // 3) Gather the newly placed stone's group
        Cell placedCell = tempBoard.getCell(logicalRow, logicalCol);
        Set<Cell> group = findGroup(tempBoard, placedCell, stone);

        // 4) Check if this group has any liberties
        boolean hasLiberty = hasLiberty(tempBoard, group);
        if (!hasLiberty) {
            // No liberty → it's a self-capture (suicide)
            // Return how many stones in that group get removed
            return group.size();
        }
        return 0;
    }

    /**
     * Copy all intersection states from 'source' into 'destination'.
     */
    private void copyBoard(CaptureGoBoard source, CaptureGoBoard destination) {
        int size = source.getSize();
        for (int r = 0; r <= size; r++) {
            for (int c = 0; c <= size; c++) {
                // Get the current state on the source board
                String state = source.getCell(r, c).getState();
                // Set the same state on the destination board
                destination.setCell(r, c, state);
            }
        }
    }



    private int countLiberties(CaptureGoBoard board, int row, int col, String stone) {
        int liberties = 0;
        List<Cell> neighbors = board.getNeighbors(board.getCell(row, col));
        for (Cell neighbor : neighbors) {
            if (neighbor.isEmpty()) {
                liberties++;
            }
        }
        return liberties;
    }
}