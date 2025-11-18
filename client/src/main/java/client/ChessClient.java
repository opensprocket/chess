package client;

import java.util.Arrays;

import chess.ChessBoard;
import chess.ChessGame;
import chess.datamodel.AuthData;

public class ChessClient {

    private final String serverUrl;
    private final ServerFacade server;
    private State state;
    private String authToken = null;

    public ChessClient(String serverUrl) {

        this.serverUrl = serverUrl;
        this.server = new ServerFacade(serverUrl);
        this.state = State.SIGNED_OUT;
    }

    public String eval(String input) {
        var tokens = input.toLowerCase().split(" ");
        var cmd = (tokens.length > 0) ? tokens[0] : "help";
        String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
        try {
            return switch (cmd) {
                case "login" -> login(params);
                case "register" -> registerUser(params);
                case "logout" -> logout();
                case "create" -> createGame(params);
                case "list" -> listGames();
                case "join" -> joinGame(params);
                case "observe" -> joinAsObserver(params);
                case "testprint" -> testPrint();
                case "quit" -> "quit";
                default -> help();
            };
        } catch (Exception ex) {
            return "Error: " + ex.getMessage();
        }
    }

    private String testPrint() {
        DisplayGameboard.testBoard();
        return "Test complete";
    }

    private String login(String[] params) {
        if (params.length == 2) {
            // call out to server
            state = State.SIGNED_IN;
            return String.format("Logged in as %s", params[0]);
        }
        return "Expected: <username> <password>";
    }

    private String registerUser(String[] params) throws FacadeException {
        if (params.length == 3) {
            // call out to server
            AuthData auth = server.register(params[0], params[1], params[2]);
            this.authToken = auth.authToken();
            state = State.SIGNED_IN;
            return String.format("Registration successful, logged in as %s", auth.username());
        }
        return "Expected: <username> <password> <email>";
    }

    private String logout() {
        state = State.SIGNED_OUT;
        return "Logged out.";
    }


    private String createGame(String[] params) {
        assertSignedIn();
        if (params.length == 1) {
            // call out to server
            return String.format("Created game: %s", params[0]);
        }
        return "Expected: <name>";
    }

    private String listGames() {
        assertSignedIn();
        // call out to server

        return "List of games...";
    }

    private String joinGame(String[] params) {
        assertSignedIn();

        // call out to server for game_id
        if (params.length == 2) {

//            if (!params[1].equalsIgnoreCase("WHITE") || !params[1].equalsIgnoreCase("BLACK")) {
//                return "Error: Invalid color, must be WHITE or BLACK";
//            }

            ChessGame.TeamColor perspective = (params[1].equalsIgnoreCase("WHITE")) ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;

            // TODO: call server facade and join game

            ChessBoard board = new ChessBoard();
            board.resetBoard();
            DisplayGameboard.drawBoard(board, perspective);

            return String.format("Joined game %s as %s", params[0], params[1]);
        }
        return "Expected <game number> [WHITE|BLACK]";
    }

    private String joinAsObserver(String[] params) {
        assertSignedIn();
        if (params.length == 1) {
            // call out to server for game_id

            // TODO: call server facade and join game from white perspective

            ChessBoard board = new ChessBoard();
            board.resetBoard();
            DisplayGameboard.drawBoard(board, ChessGame.TeamColor.WHITE);

            return String.format("Observing game %s", params[0]);
        }
        return "Expected: <game number>";
    }

    private String help() {
        if (state == State.SIGNED_OUT) {
            return """
                    Commands available:
                    - register <username> <password <email>
                    - login <username> <password>
                    - quit
                    - help
                   """;
        } else {
            return """
                    Commands available:
                    - create <name>
                    - list
                    - join <game number> [WHITE|BLACK]
                    - observe <game number>
                    - logout
                    - quit
                    - help
                   """;
        }
    }

    private void assertSignedIn() {
        if (state == State.SIGNED_OUT) {
            throw new RuntimeException("You must sign in for this");
        }
    }

    public String getState() {

        return switch (state) {
            case State.SIGNED_OUT -> "Logged Out";
            case State.SIGNED_IN ->  "Logged In ";
            case State.IN_GAME ->    "In Game   ";
            default -> "Error State";
        };
    }

}
