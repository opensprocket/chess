package service;

import dataaccess.*;
import datamodel.*;

import javax.xml.crypto.Data;


public class UserService {
    private DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public RegistrationResult register(UserData user) throws DataAccessException {

        if (user.username() == null || user.password() == null || user.email() == null) {
            throw new DataAccessException("Error: bad request");
        }

        if (dataAccess.getUser(user.username()) != null) {
            throw new DataAccessException(String.format("Error: %s already taken", user.username()));
        }

        dataAccess.createUser(user);
        AuthData auth = dataAccess.createAuth(user.username());
        return new RegistrationResult(auth.username(), auth.authToken());
    }

    public LoginResult login(LoginRequest req) throws DataAccessException {
        UserData user = dataAccess.getUser(req.username());

        if (user == null || !user.password().equals(req.password())) {
            throw new DataAccessException("Error: Unauthorized");
        }

        AuthData authData = dataAccess.createAuth(user.username());
        return new LoginResult(authData.username(), authData.authToken());
    }

    public void logout(String authToken) throws DataAccessException {
        if (dataAccess.getAuth(authToken) == null) {
            throw new DataAccessException("Error: Unauthorized");
        }
        dataAccess.deleteAuth(authToken);
    }

    public AuthData checkAuth(String authToken) throws DataAccessException {
        AuthData auth = dataAccess.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("Error: Unauthorized");
        }
        return auth;
    }
}
