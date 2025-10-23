package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import datamodel.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {
    private DataAccess dataAccess;
    private UserService userService;
    private UserData testUser;

    @BeforeEach
    void setUp() {
        dataAccess = new MemoryDataAccess();
        userService = new UserService(dataAccess);
        testUser = new UserData("player1", "hunter2", "player1@email.com");
    }

    @Test
    void registerSuccessful() throws DataAccessException {
        RegistrationResult result = userService.register(testUser);
        assertNotNull(result);
        assertEquals("player1", result.username());
        assertNotNull(result.authToken());
        assertNotNull(dataAccess.getUser("player1"));
    }

    @Test
    void usernameAlreadyTaken() throws DataAccessException {
        userService.register(testUser);
        DataAccessException ex = assertThrows(DataAccessException.class, () -> userService.register(testUser));
        assertEquals("Error: already taken", ex.getMessage());
    }

    @Test
    void registerBadRequest() {
        UserData badUser = new UserData("user", null, "email@address.com");
        DataAccessException ex = assertThrows(DataAccessException.class, () -> userService.register(badUser));
        assertEquals("Error: bad request", ex.getMessage());
    }

    @Test
    void loginSuccess() throws DataAccessException {
        userService.register(testUser);
        LoginRequest req = new LoginRequest("player1", "hunter2");
        LoginResult result = userService.login(req);

        assertNotNull(result);
        assertEquals("player1", result.username());
        assertNotNull(result.authToken());
    }

    @Test
    void loginUserNotFound() {
        LoginRequest req = new LoginRequest("skeleton", "key");
        DataAccessException ex = assertThrows(DataAccessException.class, () -> userService.login(req));
        assertEquals("Error: unauthorized", ex.getMessage());
    }

    @Test
    void loginWrongPassword() throws DataAccessException {
        userService.register(testUser);
        LoginRequest req = new LoginRequest(testUser.username(), "not-my-password");
        DataAccessException ex = assertThrows(DataAccessException.class, () -> userService.login(req));
        assertEquals("Error: unauthorized", ex.getMessage());
    }

    @Test
    void logoutSuccess() throws DataAccessException {
        RegistrationResult registrationResult = userService.register(testUser);
        String authToken = registrationResult.authToken();

        // pre
        assertNotNull(dataAccess.getAuth(authToken));
        // trigger
        userService.logout(authToken);
        // post
        assertNull(dataAccess.getAuth(authToken));
    }

    @Test
    void logoutUnauthorized() {
        String fakeToken = "utes-r-good-at-football";
        DataAccessException ex = assertThrows(DataAccessException.class, () -> userService.logout(fakeToken));
        assertEquals("Error: unauthorized", ex.getMessage());
    }

    @Test
    void checkAuthSucceed() throws DataAccessException{
        AuthData auth = dataAccess.createAuth("user");
        AuthData result = userService.checkAuth(auth.authToken());
        assertEquals(auth, result);
    }

    @Test
    void checkAuthFail() {
        DataAccessException ex = assertThrows(DataAccessException.class, () -> userService.checkAuth("fakeToken"));
        assertEquals("Error: unauthorized", ex.getMessage());
    }
}