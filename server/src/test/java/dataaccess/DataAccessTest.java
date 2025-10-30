package dataaccess;

import datamodel.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class DataAccessTest {

    private DataAccess da;
    private UserData testUser;

    @BeforeEach
    void setUp() {
        da = new MemoryDataAccess();
        testUser = new UserData("player1", "pass123", "player1@email.com");
    }

    // clear
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
//      user creation
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
//    auth
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
        
    }

}
