package Network.Server;

public class ClientHandler {
    private final ServerImp server;
    private ServerConnection serverConnection;
    private String username;

    public ClientHandler(ServerImp server) {
        this.server = server;
    }

    protected void setConnection(ServerConnection serverConnection) {
        this.serverConnection = serverConnection;
    }

    protected ServerConnection getConnection() {
        return serverConnection;
    }

    public String getUsername() {
        return username;
    }

    protected void receiveUsername(String username) {
        this.username = username;
    }

    public void handleDisconnect() {
        server.removeClient(this);
    }
}
