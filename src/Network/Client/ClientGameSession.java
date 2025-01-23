package Network.Client;

import Game.CaptureGoBoard;
import Game.Cell;
import Game.Player;

public class ClientGameSession {
    private final CaptureGoBoard board;
    private final Player player1;
    private final Player player2;
    private boolean player1Turn;

    public ClientGameSession(Player player1, Player player2){
        this.board = new CaptureGoBoard(6);
        this.player1 = player1;
        this.player2 = player2;
        this.player1Turn = true;
    }

    public synchronized void placeStone(int row, int col){
        Player currentPlayer = player1Turn ? player1 : player2;
        if(board.isValidMove(row, col)) {
            board.setCell(row, col, currentPlayer.getStone());
            player1Turn = !player1Turn;
        } else{
            throw new IllegalArgumentException("Invalid move: Intersection already taken or out of bounds!");
        }
    }

    public synchronized CaptureGoBoard getBoard(){
        return board;
    }

    public synchronized Cell[][] deepCopyBoard(){
        return board.boardDeepCopy();
    }

    public synchronized Player getCurrentPlayer(){
        return player1Turn ? player1 : player2;
    }

    public synchronized boolean isPlayer1Turn(){
        return player1Turn;
    }
}