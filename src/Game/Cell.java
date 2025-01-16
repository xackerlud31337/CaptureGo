package Game;

public class Cell {
    public static final String WHITE_O = "\033[37mO\033[0m"; // White stone
    public static final String BLUE_O = "\033[34mO\033[0m";  // Blue stone (representing black stone)


    private String state; // State of the cell: "+", WHITE_O, or BLUE_O


    public Cell() {
        this.state = "+"; // Default is an empty intersection
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
