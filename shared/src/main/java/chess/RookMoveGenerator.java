package chess;

import java.util.ArrayList;
import java.util.Collection;

public class RookMoveGenerator implements MoveCalculator {

    private static final int[][] DIRECTIONS = {
            {1,0}, // up
            {-1,0}, // down
            {0,1}, // right
            {0,-1} // left
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
            int row = from.getRow() + dir[0];
            int col = from.getColumn() + dir[1];

            while (onTheBoard(row,col)) {
                ChessPosition to = new ChessPosition(row,col);

                ChessPiece target = board.getPiece(to);

                // empty
                if (target == null) {
                    moves.add(new ChessMove(from,to, null));
                } else {
                    // valid capture target
                    if (target.getTeamColor() != myTeam) {
                        moves.add(new ChessMove(from,to,null));
                    }
                    break; // stop if friendly
                }
                row += dir[0];
                col += dir[1];
            }
        }

        return moves;
    }

    @Override
    public boolean onTheBoard(int row, int col) {
        return (row >= 1 && row <= 8 && col >= 1 && col <= 8);    }
}
