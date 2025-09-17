package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class QueenMoveGenerator implements MoveCalculator {

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
        List<ChessMove> moves = new ArrayList<>();

        ChessPiece piece = board.getPiece(from);

        if (piece == null) {
            return moves;
        }

        ChessGame.TeamColor myTeam = piece.getTeamColor();

        for (int[] dir : DIRECTIONS) {
            int row = from.getRow() + dir[0]; // offset to avoid counting start position
            int col = from.getColumn() + dir[1]; // offset to avoid counting start position

            while (onTheBoard(row,col)) {
                //check move
                ChessPosition to = new ChessPosition(row,col);

                ChessPiece target = board.getPiece(to); // destination

                // if empty
                if (target == null) {
                    moves.add(new ChessMove(from, to, null));
                } else {
                    // valid capture target
                    if (target.getTeamColor() != myTeam) {
                        moves.add(new ChessMove(from, to, null));
                    }
                    break; // stop sliding if friendly piece
                }
                //increment counters
                row += dir[0];
                col += dir[1];
            }
        }

        return moves;
    }

    @Override
    public boolean onTheBoard(int row, int col) {
        return (row >= 1 && row <= 8 && col >= 1 && col <= 8);
    }
}
