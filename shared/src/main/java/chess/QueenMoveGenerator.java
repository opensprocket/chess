package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class QueenMoveGenerator implements MoveCalculator {
    @Override
    public Collection<ChessMove> possibleMoves(ChessBoard board, ChessPosition from) {
        List<ChessMove> moves = new ArrayList<>();



        return moves;
    }

    @Override
    public boolean onTheBoard(int row, int col) {
        return (row >= 1 && row <= 8 && col >= 1 && col <= 8);
    }
}
