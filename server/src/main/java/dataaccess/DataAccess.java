package dataaccess;

import datamodel.*;

import java.util.Collection;

public interface DataAccess {
//    void saveUser(UserData user);
//    void getUser(String user);


    void clear() throws DataAccessException;
    void createUser(UserData user);
    UserData getUser(String username);
    AuthData createAuth(String username) throws DataAccessException;
    AuthData getAuth(String authToken) throws DataAccessException;
    void deleteAuth(String authToken) throws DataAccessException;

    //game
    int createGame(String gameName) throws DataAccessException;
    GameData getGame(int gameID) throws DataAccessException;
    Collection<GameData> listGames() throws DataAccessException;
    void updateGame(int gameID, GameData game) throws DataAccessException;
}
