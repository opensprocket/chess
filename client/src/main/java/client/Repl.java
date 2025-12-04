package client;

import java.util.Scanner;
import ui.EscapeSequences;

public class Repl {
    private final ChessClient client;

    public Repl(String serverUrl) {
        Scanner scanner = new Scanner(System.in);
        client = new ChessClient(serverUrl, scanner);
    }

    public void run() {
        System.out.println(EscapeSequences.WHITE_KING + " Welcome to Chess! " +
                EscapeSequences.BLACK_KING);
        System.out.println("Type 'help' to get started.");

        Scanner scanner = new Scanner(System.in);
        var result = "";
        boolean run = true;

        System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Chess Client Online");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Welcome to Chess 240, type Help to get started!");
        while (run) {
            printPrompt();
            String line = scanner.nextLine();

            try {
                if (line.equalsIgnoreCase("quit")) {
                    System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Goodbye!");
                    run = false;
                    continue;
                }

                result = client.eval(line);
                System.out.print(EscapeSequences.SET_TEXT_COLOR_BLUE + result);

            } catch (Throwable e) {
                String msg = e.getMessage();
                System.out.print(msg);
            }
        }
        System.out.println("Goodbye!");
    }

    private void printPrompt() {
        System.out.print("\n" + EscapeSequences.RESET_TEXT_COLOR + "[" + client.getState() + "] " + ">>> " + EscapeSequences.SET_TEXT_COLOR_GREEN);
    }
}