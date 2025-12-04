package server.websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import chess.datamodel.AuthData;
import chess.datamodel.GameData;
import com.google.gson.Gson;
import dataaccess.DataAccess;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsMessageContext;
import io.javalin.websocket.WsCloseContext;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

public class WebSocketHandler {
    private final ConnectionManager connections;
    private final DataAccess dataAccess;
    private final Gson gson = new Gson();

    public WebSocketHandler(ConnectionManager connections, DataAccess dataAccess) {
        this.connections = connections;
        this.dataAccess = dataAccess;
    }

    public void onConnect(WsConnectContext ctx) {
        System.out.println("WebSocket connected: " + ctx.session.getRemoteAddress());
    }

    public void onMessage(WsMessageContext ctx) {
        try {
            UserGameCommand command = gson.fromJson(ctx.message(), UserGameCommand.class);

            switch (command.getCommandType()) {
                case CONNECT -> handleConnect(ctx, command);
                case MAKE_MOVE -> handleMakeMove(ctx);
                case LEAVE -> handleLeave(ctx, command);
                case RESIGN -> handleResign(ctx, command);
            }
        } catch (Exception e) {
            sendError(ctx, "Error: " + e.getMessage());
        }
    }

    public void onClose(WsCloseContext ctx) {
        System.out.println("WebSocket closed");
    }

    private void handleConnect(WsMessageContext ctx, UserGameCommand command) {
        try {
            // Validate auth
            AuthData auth = dataAccess.getAuth(command.getAuthToken());
            if (auth == null) {
                sendError(ctx, "Error: Invalid authentication");
                return;
            }

            // Validate game exists
            GameData game = dataAccess.getGame(command.getGameID());
            if (game == null) {
                sendError(ctx, "Error: Game not found");
                return;
            }

            // Add connection
            connections.addConnection(command.getGameID(), command.getAuthToken(), ctx.session);

            // Send LOAD_GAME to root client
            connections.sendToClient(command.getGameID(), command.getAuthToken(),
                    new LoadGameMessage(game.game()));

            // Determine if player or observer
            String username = auth.username();
            String notificationMsg;
            if (username.equals(game.whiteUsername())) {
                notificationMsg = username + " joined the game as WHITE";
            } else if (username.equals(game.blackUsername())) {
                notificationMsg = username + " joined the game as BLACK";
            } else {
                notificationMsg = username + " joined as an observer";
            }

            // Broadcast notification to others
            connections.broadcast(command.getGameID(),
                    new NotificationMessage(notificationMsg), command.getAuthToken());

        } catch (Exception e) {
            sendError(ctx, "Error: " + e.getMessage());
        }
    }

    private void handleMakeMove(WsMessageContext ctx) {
        try {
            // Parse as MakeMoveCommand
            MakeMoveCommand command = gson.fromJson(ctx.message(), MakeMoveCommand.class);
            ChessMove move = command.getMove();

            // Validate auth
            AuthData auth = dataAccess.getAuth(command.getAuthToken());
            if (auth == null) {
                sendError(ctx, "Error: Invalid authentication");
                return;
            }

            // Get game
            GameData gameData = dataAccess.getGame(command.getGameID());
            if (gameData == null) {
                sendError(ctx, "Error: Game not found");
                return;
            }

            ChessGame game = gameData.game();
            String username = auth.username();

            // Check if game is over
            if (game.isGameOver()) {
                sendError(ctx, "Error: Game is over");
                return;
            }

            // Verify it's the player's turn
            ChessGame.TeamColor currentTurn = game.getTeamTurn();
            if ((currentTurn == ChessGame.TeamColor.WHITE && !username.equals(gameData.whiteUsername())) ||
                    (currentTurn == ChessGame.TeamColor.BLACK && !username.equals(gameData.blackUsername()))) {
                sendError(ctx, "Error: It's not your turn");
                return;
            }

            // Verify the user is actually a player in this game
            if (!username.equals(gameData.whiteUsername()) && !username.equals(gameData.blackUsername())) {
                sendError(ctx, "Error: You are not a player in this game");
                return;
            }

            // Make the move
            game.makeMove(move);

            // Update game in database
            GameData updatedGame = new GameData(
                    gameData.gameID(),
                    gameData.whiteUsername(),
                    gameData.blackUsername(),
                    gameData.gameName(),
                    game
            );
            dataAccess.updateGame(command.getGameID(), updatedGame);

            // Broadcast LOAD_GAME to all clients (including root)
            LoadGameMessage loadGameMessage = new LoadGameMessage(game);
            connections.broadcast(command.getGameID(), loadGameMessage, null);
            connections.sendToClient(command.getGameID(), command.getAuthToken(), loadGameMessage);

            // Send move notification to others (not to root)
            String moveNotification = username + " made move: " + formatMove(move);
            connections.broadcast(command.getGameID(),
                    new NotificationMessage(moveNotification), command.getAuthToken());

            // Check for check, checkmate, or stalemate
            ChessGame.TeamColor opponentColor = (currentTurn == ChessGame.TeamColor.WHITE) ?
                    ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;

            if (game.isInCheckmate(opponentColor)) {
                String checkmateMsg = getPlayerName(gameData, opponentColor) + " is in checkmate. " +
                        username + " wins!";
                NotificationMessage notification = new NotificationMessage(checkmateMsg);
                connections.broadcast(command.getGameID(), notification, null);
                connections.sendToClient(command.getGameID(), command.getAuthToken(), notification);
            } else if (game.isInStalemate(opponentColor)) {
                String stalemateMsg = "Game ended in stalemate";
                NotificationMessage notification = new NotificationMessage(stalemateMsg);
                connections.broadcast(command.getGameID(), notification, null);
                connections.sendToClient(command.getGameID(), command.getAuthToken(), notification);
            } else if (game.isInCheck(opponentColor)) {
                String checkMsg = getPlayerName(gameData, opponentColor) + " is in check";
                NotificationMessage notification = new NotificationMessage(checkMsg);
                connections.broadcast(command.getGameID(), notification, null);
                connections.sendToClient(command.getGameID(), command.getAuthToken(), notification);
            }

        } catch (InvalidMoveException e) {
            sendError(ctx, "Error: Invalid move");
        } catch (Exception e) {
            sendError(ctx, "Error: " + e.getMessage());
        }
    }

