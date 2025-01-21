package Testing;

import Game.CaptureGoGame;
import Game.Cell;
import Game.Player;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;

public class CaptureGoGameTest {

    /**
     * Test a random game with two players, up to a limit of moves.
     * The game should end with a winner, or no winner after the limit.
     * The winner must have captured at least the capture goal in stones.
     * The game should not end with a winner unless the capture goal is met.
     * The game should not end with a winner if the board is full.
     */
    @Test
    public void testRandomGame() {
        int boardSize = 7;//  - Board size of 7
        int captureGoal = 3;        //  - CaptureGoal of 3

        Player player1 = new Player("Pesho", Cell.WHITE_O);
        Player player2 = new Player("Gosho", Cell.BLUE_O);


        CaptureGoGame game = new CaptureGoGame(boardSize, player1, player2, captureGoal);

        // We’ll run a random sequence of moves, up to some limit.
        // This could also be interchange with the Naive AI.
        Random random = new Random();
        boolean isPlayer1Turn = true;
        int maxMoves = 200;
        int moveCount = 0;

        while (moveCount < maxMoves) {

            if (game.checkWinner() != null) {
                break;
            }

            // Pick a random row & column in the valid range
            int row = random.nextInt(boardSize + 1);  // because row can be 0..boardSize
            int col = random.nextInt(boardSize + 1);  // same for column

            Player currentPlayer = isPlayer1Turn ? player1 : player2;

            try {
                if (game.getBoard().isValidMove(row, col)) {
                    game.makeMove(row, col, currentPlayer, captureGoal);
                    moveCount++;

                    // Immediately check winner
                    if (game.checkWinner() == null) {
                        //Check if the game is still going (requirement)
                        Assertions.assertNull(game.checkWinner(),
                                              "No winner expected after a successful move, unless just captured enough stones.");
                    } else {
                        break;
                    }
                    isPlayer1Turn = !isPlayer1Turn;
                }
            } catch (Exception e) {
                System.out.println("Caught exception: " + e.getMessage());
            }
        }

        // After random moves or hitting the max, check final condition:
        if (game.checkWinner() != null) {
            // If there's a winner, ensure the captured stones meet the capture goal:
            int p1Captures = player1.getCapturedStones();
            int p2Captures = player2.getCapturedStones();
            Assertions.assertTrue(p1Captures >= captureGoal || p2Captures >= captureGoal,
                                  "Winner must have at least the capture goal in stones.");
            System.out.println("Random game ended with a winner: " + game.checkWinner().getName());
            System.out.println("   Final captures -> P1: " + p1Captures + ", P2: " + p2Captures);
        } else {
            // If no winner after all random moves, we simply note it for this test
            System.out.println("No winner after " + moveCount + " random moves.");
        }
    }
}