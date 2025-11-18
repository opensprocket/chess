package client;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.*;
import java.net.http.*;
import chess.datamodel.*;
import java.net.http.HttpRequest.*;


public class ServerFacade {

    private final String serverUrl;
    private final HttpClient client;
    private final Gson gson;

    public ServerFacade(String serverUrl) {
        this.serverUrl = serverUrl;
        this.client = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    public void clearDatabase() throws FacadeException {
        makeRequest("DELETE", "/db", null, null, null);
    }
}
