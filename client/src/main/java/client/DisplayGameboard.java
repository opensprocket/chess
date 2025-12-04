package client;

import chess.*;
import ui.EscapeSequences;

import java.io.PrintStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class DisplayGameboard {
    private static final String BOARD_COLOR_LIGHT = EscapeSequences.SET_BG_COLOR_WHITE;
    private static final String BOARD_COLOR_DARK = EscapeSequences.SET_BG_COLOR_DARK_GREY;
    private static final String HIGHLIGHT_LIGHT = EscapeSequences.SET_BG_COLOR_GREEN;
    private static final String HIGHLIGHT_DARK = EscapeSequences.SET_BG_COLOR_DARK_GREEN;
    private static final String HEADER_BG_COLOR = EscapeSequences.SET_BG_COLOR_LIGHT_GREY;
    private static final String HEADER_TEXT_COLOR = EscapeSequences.SET_TEXT_COLOR_BLACK;
    private static final String PIECE_COLOR_WHITE = EscapeSequences.SET_TEXT_COLOR_RED;
    private static final String PIECE_COLOR_BLACK = EscapeSequences.SET_TEXT_COLOR_BLUE;

    public static void drawBoard(ChessBoard board, ChessGame.TeamColor renderAs) {
        PrintStream out = new PrintStream(System.out, true);
        out.print(EscapeSequences.ERASE_SCREEN);

        if (renderAs == ChessGame.TeamColor.BLACK) {
            drawBlackPerspective(out, board, null);
        } else {
            drawWhitePerspective(out, board, null);
        }

        out.print(EscapeSequences.RESET_ALL);
    }

    public static void drawBoardWithHighlights(ChessBoard board, ChessGame.TeamColor renderAs,
                                               ChessPosition selectedPosition, Collection<ChessMove> validMoves) {
        PrintStream out = new PrintStream(System.out, true);
        out.print(EscapeSequences.ERASE_SCREEN);

        // Create set of positions to highlight
        Set<ChessPosition> highlightPositions = new HashSet<>();
        if (selectedPosition != null) {
            highlightPositions.add(selectedPosition);
        }
        if (validMoves != null) {
            for (ChessMove move : validMoves) {
                highlightPositions.add(move.getEndPosition());
            }
        }

        if (renderAs == ChessGame.TeamColor.BLACK) {
            drawBlackPerspective(out, board, highlightPositions);
        } else {
            drawWhitePerspective(out, board, highlightPositions);
        }

        out.print(EscapeSequences.RESET_ALL);
    }

    private static void drawWhitePerspective(PrintStream out, ChessBoard board, Set<ChessPosition> highlights) {
        String[] headers = {"a", "b", "c", "d", "e", "f", "g", "h"};
        drawHeader(out, headers);

        for (int row = 8; row >= 1; row--) {
            drawRow(out, board, row, true, highlights);
        }

        drawHeader(out, headers);
    }

    private static void drawBlackPerspective(PrintStream out, ChessBoard board, Set<ChessPosition> highlights) {
        String[] headers = {"h", "g", "f", "e", "d", "c", "b", "a"};
        drawHeader(out, headers);

        for (int row = 1; row <= 8; row++) {
            drawRow(out, board, row, false, highlights);
        }

        drawHeader(out, headers);
    }

    private static void drawHeader(PrintStream out, String[] headers) {
        out.print(HEADER_BG_COLOR);
        out.print(EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY + "   ");

        out.print(HEADER_TEXT_COLOR);
        for (String header : headers) {
            out.print(" " + header + " ");
        }

        out.print(EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY + "   ");
        out.println(EscapeSequences.RESET_ALL);
    }

    private static void drawRow(PrintStream out, ChessBoard board, int row,
                                boolean printAsWhitePerspective, Set<ChessPosition> highlights) {
        drawRowNumber(out, row);

        for (int col = 1; col <= 8; col++) {
            int currentPieceCol = printAsWhitePerspective ? col : (9 - col);
            ChessPosition currentPos = new ChessPosition(row, currentPieceCol);

            boolean isLightSquare = (row + currentPieceCol) % 2 != 0;
            boolean shouldHighlight = highlights != null && highlights.contains(currentPos);

            String bgColor;
            if (shouldHighlight) {
                bgColor = isLightSquare ? HIGHLIGHT_LIGHT : HIGHLIGHT_DARK;
            } else {
                bgColor = isLightSquare ? BOARD_COLOR_LIGHT : BOARD_COLOR_DARK;
            }

            ChessPiece piece = board.getPiece(currentPos);
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
            out.print("   ");
        } else {
            String pieceColor = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? PIECE_COLOR_WHITE : PIECE_COLOR_BLACK;
            out.print(pieceColor);
            out.print(getPieceSymbol(piece));
        }
    }

    private static String getPieceSymbol(ChessPiece piece) {
        return switch (piece.getPieceType()) {
            case KING -> EscapeSequences.SET_TEXT_BOLD + " K ";
            case QUEEN -> EscapeSequences.SET_TEXT_BOLD + " Q ";
            case BISHOP -> EscapeSequences.SET_TEXT_BOLD + " B ";
            case KNIGHT -> EscapeSequences.SET_TEXT_BOLD + " N ";
            case ROOK -> EscapeSequences.SET_TEXT_BOLD + " R ";
            case PAWN -> EscapeSequences.SET_TEXT_BOLD + " P ";
        };
    }

    public static void testBoard() {
        ChessBoard board = new ChessBoard();
        board.resetBoard();

        System.out.println("--- Board Test for WHITE ---");
        drawBoard(board, ChessGame.TeamColor.WHITE);

        System.out.println("\n--- Board Test for BLACK ---");
        drawBoard(board, ChessGame.TeamColor.BLACK);
    }
}