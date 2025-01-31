package Testing;

import Game.CaptureGoBoard;
import Game.Cell;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CaptureGoBoardTest {

    /**
     * Tests placing a stone on the board.
     */
    @Test
    public void testPlaceStone() {
        CaptureGoBoard board = new CaptureGoBoard(7);
        board.setCell(0, 0, Cell.BLUE_O);
        assertEquals(Cell.BLUE_O, board.getCell(0, 0).getState());
    }

    /**
     * Tests if a move is valid.
     */
    @Test
    public void testIsValidMove() {
        CaptureGoBoard board = new CaptureGoBoard(7);
        assertTrue(board.isValidMove(0, 0));
        board.setCell(0, 0, Cell.BLUE_O);
        assertFalse(board.isValidMove(0, 0));
    }

    /**
     * Tests edge cases for move validity.
     */
    @Test
    public void testEdgeCases() {
        CaptureGoBoard board = new CaptureGoBoard(7);
        assertFalse(board.isValidMove(-1, 0));
        assertFalse(board.isValidMove(0, -1));
        assertFalse(board.isValidMove(8, 0));
        assertFalse(board.isValidMove(0, 8));
    }

    /**
     * Tests getting the size of the board.
     */
    @Test
    public void testGetSize() {
        CaptureGoBoard board = new CaptureGoBoard(7);
        assertEquals(7, board.getSize());
    }

    /**
     * Tests rendering the board and checks the first symbols.
     */
    @Test
    public void testRender() {
        CaptureGoBoard board = new CaptureGoBoard(7);
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        board.render();

        String output = outContent.toString();
        assertTrue(output.startsWith("  +  ━━━━━"), "The first symbol should be '+' and the second symbol should be '━━━━━'");

        // Reset the standard output
        System.setOut(System.out);
    }
}