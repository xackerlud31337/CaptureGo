package Network.Client;

import Game.Cell;
import Game.Player;
import java.io.IOException;
import java.util.Scanner;

public class CaptureGoClient {
    private ClientConnection clientConnection;
    private ClientGameSession game;
    private Player ownPlayer;
    Player otherPlayer;
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
        Scanner scanner = new Scanner(System.in);
        clientConnection.sendHello(username);
        while (!loggedIn) {
            clientConnection.sendLogin(username);
            try {
                Thread.sleep(500); // Wait for the server to respond
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Interrupted while waiting for login.");
                return;
            }
            if (!loggedIn) {
                System.out.println("Your username is not valid, please enter a new one:");
                username = scanner.nextLine().trim();
            }
        }
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

    public void receiveGameStart(String message){
        String[] parts = message.split("~");
        otherPlayer = new Player("", null);
        if (parts[1].equals(ownPlayer.getName())){
            ownPlayer.setStone(Cell.WHITE_O);
            otherPlayer.setStone(Cell.BLUE_O);
            otherPlayer.setName(parts[2]);
            game = new ClientGameSession(ownPlayer, otherPlayer);
        }else{
            ownPlayer.setStone(Cell.BLUE_O);
            otherPlayer.setStone(Cell.WHITE_O);
            otherPlayer.setName(parts[1]);
            game = new ClientGameSession(otherPlayer, ownPlayer);
        }
        System.out.println("The game has started!");
        inGame = true;
    }

    protected void receiveHello(String s) {
        System.out.println("The server sent a hello message: " + s);
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
            System.out.print(parts[i] + ", ");
        }
        System.out.println();
        System.out.println("> ");
    }

    protected void receiveMove(int message) {
        int row = message / 7;
        int col = message % 7;
        game.placeStone(row, col);
        game.getBoard().render();
    }

    protected void receiveAlreadyLoggedIn() {
        if (loggedIn) {
            System.out.println("You are already logged in!");
        }
    }

    protected void receiveError(String message) {
        if (message.contains("Illegal move")){
            System.out.println("Please select a move between 0 and 48 again: ");
        } else if (message.contains("not your turn")) {
            System.out.println("Please wait until it's your turn: ");
        }
        System.out.println("An error occurred: " + message);
    }

    protected void setOwnPlayer(Player player){
        this.ownPlayer = player;
    }

    public Player getOwnPlayer() {
        return ownPlayer;
    }
}
