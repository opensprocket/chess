package client;

import java.util.Arrays;

public class ChessClient {

    private final String serverUrl;
    private final ServerFacade server;
    private State state;

    public ChessClient(String serverUrl) {

        this.serverUrl = serverUrl;
        this.server = new ServerFacade(serverUrl);
        this.state = State.SIGNED_OUT;
    }

    public String eval(String input) {
        var tokens = input.toLowerCase().split(" ");
        var cmd = (tokens.length > 0) ? tokens[0] : "help";
        String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);

        return switch (cmd) {
            case "login" -> login(params);
            case "register" -> registerUser(params);
            case "logout" -> logout();
            case "create" -> createGame(params);
            case "list" -> listGames();
            case "join" -> joinGame(params);
            case "observe" -> joinAsObserver(params);
            case "quit" -> "quit";
            default -> help();
        };
    }

    private String login(String[] params) {
        if (params.length == 2) {
            // call out to server
            return String.format("Logged in as %s", params[0]);
        }
        return "Expected: <username> <password>";
    }

    private String registerUser(String[] params) {
        if (params.length == 3) {
            // call out to server
            return String.format("Logged in as %s", params[0]);
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

        // call out to server
        if (params.length == 2) {
            return String.format("Joined game %s as %s", params[0], params[1]);
        }
        return "Expected <game number> [WHITE|BLACK]";
    }

    private String joinAsObserver(String[] params) {
        assertSignedIn();
        if (params.length == 1) {
            // call out to server
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

}
