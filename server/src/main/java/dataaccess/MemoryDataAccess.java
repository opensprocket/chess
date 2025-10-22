package dataaccess;

import chess.ChessGame;
import datamodel.AuthData;
import datamodel.GameData;
import datamodel.UserData;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

public class MemoryDataAccess implements DataAccess {

    private HashMap<String, UserData> users = new HashMap<>();
    private HashMap<String, AuthData> authTokens = new HashMap<>();
    private HashMap<String, GameData> games = new HashMap<>();

    private int nextGameID = 1;

//    @Override
//    public void saveUser(UserData user) {
//        users.put(user.username(), user);
//    }
//
//    @Override
//    public void getUser(String username) {
//        users.get(username);
//    }

    @Override
    public void clear() throws DataAccessException {
        users.clear();
        authTokens.clear();
        games.clear();
        nextGameID = 1;
    }

    @Override
    public void createUser(UserData user) {
        users.put(user.username(), user);
    }

    public UserData getUser(String username) {
        return users.get(username);
    }

    @Override
    public AuthData createAuth(String username) throws DataAccessException {
        String authToken = UUID.randomUUID().toString();
        AuthData auth = new AuthData(authToken, username);
        authTokens.put(authToken, auth);
        return auth;
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        return authTokens.get(authToken);
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        authTokens.remove(authToken);
    }

    @Override
    public int createGame(String gameName) throws DataAccessException {
        int gameID = nextGameID++;
        ChessGame newGame = new ChessGame();
        newGame.getBoard().resetBoard();
        GameData game = new GameData(gameID, null, null, gameName, newGame);
        games.put(Integer.toString(gameID), game); // string representation of integer?
        return gameID;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return games.get(Integer.toString(gameID)); // string representation of integer?
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        return games.values();
    }

    @Override
    public void updateGame(int gameID, GameData game) throws DataAccessException {
        games.put(Integer.toString(gameID), game);
    }
}
