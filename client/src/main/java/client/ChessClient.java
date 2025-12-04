package client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import chess.ChessGame;
import chess.datamodel.*;

public class ChessClient {

    private final String serverUrl;
    private final ServerFacade server;
    private final Scanner scanner;
    private State state;
    private String authToken = null;
    private List<GameInfo> gameList = null;
    private WebSocketCommunicator webSocket = null;

    public ChessClient(String serverUrl, Scanner scanner) {
        this.serverUrl = serverUrl;
        this.server = new ServerFacade(serverUrl);
        this.scanner = scanner;
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
            return ex.getMessage();
        }
    }

    private String testPrint() {
        DisplayGameboard.testBoard();
        return "Test complete";
    }

    private String login(String[] params) throws FacadeException {
        if (params.length == 2) {
            // call out to server
            AuthData auth = server.login(params[0], params[1]);
            this.authToken = auth.authToken();
            state = State.SIGNED_IN;
            return String.format("Logged in as %s", auth.username());
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

    private String logout() throws FacadeException {
        assertSignedIn();
        if (webSocket != null) {
            webSocket.close();
            webSocket = null;
        }
        server.logout(this.authToken);
        this.authToken = null;
        this.gameList = null;
        state = State.SIGNED_OUT;
        return "Logged out.";
    }

    private String createGame(String[] params) throws FacadeException {
        assertSignedIn();
        if (params.length == 1) {
            // call out to server
            server.createGame(params[0], this.authToken);
            return String.format("Created game: %s", params[0]);
        }
        return "Expected: <name>";
    }

    private String listGames() throws FacadeException {
        assertSignedIn();
        // call out to server
        ListGameResult res = server.listGames(this.authToken);
        this.gameList = new ArrayList<>(res.games());
        StringBuilder sb = new StringBuilder("Games:\n");

        if (this.gameList.isEmpty()) {
            sb.append("\tNo games available.\n");
        } else {
            for (int i = 0; i < this.gameList.size(); i++) {
                GameInfo game = this.gameList.get(i);
                sb.append(String.format("\t%d. %s \n\t\tWhite: %s\n\t\tBlack: %s\n",
                        i + 1,
                        game.gameName(),
                        game.whiteUsername() != null ? game.whiteUsername() : "empty",
                        game.blackUsername() != null ? game.blackUsername() : "empty"));
            }
        }
        return sb.toString();
    }

    private String joinGame(String[] params) throws FacadeException {
        assertSignedIn();

        // call out to server for game_id
        if (params.length == 2) {
            if (this.gameList == null) {
                try {
                    listGames();
                } catch (Exception ex) {
                    throw new RuntimeException("You must list games before joining");
                }
            }

            int gameNumber;

            try {
                gameNumber = Integer.parseInt(params[0]);
            } catch (NumberFormatException ex) {
                throw new RuntimeException("Invalid game number");
            }

            if (gameNumber < 1 || gameNumber > this.gameList.size()) {
                throw new RuntimeException("Invalid game number");
            }

            int gameID = this.gameList.get(gameNumber - 1).gameID();
            String playerColor = params[1].toUpperCase();

            if (!"WHITE".equals(playerColor) && !"BLACK".equals(playerColor)) {
                throw new RuntimeException("Invalid color, must be WHITE or BLACK");
            }

            // Join via HTTP first
            server.joinGame(gameID, playerColor, this.authToken);

            // Connect via WebSocket
            try {
                ChessGame.TeamColor color = playerColor.equalsIgnoreCase("white") ?
                        ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;

                webSocket = new WebSocketCommunicator(serverUrl,
                        new GameplayUI(scanner, null, authToken, gameID, color, false));

                GameplayUI gameplayUI = new GameplayUI(scanner, webSocket, authToken, gameID, color, false);
                webSocket = new WebSocketCommunicator(serverUrl, gameplayUI);

                webSocket.connect(authToken, gameID);
                state = State.IN_GAME;

                // Enter gameplay loop
                gameplayUI.run();

                // Clean up after leaving game
                webSocket.close();
                webSocket = null;
                state = State.SIGNED_IN;

                return "Returned to lobby";

            } catch (Exception e) {
                throw new RuntimeException("Failed to connect to game: " + e.getMessage());
            }
        }
        return "Expected: <game number> [WHITE|BLACK]";
    }

    private String joinAsObserver(String[] params) throws FacadeException {
        assertSignedIn();

        if (params.length == 1) {
            if (this.gameList == null) {
                try {
                    listGames();
                } catch (Exception ex) {
                    throw new RuntimeException("You must list games before observing");
                }
            }

            int gameNumber;
            try {
                gameNumber = Integer.parseInt(params[0]);
            } catch (NumberFormatException ex) {
                throw new RuntimeException("Invalid game number");
            }

            if (gameNumber < 1 || gameNumber > this.gameList.size()) {
                throw new RuntimeException("Invalid game number");
            }

            int gameID = this.gameList.get(gameNumber - 1).gameID();

            // Connect via WebSocket as observer
            try {
                GameplayUI gameplayUI = new GameplayUI(scanner, null, authToken, gameID,
                        ChessGame.TeamColor.WHITE, true);
                webSocket = new WebSocketCommunicator(serverUrl, gameplayUI);

                webSocket.connect(authToken, gameID);
                state = State.OBSERVING_GAME;

                // Enter gameplay loop
                gameplayUI.run();

                // Clean up after leaving game
                webSocket.close();
                webSocket = null;
                state = State.SIGNED_IN;

                return "Returned to lobby";

            } catch (Exception e) {
                throw new RuntimeException("Failed to observe game: " + e.getMessage());
            }
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
            case State.SIGNED_OUT ->        "Logged Out";
            case State.SIGNED_IN ->         "Logged In ";
            case State.IN_GAME ->           "In Game   ";
            case State.OBSERVING_GAME ->    "Observing ";
            default -> "Error State";
        };
    }

}
