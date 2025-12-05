package client;

import chess.*;

import java.util.Collection;
import java.util.Scanner;

public class GameplayUI implements WebSocketCommunicator.NotificationHandler {
    private final Scanner scanner;
    private WebSocketCommunicator ws;
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

    public void setWebSocket(WebSocketCommunicator ws) {
        this.ws = ws;
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

    private void redrawBoard() {
        if (currentGame != null) {
            DisplayGameboard.drawBoard(currentGame.getBoard(),
                    isObserver ? ChessGame.TeamColor.WHITE : playerColor);
        } else {
            System.out.println("No game loaded yet.");
        }
    }

    private void leave() {
        try {
            ws.leave(authToken, gameID);
            System.out.println("Left the game.");
        } catch (Exception e) {
            System.out.println("Error leaving game: " + e.getMessage());
        }
    }

    private void makeMove(String[] tokens) {
        if (isObserver) {
            System.out.println("Observers cannot make moves.");
            return;
        }

        if (tokens.length < 3) {
            System.out.println("Usage: move <from> <to> [promotion]");
            System.out.println("Example: move e2 e4");
            System.out.println("Example: move e7 e8 q (for pawn promotion to queen)");
            return;
        }

        try {
            ChessPosition start = parsePosition(tokens[1]);
            ChessPosition end = parsePosition(tokens[2]);

            ChessPiece.PieceType promotion = null;
            if (tokens.length > 3) {
                promotion = parsePromotionPiece(tokens[3]);
            }

            ChessMove move = new ChessMove(start, end, promotion);

            // Validate move locally before sending
            if (currentGame != null) {
                Collection<ChessMove> validMoves = currentGame.validMoves(start);
                if (validMoves == null || !validMoves.contains(move)) {
                    System.out.println("Error: Invalid move");
                    return;
                }
            }

            ws.makeMove(authToken, gameID, move);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void resign() {
        if (isObserver) {
            System.out.println("Observers cannot resign.");
            return;
        }

        System.out.print("Are you sure you want to resign? (yes/no): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();

        if (confirmation.equals("yes") || confirmation.equals("y")) {
            try {
                ws.resign(authToken, gameID);
            } catch (Exception e) {
                System.out.println("Error resigning: " + e.getMessage());
            }
        } else {
            System.out.println("Resignation cancelled.");
        }
    }

    private void highlightMoves(String[] tokens) {
        if (tokens.length < 2) {
            System.out.println("Usage: highlight <position>");
            System.out.println("Example: highlight e2");
            return;
        }

        try {
            ChessPosition position = parsePosition(tokens[1]);

            if (currentGame == null) {
                System.out.println("No game loaded.");
                return;
            }

            Collection<ChessMove> validMoves = currentGame.validMoves(position);

            if (validMoves == null || validMoves.isEmpty()) {
                System.out.println("No valid moves for piece at " + tokens[1]);
                return;
            }

            DisplayGameboard.drawBoardWithHighlights(currentGame.getBoard(),
                    isObserver ? ChessGame.TeamColor.WHITE : playerColor, position, validMoves);

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private ChessPosition parsePosition(String pos) throws IllegalArgumentException {
        if (pos.length() != 2) {
            throw new IllegalArgumentException("Invalid position format. Use format like 'e2'");
        }

        char col = pos.charAt(0);
        char row = pos.charAt(1);

        if (col < 'a' || col > 'h' || row < '1' || row > '8') {
            throw new IllegalArgumentException("Invalid position. Use columns a-h and rows 1-8");
        }

        int colNum = col - 'a' + 1;
        int rowNum = row - '0';

        return new ChessPosition(rowNum, colNum);
    }

    private ChessPiece.PieceType parsePromotionPiece(String piece) throws IllegalArgumentException {
        return switch (piece.toLowerCase()) {
            case "q", "queen" -> ChessPiece.PieceType.QUEEN;
            case "r", "rook" -> ChessPiece.PieceType.ROOK;
            case "b", "bishop" -> ChessPiece.PieceType.BISHOP;
            case "n", "knight" -> ChessPiece.PieceType.KNIGHT;
            default -> throw new IllegalArgumentException("Invalid promotion piece. Use q, r, b, or n");
        };
    }

    // NotificationHandler methods
    @Override
    public void onLoadGame(ChessGame game) {
        this.currentGame = game;
        System.out.println("\n=== Game Updated ===");
        DisplayGameboard.drawBoard(game.getBoard(),
                isObserver ? ChessGame.TeamColor.WHITE : playerColor);
    }

    @Override
    public void onError(String errorMessage) {
        System.out.println("\n" + errorMessage);
    }

    @Override
    public void onNotification(String message) {
        System.out.println("\n>>> " + message);
    }
}