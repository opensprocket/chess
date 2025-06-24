package chess.movecheckers;

import chess.ChessGame;
import chess.ChessBoard;
import chess.ChessPosition;
import chess.ChessMove;

public interface MoveChecker {

    // Check if the square is in bounds
    static boolean isValidSpace(ChessPosition position) {
        return (position.getRow() >= 1 && position.getRow() <= 8) &&
                (position.getColumn() >= 1 && position.getColumn() <= 8);
    }

}
