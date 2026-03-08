import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Scorechecker {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("usage: java -jar Scorechecker.jar <dictionary-file>");
            return;
        }

        String dictionaryPath = args[0];
        // TODO: Load dictionary from dictionaryPath.

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        BoardParser parser = new BoardParser();

        while (true) {
            Board original = parser.readBoard(in);
            if (original == null) {
                break;
            }

            Board result = parser.readBoard(in);
            if (result == null) {
                break;
            }

            printBoard("original board:", original);
            printBoard("result board:", result);
        }
    }

    private static void printBoard(String label, Board board) {
        System.out.println(label);
        for (int r = 0; r < board.size(); r++) {
            System.out.println(board.rowString(r));
        }
    }
}
