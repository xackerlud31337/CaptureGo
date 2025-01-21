package Game.tests;

import Game.Player;
import Game.Cell;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PlayerTest {

    @Test
    public void testAddCell() {
        Player player = new Player("Player1", Cell.BLUE_O);
        Cell cell = new Cell(0, 0);
        player.addCell(cell);
        assertTrue(player.getOccupiedCells().contains(cell));
    }

    @Test
    public void testRemoveCell() {
        Player player = new Player("Player1", Cell.BLUE_O);
        Cell cell = new Cell(0, 0);
        player.addCell(cell);
        player.removeCell(cell);
        assertFalse(player.getOccupiedCells().contains(cell));
    }

    @Test
    public void testCapturedStones() {
        Player player = new Player("Player1", Cell.BLUE_O);
        player.addCapturedStones(3);
        assertEquals(3, player.getCapturedStones());
    }
}