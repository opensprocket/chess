package server;

import com.google.gson.Gson;
import io.javalin.*;
import io.javalin.http.Context;
import dataaccess.*;
import datamodel.*;
import jakarta.servlet.Registration;
import org.jetbrains.annotations.NotNull;
import service.*;


public class Server {

    private final Javalin server;
    private DataAccess dataAccess;
    private final UserService userService;
    private final GameService gameService;
    private final ClearService clearService;
    private final Gson serializer = new Gson();


    public Server() {
        dataAccess = new MemoryDataAccess();

        this.userService = new UserService(dataAccess);
        this.gameService = new GameService(dataAccess);
        this.clearService = new ClearService(dataAccess);

        server = Javalin.create(config -> config.staticFiles.add("web"));

        // Register your endpoints and exception handlers here.

        server.delete("/db", this::clearDatabase);
        server.post("/user", this::register);
        server.post("/session", this::login);
        server.delete("/session", this::logout);
        server.get("/game", this::listGames);
        server.post("/game", this::createGame);
        server.put("/game", this::joinGame);

        // exception handling
        server.exception(DataAccessException.class, this::dataAccessExceptionHandler);
        server.exception(Exception.class, this::exceptionHandler);
    }

    private void clearDatabase(@NotNull Context ctx) throws DataAccessException{
        clearService.clearApplication();
        ctx.status(200).result("{}");
    }

    private void register(Context ctx) throws DataAccessException {
        UserData user = serializer.fromJson(ctx.body(), UserData.class);
        RegistrationResult result = userService.register(user);
        ctx.status(200).result(serializer.toJson(result));
    }

    private void login(Context ctx) throws DataAccessException {
        LoginRequest req = serializer.fromJson(ctx.body(), LoginRequest.class);
        LoginResult result = userService.login(req);
        ctx.status(200).result(serializer.toJson(result));
    }

    private void logout(Context ctx) throws DataAccessException {
        String authToken = ctx.header("authorization");
        userService.logout(authToken);
        ctx.status(200).result("{}");
    }

    private void listGames(Context ctx) throws DataAccessException {
        String authToken = ctx.header("authorization");
        userService.checkAuth(authToken); // 401 if bad
        ListGameResult result = gameService.listGames();
        ctx.status(200).result(serializer.toJson(result));
    }

    private void createGame(Context ctx) throws DataAccessException {
        String authToken = ctx.header("authorization");
        AuthData auth = userService.checkAuth(authToken); // throws 401
        CreateGameRequest req = serializer.fromJson(ctx.body(), CreateGameRequest.class);
        CreateGameResult result = gameService.createGame(req);
        ctx.status(200).result(serializer.toJson(result));
    }

    private void joinGame(Context ctx) throws DataAccessException {
        String authToken = ctx.header("authorization");
        AuthData auth = userService.checkAuth(authToken);
        JoinGameRequest req = serializer.fromJson(ctx.body(), JoinGameRequest.class);
        gameService.joinGame(req, auth.username());
        ctx.status(200).result("{}");
    }

    // exception handling
    private void dataAccessExceptionHandler(DataAccessException ex, Context ctx) {
        ErrorResponse res = new ErrorResponse(ex.getMessage());
        String message = ex.getMessage().toLowerCase();

        // parse message content and assign appropriate HTTP error code
        if (message.contains("bad request")) {
            ctx.status(400);
        } else if (message.contains("unauthorized")) {
            ctx.status(401);
        } else if (message.contains("already taken")) {
            ctx.status(403);
        } else {
            ctx.status(500);
        }
        ctx.result(serializer.toJson(res));
    }

    private void exceptionHandler (Exception ex, Context ctx) {
        ctx.status(500);
        ctx.result(serializer.toJson(new ErrorResponse("Error: " + ex.getMessage())));
    }

    // server control
    public int run(int desiredPort) {
        server.start(desiredPort);
        return server.port();
    }

    public void stop() {
        server.stop();
    }
}
