package service;

import chess.datamodel.*;
import dataaccess.*;
import org.mindrot.jbcrypt.BCrypt;


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
            throw new DataAccessException("Error: already taken");
        }

        String hashedPass = BCrypt.hashpw(user.password(), BCrypt.gensalt());
        UserData storedUser = new UserData(user.username(), hashedPass, user.email());

        dataAccess.createUser(storedUser);

        AuthData auth = dataAccess.createAuth(user.username());
        return new RegistrationResult(auth.username(), auth.authToken());
    }

    public LoginResult login(LoginRequest req) throws DataAccessException {
        if (req.username() == null || req.password() == null) {
            throw new DataAccessException("Error: bad request");
        }

        UserData user = dataAccess.getUser(req.username());

        if (user == null ||
                !BCrypt.checkpw(req.password(), user.password())) {
            throw new DataAccessException("Error: unauthorized");
        }

        AuthData authData = dataAccess.createAuth(user.username());
        return new LoginResult(authData.username(), authData.authToken());
    }

    public void logout(String authToken) throws DataAccessException {
        if (dataAccess.getAuth(authToken) == null) {
            throw new DataAccessException("Error: unauthorized");
        }
        dataAccess.deleteAuth(authToken);
    }

    public AuthData checkAuth(String authToken) throws DataAccessException {
        AuthData auth = dataAccess.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("Error: unauthorized");
        }
        return auth;
    }
}
