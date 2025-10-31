package dataaccess;

import com.google.gson.Gson;
import datamodel.*;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;


public class MySQLDataAccess implements DataAccess {

    private final Gson serializer = new Gson();


    public MySQLDataAccess() throws DataAccessException {
        configureDatabase();
    }

    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();

        try(var conn = DatabaseManager.getConnection()) {
            String[] createStatements = {
                    """
                    CREATE TABLE IF NOT EXISTS user {
                        username VARCHAR(128) NOT NULL PRIMARY KEY,
                        password VARCHAR(128) NOT NULL,
                        email VARCHAR(255) NOT NULL
                    }
                    """,
                    """
                    CREATE TABLE IF NOT EXISTS auth {
                        authToken VARCHAR(255) NOT NULL PRIMARY KEY,
                        username VARCHAR(128) NOT NULL,
                        INDEX(username)
                    }
                    """,
                    """
                    CREATE TABLE IF NOT EXISTS game {
                        gameID INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                        whiteUsername VARCHAR(128) NULL,
                        blackUsername VARCHAR(128) NULL,
                        gameName VARCHAR(128) NOT NULL,
                        game TEXT NOT NULL
                    }
                    """
            };

            for (String statement : createStatements) {
                try (var ps = conn.prepareStatement(statement)) {
                    ps.executeUpdate();
                }
            }

        } catch (SQLException ex) {
            throw new DataAccessException("Unable to configure database", ex);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            String[] statements = { "TRUNCATE auth", "TRUNCATE game", "TRUNCATE user" };
            for (String statement : statements) {
                try (var ps = conn.prepareStatement(statement)) {
                    ps.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to clear database", ex);
        }
    }

    @Override
    public void createUser(UserData user) {

    }

    @Override
    public UserData getUser(String username) {
        return null;
    }

    @Override
    public AuthData createAuth(String username) throws DataAccessException {
        return null;
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        return null;
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {

    }

    @Override
    public int createGame(String gameName) throws DataAccessException {
        return 0;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return null;
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        return List.of();
    }

    @Override
    public void updateGame(int gameID, GameData game) throws DataAccessException {

    }
}
