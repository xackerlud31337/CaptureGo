package Testing;

import Network.Server.ClientHandler;
import Network.Server.ServerImp;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.IOException;

public class ClientHandlerTest {

    @Test
    public void testReceiveUsername() throws IOException {
        // We can create a minimal server object
        ServerImp server = new ServerImp(4444);
        ClientHandler handler = new ClientHandler(server);

        assertEquals("Unknown", handler.getName()); // from Player constructor
        handler.receiveUsername("Alice");
        assertEquals("Alice", handler.getUsername());
        assertEquals("Alice", handler.getName());

        server.close();
    }

    @Test
    public void testHandleDisconnect() throws IOException {
        ServerImp server = new ServerImp(0);
        ClientHandler handler = new ClientHandler(server);

        server.addClient(handler);
        assertFalse(server.getClients().isEmpty());

        handler.handleDisconnect();
        // verify the server does not contain the client
        assertTrue(server.getClients().isEmpty());

        server.close();
    }
}

