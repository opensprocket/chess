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
        while (run) {
            String line = scanner.nextLine();

            try {
                if (line.equalsIgnoreCase("quit")) {
                    System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Goodbye!");
                    run = false;
                    continue;
                }
            } catch (Throwable e) {
                String msg = e.toString();
                System.out.print(msg);
            }


            System.out.println(EscapeSequences.SET_TEXT_COLOR_BLUE + "You typed: " + line);
        }
    }
}
