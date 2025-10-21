package datamodel;

import chess.ChessGame;

public record GameInfo(
        int gameID,
        String whiteUsername,
        String blackUsername,
        String gameName,
        ChessGame game
) {}