package Game.tests;

import Network.Client.ClientGameSession;
import Game.Player;
import Game.Cell;
import Network.Server.ClientHandler;
import Network.Server.GameManagement.GameSession;
import Network.Server.ServerImp;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ClientGameSessionTest {

    @Test
    public void testPlaceStone() {
        Player player1 = new Player("Player1", Cell.BLUE_O);
        Player player2 = new Player("Player2", Cell.WHITE_O);
        ClientGameSession session = new ClientGameSession(player1, player2);

        session.placeStone(0, 0);
        assertEquals(Cell.BLUE_O, session.getBoard().getCell(0, 0).getState());
        assertFalse(session.isPlayer1Turn()); // Player 1's turn should be false after placing a stone

        session.placeStone(0, 1);
        assertEquals(Cell.WHITE_O, session.getBoard().getCell(0, 1).getState());
        assertTrue(session.isPlayer1Turn()); // Player 1's turn should be true after player 2 places a stone
    }

    @Test
    public void testInvalidMove() {
        Player player1 = new Player("Player1", Cell.BLUE_O);
        Player player2 = new Player("Player2", Cell.WHITE_O);
        ClientGameSession session = new ClientGameSession(player1, player2);

        session.placeStone(0, 0);
        assertThrows(IllegalArgumentException.class, () -> session.placeStone(0, 0)); // Expecting IllegalArgumentException
    }

    @Test
    public void testGameOverCondition() {
        Player player1 = new Player("Player1", Cell.BLUE_O);
        Player player2 = new Player("Player2", Cell.WHITE_O);
        ClientGameSession session = new ClientGameSession(player1, player2);

        // Simulate moves leading to game over
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 7; j++) {
                if (session.getBoard().isValidMove(i, j)) {
                    session.placeStone(i, j);
                }
            }
        }

        assertTrue(session.getBoard().isFull());
    }

    @Test
    public void testWinnerDeduction() throws IOException {
        ServerImp server = new ServerImp(12345);
        ClientHandler client1 = new ClientHandler(server);
        ClientHandler client2 = new ClientHandler(server);
        client1.receiveUsername("Player1");
        client2.receiveUsername("Player2");
        client1.setStone(Cell.BLUE_O);
        client2.setStone(Cell.WHITE_O);
        GameSession gameSession = new GameSession(client1, client2, 7, 5, server);

        // Simulate moves leading to client1's win
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 7; j++) {
                if ((i + j) % 2 == 0 && gameSession.getBoard().isValidMove(i, j)) {
                    gameSession.makeMove(i, j);
                }
            }
        }

        assertEquals(client1, gameSession.checkWinner());
    }
}