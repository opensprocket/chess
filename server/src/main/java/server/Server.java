package server;

import com.google.gson.Gson;
import io.javalin.*;
import io.javalin.http.Context;

import java.util.Map;

public class Server {

    private final Javalin server;

    public Server() {
        server = Javalin.create(config -> config.staticFiles.add("web"));

        // Register your endpoints and exception handlers here.

        server.delete("db", ctx -> ctx.result("{}")); // clear database

        server.post("user", this::register);
    }

    private void register(Context ctx) {
        var serializer = new Gson();
        var request = serializer.fromJson(ctx.body(), Map.class);
        request.put("authToken", "cow");
        var response = serializer.toJson(request);
        ctx.result("{\"username\":\"john\", \"authToken\":\"xyz\"}");
    }

    public int run(int desiredPort) {
        server.start(desiredPort);
        return server.port();
    }

    public void stop() {
        server.stop();
    }
}
