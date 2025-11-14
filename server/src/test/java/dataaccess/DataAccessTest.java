package dataaccess;

import chess.ChessGame;
import chess.datamodel.AuthData;
import chess.datamodel.GameData;
import chess.datamodel.UserData;
import org.junit.jupiter.api.*;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class DataAccessTest {

    private DataAccess da;
    private UserData testUser;

    @BeforeEach
    void setUp() throws DataAccessException {
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
    void createUserSuccess() throws DataAccessException {
        da.createUser(testUser);
        UserData retrievedUser = da.getUser(testUser.username());
        assertEquals(testUser, retrievedUser);
    }

    @Test
    void createUserDuplicateFail() throws DataAccessException {
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
    void createAuthFail() throws DataAccessException {
        String uname = null;
        assertThrows(DataAccessException.class, () -> {
            da.createAuth(uname);
        });
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
    void createGameFail() throws DataAccessException {
        String gameName = null;
        assertThrows(DataAccessException.class, () -> {
            da.createGame(gameName);
        });
        assertDoesNotThrow(() -> {
            Collection<GameData> games = da.listGames();
            assertTrue(games.stream().noneMatch(g -> g.gameName().equals(gameName)));
        });
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

    @Test
    void updateGameSuccess() throws DataAccessException {
        int g1 = da.createGame("game1");
        GameData originalGame = da.getGame(g1);

        // update game
        GameData updatedGame = new GameData(
                originalGame.gameID(),
                "whitePlayer",
                originalGame.blackUsername(),
                originalGame.gameName(),
                originalGame.game()
        );

        da.updateGame(g1, updatedGame);

        GameData res = da.getGame(g1);
        assertEquals(updatedGame, res);
        assertEquals("whitePlayer", res.whiteUsername());
    }

    @Test
    void updateGameFail() throws DataAccessException {
        int fake = 1337;
        GameData newGame = new GameData(fake, null, null, "newgame", new ChessGame());

        da.updateGame(fake, newGame);

        GameData res = da.getGame(fake);
        assertNotNull(res);
        assertEquals(newGame, res);
    }

}
