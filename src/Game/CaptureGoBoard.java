package Game;

import java.util.Scanner;

public class CaptureGoBoard {
    private final int size; // Number of playable intersections
    private Cell[][] grid;  // Full grid of Cell objects, including lines

    // Constructor
    public CaptureGoBoard(int size) {
        this.size = size; // Number of playable intersections
        initializeBoard();
    }

    // Initialize the board
    private void initializeBoard() {
        grid = new Cell[size * 2 - 1][size * 2 - 1]; // Adjust for proper grid dimensions
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                if (i % 2 == 0 && j % 2 == 0) {
                    grid[i][j] = new Cell(); // Create a Cell for each intersection
                } else {
                    grid[i][j] = null; // Non-intersections are left as null
                }
            }
        }
    }

    // Place a stone on the board
    public void placeStone(int row, int col, Player player) {
        int actualRow = row * 2; // Map row to grid coordinates
        int actualCol = col * 2; // Map column to grid coordinates
        Cell cell = grid[actualRow][actualCol];
        if (cell == null || !cell.isEmpty()) {
            throw new IllegalStateException("Invalid move: Intersection already occupied!");
        }
        cell.setState(player.getStone()); // Set the stone using the Player's stone type
        player.addCell(cell); // Add the cell to the player's occupied list
    }

    // Render the board
    public void render() {
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                if (grid[i][j] != null) {
                    System.out.print(grid[i][j].toString());
                } else if (i % 2 == 0) {
                    System.out.print("---");
                } else if (j % 2 == 0) {
                    System.out.print("|");
                } else {
                    System.out.print("   ");
                }
            }
            System.out.println();
        }
    }

    // Check if a move is valid
    public boolean isValidMove(int row, int col) {
        if (row < 0 || row >= size || col < 0 || col >= size) {
            return false; // Input is outside playable grid
        }
        int actualRow = row * 2; // Map row to grid coordinates
        int actualCol = col * 2; // Map column to grid coordinates
        Cell cell = grid[actualRow][actualCol];
        return cell != null && cell.isEmpty();
    }

    // Playable game loop
    public void playGame(Player player1, Player player2) {
        Scanner scanner = new Scanner(System.in);
        boolean isPlayer1Turn = true;

        while (true) {
            render(); // Display the board
            Player currentPlayer = isPlayer1Turn ? player1 : player2;
            System.out.println(currentPlayer.getName() + "'s turn (" + (isPlayer1Turn ? "White" : "Blue") + ").");

            System.out.print("Enter row (0 to " + (size - 1) + ") or -1 to quit: ");
            int row = scanner.nextInt();
            if (row == -1) break;

            System.out.print("Enter column (0 to " + (size - 1) + "): ");
            int col = scanner.nextInt();

            if (isValidMove(row, col)) {
                placeStone(row, col, currentPlayer);
                isPlayer1Turn = !isPlayer1Turn; // Switch turns
            } else {
                System.out.println("Invalid move. Make sure the row and column are within 0 to " + (size - 1) + " and the spot is not occupied.");
            }
        }

        System.out.println("Game over! Final board:");
        render();
    }

    public static void main(String[] args) {
        CaptureGoBoard board = new CaptureGoBoard(5);

        // Players
        Player whitePlayer = new Player("Alice", Cell.WHITE_O); // White stones
        Player bluePlayer = new Player("Bob", Cell.BLUE_O);     // Blue stones (black stones)

        // Play the game
        board.playGame(whitePlayer, bluePlayer);
    }
}
