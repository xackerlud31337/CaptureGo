package Game;

public class Cell {
    // ANSI color codes for white and blue
    public static final String WHITE_O = "\033[37mO\033[0m"; // White stone
    public static final String BLUE_O = "\033[34mO\033[0m";  // Blue stone (representing black stone)

    private String state; // State of the cell: "+", WHITE_O, or BLUE_O

    // Constructor
    public Cell() {
        this.state = "+"; // Default is an empty intersection
    }

    // Check if the cell is empty
    public boolean isEmpty() {
        return state.equals("+");
    }

    // Set the state of the cell
    public void setState(String state) {
        if (!state.equals(WHITE_O) && !state.equals(BLUE_O) && !state.equals("+")) {
            throw new IllegalArgumentException("Invalid cell state!");
        }
        this.state = state;
    }

    // Get the state of the cell
    public String getState() {
        return state;
    }

    // Reset the cell to its default state
    public void reset() {
        this.state = "+";
    }

    @Override
    public String toString() {
        return state;
    }
}
