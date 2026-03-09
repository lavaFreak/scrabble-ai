/**
 * Author: Garion
 *
 * File purpose: scorer program entry point and output orchestration.
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class Scorechecker {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("usage: java -jar Scorechecker.jar <dictionary-file>");
            return;
        }

        Dictionary dictionary = Dictionary.fromFile(args[0]);
        BoardParser parser = new BoardParser();
        CompatibilityChecker compatibilityChecker = new CompatibilityChecker();
        MoveExtractor moveExtractor = new MoveExtractor();
        WordFinder wordFinder = new WordFinder();
        LegalityChecker legalityChecker = new LegalityChecker(dictionary);
        Scorer scorer = new Scorer();

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        boolean firstCase = true;

        while (true) {
            Board original = parser.readBoard(in);
            if (original == null) {
                break;
            }

            Board result = parser.readBoard(in);
            if (result == null) {
                break;
            }

            if (!firstCase) {
                System.out.println();
            }
            firstCase = false;

            printBoard("original board:", original);
            printBoard("result board:", result);

            String incompatibility = compatibilityChecker.findIncompatibility(original, result);
            if (incompatibility != null) {
                System.out.println("Incompatible boards: " + incompatibility);
                continue;
            }

            List<PlayedTile> played = moveExtractor.collectNewTiles(original, result);
            System.out.println(moveExtractor.formatPlay(played));

            List<WordPlacement> words = wordFinder.collectFormedWordPlacements(result, played);
            boolean legal = legalityChecker.isLegalMove(original, result, played, words);
            System.out.println(legal ? "play is legal" : "play is not legal");
            if (legal) {
                int score = scorer.computeScore(original, result, played, words);
                System.out.println("score is " + score);
            }
        }
    }

    private static void printBoard(String label, Board board) {
        System.out.println(label);
        for (int r = 0; r < board.size(); r++) {
            System.out.println(board.rowStringPadded(r));
        }
    }
}
