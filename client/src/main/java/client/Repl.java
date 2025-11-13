package client;
import ui.EscapeSequences;
import java.util.Scanner;

public class Repl {


    public Repl() {
//        ChessClient client = new ChessClient(serverUrl);
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);
        var result = "";

        boolean run = true;
        while (run) {
            String line = scanner.nextLine();
            System.out.println(line);

            if (line.toLowerCase().equals("quit")) {
                run = false;
            }
        }
    }
}
