package Players;

import Game.CaptureGoBoard;
import Game.Cell;

import java.util.List;
import java.util.Random;

public class MonteCarloTreeSearch {
    private static final Random RAND = new Random();

    public Node runSearch(Node root, int simulations, double explorationConstant) {
        // If no legal moves from the root, return null
        if (root.getLegalMoves().isEmpty()) {
            return null;
        }

        for (int i = 0; i < simulations; i++) {
            // 1. Selection
            Node selectedNode = select(root, explorationConstant);

            // 2. Expansion
            if (selectedNode.getVisits() > 0 && !selectedNode.getLegalMoves().isEmpty()) {
                selectedNode = expand(selectedNode);
            }

            // 3. Simulation
            double result = simulate(selectedNode);

            // 4. Backpropagation
            backpropagate(selectedNode, result);
        }

        // Pick child with the highest visit count
        Node bestChild = null;
        double bestVisits = -1;
        for (Node child : root.getChildren()) {
            if (child.getVisits() > bestVisits) {
                bestVisits = child.getVisits();
                bestChild = child;
            }
        }
        return bestChild;
    }

    /**
     * Selection: descend the tree using the UCT formula until reaching a leaf node.
     */
    private Node select(Node node, double c) {
        Node current = node;
        while (!current.getChildren().isEmpty()) {
            Node best = null;
            double bestUCT = Double.NEGATIVE_INFINITY;

            for (Node child : current.getChildren()) {
                double uctValue = uctValue(child, c);
                if (uctValue > bestUCT) {
                    bestUCT = uctValue;
                    best = child;
                }
            }

            if (best == null) break;
            current = best;
        }
        return current;
    }

    /**
     * Expansion: create a new child node from one untried move (if available).
     */
    private Node expand(Node node) {
        List<Cell> moves = node.getLegalMoves();
        if (moves.isEmpty()) {
            return node; // Can't expand
        }
        // Pick a random move
        Cell randomMove = moves.get(RAND.nextInt(moves.size()));
        Node child = node.createChildNode(randomMove);
        node.addChild(child);
        return child;
    }

    /**
     * Simulation: from the selected node's state, randomly play until terminal or a move limit.
     * Return 1 if the node's 'currentStone' eventually wins, else 0. (Heuristic example)
     */
    private double simulate(Node node) {
        // Create a temporary board from this node's state
        CaptureGoBoard simBoard = node.toCaptureGoBoard();
        String stone = node.getCurrentStone();

        // We'll do random moves up to a certain limit
        int moveLimit = 30;  // for example
        while (moveLimit-- > 0) {
            // Get all valid moves for the current stone
            List<Cell> validMoves = getAllValidMoves(simBoard, stone);
            if (validMoves.isEmpty()) {
                // If no moves, break
                break;
            }
            // pick a random move
            Cell move = validMoves.get(RAND.nextInt(validMoves.size()));
            simBoard.setCell(move.getRow()/2, move.getCol()/2, stone);

            // Switch stone
            stone = flipStone(stone);
        }

        // Simplistic: return 0.5 to represent a "drawish" outcome,
        // or 1.0 if we want to artificially favor the node's starting stone.
        // You can incorporate real capturing logic from simBoard if needed.
        return 0.5;
    }

    /**
     * Backpropagation: propagate the result (win=1, loss=0, or draw=0.5) up the tree.
     */
    private void backpropagate(Node node, double result) {
        Node current = node;
        while (current != null) {
            current.addVisit();
            current.addWin(result);
            current = current.getParent();
        }
    }

    /**
     * Upper Confidence Bound for Trees (UCT): (w / n) + c * sqrt(ln(N) / n)
     */
    private double uctValue(Node child, double c) {
        double w = child.getWins();
        double n = child.getVisits();
        double N = (child.getParent() == null) ? 1 : child.getParent().getVisits();

        if (n == 0) {
            return Double.MAX_VALUE;
        }
        return (w / n) + c * Math.sqrt(Math.log(N) / n);
    }

    /**
     * Return all valid moves for the given stone on simBoard.
     * This is effectively what we do in Node::getLegalMoves, but we need it here for the simulation.
     */
    private List<Cell> getAllValidMoves(CaptureGoBoard simBoard, String stone) {
        int size = simBoard.getSize();
        List<Cell> legalMoves = new java.util.ArrayList<>();
        for (int r = 0; r <= size; r++) {
            for (int c = 0; c <= size; c++) {
                if (simBoard.isValidMove(r, c)) {
                    legalMoves.add(new Cell(r*2, c*2));
                }
            }
        }
        return legalMoves;
    }

    private String flipStone(String stone) {
        return (Cell.WHITE_O.equals(stone)) ? Cell.BLUE_O : Cell.WHITE_O;
    }
}
