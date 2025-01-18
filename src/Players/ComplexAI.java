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
        this.mcts = new MonteCarloTreeSearch();
        this.simulations = simulations;
        this.exploration = exploration;
    }

    @Override
    public Cell chooseMove(CaptureGoBoard board) {
        Node root = new Node(board.boardDeepCopy(), null, getStone(), null, 0, 0);

        Node bestChild = mcts.runMCTS(root, simulations);

        if (bestChild == null) {
            return null;
        }

        return bestChild.getMoveMade();
    }
}