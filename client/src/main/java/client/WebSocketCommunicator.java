package client;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import jakarta.websocket.*;

import java.io.IOException;
import java.net.URI;

@ClientEndpoint
public class WebSocketCommunicator {
    private Session session;
    private final Gson gson = new Gson();
    private final NotificationHandler notificationHandler;

    public WebSocketCommunicator(String url, NotificationHandler handler) throws Exception {
        this.notificationHandler = handler;
        URI uri = new URI(url.replace("http", "ws") + "/ws");

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        this.session = container.connectToServer(this, uri);
    }

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        // Connection opened
        this.session = session;
    }
}