package Game;

public class Cell {
    public static final String WHITE_O = "\033[37mO\033[0m"; // White stone ―――
    public static final String BLUE_O = "\033[34mO\033[0m";  // Blue stone (representing black stone)


    private String state; // State of the cell: "+", WHITE_O, or BLUE_O
    private final int row; // Row position of the cell
    private final int column;


    /**
     * Create a new cell with the given row and column position.
     * @param row the row position of the cell
     * @param column the column position of the cell
     */
    public Cell(int row, int column) {
        this.state = "+";
        this.row = row;
        this.column = column;// Default is an empty intersection
    }

    /**
     * Check if the cell is empty.
     * @return true if the cell is empty, false otherwise;
     */
    public boolean isEmpty() {
        return state.equals("+");
    }

    /**
     * Set the state of the cell.
     * @param state the state to set
     */
    public void setState(String state) {
        if (!state.equals(WHITE_O) && !state.equals(BLUE_O) && !state.equals("+")) {
            throw new IllegalArgumentException("Invalid cell state!");
        }
        this.state = state;
    }

    /**
     * Get the state of the cell.
     * @return the state of the cell
     */
    public String getState() {
        return state;
    }

    /**
     * Get the row position of the cell.
     * @return the row position
     */
    public int getRow() {
        return row;
    }

    /**
     * Get the column position of the cell.
     * @return the column position
     */
    public int getCol() {
        return column;
    }

    /**
     * Reset the cell to an empty intersection.
     */
    public void reset() {
        this.state = "+";
    }


    @Override
    public String toString() {
        return state;
    }
}
