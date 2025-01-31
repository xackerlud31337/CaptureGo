package Testing;

import Game.CaptureGoGame;
import Game.CaptureGoBoard;
import Game.Cell;
import Game.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CaptureGoGame.
 */
public class CaptureGoGameTest {

    private CaptureGoGame game;
    private Player player1;
    private Player player2;
    private CaptureGoBoard board;
    private final int boardSize = 5;
    private final int captureGoal = 3;

    @BeforeEach
    public void setUp() {
        player1 = new Player("Player1", Cell.WHITE_O);
        player2 = new Player("Player2", Cell.BLUE_O);

        game = new CaptureGoGame(boardSize, player1, player2, captureGoal);
        board = game.getBoard();
    }

    @Test
    public void testConstructor() {
        assertNotNull(game, "Game object should be created successfully.");
        assertNotNull(board, "Board should not be null after game is constructed.");
        // We can also verify that no one has captured stones yet
        assertEquals(0, player1.getCapturedStones(), "Player1 starts with 0 captures.");
        assertEquals(0, player2.getCapturedStones(), "Player2 starts with 0 captures.");
    }

    @Test
    public void testIsSuicidalCell_True() {
        Cell[][] boardCopy = board.boardDeepCopy();

        // Mark center as WHITE
        boardCopy[4][4].setState(Cell.WHITE_O);

        // Orthogonal neighbors at ±2
        boardCopy[4][2].setState(Cell.WHITE_O);
        boardCopy[4][6].setState(Cell.WHITE_O);
        boardCopy[2][4].setState(Cell.WHITE_O);
        boardCopy[6][4].setState(Cell.WHITE_O);

        // Because all neighbors are WHITE (no empty neighbor),
        // the cell at [4][4] has no liberties => suicidal.
        assertTrue(
                game.isSuicidalCell(boardCopy, 4, 4),
                "Cell [4][4] should be suicidal (no empty liberties)."
        );
    }

    @Test
    public void testIsSuicidalCell_False_HasLiberty() {
        Cell[][] boardCopy = board.boardDeepCopy();
        // Mark center as WHITE
        boardCopy[4][4].setState(Cell.WHITE_O);

        // Give it at least one empty neighbor => NOT suicidal
        boardCopy[4][2].setState(Cell.WHITE_O);

        assertFalse(
                game.isSuicidalCell(boardCopy, 4, 4),
                "Cell [4][4] should NOT be suicidal because it has at least one liberty."
        );
    }

    @Test
    public void testMakeMove_Valid() {

        game.makeMove(2, 3, player1, captureGoal);

        assertEquals(
                Cell.WHITE_O,
                board.getCell(2, 3).getState(),
                "makeMove(...) should place a WHITE stone at (2,3)."
        );

        // Check that it switched the turn (if you track that publicly).
        // We can't directly check isPlayer1turn because it's private,
        // but we can see if the next move is for player2, or observe that
        // the board didn't block the next move for player2, etc.
        // For minimal coverage, we just confirm the stone was placed.
    }

    @Test
    public void testMakeMove_Invalid() {
        board.setCell(2, 3, Cell.WHITE_O); // Already occupied
        game.makeMove(2, 3, player1, captureGoal);  // Should say "Invalid move" and do nothing

        // It should remain WHITE_O, no second stone placed
        assertEquals(
                Cell.WHITE_O,
                board.getCell(2, 3).getState(),
                "Cell (2,3) should remain WHITE_O (still occupied)."
        );
    }

    @Test
    public void testMakeMove_TriggersCapture() {
        /*
         * We'll set up a scenario where player2 has a stone that is about to be surrounded.
         * Then player1 places a stone to finalize the capture.
         */
        // Suppose we place BLUE at (2,2).
        board.setCell(2, 2, Cell.BLUE_O);
        player2.addCell(board.getCell(2, 2));

        // Surround it with WHITE except for one liberty
        board.setCell(2, 1, Cell.WHITE_O);
        player1.addCell(board.getCell(2, 1));
        board.setCell(2, 3, Cell.WHITE_O);
        player1.addCell(board.getCell(2, 3));
        board.setCell(1, 2, Cell.WHITE_O);
        player1.addCell(board.getCell(1, 2));
        // The last liberty is presumably (3,2).

        // Now place the final stone at (3,2) => capturing the BLUE stone
        game.makeMove(3, 2, player1, captureGoal);

        // After capture, the BLUE stone at (2,2) should turn WHITE
        assertEquals(
                Cell.WHITE_O,
                board.getCell(2, 2).getState(),
                "The BLUE stone at (2,2) should be captured and turned WHITE."
        );
        assertEquals(
                1,
                player1.getCapturedStones(),
                "Player1 should have gained 1 captured stone."
        );
    }

