package chess;

import java.util.ArrayList;
import java.util.Collection;

final class OmnidirectionalMoves {
    private OmnidirectionalMoves() {}

    static Collection<ChessMove> generate(ChessBoard board,
                                          ChessPosition from,
                                          ChessGame.TeamColor color,
                                          int[][] directions,
                                          int maxSteps) {
        Collection<ChessMove> moves = new ArrayList<>();

        for (int[] dir : directions) {
            int row = from.getRow() + dir[0];
            int col = from.getColumn() + dir[1];
            int steps = 0;

            while (onBoard(row, col) && steps < maxSteps) {
                ChessPosition to = new ChessPosition(row, col);
                ChessPiece target = board.getPiece(to);

                if (target == null) {
                    moves.add(new ChessMove(from, to, null));
                } else {
                    if (target.getTeamColor() != color) {
                        moves.add(new ChessMove(from, to, null));
                    }
                    break; // blocked by piece
                }

                row += dir[0];
                col += dir[1];
                steps++;
            }
        }
        return moves;
    }

    static boolean onBoard(int row, int col) {
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }
}
