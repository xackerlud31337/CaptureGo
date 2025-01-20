package Network.Server;

import Game.Player;

public class ClientHandler extends Player {
    private final ServerImp server;
    private ServerConnection serverConnection;
    private String username;

    public ClientHandler(ServerImp server) {
        super("Unknown", "+");
        this.server = server;
    }

    protected void setConnection(ServerConnection serverConnection) {
        this.serverConnection = serverConnection;
    }

    public ServerConnection getConnection() {
        return serverConnection;
    }

    public String getUsername() {
        return username;
    }

    protected void receiveUsername(String username) {
        this.username = username;
        super.setName(username);
    }

    public void handleDisconnect() {
        server.removeClient(this);
    }
}
