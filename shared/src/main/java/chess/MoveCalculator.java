package chess;

import java.util.Collection;

public interface MoveCalculator {
    Collection<ChessMove> possibleMoves(ChessBoard board, ChessPosition from);
    boolean onTheBoard(int row, int col);
}
