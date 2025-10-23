package service;

import chess.ChessGame;
import dataaccess.*;
import datamodel.*;

import java.util.Collection;
import java.util.stream.Collectors;


public class GameService {
    private DataAccess dataAccess;

    public GameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public ListGameResult listGames() throws DataAccessException {
        Collection<GameData> allGames = dataAccess.listGames();
        Collection<GameInfo> gameInformation = allGames.stream()
                .map(game -> new GameInfo(game.gameID(), game.whiteUsername(), game.blackUsername(), game.gameName()))
                .collect(Collectors.toList());
        return new ListGameResult(gameInformation);
    }

    public CreateGameResult createGame(CreateGameRequest req) throws DataAccessException {
        if (req.gameName() == null) {
           throw new DataAccessException("Error: bad request");
        }
        int gameID = dataAccess.createGame(req.gameName());
        return new CreateGameResult(gameID);
    }

    public void joinGame(JoinGameRequest req, String username) throws DataAccessException {
        GameData game = dataAccess.getGame(req.gameID());
        if (game == null) {
            throw new DataAccessException("Error: bad request");
        }

        // observer
        if (req.playerColor() == null) {
            throw new DataAccessException("Error: bad request");
        }

        // player
        GameData updatedGame = null;
        if (req.playerColor() == ChessGame.TeamColor.WHITE) {
            if (game.whiteUsername() != null) {
                throw new DataAccessException("Error: already taken");
            }
            updatedGame = new GameData(game.gameID(), username, game.blackUsername(), game.gameName(), game.game());
        } else if (req.playerColor() == ChessGame.TeamColor.BLACK) {
            if (game.blackUsername() != null) {
                throw new DataAccessException("Error: already taken");
            }
            updatedGame = new GameData(game.gameID(), game.whiteUsername(), username, game.gameName(), game.game());
        } else {
            // only accept white, black or null for colors and reject all others
            throw new DataAccessException("Error: bad request");
        }

        if (updatedGame != null) {
            dataAccess.updateGame(game.gameID(), updatedGame); // this might be redundant, but leaving it here anyways
        }

    }

}
