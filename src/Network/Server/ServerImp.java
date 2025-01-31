package Network.Server;

import Network.Server.Base.SocketServer;
import Network.Server.GameManagement.GameSession;
import Network.Server.Protocol.Protocol;
import java.io.IOException;
import java.net.Socket;
import java.util.*;

public class ServerImp extends SocketServer {
    private Set<ClientHandler> clients = new HashSet<>();
    private Queue<ClientHandler> playersQueue = new LinkedList<>();
    private HashMap<ClientHandler, ClientHandler> playersInGame = new HashMap<>();
    private final Map<ClientHandler, GameSession> gameSessions = new HashMap<>();

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
    public Set<ClientHandler> getClients() {
        return Set.copyOf(clients);
    }


    /**
     * Removes a client to the server.
     * @param clientHandler the client to remove
     */
    public synchronized void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        System.out.println(clientHandler.getUsername() + " has disconnected.");
    }

    /**
     * Adds a client to the server.
     * @param clientHandler the client to add
     */
    public synchronized void addClient(ClientHandler clientHandler) {
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
     * Starts a thread that checks if there are enough players to start a game every second.
     */
    public void waitForPlayersAndStartGame() {
        Thread gameStarterThread = new Thread(() -> {
            while (true) {
                synchronized (this) {
                    if (playersQueue.size() >= 2) {
                        startGame();
                    }
                }
                try {
                    Thread.sleep(1000); // Check every second
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        gameStarterThread.setDaemon(true);
        gameStarterThread.start();
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
    public void startGame() {
        if (playersQueue.size() < 2) {
            System.out.println("Not enough players to start a game.");
            return;
        }

        System.out.println("Game has started!");
        ClientHandler player1 = playersQueue.poll();
        ClientHandler player2 = playersQueue.poll();
        assert player1 != null;
        assert player2 != null;
        assert player1.getUsername() != null;
        assert player2.getUsername() != null;

        player1.getConnection().sendMessage(Protocol.formatNewGame(player1.getUsername(), player2.getUsername()));
        player2.getConnection().sendMessage(Protocol.formatNewGame(player1.getUsername(), player2.getUsername()));

        //Change this to change the game
        GameSession newGameSession = new GameSession(player1, player2, 6, 1, this);
        gameSessions.put(player1, newGameSession);
        gameSessions.put(player2, newGameSession);
        playersInGame.put(player1, player2);

        Thread gameThread = new Thread(newGameSession::playGame);
        gameThread.start();
    }

    /**
     * Ends the game when a player disconnects.
     * @param clientHandler the player that disconnected
     */
    public void endGameDisconnected(ClientHandler clientHandler) {
        GameSession gameSession = gameSessions.get(clientHandler);
        if (gameSession != null) {
            ClientHandler opponent = (gameSession.getPlayer1() == clientHandler)
                    ? gameSession.getPlayer2()
                    : gameSession.getPlayer1();
            gameSessions.remove(clientHandler);
            gameSessions.remove(opponent);
            if (playersInGame.containsKey(clientHandler)){
                playersInGame.remove(clientHandler);
            }else{
                playersInGame.remove(opponent);
            }

            opponent.getConnection().sendMessage(Protocol.formatGameOver(Protocol.GAMEOVER_DISCONNECT, opponent.getUsername()));
        }
    }
    /**
     * Returns the game session for the given client handler.
     * @param clientHandler the client handler
     * @return the game session for the given client handler
     */
    public GameSession getGameSession(ClientHandler clientHandler) {
        return gameSessions.get(clientHandler);
    }

    public void removeGameSession(GameSession gameSession){
        gameSessions.remove(gameSession.getPlayer1());
        gameSessions.remove(gameSession.getPlayer2());
    }

    public void removePlayerInGame(ClientHandler clientHandler){
        playersInGame.remove(clientHandler);
    }


    public static void main(String[] args) throws IOException {
        Scanner input = new Scanner(System.in);
        int port = -1;
        while (port < 0 || port > 65535) {
            System.out.println("Please provide a port:");
            try {
                port = input.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                input.next(); // Clear the invalid input
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
        ServerImp server = new ServerImp(port);
        server.isServerEmpty();
        server.waitForPlayersAndStartGame();
        server.acceptConnections();
    }
}
