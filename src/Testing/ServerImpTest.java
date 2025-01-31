package Testing;
import Network.Server.ClientHandler;
import Network.Server.GameManagement.GameSession;
import Network.Server.ServerConnection;
import Network.Server.ServerImp;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Set;

public class ServerImpTest {

    private ServerImp server;

    @Before
    public void setUp() throws IOException {
        server = new ServerImp(4444);
    }

    @After
    public void tearDown() {
        server.close();
    }

    /**
     * Test adding a client to the server.
     */
    @Test
    public void testAddClient() {
        ClientHandler ch1 = new ClientHandler(server);
        ch1.receiveUsername("Alice");
        assertTrue(server.getClients().isEmpty());

        server.addClient(ch1);
        Set<ClientHandler> clients = server.getClients();
        assertEquals("Server should have exactly 1 client.", 1, clients.size());
        assertTrue("Server should contain ch1 in its client set.", clients.contains(ch1));
    }

    /**
     * Test removing a client from the server.
     */
    @Test
    public void testRemoveClient() {
        ClientHandler ch1 = new ClientHandler(server);
        ch1.receiveUsername("Alice");
        server.addClient(ch1);
        assertEquals(1, server.getClients().size());

        server.removeClient(ch1);
        assertTrue("After remove, client set should be empty.", server.getClients().isEmpty());
    }

    /**
     * Test adding two players to the queue and starting a game.
     */
//@Test
//public void testStartGame() {
//    // Create mock ServerConnection objects
//    ServerConnection connection1 = mock(ServerConnection.class);
//    ServerConnection connection2 = mock(ServerConnection.class);
//
//    // Two client handlers with mock connections
//    ClientHandler ch1 = new ClientHandler(server, connection1);
//    ch1.receiveUsername("Alice");
//    ClientHandler ch2 = new ClientHandler(server, connection2);
//    ch2.receiveUsername("Bob");
//
//    // Put them in the waiting queue
//    server.addToQueue(ch1);
//    server.addToQueue(ch2);
//
//    // Manually trigger startGame() (which is normally triggered by a background thread)
//    server.startGame();
//
//    // They should now appear as "in game"
//    assertTrue("playersInGame should contain ch1",
//               server.getPlayersInGame().containsKey(ch1) || server.getPlayersInGame().containsValue(ch1));
//    assertTrue("playersInGame should contain ch2",
//               server.getPlayersInGame().containsKey(ch2) || server.getPlayersInGame().containsValue(ch2));
//
//    // Also the gameSessions map should have a game for both
//    assertNotNull("Game session for ch1 should not be null", server.getGameSession(ch1));
//    assertNotNull("Game session for ch2 should not be null", server.getGameSession(ch2));
//}
//
//    /**
//     * Test removing a game session.
//     */
//    @Test
//    public void testRemoveGameSession() {
//        ClientHandler ch1 = new ClientHandler(server);
//        ClientHandler ch2 = new ClientHandler(server);
//
//        server.addToQueue(ch1);
//        server.addToQueue(ch2);
//        server.startGame();
//
//        GameSession session1 = server.getGameSession(ch1);
//        assertNotNull(session1);
//
//        server.removeGameSession(session1);
//        assertNull("Game session for ch1 should be removed", server.getGameSession(ch1));
//        assertNull("Game session for ch2 should be removed", server.getGameSession(ch2));
//    }
}
