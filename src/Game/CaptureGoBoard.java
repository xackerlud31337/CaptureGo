package Game;

import Players.NaiveAI;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CaptureGoBoard {
    private final int size;
    private Cell[][] grid;

    public CaptureGoBoard(int size) {
        this.size = size;
        initializeBoard();
    }

    /**
     * Get the size of the board.
     * @return the size of the board (logical size, not including the extra grid spaces for intersections)
     */
    public int getSize() {
        return size;
    }

    /**
     * Initialize the board with empty cells.
     */
    private void initializeBoard() {
        grid = new Cell[size * 2 + 1][size * 2 + 1];
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                if (i % 2 == 0 && j % 2 == 0) {
                    grid[i][j] = new Cell(i, j);
                } else {
                    grid[i][j] = null; // Non-intersections are left as null
                }
            }
        }
    }

    /**
     * This is the method that will be used to create a deep copy of the board.
     * @return a deep copy of the board
     */
    public Cell[][] boardDeepCopy() {
        Cell[][] boardCopy = new Cell[size * 2 + 1][size * 2 + 1];
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                if (grid[i][j] != null) {
                    boardCopy[i][j] = new Cell(i, j);
                    boardCopy[i][j].setState(grid[i][j].getState());
                } else {
                    boardCopy[i][j] = null;
                }
            }
        }
        return boardCopy;
    }

    /**
     * Get the size of the board.
     * @return the size of the board
     */
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

    /**
     * Check if the move is valid.
     * @param row the row of the move
     * @param col the column of the move
     * @return true if the move is valid, false otherwise
     */
    public boolean isValidMove(int row, int col) {
        int actualRow = row * 2;
        int actualCol = col * 2;
        if (actualRow < 0 || actualRow >= grid.length || actualCol < 0 || actualCol >= grid[0].length) {
            return false;
        }
        Cell cell = grid[actualRow][actualCol];
        return cell != null && cell.isEmpty();
    }

    /**
     * Get the neighbors of a cell.
     * @param cell the cell to get neighbors of
     * @return the list of neighbors
     */
    public List<Cell> getNeighbors(Cell cell) {
        List<Cell> neighbors = new ArrayList<>();
        int[][] directions = {{2, 0}, {-2, 0}, {0, 2}, {0, -2}}; // Move to adjacent intersections

        for (int[] dir : directions) {
            int newRow = cell.getRow() + dir[0];
            int newCol = cell.getCol() + dir[1];

            // Ensure the neighbor is within bounds and is a valid intersection
            if (newRow >= 0 && newRow < grid.length && newCol >= 0 && newCol < grid[0].length) {
                Cell neighbor = grid[newRow][newCol];
                if (neighbor != null) { // Check for valid intersection
                    neighbors.add(neighbor);
                }
            }
        }
        return neighbors;
    }

    /**
     * Set the cell at the given row and column to the specified state.
     *
     * @param row   The logical row (not actual index) of the cell.
     * @param col   The logical column (not actual index) of the cell.
     * @param state The state to set the cell to.
     */
    public void setCell(int row, int col, String state) {
        int actualRow = row * 2;
        int actualCol = col * 2;
        grid[actualRow][actualCol].setState(state);
    }

    /**
     * Retrieve a cell at the given row and column.
     *
     * @param row The logical row (not actual index) of the cell.
     * @param col The logical column (not actual index) of the cell.
     * @return The cell at the specified location.
     */
    public Cell getCell(int row, int col) {
        int actualRow = row * 2;
        int actualCol = col * 2;

        if (actualRow < 0 || actualRow >= grid.length || actualCol < 0 || actualCol >= grid[0].length) {
            throw new IllegalArgumentException("Invalid row or column: Out of bounds!");
        }

        return grid[actualRow][actualCol];
    }

    public boolean isFull() {
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                if (grid[i][j] != null && grid[i][j].isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }
}
