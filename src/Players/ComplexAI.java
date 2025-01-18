package Players;

import Game.CaptureGoBoard;
import Game.Cell;
import Game.Player;

public class ComplexAI extends Player implements GoAI {

    private final MonteCarloTreeSearch mcts;
    private final int simulations;    // Number of MCTS simulations per move
    private final double exploration; // Exploration constant (C in UCT)

    public ComplexAI(String name, String stone, int simulations, double exploration) {
        super(name, stone);
        this.simulations = simulations;
        this.exploration = exploration;
        this.mcts = new MonteCarloTreeSearch();
    }

    @Override
    public Cell chooseMove(CaptureGoBoard board) {
        Node root = new Node(board.boardDeepCopy(), null, getStone(), null, 0, 0);
        // Run MCTS for the specified number of simulations, 2000 seems to be a good number
        Node bestChild = mcts.runMCTS(root, simulations);
        // If no child was found (e.g. no legal moves), return null, should be considered a pass, but we force a move anyway.
        if (bestChild == null) {
            return null;
        }
        return bestChild.getMove();
    }
}
