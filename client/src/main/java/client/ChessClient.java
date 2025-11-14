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
}