    private void handleLeave(WsMessageContext ctx, UserGameCommand command) {
        try {
            // Validate auth
            AuthData auth = dataAccess.getAuth(command.getAuthToken());
            if (auth == null) {
                sendError(ctx, "Error: Invalid authentication");
                return;
            }

            // Get game
            GameData gameData = dataAccess.getGame(command.getGameID());
            if (gameData == null) {
                sendError(ctx, "Error: Game not found");
                return;
            }

            String username = auth.username();

            // If player is leaving, remove them from the game
            if (username.equals(gameData.whiteUsername()) || username.equals(gameData.blackUsername())) {
                String newWhite = username.equals(gameData.whiteUsername()) ? null : gameData.whiteUsername();
                String newBlack = username.equals(gameData.blackUsername()) ? null : gameData.blackUsername();

                GameData updatedGame = new GameData(
                        gameData.gameID(),
                        newWhite,
                        newBlack,
                        gameData.gameName(),
                        gameData.game()
                );
                dataAccess.updateGame(command.getGameID(), updatedGame);
            }

            // Remove connection
            connections.removeConnection(command.getGameID(), command.getAuthToken());

            // Notify others
            String notification = username + " left the game";
            connections.broadcast(command.getGameID(),
                    new NotificationMessage(notification), command.getAuthToken());

        } catch (Exception e) {
            sendError(ctx, "Error: " + e.getMessage());
        }
    }

    private void handleResign(WsMessageContext ctx, UserGameCommand command) {
        try {
            // Validate auth
            AuthData auth = dataAccess.getAuth(command.getAuthToken());
            if (auth == null) {
                sendError(ctx, "Error: Invalid authentication");
                return;
            }

            // Get game
            GameData gameData = dataAccess.getGame(command.getGameID());
            if (gameData == null) {
                sendError(ctx, "Error: Game not found");
                return;
            }

            String username = auth.username();

            // Verify user is a player
            if (!username.equals(gameData.whiteUsername()) && !username.equals(gameData.blackUsername())) {
                sendError(ctx, "Error: Observers cannot resign");
                return;
            }

            // Check if game is already over
            if (gameData.game().isGameOver()) {
                sendError(ctx, "Error: Game is already over");
                return;
            }

            // Mark game as over
            gameData.game().setGameOver(true);
            dataAccess.updateGame(command.getGameID(), gameData);

            // Notify all clients (including root)
            String notification = username + " resigned. Game over.";
            NotificationMessage message = new NotificationMessage(notification);
            connections.broadcast(command.getGameID(), message, null);
            connections.sendToClient(command.getGameID(), command.getAuthToken(), message);

        } catch (Exception e) {
            sendError(ctx, "Error: " + e.getMessage());
        }
    }

    private void sendError(WsMessageContext ctx, String errorMessage) {
        try {
            ctx.send(gson.toJson(new ErrorMessage(errorMessage)));
        } catch (Exception e) {
            System.err.println("Failed to send error: " + e.getMessage());
        }
    }

    private String formatMove(ChessMove move) {
        return positionToString(move.getStartPosition()) + " to " +
                positionToString(move.getEndPosition());
    }

}