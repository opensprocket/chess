package client;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import ui.EscapeSequences;
import ui.EscapeSequences.*;

import java.io.PrintStream;

public class DisplayGameboard {
    private static final String BOARD_COLOR_LIGHT = EscapeSequences.SET_BG_COLOR_WHITE;
    private static final String BOARD_COLOR_DARK = EscapeSequences.SET_BG_COLOR_DARK_GREY;
    private static final String HEADER_BG_COLOR = EscapeSequences.SET_BG_COLOR_LIGHT_GREY;
    private static final String HEADER_TEXT_COLOR = EscapeSequences.SET_TEXT_COLOR_BLACK;
    private static final String PIECE_COLOR_WHITE = EscapeSequences.SET_TEXT_COLOR_RED;
    private static final String PIECE_COLOR_BLACK = EscapeSequences.SET_TEXT_COLOR_BLUE;

    public static void drawBoard(ChessBoard board, ChessGame.TeamColor render_as) {
        PrintStream out = new PrintStream(System.out, true);
        out.print(EscapeSequences.ERASE_SCREEN);

        if (render_as == ChessGame.TeamColor.BLACK) {
            drawBlackPerspective(out, board);
        } else {
            drawWhitePerspective(out, board);
        }

        out.print(EscapeSequences.RESET_ALL);
    }

    private static void drawWhitePerspective(PrintStream out, ChessBoard board) {
        String[] headers = {"a", "b", "c", "d", "e", "f", "g", "h"};
        drawHeader(out, headers);

        for (int row = 8; row >= 1; row--) {
            drawRow(out, board, row, headers, true);
        }

        drawHeader(out, headers);
    }

    private static void drawBlackPerspective(PrintStream out, ChessBoard board) {
        String[] headers = {"h", "g", "f", "e", "d", "c", "b", "a"};
        drawHeader(out, headers);

        for (int row = 1; row <= 8; row++) {
            drawRow(out, board, row, headers, false);
        }

        drawHeader(out, headers);
    }

    private static void drawHeader(PrintStream out, String[] headers) {
        out.print(HEADER_BG_COLOR);
        out.print(HEADER_TEXT_COLOR);

        out.print(EscapeSequences.EMPTY);
        for (String header : headers) {
            out.print(" " + header + " ");
        }

        out.print(EscapeSequences.EMPTY);
        out.println(EscapeSequences.RESET_ALL);
    }

    private static void drawRow(PrintStream out, ChessBoard board, int row, String[] colHeaders, boolean printAsWhitePerspective) {
        drawRowNumber(out, row);

        for (int col = 1; col <= 8 ; col++) {
            int currentPieceCol = printAsWhitePerspective ? col : (9 - col);

            boolean isLightSquare = (row + currentPieceCol) % 2 != 0;
            String bgColor = isLightSquare ? BOARD_COLOR_LIGHT : BOARD_COLOR_DARK;

            ChessPiece piece = board.getPiece(new ChessPosition(row, currentPieceCol));
            printSquare(out, piece, bgColor);
        }

        drawRowNumber(out, row);
        out.println(EscapeSequences.RESET_ALL);
    }

    private static void drawRowNumber(PrintStream out, int row) {
        out.print(HEADER_BG_COLOR);
        out.print(HEADER_TEXT_COLOR);
        out.print(" " + row + " ");
        out.print(EscapeSequences.RESET_ALL);
    }

    private static void printSquare(PrintStream out, ChessPiece piece, String bgColor) {
        out.print(bgColor);
        if (piece == null) {
            out.print(EscapeSequences.EMPTY);
        } else {
            String pieceColor = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? PIECE_COLOR_WHITE : PIECE_COLOR_BLACK;
            out.print(pieceColor);
            out.print(getPieceSymbol(piece));
        }
    }

    private static String getPieceSymbol(ChessPiece piece) {
        return switch (piece.getPieceType()) {
            case KING -> (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? EscapeSequences.WHITE_KING : EscapeSequences.BLACK_KING;
            case QUEEN -> (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? EscapeSequences.WHITE_QUEEN : EscapeSequences.BLACK_QUEEN;
            case BISHOP -> (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? EscapeSequences.WHITE_BISHOP : EscapeSequences.BLACK_BISHOP;
            case KNIGHT -> (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? EscapeSequences.WHITE_KNIGHT : EscapeSequences.BLACK_KNIGHT;
            case ROOK -> (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? EscapeSequences.WHITE_ROOK : EscapeSequences.BLACK_ROOK;
            case PAWN -> (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? EscapeSequences.WHITE_PAWN : EscapeSequences.BLACK_PAWN;
        };
    }

    public static void testBoard() {
        ChessBoard board = new ChessBoard();
        board.resetBoard();;

        System.out.println("--- Board Test for WHITE ---");
        drawBoard(board, ChessGame.TeamColor.WHITE);

        System.out.println("--- Board Test for BLACK ---");
        drawBoard(board, ChessGame.TeamColor.BLACK);
    }
}
