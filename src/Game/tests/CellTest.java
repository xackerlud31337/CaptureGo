package Game.tests;

import Game.Cell;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CellTest {

    @Test
    public void testSetState() {
        Cell cell = new Cell(0, 0);
        cell.setState(Cell.BLUE_O);
        assertEquals(Cell.BLUE_O, cell.getState());
    }

    @Test
    public void testIsEmpty() {
        Cell cell = new Cell(0, 0);
        assertTrue(cell.isEmpty());
        cell.setState(Cell.BLUE_O);
        assertFalse(cell.isEmpty());
    }

    @Test
    public void testReset() {
        Cell cell = new Cell(0, 0);
        cell.setState(Cell.BLUE_O);
        cell.reset();
        assertTrue(cell.isEmpty());
    }
}