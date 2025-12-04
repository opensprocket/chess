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

}