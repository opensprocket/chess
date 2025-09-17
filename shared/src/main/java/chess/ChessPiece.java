package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor color;
    private final PieceType type;


    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.color = pieceColor;
        this.type = type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return  false;
        }

        ChessPiece that = (ChessPiece) o;
        return color == that.color && type == that.type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return color;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {

        MoveCalculator moves = switch (type) {
            case BISHOP -> new BishopMoveGenerator();
            case QUEEN -> new QueenMoveGenerator();
            case ROOK -> new RookMoveGenerator();
            case KING -> new KingMoveGenerator();
            case KNIGHT -> new KnightMoveGenerator();
//            case PAWN -> new PawnMoveGenerator();
            default -> throw new UnsupportedOperationException("MoveCalculator not implemented for " + type);
        };

        return moves.possibleMoves(board, myPosition);
    }

    /**
     * Checks if a position is on the board or not
     * @param row position to check
     * @param col position to check
     * @return bool indicating whether position is in bounds
     */
    private boolean onTheBoard(int row, int col) {
        return (row >= 1 && row <= 8 && col >= 1 && col <= 8);
    }
}
