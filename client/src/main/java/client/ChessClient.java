package client;

public class ChessClient {

    private final String serverUrl;
    private final ServerFacade server;
    public ChessClient(String serverUrl, String serverUrl1, ServerFacade server) {

        this.serverUrl = serverUrl;
        this.server = new ServerFacade(serverUrl);
    }
}
