/**
 * Author: Garion
 *
 * File purpose: local utility for validating board parsing behavior.
 */
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ParseBoardCheck {

    /**
     * Runs parse utility against a board-pair input file.
     *
     * @param args expected single argument: input file path
     * @throws IOException if file read fails
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("usage: java ParseBoardCheck <input-file>");
            return;
        }

        BoardParser parser = new BoardParser();

        try (BufferedReader in = new BufferedReader(new FileReader(args[0]))) {
            Board original = parser.readBoard(in);
            Board result = parser.readBoard(in);

            if (original == null || result == null) {
                System.err.println("Could not read two boards from input file.");
                return;
            }

            printBoard("original board", original);
            printBoard("result board", result);
        }
    }

    // Prints one board with label for manual parser verification.
    private static void printBoard(String label, Board board) {
        System.out.println(label + ":");
        for (int r = 0; r < board.size(); r++) {
            System.out.println(board.rowString(r));
        }
    }
}
