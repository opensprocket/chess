package dataaccess;

import com.google.gson.Gson;
import datamodel.*;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;


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
    public void createUser(UserData user) throws DataAccessException {
        String hashedPass = BCrypt.hashpw(user.password(), BCrypt.gensalt());

        String statement = "INSERT INTO user (username, password, email) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE password = ?, email = ?";

        try (var conn = DatabaseManager.getConnection()) {
            var ps = conn.prepareStatement(statement);

            ps.setString(1, user.username());
            ps.setString(2, hashedPass);
            ps.setString(3, user.email());
            // on user update
            ps.setString(4, hashedPass);
            ps.setString(5, user.email());

            ps.executeUpdate();

        } catch (SQLException ex) {
            throw new DataAccessException("Failed to create user", ex);
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        var statement = "SELECT username, password, email FROM user WHERE username = ?";
        try (var conn = DatabaseManager.getConnection()) {
            var ps = conn.prepareStatement(statement);

            ps.setString(1, username);

            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new UserData(rs.getString("username"), rs.getString("password"), rs.getString("email"));
                }
            }

        } catch (SQLException ex) {
            throw new DataAccessException("Failed to get user", ex);
        }
        // no record found
        return null;
    }

    @Override
    public AuthData createAuth(String username) throws DataAccessException {
        String authToken = UUID.randomUUID().toString();
        var statement = "INSERT INTO auth (authToken, username) VALUES (?, ?)";

        try (var conn = DatabaseManager.getConnection()) {
            var ps = conn.prepareStatement(statement);

            ps.setString(1, authToken);
            ps.setString(2, username);

            ps.executeUpdate();

            return new AuthData(authToken, username);

        } catch (SQLException ex) {
            throw new DataAccessException("Failed to create auth", ex);
        }
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
