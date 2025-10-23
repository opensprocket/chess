package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class QueenMoveGenerator implements MoveCalculator {

    private static final int[][] directions = {
            {1,0},
            {-1,0},
            {0,1},
            {0,-1},
            {1,1},
            {1,-1},
            {-1,1},
            {-1,-1}
    };

    @Override
    public Collection<ChessMove> possibleMoves(ChessBoard board, ChessPosition from) {
        ChessPiece piece = board.getPiece(from);
        return OmnidirectionalMoves.generate(board, from, piece.getTeamColor(), directions, 7);
    }

    @Override
    public boolean onTheBoard(int row, int col) {
        return (row >= 1 && row <= 8 && col >= 1 && col <= 8);
    }
}
