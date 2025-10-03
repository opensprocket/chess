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
        // create bucket for returned data
        Collection<ChessMove> validMoves = new ArrayList<>();

        // get the piece at the position
        ChessPiece piece = board.getPiece(startPosition);

        // no piece at position
        if (piece == null) {
            return validMoves;
        }

        // who owns the piece at start position?
        ChessGame.TeamColor playerColor = piece.getTeamColor();

        // get potential moves
        Collection<ChessMove> potentialMoves = piece.pieceMoves(board, startPosition);

        for (ChessMove move : potentialMoves) {
            if (!moveMakesKingVulnerable(move, playerColor)) {
                validMoves.add(move);
            }
        }
        // return valid choices
        return validMoves;
    }

    /**
     *
     * @param move ChessMove to compare
     * @param playerColor TeamColor who is making the move
     * @return boolean value of vulnerability
     */
    private boolean moveMakesKingVulnerable(ChessMove move, TeamColor playerColor) {
        // copy the board

        ChessBoard tempBoard = new ChessBoard();
        ChessBoard originalBoard = this.board;

        // clone board
        for (int row = 1; row <= 8 ; row++) {
            for (int col = 1; col <= 8 ; col++) {
                ChessPosition pos = new ChessPosition(row,col);
                ChessPiece piece = originalBoard.getPiece(pos);

                if (piece != null) {
                    tempBoard.addPiece(pos, piece);
                }
            }

        }

        try {
            tempBoard.movePiece(move);
        } catch (InvalidMoveException e) {
            return true;
        }

        // swap boards before running check
        setBoard(tempBoard);

        boolean isVulnerable = isInCheck(playerColor);

        // restore board state
        setBoard(originalBoard);

        return isVulnerable;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition startPos = move.getStartPosition();
        ChessPiece piece = board.getPiece(startPos);

        // starting position
        if (piece == null) {
            throw new InvalidMoveException("No piece at starting position.");
        }

        // wrong turn
        if (piece.getTeamColor() != currentTurn) {
            throw new InvalidMoveException("Cannot move piece, it is " + currentTurn + " 's turn.");
        }

        Collection<ChessMove> allowedMoves = validMoves(startPos);

        // is proposed move allowed?
        if (!allowedMoves.contains(move)) {
            // check promotion pieces
            boolean found = false;

            // compare move start and end
            for (ChessMove allowedMove : allowedMoves) {
                if (allowedMove.getEndPosition() == move.getEndPosition() &&
                        allowedMove.getStartPosition() == move.getEndPosition()) {
                    found = true; // end search
                    break;
                }
            }
            if (!found) {
                throw new InvalidMoveException("Move is not allowed or leaves King in check.");
            }
        }

        // move piece
        board.movePiece(move);

        // en passant state tracking
        ChessPosition pawnStart = move.getStartPosition();
        ChessPosition pawnEnd = move.getEndPosition();
        ChessPiece movedPiece = board.getPiece(pawnEnd);

        // clear target square
        board.setEnPassantTgtSquare(null);

        if (movedPiece != null && movedPiece.getPieceType() == ChessPiece.PieceType.PAWN) {
            int startRow = pawnStart.getRow();
            int endRow = pawnEnd.getRow();

            // check for two square move
            if (Math.abs(startRow - endRow) == 2) {
                // en passant square has been passed
                int tgtRow = (startRow + endRow) / 2;
                ChessPosition target = new ChessPosition(tgtRow, pawnEnd.getColumn());
                board.setEnPassantTgtSquare(target);
            }
        }

        // change turn
        currentTurn = (currentTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;

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
                        // compare objects using .equals() instead of ==
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
        if (!isInCheck(teamColor)) {
            return false; // cannot be checkmate if not in check
        }
        // if the collection is empty, then no valid moves remaining
        return allValidMoves(teamColor).isEmpty();
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false; // cannot be in check and stalemated
        }
        // if not in check, but no valid moves exist to end game state, then game state is stalemate
        return allValidMoves(teamColor).isEmpty();
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    /**
     * Helper function that returns all possible moves for a team
     * @param teamColor Team to check moves for
     * @return Collection<ChessMove> allValidMoves for team
     */
    private Collection<ChessMove> allValidMoves(TeamColor teamColor) {
        Collection<ChessMove> allValidMoves = new ArrayList<>();

        // iterate through board
        for (int row = 1; row <= 8 ; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition tempPos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(tempPos);

                // if not empty or other team, calculate valid moves
                if (piece != null && piece.getTeamColor() == teamColor) {
                    allValidMoves.addAll(validMoves(tempPos));
                }
            }
        }
        // return all possible moves for team
        return allValidMoves;
    }
}
