package Network.Server;

import Network.Server.Base.SocketConnection;
import Network.Server.GameManagement.GameSession;
import Network.Server.Protocol.Protocol;
import java.io.IOException;
import java.net.Socket;

public class ServerConnection extends SocketConnection {

    ClientHandler clientHandler;
    ServerImp server;
    boolean loggedIn = false;
    private boolean helloNotReceived = false;

    protected ServerConnection(Socket socket) throws IOException {
        super(socket);
    }

    /**
     * Sets the client handler and server for this connection.
     * @param clientHandler the client handler to set
     * @param server the server to set
     */
    protected void setClientHandlerAndServer(ClientHandler clientHandler, ServerImp server){
        this.clientHandler = clientHandler;
        this.server = server;
        clientHandler.setConnection(this);
    }

    @Override
    protected void handleMessage(String message) {
        if (!helloNotReceived){
            if(message.startsWith(Protocol.HELLO)){
                helloNotReceived = true;
                sendMessage(Protocol.formatHello("Welcome from the Capture Go Game server!"));
            }
        } else if (message.startsWith(Protocol.LOGIN)) {
            // We don't check if the player is already in a game, and they send LOGIN again, return ALREADYLOGGEDIN
            if (loggedIn || server.getClients().stream().anyMatch(client -> client.getUsername().equals(message.split(Protocol.DELIMITER)[1]))){
                sendMessage(Protocol.ALREADYLOGGEDIN);
            }else{
                clientHandler.receiveUsername(message.split(Protocol.DELIMITER)[1]);
                loggedIn = true;
                System.out.println("Client has connected with username: " + message.split(Protocol.DELIMITER)[1]);
                sendMessage(Protocol.LOGIN);
                server.addClient(clientHandler);
            }
        }else{
            switch (message.split(Protocol.DELIMITER)[0]) {
                case Protocol.LIST -> sendMessage(Protocol.formatList(server.getClients()));
                case Protocol.QUEUE -> queuePlayer(clientHandler);
                case Protocol.MOVE -> {
                    GameSession gameSession = server.getGameSession(clientHandler);
                    if (gameSession != null) {
                        if (gameSession.getTurn() && gameSession.getPlayer1() == clientHandler) {
                            gameSession.queueMove(Integer.parseInt(message.split(Protocol.DELIMITER)[1]));
                        } else if (!gameSession.getTurn() && gameSession.getPlayer2() == clientHandler) {
                            gameSession.queueMove(Integer.parseInt(message.split(Protocol.DELIMITER)[1]));
                        } else {
                            sendMessage(Protocol.formatError("It is not your turn."));
                        }
                    } else {
                        sendMessage(Protocol.formatError("You are not in a game."));
                    }
                }
                default -> sendMessage(Protocol.formatError("Invalid message received: " + message));
            }
        }
    }

    /**
     * Adds the player to the queue if the queue is empty, otherwise starts the game.
     * @param clientHandler the player to add to the queue
     */
    private void queuePlayer(ClientHandler clientHandler){
        if (server.getPlayersInGame().containsKey(clientHandler) || server.getPlayersInGame().containsValue(clientHandler)){
            sendMessage(Protocol.formatError("You are already in a game."));
        }else if (server.getQueue().contains(clientHandler)){
            server.removeQueue(clientHandler);
        }else{
            server.addToQueue(clientHandler);
        }
    }

    @Override
    protected void handleDisconnect() {
        clientHandler.handleDisconnect();
        if (server.getPlayersInGame().containsKey(clientHandler) || server.getPlayersInGame().containsValue(clientHandler)){
            server.endGameDisconnected(clientHandler);
        }
    }

    @Override
    public boolean sendMessage(String msg){
        super.sendMessage(msg);
        return true;
    }

    @Override
    public void start(){
        super.start();
    }

}
