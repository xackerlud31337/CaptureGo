package Players;

import Game.CaptureGoBoard;
import Game.Cell;
import Game.Player;

public class ComplexAI extends Player implements GoAI {

    private final MonteCarloTreeSearch mcts;
    private final int simulations;      // Number of MCTS simulations per move
    private final double exploration;   // Exploration constant (C in UCT formula)

    public ComplexAI(String name, String stone, int simulations, double exploration) {
        super(name, stone);  // Stone is passed to the parent, but we won't mutate it later
        this.mcts = new MonteCarloTreeSearch();
        this.simulations = simulations;
        this.exploration = exploration;
    }

    /**
     * Called by CaptureGoGame or TUI to pick a move for this AI.
     * @param board Current board state
     * @return A cell (row,col) to place a stone, or null if no moves are available
     */
    public Cell chooseMove(CaptureGoBoard board) {
        // Build a new Node (root) from the current board state, referencing this AI's stone.
        // 'currentStone' is the stone that is about to move (this AI).
        Node root = new Node(board.boardDeepCopy(), null, getStone(), null);

        // Run MCTS from this root
        Node bestChild = mcts.runSearch(root, simulations, exploration);

        // If there's no valid child, AI passes or there's no move
        if (bestChild == null) {
            return null;
        }

        // Return the move that got us to that best child
        return bestChild.getMoveMade();
    }
}
