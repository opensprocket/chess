package service;

import chess.ChessGame;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import datamodel.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTest {
    private DataAccess dataAccess;
    private GameService gameService;
    private String testUsername;
    private int testGameID;

    @BeforeEach
    void setUp() throws DataAccessException {
        dataAccess = new MemoryDataAccess();
        gameService = new GameService(dataAccess);
        testUsername = "player1";
        testGameID = dataAccess.createGame("Ernest Cline's test game");
    }

    // game list
    @Test
    void listTwoGames() throws DataAccessException {
        dataAccess.createGame("Game 2");
        ListGameResult result = gameService.listGames();
        assertNotNull(result);
        assertEquals(2, result.games().size());
    }

    @Test
    void listGamesEmpty() throws DataAccessException {
        dataAccess.clear(); // remove setup game
        ListGameResult result = gameService.listGames();
        assertNotNull(result);
        assertTrue(result.games().isEmpty());
    }

    // create games
    @Test
    void createGameSuccess() throws DataAccessException {
        String gameName = "New Game";
        CreateGameRequest req = new CreateGameRequest(gameName);
        CreateGameResult result = gameService.createGame(req);

        assertNotNull(result);
        assertTrue(result.gameID() > 0);
        assertNotNull(dataAccess.getGame(result.gameID()));
        assertEquals(gameName, dataAccess.getGame(result.gameID()).gameName());
    }

    @Test
    void createGameFailBadRequest() {
        CreateGameRequest req = new CreateGameRequest(null);
        DataAccessException ex = assertThrows(DataAccessException.class, () -> gameService.createGame(req));
        assertEquals("Error: bad request", ex.getMessage());
    }

    // join games
    @Test
    void joinGameAsWhiteSuccess() throws DataAccessException {
        JoinGameRequest req = new JoinGameRequest(ChessGame.TeamColor.WHITE, testGameID);
        gameService.joinGame(req, testUsername);

        GameData game = dataAccess.getGame(testGameID);

        assertEquals(testUsername, game.whiteUsername());
        assertNull(game.blackUsername());
    }

    @Test
    void joinGameAsBlackSuccess() throws DataAccessException {
        JoinGameRequest req = new JoinGameRequest(ChessGame.TeamColor.BLACK, testGameID);
        gameService.joinGame(req, testUsername);

        GameData game = dataAccess.getGame(testGameID);

        assertEquals(testUsername, game.blackUsername());
        assertNull(game.whiteUsername());
    }

    @Test
    void joinGameAsObserverSuccess() throws DataAccessException {
        String observerUsername = "observer name";
        JoinGameRequest req = new JoinGameRequest(null, testGameID);
        gameService.joinGame(req, observerUsername);

        GameData game = dataAccess.getGame(testGameID);
        assertNull(game.whiteUsername());
        assertNull(game.blackUsername());
    }

    @Test
    void joinGameNotFound() {
        JoinGameRequest req = new JoinGameRequest(ChessGame.TeamColor.WHITE, 1337); // fake id, fake game
        DataAccessException ex = assertThrows(DataAccessException.class, () -> gameService.joinGame(req, "some-username"));
        assertEquals("Error: bad request", ex.getMessage());
    }

    @Test
    void joinGameSpotTaken() throws DataAccessException {
        JoinGameRequest joinAsWhite = new JoinGameRequest(ChessGame.TeamColor.WHITE, testGameID);
        gameService.joinGame(joinAsWhite, testUsername);

        JoinGameRequest joinAsWhiteTwo = new JoinGameRequest(ChessGame.TeamColor.WHITE, testGameID);
        DataAccessException ex = assertThrows(DataAccessException.class, () -> gameService.joinGame(joinAsWhiteTwo, "player2"));
        assertEquals("Error: already taken", ex.getMessage());
    }

}
