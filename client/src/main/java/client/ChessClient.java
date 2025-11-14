package client;

import java.util.Arrays;
import client.State;

public class ChessClient {

    private final String serverUrl;
    private final ServerFacade server;
    private State state;

    public ChessClient(String serverUrl, ServerFacade server) {

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
    private String help() {
        if (state == State.SIGNED_OUT) {
            return """
                    - register <username> <password <email>
                    - login <username> <password>
                    - quit
                    - help
                   """;
        } else {
            return """
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
