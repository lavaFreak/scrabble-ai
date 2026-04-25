/**
 * Author: Garion
 *
 * File purpose: regression coverage for the Part 3 game backend.
 */
import java.nio.file.Files;
import java.nio.file.Path;
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
        testPlacementBufferBuildsPreviewBoard();
        testPlacementBufferRejectsFixedTileOverwrite();
        testScrabbleGameHumanExchangeUsesBagAndPassesTurn();
        testScrabbleGameComputerTurnUsesSolverMove();
        testScrabbleGameAppliesEndgameLeaveScoring();
        testScrabbleGameTracksTurnRecords();
        testScrabbleGameEndsWhenBagIsEmptyAndNeitherPlayerCanMove();
        testScrabbleGameEndsAfterSixConsecutiveScorelessTurns();
        testScrabbleGameSnapshotReflectsCurrentState();
        testScrabbleGameAcceptsPlacementBufferMoves();
        testScrabbleGameReportsWinnerSummary();
        testScrabbleGameUsesPreEndgameTieBreak();
        testGameLogWriterRecordsTurnAndWinnerSummary();

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

    private static void testPlacementBufferBuildsPreviewBoard() {
        Board board = emptyPlainBoard(5);
        PlacementBuffer buffer = new PlacementBuffer(board);

        buffer.placeLetter(2, 1, 'c');
        buffer.placeBlank(2, 2, 'a');

        Board preview = buffer.previewBoard();
        check(buffer.placementCount() == 2, "Expected two pending placements");
        check(preview.isTile(2, 1) && preview.tileAt(2, 1) == 'c', "Expected preview letter tile");
        check(preview.isTile(2, 2) && preview.tileAt(2, 2) == 'a', "Expected preview blank letter");
        check(preview.isBlankTile(2, 2), "Expected preview blank tile marker to be preserved");
    }

    private static void testPlacementBufferRejectsFixedTileOverwrite() {
        PlacementBuffer buffer = new PlacementBuffer(boardFromRows(new String[] {
            ".. .. ..",
            ".. c ..",
            ".. .. .."
        }));

        boolean threw = false;
        try {
            buffer.placeLetter(1, 1, 'a');
        } catch (IllegalArgumentException ex) {
            threw = ex.getMessage().contains("existing board tile");
        }
        check(threw, "Expected placement buffer to reject overwriting fixed tiles");
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
            new TileBag(List.of('m', 'n', 'o', 'p'), new Random(3L)),
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

    private static void testScrabbleGameTracksTurnRecords() throws Exception {
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
            new TileBag(List.of('m', 'n'), new Random(5L)),
            new ScrabblePlayer("Human", true, RackState.fromTray("abc")),
            new ScrabblePlayer("Computer", false, RackState.fromTray("atzzzzz")),
            true
        );

        game.exchangeHumanTiles("ac");
        check(game.turnCount() == 1, "Expected one recorded turn after exchange");
        check(game.latestTurn().actor() == TurnActor.HUMAN, "Expected human exchange turn actor");
        check(game.latestTurn().type() == TurnType.EXCHANGE, "Expected exchange turn type");
        check(game.latestTurn().number() == 1, "Expected first turn number to be 1");

        game.playComputerTurn();
        check(game.turnCount() == 2, "Expected second recorded turn after computer move");
        check(game.latestTurn().actor() == TurnActor.COMPUTER, "Expected computer move turn actor");
        check(game.latestTurn().type() == TurnType.MOVE, "Expected move turn type");
        check(game.latestTurn().scoreDelta() == 5, "Expected computer move score delta");
        check(game.latestTurn().play() != null, "Expected move turn to retain resolved play");
    }

    private static void testScrabbleGameEndsWhenBagIsEmptyAndNeitherPlayerCanMove() throws Exception {
        Dictionary dictionary = dictionaryOf("cat");
        ScrabbleGame game = new ScrabbleGame(
            dictionary,
            emptyPlainBoard(5),
            new TileBag(List.of(), new Random(6L)),
            new ScrabblePlayer("Human", true, RackState.fromTray("qq")),
            new ScrabblePlayer("Computer", false, RackState.fromTray("zz")),
            true
        );

        game.passHumanTurn();

        check(game.isGameOver(), "Expected empty-bag game to end when neither player can move");
        check(game.latestTurn().type() == TurnType.PASS, "Expected the recorded turn to be a pass");
        check(game.lastStatusMessage().endsWith("Game over."), "Expected game-over summary text");
        check(game.humanPlayer().score() == -20, "Expected human leave penalty to be applied");
        check(game.computerPlayer().score() == -20, "Expected computer leave penalty to be applied");
    }

    private static void testScrabbleGameEndsAfterSixConsecutiveScorelessTurns() throws Exception {
        Dictionary dictionary = dictionaryOf("cat");
        ScrabbleGame game = new ScrabbleGame(
            dictionary,
            emptyPlainBoard(5),
            new TileBag(List.of('q', 'z', 'x', 'j', 'q', 'z'), new Random(12L)),
            new ScrabblePlayer("Human", true, RackState.fromTray("qq")),
            new ScrabblePlayer("Computer", false, RackState.fromTray("zz")),
            true
        );

        for (int cycle = 0; cycle < 3; cycle++) {
            game.exchangeHumanTiles(game.humanPlayer().rack().trayString());
            game.playComputerTurn();
        }

        check(game.isGameOver(), "Expected six scoreless turns to end the game");
        check(game.turnCount() == 6, "Expected exactly six turns before termination");
        check(game.latestTurn().type() == TurnType.EXCHANGE, "Expected final scoreless turn to be recorded");
        check(game.lastStatusMessage().endsWith("Game over."), "Expected scoreless-turn game-over summary text");
        check(game.tileBagRemaining() == 6, "Expected exchanges to leave the bag size unchanged");
        check(game.humanPlayer().score() == -20, "Expected human leave penalty after scoreless-turn ending");
        check(game.computerPlayer().score() == -20, "Expected computer leave penalty after scoreless-turn ending");
    }

    private static void testScrabbleGameSnapshotReflectsCurrentState() throws Exception {
        Dictionary dictionary = dictionaryOf("cat");
        ScrabbleGame game = new ScrabbleGame(
            dictionary,
            emptyPlainBoard(5),
            new TileBag(List.of('x', 'y', 'z'), new Random(7L)),
            new ScrabblePlayer("Human", true, RackState.fromTray("abc")),
            new ScrabblePlayer("Computer", false, RackState.fromTray("def")),
            true
        );

        game.exchangeHumanTiles("ac");
        GameSnapshot snapshot = game.snapshot();

        check(snapshot.board().size() == 5, "Expected snapshot board to match game board");
        check("bxy".equals(snapshot.humanRack()), "Expected snapshot to expose updated human rack");
        check("def".equals(snapshot.computerRack()), "Expected snapshot to expose computer rack");
        check(!snapshot.isHumanTurn(), "Expected snapshot turn flag to reflect the exchange");
        check(snapshot.latestTurn() != null && snapshot.latestTurn().type() == TurnType.EXCHANGE,
            "Expected snapshot to expose the latest turn record");
    }

    private static void testScrabbleGameAcceptsPlacementBufferMoves() throws Exception {
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
            new TileBag(List.of(), new Random(8L)),
            new ScrabblePlayer("Human", true, RackState.fromTray("at")),
            new ScrabblePlayer("Computer", false, RackState.fromTray("z")),
            true
        );
        PlacementBuffer buffer = new PlacementBuffer(game.board());
        buffer.placeLetter(2, 2, 'a');
        buffer.placeLetter(2, 3, 't');

        ResolvedPlay play = game.playHumanMove(buffer);

        check("cat".equals(play.move().mainWord()), "Expected placement-buffer move to resolve to cat");
        check(game.board().isTile(2, 3) && game.board().tileAt(2, 3) == 't',
            "Expected game board to reflect buffered move");
    }

    private static void testScrabbleGameReportsWinnerSummary() throws Exception {
        Dictionary dictionary = dictionaryOf("cat");
        ScrabbleGame game = new ScrabbleGame(
            dictionary,
            emptyPlainBoard(5),
            new TileBag(List.of(), new Random(9L)),
            new ScrabblePlayer("Human", true, RackState.fromTray("qq")),
            new ScrabblePlayer("Computer", false, RackState.fromTray("zz")),
            true
        );

        game.passHumanTurn();

        check("The game is tied at -20 points each.".equals(game.winnerSummary()),
            "Expected tie summary when final and pre-endgame scores match");
        check("The game is tied at -20 points each.".equals(game.snapshot().winnerSummary()),
            "Expected snapshot to expose winner summary");
    }

    private static void testScrabbleGameUsesPreEndgameTieBreak() throws Exception {
        Dictionary dictionary = dictionaryOf("cat");
        ScrabblePlayer human = new ScrabblePlayer("Human", true, RackState.fromTray("z"));
        ScrabblePlayer computer = new ScrabblePlayer("Computer", false, RackState.fromTray("k"));
        human.addScore(30);
        computer.addScore(25);
        ScrabbleGame game = new ScrabbleGame(
            dictionary,
            emptyPlainBoard(5),
            new TileBag(List.of(), new Random(10L)),
            human,
            computer,
            true
        );

        game.passHumanTurn();

        check("Human wins the tie-break 20 to 20.".equals(game.winnerSummary()),
            "Expected pre-endgame score tie-break to choose the human player");
    }

    private static void testGameLogWriterRecordsTurnAndWinnerSummary() throws Exception {
        Dictionary dictionary = dictionaryOf("cat");
        ScrabbleGame game = new ScrabbleGame(
            dictionary,
            emptyPlainBoard(5),
            new TileBag(List.of(), new Random(11L)),
            new ScrabblePlayer("Human", true, RackState.fromTray("qq")),
            new ScrabblePlayer("Computer", false, RackState.fromTray("zz")),
            true
        );

        Path logPath = Files.createTempFile("scrabble-game-log", ".log");
        try (GameLogWriter writer = new GameLogWriter(logPath, "test-session", "test-dictionary.txt")) {
            writer.sync(game.snapshot());
            game.passHumanTurn();
            writer.sync(game.snapshot());
        }

        String logText = Files.readString(logPath);
        check(logText.contains("SCRABBLE SESSION LOG"), "Expected log header");
        check(logText.contains("== Initial State =="), "Expected initial state block");
        check(logText.contains("== Turn 1 =="), "Expected turn block");
        check(logText.contains("summary: Human passed. Game over."), "Expected pass summary in log");
        check(logText.contains("== Game Over =="), "Expected game-over block");
        check(logText.contains("winner: The game is tied at -20 points each."),
            "Expected final winner summary in log");
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
