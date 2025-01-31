package Network.Client;

import Network.Server.Base.SocketConnection;
import Network.Server.Protocol.Protocol;
import java.io.IOException;
import java.net.Socket;

public class ClientConnection extends SocketConnection {

    private CaptureGoClient client;

    public ClientConnection(String adress, int port, CaptureGoClient client) throws IOException {
        super(new Socket(adress, port));
        this.client = client;
    }

    protected void sendHello(String username){
        sendMessage(Protocol.formatHello("Client by " + username));
    }

    protected void sendLogin(String username){
        sendMessage(Protocol.formatLogin(username));
    }

    protected void sendMove(int move){
        sendMessage(Protocol.formatMove(move));
    }

    protected void sendQueue(){
        sendMessage(Protocol.QUEUE);
    }

    protected void sendList(){
        sendMessage(Protocol.LIST);
    }

    @Override
    protected void handleMessage(String message) {
        switch (message.split(Protocol.DELIMITER)[0]) {
            case Protocol.HELLO -> client.receiveHello(message.split(Protocol.DELIMITER)[1]);
            case Protocol.LOGIN -> client.receiveLogin();
            case Protocol.LIST -> client.receiveList(message);
            case Protocol.QUEUE -> client.receiveQueue();
            case Protocol.NEWGAME -> client.receiveGameStart(message);
            case Protocol.MOVE -> client.receiveMove(message); // Pass the full message string
            case Protocol.GAMEOVER -> {
                String[] parts = message.split(Protocol.DELIMITER);
                String result = parts[1]; // VICTORY, DRAW, etc.
                String winner = parts.length > 2 ? parts[2] : ""; // Winner's name, if applicable
                client.receiveGameOver(result, winner);
            }
            case Protocol.ALREADYLOGGEDIN -> client.receiveAlreadyLoggedIn();
            case Protocol.ERROR -> client.receiveError(message.split(Protocol.DELIMITER)[1]);
            default -> System.out.println("Invalid message received: " + message);
        }
    }

    @Override
    protected void handleDisconnect() {
    }

    protected void start(){
        super.start();
    }

    protected void close(){
        super.close();
    }
}
