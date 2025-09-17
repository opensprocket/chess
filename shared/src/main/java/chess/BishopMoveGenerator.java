package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BishopMoveGenerator implements MoveCalculator {

    // relative directions for moves (up right, up left, down right, down left)
    private static final int[][] DIRECTIONS = {
            {1,1},{1,-1},{-1,1},{-1,-1}
    };

    @Override
    public Collection<ChessMove> possibleMoves(ChessBoard board, ChessPosition from) {
        List<ChessMove> moves = new ArrayList<>();

        ChessPiece piece = board.getPiece(from);
        if (piece == null) return moves;

        ChessGame.TeamColor myTeam = piece.getTeamColor();

        for (int[] dir : DIRECTIONS) {
            int row = from.getRow();
            int col = from.getColumn();

            while (onTheBoard(row, col)) {
                // check
                ChessPosition to = new ChessPosition(row, col);

                ChessPiece target = board.getPiece(to);

                // if empty
                if (target == null) {
                    moves.add(new ChessMove(from, to, null));
                } else {
                    // square has a valid capture target
                    if (target.getTeamColor() != myTeam) {
                        moves.add(new ChessMove(from, to, null));
                    }
                    break; // stop sliding if friendly piece
                }

                // increment counters
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
