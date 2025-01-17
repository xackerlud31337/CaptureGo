package Game;

import java.util.*;
import Players.NaiveAI;

public class CaptureGoGame {
    private CaptureGoBoard board;
    private final Player player1;
    private final Player player2;
    private boolean isPlayer1turn;
    private Cell[][] boardCopy;
    private final int size;

    public CaptureGoGame(int size, Player player1, Player player2) {
        this.board = new CaptureGoBoard(size);
        this.size = size;
        this.player1 = player1;
        this.player2 = player2;
        this.isPlayer1turn = true;
        playGame(player1, player2);
    }

    /**
     * This method will check if the cell is surrounded by different stones.
     * If the cell is surrounded by different stones, then the cell is not captured.
     * If the cell is surrounded by the same stones, then the cell is captured.
     * @param boardCopy is the copy of the board.
     * @param i is the row of the cell.
     * @param j is the column of the cell.
     * @return true if the cell is surrounded by different stones, false otherwise.
     */
    private boolean isSurroundedByDifferentStones(Cell[][] boardCopy, int i, int j) {
        List<Cell> neighbours = board.getNeighbors(boardCopy[i][j]);
        for (Cell neighbour : neighbours) {
            if (neighbour.getState().equals(boardCopy[i][j].getState())) {
                return false;
            }
        }
        return true;
    }

    /**
     * This method will check if the cell is suicidal.
     * A move to be suicidal, it must be surrounded by the same stones and there must be no free cell around it.
     * And the cells surrounding the similar stones to be surrounded by different stones.
     * @param boardCopy is the copy of the board.
     * @param i is the row of the cell.
     * @param j is the column of the cell.
     * @return true if the cell is suicidal, false otherwise.
     */
    private boolean isSuicidalCell(Cell[][] boardCopy, int i, int j) {
        int[][] directions = {{2, 0}, {-2, 0}, {0, 2}, {0, -2}}; // Orthogonal directions only

        // A cell is suicidal if it has no liberties (free cells around it)
        boolean hasLiberties = false;

        for (int[] dir : directions) {
            int newRow = i + dir[0];
            int newCol = j + dir[1];

            // Ensure the neighbor is within bounds
            if (newRow >= 0 && newRow < boardCopy.length && newCol >= 0 && newCol < boardCopy[0].length) {
                Cell neighbor = boardCopy[newRow][newCol];

                // If there's an empty cell (liberty), it's not suicidal
                if (neighbor != null && neighbor.isEmpty()) {
                    hasLiberties = true;
                    break; // Stop checking further if we found a liberty
                }
            }
        }

        return !hasLiberties; // If no liberties, the cell is suicidal
    }


