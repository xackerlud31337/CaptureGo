package Network.Server.Protocol;

import Network.Server.ClientHandler;
import java.util.Set;

public final class Protocol {

    // Constants for command types
    public static final String HELLO = "HELLO";
    public static final String LOGIN = "LOGIN";
    public static final String ALREADYLOGGEDIN = "ALREADYLOGGEDIN";
    public static final String LIST = "LIST";
    public static final String QUEUE = "QUEUE";
    public static final String NEWGAME = "NEWGAME";
    public static final String MOVE = "MOVE";
    public static final String GAMEOVER = "GAMEOVER";
    public static final String ERROR = "ERROR";

    // Error descriptions
    public static final String ERROR_ILLEGAL_MOVE = "Illegal move";
    public static final String ERROR_ALREADY_LOGGED_IN = "Already logged in";
    public static final String ERROR_UNKNOWN = "Unknown error";

    // Delimiter for separating arguments
    public static final String DELIMITER = "~";

    // Game over reasons
    public static final String GAMEOVER_DISCONNECT = "DISCONNECT";
    public static final String GAMEOVER_VICTORY = "VICTORY";
    public static final String GAMEOVER_DRAW = "DRAW";

    private Protocol() {
        // Prevent instantiation
    }


    //------------------------ Protocol formatting methods ------------------------
    /**
     * Formats a HELLO command.
     * @param description Client or server description.
     * @param extensions Supported extensions (optional).
     * @return The formatted HELLO command.
     */
    public static String formatHello(String description, String... extensions) {
        StringBuilder builder = new StringBuilder(HELLO).append(DELIMITER).append(description);
        for (String extension : extensions) {
            builder.append(DELIMITER).append(extension);
        }
        return builder.toString();
    }

    /**
     * Formats a LOGIN command.
     * @param username The username to log in with.
     * @return The formatted LOGIN command.
     */
    public static String formatLogin(String username) {
        return LOGIN + DELIMITER + username;
    }

    /**
     * Formats a LIST command for the server.
     * @param usernames The list of usernames currently logged in.
     * @return The formatted LIST command.
     */
    public static String formatList(Set<ClientHandler> usernames) {
        StringBuilder builder = new StringBuilder(LIST);
        for (ClientHandler client : usernames) {
            builder.append(DELIMITER).append(client.getUsername());
        }
        return builder.toString();
    }

    /**
     * Formats a QUEUE command.
     * @return The formatted QUEUE command.
     */
    public static String formatQueue() {
        return QUEUE;
    }

    /**
     * Formats a NEWGAME command.
     * @param player1 First player's name.
     * @param player2 Second player's name.
     * @return The formatted NEWGAME command.
     */
    public static String formatNewGame(String player1, String player2) {
        return NEWGAME + DELIMITER + player1 + DELIMITER + player2;
    }

    /**
     * Formats a MOVE command.
     * @param move The move made (integer N).
     * @return The formatted MOVE command.
     */
    public static String formatMove(int move) {
        return MOVE + DELIMITER + move;
    }

    /**
     * Formats a GAMEOVER command.
     * @param reason The reason for the game ending.
     * @param winner The winner's name (optional, null for draw).
     * @return The formatted GAMEOVER command.
     */
    public static String formatGameOver(String reason, String winner) {
        String command = GAMEOVER + DELIMITER + reason;
        if (winner != null) {
            command += DELIMITER + winner;
        }
        return command;
    }

    /**
     * Formats an ERROR command.
     * @param description The description of the error (optional).
     * @return The formatted ERROR command.
     */
    public static String formatError(String description) {
        return description == null ? ERROR : ERROR + DELIMITER + description;
    }
}
