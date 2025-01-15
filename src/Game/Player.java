package Game;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private String name;              // Player's name
    private String stone;             // Stone representation (Cell.WHITE_O or Cell.BLUE_O)
    private List<Cell> occupiedCells; // Cells occupied by the player

    // Constructor
    public Player(String name, String stone) {
        this.name = name;
        if (!stone.equals(Cell.WHITE_O) && !stone.equals(Cell.BLUE_O)) {
            throw new IllegalArgumentException("Invalid stone type! Use Cell.WHITE_O or Cell.BLUE_O.");
        }
        this.stone = stone;
        this.occupiedCells = new ArrayList<>();
    }

    // Get the player's name
    public String getName() {
        return name;
    }

    // Get the player's stone type
    public String getStone() {
        return stone;
    }

    // Add a cell to the player's occupied list
    public void addCell(Cell cell) {
        if (cell != null) {
            occupiedCells.add(cell);
        }
    }

    // Get the list of cells occupied by the player
    public List<Cell> getOccupiedCells() {
        return occupiedCells;
    }

    // Reset the player's occupied cells
    public void resetOccupiedCells() {
        occupiedCells.clear();
    }

    @Override
    public String toString() {
        return name + " (" + (stone.equals(Cell.WHITE_O) ? "White" : "Blue") + ")";
    }
}
