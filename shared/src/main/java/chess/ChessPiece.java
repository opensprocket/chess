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

    private final ChessGame.TeamColor pieceColor;
    private final ChessPiece.PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
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
        return pieceColor;
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

        ChessPiece piece = board.getPiece(myPosition);

        Collection<ChessMove> moves = switch (piece.getPieceType()) {
            case KING -> addKingMoves(board, myPosition);
            case QUEEN -> addQueenMoves(board, myPosition);
            case BISHOP -> addBishopMoves(board, myPosition);
            case KNIGHT -> addKnightMoves(board, myPosition);
            case ROOK -> addRookMoves(board, myPosition);
            case PAWN -> addPawnMoves(board, myPosition);
            default -> throw new IllegalStateException("Unknown state or type: " + piece);
        };

        switch (piece.getPieceType()) {
            case KING:
                moves = addKingMoves(board, myPosition);
                break;
        return moves;
    }

    private Collection<ChessMove> addKingMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> validMoves = new ArrayList<>();



        return validMoves;
    }


    private Collection<ChessMove> addQueenMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> validMoves = new ArrayList<>();

        return moves;
    }



    }
}
