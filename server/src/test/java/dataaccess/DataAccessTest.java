package dataaccess;

import datamodel.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class DataAccessTest {

    private DataAccess dataAccess;
    private UserData testUser;

    @BeforeEach
    void setUp() {
        dataAccess = new MemoryDataAccess();
        testUser = new UserData("player1", "pass123", "player1@email.com");
    }

    // clear
    @Test
    void clearSuccess() throws DataAccessException {
        dataAccess.createUser(testUser);
        dataAccess.createAuth(testUser.username());
        dataAccess.createGame("test game");

        assertNotNull(dataAccess.getUser(testUser.username()));
        assertFalse(dataAccess.listGames().isEmpty());

        dataAccess.clear();

        assertNull(dataAccess.getUser(testUser.username()));
        assertTrue(dataAccess.listGames().isEmpty());
    }

    @Test
    void createUserSuccess() {
        dataAccess.createUser(testUser);
        UserData retrievedUser = dataAccess.getUser(testUser.username());
        assertEquals(testUser, retrievedUser);
    }

    @Test
    void createUserDuplicateFail() {
        dataAccess.createUser(testUser);
        UserData updatedUser = new UserData("player1", "hunter2", "notplayer1@email.com");
        dataAccess.createUser(updatedUser);

        UserData retrievedUser = dataAccess.getUser("player1");
        assertEquals(updatedUser, retrievedUser);
        assertEquals("hunter2", retrievedUser.password());
    }

}
