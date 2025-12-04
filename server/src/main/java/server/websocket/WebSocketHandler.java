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
}