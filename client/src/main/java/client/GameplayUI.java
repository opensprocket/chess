package client;

import chess.*;

import java.util.Collection;
import java.util.Scanner;

public class GameplayUI implements WebSocketCommunicator.NotificationHandler {
    private final Scanner scanner;
    private final WebSocketCommunicator ws;
    private final String authToken;
    private final Integer gameID;
    private final ChessGame.TeamColor playerColor;
    private ChessGame currentGame;
    private boolean isObserver;

    public GameplayUI(Scanner scanner, WebSocketCommunicator ws, String authToken, Integer gameID,
                      ChessGame.TeamColor playerColor, boolean isObserver) {
        this.scanner = scanner;
        this.ws = ws;
        this.authToken = authToken;
        this.gameID = gameID;
        this.playerColor = playerColor;
        this.isObserver = isObserver;
    }

    public void run() {
        System.out.println("\nEntering gameplay mode...");
        displayHelp();

        while (true) {
            System.out.print("\n>>> ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                continue;
            }

            String[] tokens = input.toLowerCase().split("\\s+");
            String command = tokens[0];

            try {
                boolean shouldExit = switch (command) {
                    case "help" -> {
                        displayHelp();
                        yield false;
                    }
                    case "redraw" -> {
                        redrawBoard();
                        yield false;
                    }
                    case "leave" -> {
                        leave();
                        yield true;
                    }
                    case "move" -> {
                        makeMove(tokens);
                        yield false;
                    }
                    case "resign" -> {
                        resign();
                        yield false;
                    }
                    case "highlight" -> {
                        highlightMoves(tokens);
                        yield false;
                    }
                    default -> {
                        System.out.println("Unknown command. Type 'help' for available commands.");
                        yield false;
                    }
                };

                if (shouldExit) {
                    break;
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void displayHelp() {
        System.out.println("\nAvailable commands:");
        System.out.println("  help              - Display this help message");
        System.out.println("  redraw            - Redraw the chess board");
        System.out.println("  leave             - Leave the game");
        if (!isObserver) {
            System.out.println("  move <from> <to>  - Make a move (e.g., 'move e2 e4')");
            System.out.println("  resign            - Resign from the game");
        }
        System.out.println("  highlight <pos>   - Highlight legal moves for a piece (e.g., 'highlight e2')");
    }

