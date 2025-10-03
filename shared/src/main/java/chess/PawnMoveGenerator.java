package chess;

import jdk.jshell.execution.JdiExecutionControl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PawnMoveGenerator implements MoveCalculator {
    @Override
    public Collection<ChessMove> possibleMoves(ChessBoard board, ChessPosition from) {
        Collection<ChessMove> moves = new ArrayList<>();

        ChessPiece pawn = board.getPiece(from);

        // if empty or no longer a pawn
        if (pawn == null || pawn.getPieceType() != ChessPiece.PieceType.PAWN) {
            return moves;
        }

        ChessGame.TeamColor myTeam = pawn.getTeamColor();
        int direction = (myTeam == ChessGame.TeamColor.WHITE) ? 1 : -1; // 1 for white, -1 for black

        // single step
        int nextRow = from.getRow() + direction;
        int col = from.getColumn();

        ChessPosition singleMove = new ChessPosition(nextRow,col);

        // forward movement and first turn rule
        if (onTheBoard(nextRow, col) && board.getPiece(singleMove) == null) {
            addMove(moves, from, singleMove, nextRow);

            // two-square move
            if ((myTeam == ChessGame.TeamColor.WHITE && from.getRow() == 2) ||
                    (myTeam == ChessGame.TeamColor.BLACK && from.getRow() == 7)) {
                int secondRow = from.getRow() + 2 * direction;

                ChessPosition twoSquareMove = new ChessPosition(secondRow, col);

                if (board.getPiece(twoSquareMove) == null) {
                    addMove(moves, from, twoSquareMove, secondRow);
                }
            }
        }

        // diagonal captures
        int[] captureColumns = {col -1, col + 1};

        for (int captureColumn : captureColumns) {

            if (onTheBoard(nextRow, captureColumn)) {
                ChessPosition capturePosition = new ChessPosition(nextRow,captureColumn);
                ChessPiece targetPiece = board.getPiece(capturePosition);

                if (targetPiece != null && targetPiece.getTeamColor() != myTeam) {
                    addMove(moves, from, capturePosition, nextRow);
                }
            }
        }

        // en passant logic
        ChessPosition enPassantTgtSquare = board.getEnPassantTgtSquare();

        if (enPassantTgtSquare != null) {
            int tgtRow = enPassantTgtSquare.getRow();
            int tgtCol = enPassantTgtSquare.getColumn();

            // if WHITE, pawn should be on row 5
            // if BLACK, pawn should be on row 4
            int enPassantRow = (myTeam == ChessGame.TeamColor.WHITE) ? 5 : 4;

            if (from.getRow() == enPassantRow) {

                // check col adjacency, absolute needed because of how column is handled above
                if (Math.abs(from.getColumn() - tgtCol) == 1) {
                    ChessPosition endPos = new ChessPosition(nextRow, tgtCol);

                    // does the landing square match the en passant tgt square?
                    if (endPos.equals(enPassantTgtSquare)) {
                        moves.add(new ChessMove(from, endPos, null));
                    }
                }
            }
        }
        // end en passant logic

        return moves;
    }

    @Override
    public boolean onTheBoard(int row, int col) {
        return (row >= 1 && row <= 8 && col >= 1 && col <= 8);
    }

    // if back row -> promote
    private void addMove(Collection<ChessMove> moves, ChessPosition from, ChessPosition to, int nextRow) {
        if (nextRow == 8 || nextRow == 1) {
            moves.add(new ChessMove(from, to, ChessPiece.PieceType.QUEEN));
            moves.add(new ChessMove(from, to, ChessPiece.PieceType.ROOK));
            moves.add(new ChessMove(from, to, ChessPiece.PieceType.BISHOP));
            moves.add(new ChessMove(from, to, ChessPiece.PieceType.KNIGHT));
        } else {
            moves.add(new ChessMove(from, to, null));
        }
    }
}