    /**
     * This method will check if there is a free cell around the cell.
     * @param boardCopy is the copy of the board.
     * @param i is the row of the cell.
     * @param j is the column of the cell.
     * @return true if there is a free cell around the cell, false otherwise.
     */
    private boolean isThereFreeCell(Cell[][] boardCopy, int i, int j) {
        List<Cell> neighbours = board.getNeighbors(boardCopy[i][j]);
        for (Cell neighbour : neighbours) {
            if (neighbour.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method will check if the cell is free to place or if you get captured immediately.
     * If the cell is captured, then the stones in the chain will be captured.
     * @param i is the row of the cell.
     * @param j is the column of the cell
     */
    public void makeMove(int i, int j, Player currentPlayer, int captureGoal) {
        if (!board.isValidMove(i, j)) {
            System.out.println("Invalid move. Try again.");
            return;
        }

        // Place the stone on the board
        board.setCell(i, j, currentPlayer.getStone());

        // Update the current player's occupied cells
        Cell placedCell = board.getCell(i, j);
        currentPlayer.addCell(placedCell);

        // Check for captures for the opponent
        Player opponent = (currentPlayer == player1) ? player2 : player1;
        List<Cell> opponentStones = new ArrayList<>(opponent.getOccupiedCells());
        for (Cell opponentStone : opponentStones) {
            captureStones(opponentStone, opponent);
        }

        // Check for winner
        Player winner = checkWinner();
        if (winner != null) {
            System.out.println("The winner is " + winner.getName() + "!");
            gameOver = true;
            return; // Exit the method gracefully
        }

        // Switch turns
        isPlayer1turn = !isPlayer1turn;
    }

    /**
     * Captures a stone or group of stones if they have no liberties.
     * @param stone the stone being checked for capture
     * @param owner the player who owns the stone
     */
    private void captureStones(Cell stone, Player owner) {
        // Group of stones being checked for capture
        Set<Cell> group = new HashSet<>();
        // Set of liberties (adjacent empty spaces)
        Set<Cell> liberties = new HashSet<>();
        Queue<Cell> queue = new LinkedList<>();
        queue.add(stone);

        // Perform BFS to find the group and its liberties
        while (!queue.isEmpty()) {
            Cell current = queue.poll();

            // Skip if already part of the group
            if (group.contains(current)) continue;

            group.add(current);

            // Check all neighbors
            for (Cell neighbor : board.getNeighbors(current)) {
                if (neighbor.isEmpty()) {
                    liberties.add(neighbor); // Found a liberty
                } else if (neighbor.getState().equals(stone.getState())) {
                    queue.add(neighbor); // Part of the same group
                }
            }
        }

        // If no liberties, capture the entire group
        if (liberties.isEmpty()) {
            // Determine the capturing player
            Player capturingPlayer = (owner == player1) ? player2 : player1;

            // Update the captured stones to the capturing player's stone type
            for (Cell capturedStone : group) {
                capturedStone.setState(capturingPlayer.getStone()); // Change to capturing player's stone
                owner.removeCell(capturedStone); // Remove from the original owner's list
                capturingPlayer.addCell(capturedStone); // Add to the capturing player's list
            }

            // Update the capturing player's score
            capturingPlayer.addCapturedStones(group.size());
        }
    }


    /**
     * Places a stone on the board at the specified row and column.
     * @param row the row where the stone is to be placed
     * @param col the column where the stone is to be placed
     * @param player the player placing the stone
     */
    public void placeStone(int row, int col, Player player) {
        int actualRow = row * 2;
        int actualCol = col * 2;
        boardCopy = board.boardDeepCopy();


        // Validate that the move is on a valid, empty intersection
        if (!board.isValidMove(row, col)) {
            throw new IllegalStateException("Invalid move: Intersection already occupied or out of bounds!");
        }

        // Place the stone on the board
        makeMove(row, col, player, 10);

        // Add the cell to the player's occupied cells list
        player.addCell(boardCopy[actualRow][actualCol]);
    }

    public void playGame(Player player1, Player player2) {
        Scanner scanner = new Scanner(System.in);
        boolean isPlayer1Turn = true;

        while (!gameOver) {
            board.render();
            Player currentPlayer = isPlayer1Turn ? player1 : player2;
            System.out.println(currentPlayer.getName() + "'s turn (" + (isPlayer1Turn ? "White" : "Blue") + ").");

            int row, col;
            if (currentPlayer instanceof NaiveAI) {
                // Let the AI choose a move
                Cell move = ((NaiveAI) currentPlayer).chooseMove(board);
                if (move == null) {
                    System.out.println("No valid moves available. Game over!");
                    break;
                }
                row = move.getRow() / 2; // Convert actual row to logical row
                col = move.getCol() / 2; // Convert actual col to logical col
                System.out.println(currentPlayer.getName() + " chooses (" + row + ", " + col + ").");
            } else {
                // Let the human player make a move
                System.out.print("Enter row (0 to " + (size) + ") or -1 to quit: ");
                row = scanner.nextInt();
                if (row == -1) break;

                System.out.print("Enter column (0 to " + (size) + "): ");
                col = scanner.nextInt();
            }

            // Validate and process the move
            if (board.isValidMove(row, col)) {
                placeStone(row, col, currentPlayer);
                isPlayer1Turn = !isPlayer1Turn; // Switch turns
            } else {
                System.out.println("Invalid move. Make sure the row and column are within 0 to " + (size) + " and the spot is not occupied.");
            }
        }

        System.out.println("Game over! Final board:");
        board.render();
    }



    /**
     * Checks if there is a winner based on the capture-based victory condition.
     * If a player has captured the required number of stones, they are declared the winner.
     * @return the winning player, or null if there is no winner yet.
     */
    private Player checkWinner(int captureGoal) {
        if (player1.getCapturedStones() >= captureGoal) {
            return player1;
        } else if (player2.getCapturedStones() >= captureGoal) {
            return player2;
        }
        return null; // No winner yet
    }
}