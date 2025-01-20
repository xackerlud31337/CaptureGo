package Game;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private String name;
    private String stone;             // Stone representation (Cell.WHITE_O or Cell.BLUE_O);
    private List<Cell> occupiedCells;
    private int capturedStones;


    public Player(String name, String stone) {
        this.name = name;
        this.stone = stone;
        this.occupiedCells = new ArrayList<>();
    }

    /**
     * Get the name of the player.
     * @return the name of the player
     */
    public String getName() {
        return name;
    }

    /**
     * Get the stone representation of the player.
     * @return the stone representation of the player
     */
    public String getStone() {
        return stone;
    }

    /**
     * Set the stone of the player.
     * @param stone the stone to set
     */
    public void setStone(String stone) {
        this.stone = stone;
    }

    /**
     * Add a cell to the player's occupied cells list.
     * @param cell the cell to add
     */
    public void addCell(Cell cell) {
        if (cell != null) {
            occupiedCells.add(cell);
        }
    }

    /**
     * Get the list of cells occupied by the player.
     * @return the list of cells occupied by the player
     */
    public List<Cell> getOccupiedCells() {
        return occupiedCells;
    }

    /**
     * Reset the list of occupied cells.
     */
    public void resetOccupiedCells() {
        occupiedCells.clear();
    }

    /**
     * Remove a cell from the player's occupied cells list.
     * @param cell the cell to remove
     */
    public void removeCell(Cell cell) {
        if (cell != null) {
            occupiedCells.remove(cell);
        }
    }

    /**
     * Add to the player's captured stones count.
     * @param count the number of stones captured.
     */
    public void addCapturedStones(int count) {
        this.capturedStones += count;
    }

    /**
     * Get the number of stones captured by the player.
     * @return the number of captured stones.
     */
    public int getCapturedStones() {
        return capturedStones;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name + " (" + (stone.equals(Cell.WHITE_O) ? "White" : "Blue") + ")";
    }
}
