package Network.Client;

import Game.Cell;
import Game.Player;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CaptureGoClient {
    private ClientConnection clientConnection;
    private String username;
    private boolean loggedIn = false;
    private boolean inGame = false;

    public CaptureGoClient(String address, int port) throws IOException {
        clientConnection = new ClientConnection(address, port, this);
        clientConnection.start();
    }

    public void close() {
        clientConnection.close();
    }

    protected void login(String username) {
        clientConnection.sendHello(username);
        clientConnection.sendLogin(username);
    }

    protected void sendMove(int move) {
        clientConnection.sendMove(move);
    }

    protected void sendList() {
        clientConnection.sendList();
    }

    protected void sendQueue() {
        clientConnection.sendQueue();
    }

    protected void receiveQueue(){
        System.out.println("You are now in the queue!");
    }

    public List<Player> receiveGameStart(String message){
        List<Player> players = new ArrayList<>();
        String[] parts = message.split("~");
        Player player1 = new Player(parts[1], Cell.BLUE_O);
        Player player2 = new Player(parts[2], Cell.WHITE_O);
        players.add(player1);
        players.add(player2);
        System.out.println("The game has started!");
        inGame = true;
        return players;
    }

    protected void receiveHello(String s) {
        System.out.println("The server send a hello message: " + s);
    }

    protected void receiveLogin() {
        loggedIn = true;
    }

    protected boolean getLoggedIn(){
        return loggedIn;
    }
    protected boolean inGame(){
        return inGame;
    }

    protected void receiveList(String message) {
        System.out.println("The list of players is: ");
        String[] parts = message.split("~");
        for (int i = 1; i < parts.length; i++) {
            System.out.print(" " + parts[i]);
        }
    }

    protected void receiveMove(int message) {
        int row = message / 7;
        int col = message % 7;
        System.out.println("The server send a move message: " + message);
    }

    protected void receiveAlreadyLoggedIn() {
        System.out.println("You are already logged in!");
    }

    protected void receiveError(String message) {
        System.out.println("An error occurred: " + message);
    }
}
