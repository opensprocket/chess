package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class KnightMoveGenerator implements MoveCalculator {
    private static final int[][] DIRECTIONS = {
            {2,-1},  // top left
            {2, 1},  // top right
            {1,-2},  // above left
            {1,2},   // above right
            {-1,-2}, // below left
            {-1, 2}, // below right
            {-2,-1}, // bottom left
            {-2, 1}  // bottom right
    };

    @Override
    public Collection<ChessMove> possibleMoves(ChessBoard board, ChessPosition from) {
        Collection<ChessMove> moves = new ArrayList<>();

        ChessPiece piece = board.getPiece(from);

        if (piece == null) {
            return moves;
        }

        ChessGame.TeamColor myTeam = piece.getTeamColor();

        for (int[] dir : DIRECTIONS) {

            // offset to exclude start location
            int row = from.getRow() + dir[0];
            int col = from.getColumn() + dir[1];

            if (onTheBoard(row, col)) {
                ChessPosition to = new ChessPosition(row,col); // potential move

                ChessPiece target = board.getPiece(to); // check destination piece

                if (target == null) {
                    moves.add(new ChessMove(from, to, null));
                } else {
                    if (target.getTeamColor() != myTeam) {
                        moves.add(new ChessMove(from, to, null));
                    }
                    // do nothing if friendly piece
                }
            }
        }

        return moves;
    }

    @Override
    public boolean onTheBoard(int row, int col) {
        return (row >=1 && row <= 8 && col >= 1 && col <= 8);
    }
}
