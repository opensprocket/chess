package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    private final ConcurrentHashMap<Integer, ArrayList<Connection>> gameConnections = new ConcurrentHashMap<>();
    private final Gson gson = new Gson();

    public void addConnection(Integer gameID, String authToken, Session session) {
        var connection = new Connection(authToken, session);
        gameConnections.computeIfAbsent(gameID, k -> new ArrayList<>()).add(connection);
    }

    public void removeConnection(Integer gameID, String authToken) {
        var connections = gameConnections.get(gameID);
        if (connections != null) {
            connections.removeIf(c -> c.authToken.equals(authToken));
            if (connections.isEmpty()) {
                gameConnections.remove(gameID);
            }
        }
    }

    public void broadcast(Integer gameID, ServerMessage message, String excludeAuthToken) throws IOException {
        var connections = gameConnections.get(gameID);
        if (connections != null) {
            var removeList = new ArrayList<Connection>();
            for (var c : connections) {
                if (c.session.isOpen()) {
                    if (!c.authToken.equals(excludeAuthToken)) {
                        c.send(gson.toJson(message));
                    }
                } else {
                    removeList.add(c);
                }
            }
            for (var c : removeList) {
                connections.remove(c);
            }
        }
    }

    public void sendToClient(Integer gameID, String authToken, ServerMessage message) throws IOException {
        var connections = gameConnections.get(gameID);
        if (connections != null) {
            for (var c : connections) {
                if (c.session.isOpen() && c.authToken.equals(authToken)) {
                    c.send(gson.toJson(message));
                    return;
                }
            }
        }
    }

    private static class Connection {
        public String authToken;
        public Session session;

        public Connection(String authToken, Session session) {
            this.authToken = authToken;
            this.session = session;
        }

        public void send(String msg) throws IOException {
            session.getRemote().sendString(msg);
        }
    }
}