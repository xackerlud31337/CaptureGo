package Network.Server;

import Network.Server.Base.SocketServer;
import Network.Server.Protocol.Protocol;
import java.io.IOException;
import java.net.Socket;
import java.util.*;

public class ServerImp extends SocketServer {
    private Set<ClientHandler> clients = new HashSet<>();
    private Queue<ClientHandler> playersQueue = new LinkedList<>();
    private HashMap<ClientHandler, ClientHandler> playersInGame = new HashMap<>();

    public ServerImp(int port) throws IOException {
        super(port);
    }

    /**
     * Accepts connections and starts a new thread for each connection.
     * This method will block until the server socket is closed, for example by invoking closeServerSocket.
     *
     * @throws IOException if an I/O error occurs when waiting for a connection
     */
    @Override
    protected void acceptConnections() throws IOException {
        super.acceptConnections();
    }

    /**
     * Closes the server socket. This will cause the server to stop accepting new connections.
     * If called from a different thread than the one running acceptConnections, then that thread will return from
     * acceptConnections.
     */
    @Override
    public synchronized void close() {
        super.close();
    }

    /**
     * Returns the clients connected to this server.
     * @return set of the clients connected to this server.
     */
    protected Set<ClientHandler> getClients() {
        return Set.copyOf(clients);
    }


    /**
     * Removes a client to the server.
     * @param clientHandler the client to remove
     */
    protected synchronized void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        System.out.println(clientHandler.getUsername() + " has disconnected.");
    }

    /**
     * Adds a client to the server.
     * @param clientHandler the client to add
     */
    protected synchronized void addClient(ClientHandler clientHandler) {
        clients.add(clientHandler);
    }

    /**
     * Creates a new connection handler for the given socket.
     *
     * @param socket the socket for the connection
     * @return the connection handler
     */
    @Override
    protected void handleConnection(Socket socket) {
        try {
            ServerConnection serverCon1 = new ServerConnection(socket);
            ClientHandler clientHandler = new ClientHandler(this);
            serverCon1.start();
            serverCon1.setClientHandlerAndServer(clientHandler, this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * Starts a thread that checks if the server is empty every minute.
     */
    public void isServerEmpty() {
        Thread emptyServerListener = new Thread(() -> {
            while (true) {
                synchronized (this) {
                    if (clients.isEmpty()) {
                        System.out.println("The server is empty.");
                    }
                }
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        emptyServerListener.setDaemon(true);
        emptyServerListener.start();
    }
    /**
     * Returns the queue of players waiting to play.
     * @return the queue of players waiting to play.
     */
    public Queue<ClientHandler> getQueue() {
        return playersQueue;
    }
    /**
     * Removes a player from the queue.
     * @param clientHandler the player to remove
     */
    public void removeQueue(ClientHandler clientHandler) {
        playersQueue.remove(clientHandler);
    }
    /**
     * Adds a player to the queue.
     * @param clientHandler the player to add
     */
    public void addToQueue(ClientHandler clientHandler) {
        playersQueue.add(clientHandler);
    }
    /**
     * Returns the players in the game.
     * @return the players in the game.
     */
    public HashMap<ClientHandler, ClientHandler> getPlayersInGame() {
        return playersInGame;
    }

    /**
     * Starts a game with the first two players in the queue.
     */
    public void startGame(){
        if (playersQueue.size() < 2) {
            System.out.println("Not enough players to start a game.");
            return;
        }
        //Some dummy code to show the game has started
        System.out.println("Game has started!");
        ClientHandler player1 = playersQueue.poll();
        ClientHandler player2 = playersQueue.poll();
        assert player1 != null;
        assert player2 != null;
        player1.getConnection().sendMessage(Protocol.formatNewGame(player1.getUsername(), player2.getUsername()));
        player2.getConnection().sendMessage(Protocol.formatNewGame(player1.getUsername(), player2.getUsername()));
        playersInGame.put(player1, player2);
    }

    public void endGameDisconnected(ClientHandler clientHandler) {
        if (playersInGame.containsKey(clientHandler)) {
            ClientHandler opponent = playersInGame.get(clientHandler);
            playersInGame.remove(clientHandler);
            playersInGame.remove(opponent);
            opponent.getConnection().sendMessage(Protocol.formatGameOver("Opponent disconnected", opponent.getUsername()));
        } else if (playersInGame.containsValue(clientHandler)) {
            Optional<Map.Entry<ClientHandler, ClientHandler>> entry = playersInGame.entrySet().stream()
                    .filter(e -> e.getValue().equals(clientHandler)).findFirst();
            if (entry.isPresent()) {
                ClientHandler opponent = entry.get().getKey();
                playersInGame.remove(opponent);
                playersInGame.remove(clientHandler);
                opponent.getConnection().sendMessage(Protocol.formatGameOver("Opponent disconnected", opponent.getUsername()));
            }
        }
    }


    public static void main(String[] args) throws IOException {
        Scanner input = new Scanner(System.in);
        System.out.println("Please provide a port:");
        int port = input.nextInt();
        while (port < 0 || port > 65535) {
            System.out.println("Please provide a valid port:");
            port = input.nextInt();
        }
        ServerImp server = new ServerImp(port);
        server.isServerEmpty();
        server.acceptConnections();
    }


}