    @Test
    public void testCaptureStones_SingleStone() {
        // Place a single BLUE stone at (2,2).
        Cell stone = board.getCell(2, 2);
        stone.setState(Cell.BLUE_O);
        player2.addCell(stone);

        // Surround with WHITE
        board.getCell(2, 1).setState(Cell.WHITE_O); player1.addCell(board.getCell(2, 1));
        board.getCell(2, 3).setState(Cell.WHITE_O); player1.addCell(board.getCell(2, 3));
        board.getCell(1, 2).setState(Cell.WHITE_O); player1.addCell(board.getCell(1, 2));
        board.getCell(3, 2).setState(Cell.WHITE_O); player1.addCell(board.getCell(3, 2));

        // Manually call captureStones
        game.captureStones(stone, player2);

        // Should be captured => now WHITE
        assertEquals(
                Cell.WHITE_O,
                board.getCell(2, 2).getState(),
                "The single BLUE stone was captured and converted to WHITE."
        );
        assertEquals(
                1,
                player1.getCapturedStones(),
                "Player1's captured count should increase by 1."
        );
    }

    @Test
    public void testCaptureStones_NoCaptureIfLibertyExists() {

        // Place a single BLUE stone at (2,2).
        Cell stone = board.getCell(2, 2);
        stone.setState(Cell.BLUE_O);
        player2.addCell(stone);

        // Surround with WHITE on three sides, but leave one side empty => a liberty
        board.getCell(2, 1).setState(Cell.WHITE_O);
        board.getCell(2, 3).setState(Cell.WHITE_O);
        board.getCell(1, 2).setState(Cell.WHITE_O);
        // We'll leave (3,2) empty

        game.captureStones(stone, player2);

        // The stone should remain BLUE, not captured
        assertEquals(
                Cell.BLUE_O,
                board.getCell(2, 2).getState(),
                "Stone should NOT be captured due to having a liberty at (3,2)."
        );
        assertEquals(
                0,
                player1.getCapturedStones(),
                "Player1 capture count remains 0."
        );
    }

    @Test
    public void testPlaceStone_Valid() {
        game.placeStone(1, 1, player1);  // logical intersection
        assertEquals(
                Cell.WHITE_O,
                board.getCell(1, 1).getState(),
                "Stone should be placed at (1,1)."
        );
        assertTrue(player1.getOccupiedCells().contains(board.getCell(1, 1)),
                   "Player1's occupied cells should include (1,1).");
    }

    @Test
    public void testPlaceStone_InvalidOccupied() {
        // Occupy (1,1) first
        board.setCell(1, 1, Cell.WHITE_O);

        assertThrows(
                IllegalStateException.class,
                () -> game.placeStone(1, 1, player1),
                "Should throw IllegalStateException for an occupied intersection."
        );
    }
    @Test
    public void testCheckWinner_NoWinnerYet() {
        assertNull(game.checkWinner(), "No captures => no winner => return null.");
    }

    @Test
    public void testCheckWinner_Player1Wins() {
        // Force Player1 to meet or exceed captureGoal
        player1.addCapturedStones(captureGoal);
        assertEquals(player1, game.checkWinner(),
                     "Player1 reached captureGoal => should be winner.");
    }

    @Test
    public void testCheckWinner_Player2Wins() {
        // Force Player2 to meet or exceed captureGoal
        player2.addCapturedStones(captureGoal);
        assertEquals(player2, game.checkWinner(),
                     "Player2 reached captureGoal => should be winner.");
    }

    @Test
    public void testGetBoard() {
        CaptureGoBoard returnedBoard = game.getBoard();
        assertNotNull(returnedBoard, "getBoard() should return a non-null board.");
        assertSame(board, returnedBoard, "getBoard() should return the same board instance used by the game.");
    }
}
