package client;
import ui.EscapeSequences;
import java.util.Scanner;

public class Repl {
    private final ChessClient client;

    public Repl(String serverUrl) {
        client = new ChessClient(serverUrl);
    }

    public void run() {
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
            System.out.println();

//            System.out.println(EscapeSequences.SET_TEXT_COLOR_BLUE + "You typed: " + line);
        }
    }

    private void printPrompt() {
        System.out.print(EscapeSequences.RESET_TEXT_COLOR + "[ " + client.getState() + " ] >>> " + EscapeSequences.SET_TEXT_COLOR_GREEN);
    }
}
