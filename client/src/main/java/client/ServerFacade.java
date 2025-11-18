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

    public AuthData register(String username, String password, String email) throws FacadeException {
        var req = new UserData(username, password, email);
        var res = makeRequest("POST", "/user", req, null, RegistrationResult.class);
        return new AuthData(res.authToken(), res.username());
    }

    public AuthData login(String username, String password) throws FacadeException {
        var req = new LoginRequest(username, password);
        var res = makeRequest("POST", "/session", req, null, LoginResult.class);
        return new AuthData(res.authToken(), res.username());
    }

    private <T> T makeRequest(String method, String path, Object reqObj, String authToken, Class<T> responseClass) throws FacadeException {
        try {
            URI uri = new URI(serverUrl + path);
            HttpRequest.Builder builder = HttpRequest.newBuilder(uri);

            String reqBody = (reqObj == null) ? "" : gson.toJson(reqObj);

            HttpRequest.BodyPublisher bodyPublisher = (reqBody.isEmpty()) ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(reqBody);
            builder.method(method, bodyPublisher);

            if (!reqBody.isEmpty()) {
                builder.header("Content-Type", "application/json");
            }
            if (authToken != null) {
                builder.header("Authorization", authToken);
            }

            HttpRequest req = builder.build();
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

            return handleResponse(res, responseClass);

        } catch (Exception ex) {
            throw new FacadeException(ex.getMessage());
        }
    }

    private <T> T handleResponse(HttpResponse<String> res, Class<T> responseClass) throws FacadeException {
        try {
            if (res.statusCode() >= 200 && res.statusCode() < 300) {
                if (responseClass != null && res.body() != null && !res.body().isEmpty()) {
                    return gson.fromJson(res.body(), responseClass);
                } else {
                    return null;
                }
            } else {
                try {
                    ErrorResponse errRes = gson.fromJson(res.body(), ErrorResponse.class);
                    throw new FacadeException(errRes.message());
                } catch (Exception ex) {
                    throw new FacadeException(String.format("Error: %d - %s", res.statusCode(), res.body()));
                }
            }
        } catch (Exception ex) {
            throw new FacadeException(ex.getMessage());
        }
    }

}
