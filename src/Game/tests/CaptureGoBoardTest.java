package Game.tests;

import Game.CaptureGoBoard;
import Game.Cell;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CaptureGoBoardTest {

    @Test
    public void testPlaceStone() {
        CaptureGoBoard board = new CaptureGoBoard(7);
        board.setCell(0, 0, Cell.BLUE_O);
        assertEquals(Cell.BLUE_O, board.getCell(0, 0).getState());
    }

    @Test
    public void testIsValidMove() {
        CaptureGoBoard board = new CaptureGoBoard(7);
        assertTrue(board.isValidMove(0, 0));
        board.setCell(0, 0, Cell.BLUE_O);
        assertFalse(board.isValidMove(0, 0));
    }

    @Test
    public void testEdgeCases() {
        CaptureGoBoard board = new CaptureGoBoard(7);
        assertFalse(board.isValidMove(-1, 0));
        assertFalse(board.isValidMove(0, -1));
        assertFalse(board.isValidMove(7, 0));
        assertFalse(board.isValidMove(0, 7));
    }
}