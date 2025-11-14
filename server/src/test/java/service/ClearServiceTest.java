package service;

import chess.datamodel.UserData;
import dataaccess.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;


public class ClearServiceTest {
    private DataAccess dataAccess;
    private ClearService clearService;

    @BeforeEach
    void setUp() {
        dataAccess = new MemoryDataAccess();
        clearService = new ClearService(dataAccess);
    }

    @Test
    void clearApplicationData() throws DataAccessException {
        dataAccess.createUser(new UserData("user", "pass", "email"));
        dataAccess.createAuth("user");
        dataAccess.createGame("testGame");

        // pre
        assertNotNull(dataAccess.getUser("user"));
        assertFalse(dataAccess.listGames().isEmpty());

        // trigger clear
        clearService.clearApplication();

        // evaluate result
        assertNull(dataAccess.getUser("user"));
        assertTrue(dataAccess.listGames().isEmpty());
    }
}
