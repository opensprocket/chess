package client;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Assertions.*;

import server.Server;
import chess.datamodel.*;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    private static int port;
    private static String serverUrl;

    private final String usr1 = "player1";
    private final String pass1 = "password123";
    private final String email1 = "p1@test.com";

    private final String usr2 = "player2";
    private final String pass2 = "hunter2";
    private final String email2 = "p2@test.com";

    private final String game1 = "game1";
    private final String game2 = "game2";

    public void regUsr1() throws FacadeException {
        facade.register(usr1, pass1, email1);
    }

    public AuthData regAndAuthUsr1() throws  FacadeException {
        return facade.register(usr1, pass1, email1);
    }

    public AuthData regAndAuthUsr2() throws FacadeException {
        return facade.register(usr2, pass2, email2);
    }



    @BeforeAll
    public static void init() {
        server = new Server();
        port = server.run(0);
        serverUrl = "http://localhost:" + port;

        System.out.println("Started test HTTP server on " + port);

        facade = new ServerFacade(serverUrl);
    }

    @BeforeEach
    public void setup() throws FacadeException {
        facade.clearDatabase();
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    void regSuccess() throws FacadeException {
        AuthData auth = facade.register(usr1, pass1, email1);
        assertNotNull(auth);
        assertNotNull(auth.authToken());
        assertEquals(usr1, auth.username());
    }

    @Test
    void regDuplicate() throws FacadeException {
        facade.register(usr1, pass1, email1);
        assertThrows(FacadeException.class, () -> facade.register(usr1, pass1, email1));
    }

    @Test
    void loginSuccess() throws FacadeException {
        regUsr1();
        AuthData auth = facade.login(usr1, pass1);
        assertNotNull(auth);
        assertNotNull(auth.authToken());
        assertEquals(usr1, auth.username());
    }

    @Test
    void loginFail() throws FacadeException {
        regUsr1();
        assertThrows(FacadeException.class, () -> facade.login(usr1, "notmypassword"));
    }

    @Test
    void logoutSuccess() throws FacadeException {
        AuthData auth = facade.register(usr1, pass1, email1);
        assertDoesNotThrow(() -> facade.logout(auth.authToken()));
        assertThrows(FacadeException.class, () -> facade.listGames(auth.authToken()));
    }

    @Test
    void logoutFailure() {
        assertThrows(FacadeException.class, () -> facade.logout("not-a-token"));
    }

    @Test
    void createGameSuccess() throws FacadeException {
        AuthData auth = facade.register(usr1, pass1, email1);
        String gameName = "AmazingGame";
        int gameID = facade.createGame(gameName, auth.authToken()).gameID();
        assertTrue(gameID > 0);
    }

    @Test
    void createGameFail() throws FacadeException {
        AuthData auth = facade.register(usr1, pass1, email1);
        String gameName = "";
        assertThrows(FacadeException.class, () -> facade.createGame(gameName, auth.authToken()));
    }

    @Test
    void listGameSuccess() throws FacadeException {
        AuthData auth = regAndAuthUsr1();
        facade.createGame(game1, auth.authToken());
        facade.createGame(game2, auth.authToken());

        ListGameResult res = facade.listGames(auth.authToken());
        assertNotNull(res.games());
        assertEquals(2, res.games().size());
    }

    @Test
    void listGameFail() throws FacadeException {
        AuthData authData = regAndAuthUsr1();
        assertThrows(FacadeException.class, () -> facade.listGames("NotAnAuthToken"));
    }

    @Test
    void joinGameSuccess() throws FacadeException {
        AuthData auth = regAndAuthUsr1();
        int gameID = facade.createGame(game1,auth.authToken()).gameID();
        assertDoesNotThrow(() -> facade.joinGame(gameID, "white", auth.authToken()));
    }

    @Test
    void joinGameFail() throws FacadeException {
        AuthData authData = regAndAuthUsr1();
        int gameID = -1;
        assertThrows(FacadeException.class, () -> facade.joinGame(gameID, "white", authData.authToken()));
    }

    @Test
    void joinObserverSuccess() throws FacadeException {
        AuthData auth = regAndAuthUsr1();
        int gameID = facade.createGame(game1, auth.authToken()).gameID();
        assertDoesNotThrow(() -> facade.joinGame(gameID, "black", auth.authToken()));
    }

    @Test
    void joinObserverFail() throws FacadeException {
        AuthData auth = regAndAuthUsr1();
        int gameID = 0;
        assertThrows(FacadeException.class, () -> facade.joinGame(gameID, "white", auth.authToken()));
    }

    @Test
    void joinGameAlreadyTaken() throws FacadeException {
        AuthData auth1 = regAndAuthUsr1();
        AuthData auth2 = regAndAuthUsr2();

        int gameID = facade.createGame(game1, auth2.authToken()).gameID();

        facade.joinGame(gameID, "white", auth1.authToken());

        assertThrows(FacadeException.class, () -> facade.joinGame(gameID, "white", auth2.authToken()));
    }

    @Test
    void joinGameInvalidGameID() throws FacadeException {
        AuthData auth = regAndAuthUsr1();
        int fakeID = 1337;
        assertThrows(FacadeException.class, () -> facade.joinGame(fakeID, "white", auth.authToken()));
    }




}
