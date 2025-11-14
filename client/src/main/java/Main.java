import chess.*;
import client.Repl;

public class Main {
    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);

        System.out.println("Starting CS240 Chess Client");
        int port = 8080;
        var serverUrl = "http://localhost:" + port;

        new Repl(serverUrl).run();
    }
}