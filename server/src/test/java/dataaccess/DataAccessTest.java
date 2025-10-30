package dataaccess;

import chess.ChessGame;
import datamodel.*;
import org.junit.jupiter.api.*;

import javax.xml.crypto.Data;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class DataAccessTest {

    private DataAccess da;
    private UserData testUser;

    @BeforeEach
    void setUp() {
        da = new MemoryDataAccess();
        testUser = new UserData("player1", "pass123", "player1@email.com");
    }

//  clear
    @Test
    void clearSuccess() throws DataAccessException {
        da.createUser(testUser);
        da.createAuth(testUser.username());
        da.createGame("test game");

        assertNotNull(da.getUser(testUser.username()));
        assertFalse(da.listGames().isEmpty());

        da.clear();

        assertNull(da.getUser(testUser.username()));
        assertTrue(da.listGames().isEmpty());
    }
//  user creation
    @Test
    void createUserSuccess() {
        da.createUser(testUser);
        UserData retrievedUser = da.getUser(testUser.username());
        assertEquals(testUser, retrievedUser);
    }

    @Test
    void createUserDuplicateFail() {
        da.createUser(testUser);
        UserData updatedUser = new UserData("player1", "hunter2", "notplayer1@email.com");
        da.createUser(updatedUser);

        UserData retrievedUser = da.getUser("player1");
        assertEquals(updatedUser, retrievedUser);
        assertEquals("hunter2", retrievedUser.password());
    }

    @Test
    void getUserSuccess() throws DataAccessException {
        da.createUser(testUser);
        UserData retrievedUser = da.getUser("player1");
        assertNotNull(retrievedUser);
        assertEquals(testUser, retrievedUser);
    }

    @Test
    void getUserNotFound() throws DataAccessException {
        UserData retrievedUser = da.getUser("non-existent user");
        assertNull(retrievedUser);
    }
//  auth
    @Test
    void createAuthSuccess() throws DataAccessException {
        AuthData auth = da.createAuth(testUser.username());
        assertNotNull(auth);
        assertNotNull(auth.authToken());
        assertEquals(testUser.username(), auth.username());

        // pull from storage
        AuthData res = da.getAuth(auth.authToken());
        assertEquals(auth, res);
    }

    @Test
    void getAuthSuccess() throws DataAccessException {
        AuthData auth = da.createAuth(testUser.username());
        AuthData res = da.getAuth(auth.authToken());
        assertEquals(auth, res);
    }

    @Test
    void authNotFound() throws DataAccessException {
        AuthData auth = da.getAuth("fakedata");
        assertNull(auth);
    }

    @Test
    void deleteAuthSuccess() throws DataAccessException {
        AuthData auth = da.createAuth(testUser.username());
        assertNotNull(da.getAuth(auth.authToken()));

        da.deleteAuth(auth.authToken());

        assertNull(da.getAuth(auth.authToken()));
    }

    @Test
    void deleteAuthFail() {
        assertDoesNotThrow(() -> da.deleteAuth("fake data"));
    }

//  game
    @Test
    void createGameSuccess() throws DataAccessException {
        int gameID = da.createGame("My Game");
        assertTrue(gameID > 0);

        GameData game = da.getGame(gameID);
        assertNotNull(game);
        assertEquals(gameID, game.gameID());
        assertEquals("My Game", game.gameName());
        assertNull(game.whiteUsername());
        assertNull(game.blackUsername());
        assertNotNull(game.game());
        assertEquals(ChessGame.TeamColor.WHITE, game.game().getTeamTurn());
    }

    @Test
    void getGameSuccess() throws DataAccessException {
        int id = da.createGame("My Game");
        GameData game = da.getGame(id);
        assertNotNull(game);
        assertEquals(id, game.gameID());
    }

    @Test
    void getGameFail() throws DataAccessException {
        GameData game = da.getGame(1337);
        assertNull(game);
    }

    @Test
    void listGamesSuccess() throws DataAccessException {
        int g1 = da.createGame("game1");
        int g2 = da.createGame("game2");

        Collection<GameData> games = da.listGames();
        assertEquals(2, games.size());
        assertTrue(games.stream().anyMatch(g -> g.gameID() == g1));
        assertTrue(games.stream().anyMatch(g -> g.gameID() == g2));
    }

    @Test
    void listGamesSuccessEmpty() throws DataAccessException {
        Collection<GameData> games = da.listGames();
        assertNotNull(games);
        assertTrue(games.isEmpty());
    }
}
