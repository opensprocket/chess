package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    public TeamColor currentTurn = TeamColor.WHITE; // start on White turn by default
    private ChessBoard board = new ChessBoard();

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return currentTurn == chessGame.currentTurn && Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentTurn, board);
    }

    public ChessGame() {
        board.resetBoard(); // initialize to blank state
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return currentTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        currentTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        Collection<ChessMove> validMoves = new ArrayList<>();

        ChessPiece piece = board.getPiece(startPosition);

        // no piece at position
        if (piece == null) {
            return validMoves;
        }

        // get potential moves
        Collection<ChessMove> potentialMoves = piece.pieceMoves(board, startPosition);

        // working copy of the board
        ChessBoard tempBoard = getBoard();

        // make move



        return validMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        // part 1
        // find king position
        ChessPosition kingPos = null;

        for (int row = 1; row <=8 ; row++) {
            for (int col = 1; col <= 8 ; col++) {
                // current cell
                ChessPosition tempPos = new ChessPosition(row,col);
                ChessPiece tempPiece = board.getPiece(tempPos);

                // check null, team and type
                if (tempPiece != null
                        && tempPiece.getTeamColor() == teamColor
                        && tempPiece.getPieceType() == ChessPiece.PieceType.KING ) {
                    kingPos = tempPos;
                    break; // terminate search
                }
            }
        }

        // if no king found, return false
        // you should never get to this point
        // anarchy chess mode?
        if (kingPos == null) {
            return false;
        }

        // part 2
        // find enemy pieces

        ChessGame.TeamColor enemyColor = (teamColor == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <=8 ; col++) {
                ChessPosition tempPos = new ChessPosition(row,col);
                ChessPiece tempPiece = board.getPiece(tempPos);

                // if enemy piece, get moves
                if (tempPiece != null && tempPiece.getTeamColor() == enemyColor) {
                    Collection<ChessMove> enemyMoves = tempPiece.pieceMoves(board, tempPos);

                    // check all possible moves against the king's location
                    for (ChessMove move : enemyMoves) {
                        if (move.getEndPosition().equals(kingPos)) {
                            return true; // king is being attacked
                        }
                    }
                }
            }
        }
        // search space exhausted, no piece can capture king so return false
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }
}
