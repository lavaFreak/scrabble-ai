/**
 * Author: Garion
 *
 * File purpose: regression coverage for the Part 3 game backend.
 */
import java.util.List;
import java.util.Random;

public class Part3GameTests {
    /**
     * Executes Part 3 backend tests.
     *
     * @param args unused
     * @throws Exception if setup or assertions fail
     */
    public static void main(String[] args) throws Exception {
        testTileBagStandardCount();
        testRackStateTracksLettersAndPlayedBlanks();
        testMoveResolverValidatesAndScoresHumanMove();
        testScrabbleGameHumanExchangeUsesBagAndPassesTurn();
        testScrabbleGameComputerTurnUsesSolverMove();
        testScrabbleGameAppliesEndgameLeaveScoring();

        System.out.println("All tests passed.");
    }

    private static void testTileBagStandardCount() {
        TileBag bag = TileBag.standard(new Random(1L));
        check(bag.remaining() == 100, "Expected standard bag to start with 100 tiles");
        check(bag.draw(7).size() == 7, "Expected standard bag draw to return requested tile count");
        check(bag.remaining() == 93, "Expected bag count to drop after drawing tiles");
    }

    private static void testRackStateTracksLettersAndPlayedBlanks() {
        RackState rack = RackState.fromTray("ab*de");
        List<PlayedTile> played = List.of(
            new PlayedTile('a', 0, 0),
            new PlayedTile('c', 0, 1, true)
        );

        check(rack.canSupply(played), "Expected rack to cover explicit letter and blank plays");
        rack.removePlayedTiles(played);
        check("bde".equals(rack.trayString()), "Expected rack removal to preserve remaining tile order");
    }

    private static void testMoveResolverValidatesAndScoresHumanMove() throws Exception {
        Dictionary dictionary = dictionaryOf("cat");
        MoveResolver resolver = new MoveResolver(dictionary);
        RackState rack = RackState.fromTray("at");

        Board original = boardFromRows(new String[] {
            ".. .. .. .. ..",
            ".. .. .. .. ..",
            ".. c .. .. ..",
            ".. .. .. .. ..",
            ".. .. .. .. .."
        });
        Board proposed = boardFromRows(new String[] {
            ".. .. .. .. ..",
            ".. .. .. .. ..",
            ".. c a t ..",
            ".. .. .. .. ..",
            ".. .. .. .. .."
        });

        ResolvedPlay play = resolver.resolve(original, proposed, rack);
        check("cat".equals(play.move().mainWord()), "Expected move resolver main word to match the board");
        check(play.score() == 5, "Expected cat extension to score 5");
        check(play.playedTiles().size() == 2, "Expected two newly played tiles");
    }

    private static void testScrabbleGameHumanExchangeUsesBagAndPassesTurn() throws Exception {
        Dictionary dictionary = dictionaryOf("cat");
        ScrabbleGame game = new ScrabbleGame(
            dictionary,
            emptyPlainBoard(5),
            new TileBag(List.of('x', 'y', 'z'), new Random(2L)),
            new ScrabblePlayer("Human", true, RackState.fromTray("abc")),
            new ScrabblePlayer("Computer", false, RackState.fromTray("def")),
            true
        );

        game.exchangeHumanTiles("ac");

        check(!game.isHumanTurn(), "Expected exchange to pass the turn to the computer");
        check(game.tileBagRemaining() == 3, "Expected bag count to stay unchanged after exchange");
        check(game.humanPlayer().rack().size() == 3, "Expected rack size to remain unchanged after exchange");
        check(game.humanPlayer().rack().trayString().indexOf('a') < 0,
            "Expected exchanged tile a to be absent from the rack");
        check(game.humanPlayer().rack().trayString().indexOf('c') < 0,
            "Expected exchanged tile c to be absent from the rack");
    }

    private static void testScrabbleGameComputerTurnUsesSolverMove() throws Exception {
        Dictionary dictionary = dictionaryOf("cat");
        Board board = boardFromRows(new String[] {
            ".. .. .. .. ..",
            ".. .. .. .. ..",
            ".. c .. .. ..",
            ".. .. .. .. ..",
            ".. .. .. .. .."
        });
        ScrabbleGame game = new ScrabbleGame(
            dictionary,
            board,
            new TileBag(List.of('m', 'n'), new Random(3L)),
            new ScrabblePlayer("Human", true, RackState.fromTray("qqqqqqq")),
            new ScrabblePlayer("Computer", false, RackState.fromTray("atzzzzz")),
            false
        );

        ResolvedPlay play = game.playComputerTurn();

        check(play != null, "Expected computer to find and play a legal move");
        check("cat".equals(play.move().mainWord()), "Expected solver to extend c into cat");
        check(game.board().isTile(2, 2) && game.board().tileAt(2, 2) == 'a',
            "Expected computer move to update the board");
        check(game.computerPlayer().score() == 5, "Expected computer move to score 5 points");
        check(game.isHumanTurn(), "Expected play to pass control back to the human");
        check(game.computerPlayer().rack().size() == 7, "Expected computer rack to refill after the move");
    }

    private static void testScrabbleGameAppliesEndgameLeaveScoring() throws Exception {
        Dictionary dictionary = dictionaryOf("cat");
        Board board = boardFromRows(new String[] {
            ".. .. .. .. ..",
            ".. .. .. .. ..",
            ".. c .. .. ..",
            ".. .. .. .. ..",
            ".. .. .. .. .."
        });
        ScrabbleGame game = new ScrabbleGame(
            dictionary,
            board,
            new TileBag(List.of(), new Random(4L)),
            new ScrabblePlayer("Human", true, RackState.fromTray("at")),
            new ScrabblePlayer("Computer", false, RackState.fromTray("z")),
            true
        );

        Board proposed = boardFromRows(new String[] {
            ".. .. .. .. ..",
            ".. .. .. .. ..",
            ".. c a t ..",
            ".. .. .. .. ..",
            ".. .. .. .. .."
        });
        ResolvedPlay play = game.playHumanMove(proposed);

        check(play.score() == 5, "Expected move itself to score 5");
        check(game.isGameOver(), "Expected empty bag plus empty rack to trigger endgame");
        check(game.humanPlayer().score() == 15, "Expected human to gain the opponent leave bonus");
        check(game.computerPlayer().score() == -10, "Expected computer to lose its leave value");
    }

    private static Dictionary dictionaryOf(String... words) throws Exception {
        java.nio.file.Path path = java.nio.file.Files.createTempFile("scrabble-part3-dict", ".txt");
        java.nio.file.Files.write(path, List.of(words));
        return Dictionary.fromFile(path.toString());
    }

    private static Board emptyPlainBoard(int size) {
        String[] rows = new String[size];
        for (int row = 0; row < size; row++) {
            StringBuilder sb = new StringBuilder();
            for (int col = 0; col < size; col++) {
                if (col > 0) {
                    sb.append(' ');
                }
                sb.append("..");
            }
            rows[row] = sb.toString();
        }
        return boardFromRows(rows);
    }

    private static Board boardFromRows(String[] rows) {
        int size = rows.length;
        String[][] tokens = new String[size][size];
        for (int row = 0; row < size; row++) {
            String[] parts = rows[row].trim().split("\\s+");
            if (parts.length != size) {
                throw new IllegalArgumentException("row " + row + " expected " + size + " tokens");
            }
            System.arraycopy(parts, 0, tokens[row], 0, size);
        }
        return new Board(size, tokens);
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
