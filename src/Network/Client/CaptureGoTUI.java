package Network.Client;

import Game.CaptureGoGame;
import Game.Cell;
import Game.Player;
import Players.ComplexAI;
import Players.NaiveAI;
import Players.SafeAI;
import java.util.Scanner;

public class CaptureGoTUI {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to Capture Go!");

        // ------------------------- Player 1 setup ---------------------------
        System.out.print("Enter the name of Player 1: ");
        String player1Name = scanner.nextLine();

        System.out.print("Should Player 1 be an AI? (yes/no): ");
        boolean isPlayer1AI = scanner.nextLine().trim().equalsIgnoreCase("yes");

        Player player1;
        if (isPlayer1AI) {
            // Ask which AI
            System.out.print("Which AI should Player 1 use? (naive/safe): ");
            String aiChoice = scanner.nextLine().trim().toLowerCase();
            if (aiChoice.equals("safe")) {
                player1 = new SafeAI(player1Name, Cell.WHITE_O);
            } else {
                // Default/fallback to naive if user typed something else
                player1 = new NaiveAI(player1Name, Cell.WHITE_O);
            }
        } else {
            // Human player
            player1 = new Player(player1Name, Cell.WHITE_O);
        }

        // ------------------------- Player 2 setup ---------------------------
        System.out.print("Enter the name of Player 2: ");
        String player2Name = scanner.nextLine();

        System.out.print("Should Player 2 be an AI? (yes/no): ");
        boolean isPlayer2AI = scanner.nextLine().trim().equalsIgnoreCase("yes");

        Player player2;
        if (isPlayer2AI) {
            // Ask which AI
            System.out.print("Which AI should Player 2 use? (naive/safe): ");
            String aiChoice = scanner.nextLine().trim().toLowerCase();
            if (aiChoice.equals("safe")) {
                player2 = new SafeAI(player2Name, Cell.BLUE_O);
            } else if (aiChoice.equals("opa")) {
                player2 = new ComplexAI(player1Name, Cell.BLUE_O, 500, 1.4);
            } else {
                // Default/fallback to naive if user typed something else
                player2 = new NaiveAI(player2Name, Cell.BLUE_O);
            }
        } else {
            // Human player
            player2 = new Player(player2Name, Cell.BLUE_O);
        }

        // ------------------------- Board setup ---------------------------
        System.out.print("Enter the board size (e.g., 5 for 5x5): ");
        int boardSize = scanner.nextInt();
        while(boardSize < 4 || boardSize > 10){
            System.out.println("The board size should be bigger than 4 and smaller than 10. Please enter a new board size: ");
            boardSize = scanner.nextInt();
        }

        // ------------------------- Capture Goal setup ---------------------------
        System.out.print("Enter the number of stones required to win: ");
        int captureGoal = scanner.nextInt();
        // A simplistic check to ensure captureGoal is "realistic"
        while(captureGoal < 1 || captureGoal > ((boardSize - 1) * (boardSize - 1)) / 2){
            System.out.println("The capture goal should be realistic. Please enter a new capture goal: ");
            captureGoal = scanner.nextInt();
        }

        // ------------------------- Start the game ---------------------------
        CaptureGoGame game = new CaptureGoGame(boardSize - 1, player1, player2, captureGoal);
        game.playGame(player1, player2);
    }
}
