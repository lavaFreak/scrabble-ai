/**
 * Author: Garion
 *
 * Shared Part 3 game controller for a human-vs-computer Scrabble match.
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;

public class ScrabbleGame {
    private static final int RACK_SIZE = 7;
    private static final Path STANDARD_BOARD_PATH = Path.of("Resources/config/scrabble_board.txt");

    private final SolverEngine solverEngine;
    private final MoveResolver moveResolver;
    private final WordFinder wordFinder = new WordFinder();
    private final TileBag tileBag;
    private final ScrabblePlayer humanPlayer;
    private final ScrabblePlayer computerPlayer;

    private Board board;
    private boolean humanTurn;
    private boolean gameOver;
    private boolean endgameAdjusted;
    private String lastStatusMessage;

    /**
     * Creates a new standard game using the standard Scrabble board and tile bag.
     *
     * The human player always goes first in this implementation.
     *
     * @param dictionary loaded game dictionary
     * @param random randomness source for the tile bag
     * @return initialized game
     * @throws IOException if the standard board file cannot be read
     */
    public static ScrabbleGame createStandardGame(Dictionary dictionary, Random random) throws IOException {
        if (dictionary == null) {
            throw new IllegalArgumentException("dictionary is required");
        }
        if (random == null) {
            throw new IllegalArgumentException("random is required");
        }

        BoardParser parser = new BoardParser();
        Board board;
        try (BufferedReader reader = Files.newBufferedReader(STANDARD_BOARD_PATH)) {
            board = parser.readBoard(reader);
        }

        TileBag bag = TileBag.standard(random);
        RackState humanRack = new RackState();
        RackState computerRack = new RackState();
        humanRack.fillToSize(bag, RACK_SIZE);
        computerRack.fillToSize(bag, RACK_SIZE);

        return new ScrabbleGame(
            dictionary,
            board,
            bag,
            new ScrabblePlayer("Human", true, humanRack),
            new ScrabblePlayer("Computer", false, computerRack),
            true
        );
    }

    /**
     * Creates a game from explicit state, primarily for tests and future UI wiring.
     *
     * @param dictionary loaded game dictionary
     * @param board starting board
     * @param tileBag tile bag state
     * @param humanPlayer human player state
     * @param computerPlayer computer player state
     * @param humanTurn true when it is the human player's turn
     */
    public ScrabbleGame(
        Dictionary dictionary,
        Board board,
        TileBag tileBag,
        ScrabblePlayer humanPlayer,
        ScrabblePlayer computerPlayer,
        boolean humanTurn
    ) {
        if (dictionary == null) {
            throw new IllegalArgumentException("dictionary is required");
        }
        if (board == null) {
            throw new IllegalArgumentException("board is required");
        }
        if (tileBag == null) {
            throw new IllegalArgumentException("tile bag is required");
        }
        if (humanPlayer == null || !humanPlayer.isHuman()) {
            throw new IllegalArgumentException("human player is required");
        }
        if (computerPlayer == null || computerPlayer.isHuman()) {
            throw new IllegalArgumentException("computer player is required");
        }

        this.solverEngine = new SolverEngine(dictionary);
        this.moveResolver = new MoveResolver(dictionary);
        this.board = board;
        this.tileBag = tileBag;
        this.humanPlayer = humanPlayer;
        this.computerPlayer = computerPlayer;
        this.humanTurn = humanTurn;
        this.lastStatusMessage = humanTurn ? "Human to move." : "Computer to move.";
    }

    /**
     * Returns the current board.
     *
     * @return current board state
     */
    public Board board() {
        return board;
    }

    /**
     * Returns the human player state.
     *
     * @return human player
     */
    public ScrabblePlayer humanPlayer() {
        return humanPlayer;
    }

    /**
     * Returns the computer player state.
     *
     * @return computer player
     */
    public ScrabblePlayer computerPlayer() {
        return computerPlayer;
    }

    /**
     * Returns whether it is currently the human player's turn.
     *
     * @return true when the human should act next
     */
    public boolean isHumanTurn() {
        return humanTurn;
    }

    /**
     * Returns whether the game is over.
     *
     * @return true when endgame scoring has been applied
     */
    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * Returns the number of tiles left in the bag.
     *
     * @return remaining bag count
     */
    public int tileBagRemaining() {
        return tileBag.remaining();
    }

    /**
     * Returns the most recent game status message.
     *
     * @return latest status
     */
    public String lastStatusMessage() {
        return lastStatusMessage;
    }

    /**
     * Applies a human move described by a proposed result board.
     *
     * @param proposedBoard board after the human's placement
     * @return resolved move details
     */
    public ResolvedPlay playHumanMove(Board proposedBoard) {
        ensureActiveHumanTurn();

        ResolvedPlay play = moveResolver.resolve(board, proposedBoard, humanPlayer.rack());
        String moveSummary = "Human played " + play.move().mainWord() + " for " + play.score() + " points.";
        applyResolvedPlay(humanPlayer, play);
        lastStatusMessage = gameOver ? moveSummary + " Game over." : moveSummary;
        if (!gameOver) {
            humanTurn = false;
        }
        return play;
    }

    /**
     * Lets the human pass the turn without penalty.
     */
    public void passHumanTurn() {
        ensureActiveHumanTurn();
        lastStatusMessage = "Human passed.";
        humanTurn = false;
        updateGameOverState();
    }

    /**
     * Exchanges selected human tiles and passes the turn.
     *
     * @param letters rack letters to exchange
     */
    public void exchangeHumanTiles(String letters) {
        ensureActiveHumanTurn();
        if (letters == null || letters.isEmpty()) {
            throw new IllegalArgumentException("letters are required");
        }
        if (!tileBag.canExchange(letters.length())) {
            throw new IllegalArgumentException("not enough tiles remain to exchange");
        }

        List<Character> removed = humanPlayer.rack().removeLetters(letters);
        humanPlayer.rack().addTiles(tileBag.exchange(removed));
        lastStatusMessage = "Human exchanged " + letters.length() + " tile(s).";
        humanTurn = false;
        updateGameOverState();
    }

    /**
     * Lets the computer play its best move, exchange its rack, or pass when blocked.
     *
     * @return resolved move details when a move was played, otherwise null
     */
    public ResolvedPlay playComputerTurn() {
        ensureActiveComputerTurn();

        SolverResult solved = solverEngine.solve(board, computerPlayer.rack().trayString());
        if (solved == null) {
            if (!computerPlayer.rack().isEmpty() && tileBag.canExchange(computerPlayer.rack().size())) {
                List<Character> removed = computerPlayer.rack().removeAllTiles();
                computerPlayer.rack().addTiles(tileBag.exchange(removed));
                lastStatusMessage = "Computer exchanged its rack.";
            } else {
                lastStatusMessage = "Computer had no legal move.";
            }
            humanTurn = true;
            updateGameOverState();
            return null;
        }

        List<WordPlacement> formedWords = wordFinder.collectFormedWordPlacements(
            solved.resultBoard(),
            solved.move().playedTiles()
        );
        ResolvedPlay play = new ResolvedPlay(solved.move(), solved.resultBoard(), formedWords);
        String moveSummary = "Computer played " + play.move().mainWord() + " for " + play.score() + " points.";
        applyResolvedPlay(computerPlayer, play);
        lastStatusMessage = gameOver ? moveSummary + " Game over." : moveSummary;
        if (!gameOver) {
            humanTurn = true;
        }
        return play;
    }

    // Applies a successful move, updates score/board/rack, and checks endgame.
    private void applyResolvedPlay(ScrabblePlayer player, ResolvedPlay play) {
        player.rack().removePlayedTiles(play.playedTiles());
        player.addScore(play.score());
        board = play.resultBoard();
        player.rack().fillToSize(tileBag, RACK_SIZE);
        updateGameOverState();
    }

    // Applies endgame leave penalties/bonuses once the bag is empty and one rack empties.
    private void updateGameOverState() {
        if (endgameAdjusted) {
            return;
        }
        if (tileBag.remaining() > 0) {
            return;
        }
        if (!humanPlayer.rack().isEmpty() && !computerPlayer.rack().isEmpty()) {
            return;
        }

        int humanLeave = rackLeaveValue(humanPlayer.rack());
        int computerLeave = rackLeaveValue(computerPlayer.rack());

        humanPlayer.addScore(-humanLeave);
        computerPlayer.addScore(-computerLeave);
        if (humanPlayer.rack().isEmpty()) {
            humanPlayer.addScore(computerLeave);
        }
        if (computerPlayer.rack().isEmpty()) {
            computerPlayer.addScore(humanLeave);
        }

        gameOver = true;
        endgameAdjusted = true;
    }

    // Computes the remaining face value of a rack.
    private int rackLeaveValue(RackState rack) {
        int total = 0;
        for (char tile : rack.tiles()) {
            total += TileValues.faceValue(tile);
        }
        return total;
    }

    // Verifies that the human is the active player.
    private void ensureActiveHumanTurn() {
        if (gameOver) {
            throw new IllegalStateException("game is already over");
        }
        if (!humanTurn) {
            throw new IllegalStateException("it is not the human player's turn");
        }
    }

    // Verifies that the computer is the active player.
    private void ensureActiveComputerTurn() {
        if (gameOver) {
            throw new IllegalStateException("game is already over");
        }
        if (humanTurn) {
            throw new IllegalStateException("it is not the computer player's turn");
        }
    }
}
