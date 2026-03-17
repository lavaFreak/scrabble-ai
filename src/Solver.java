/**
 * Author: Garion
 *
 * File purpose: solver program entry point and output orchestration.
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Solver {

    /**
     * Runs the solver command-line program.
     *
     * @param args expected single dictionary-file path
     * @throws IOException if dictionary or stdin reads fail
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("usage: java -jar Solver.jar <dictionary-file>");
            return;
        }

        Dictionary dictionary = Dictionary.fromFile(args[0]);
        SolverInputParser parser = new SolverInputParser();
        SolverEngine solverEngine = new SolverEngine(dictionary);

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            SolverCase solverCase = parser.readCase(in);
            if (solverCase == null) {
                break;
            }

            printBoard("Input Board:", solverCase.board());
            System.out.println("Tray: " + solverCase.tray());

            SolverResult solution = solverEngine.solve(solverCase.board(), solverCase.tray());
            if (solution == null) {
                System.out.println("No legal move found");
                System.out.println("Solution Board:");
                printBoardRows(solverCase.board());
                System.out.println();
                continue;
            }

            System.out.println("Solution " + solution.move().mainWord() + " has " + solution.move().score() + " points");
            printBoard("Solution Board:", solution.resultBoard());
            System.out.println();
        }
    }

    // Prints a labeled board using the solver's expected row formatting.
    private static void printBoard(String label, Board board) {
        System.out.println(label);
        printBoardRows(board);
    }

    // Prints only the board rows without a heading.
    private static void printBoardRows(Board board) {
        for (int r = 0; r < board.size(); r++) {
            System.out.println(board.rowStringPadded(r));
        }
    }
}
