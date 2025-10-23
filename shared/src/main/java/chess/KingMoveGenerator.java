package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class KingMoveGenerator implements MoveCalculator {

    private static final int[][] DIRECTIONS = {
            {1,0}, // up
            {-1,0}, // down
            {0,1}, // right
            {0,-1}, // left
            {1,1}, // up right
            {1,-1}, // up left
            {-1,1}, // down right
            {-1,-1}, // down left
    };

    @Override
    public Collection<ChessMove> possibleMoves(ChessBoard board, ChessPosition from) {
        ChessPiece piece = board.getPiece(from);
        return OmnidirectionalMoves.generate(board, from, piece.getTeamColor(), DIRECTIONS, 1);
    }

    @Override
    public boolean onTheBoard(int row, int col) {
        return (row >= 1 && row <= 8 && col >= 1 && col <= 8);
    }
}
