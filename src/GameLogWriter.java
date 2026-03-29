/**
 * Author: Garion
 *
 * Appends one human-readable session log for a Scrabble match.
 *
 * The logger writes the initial state, each completed turn, and the final
 * winner summary. It is designed to be shared by the JavaFX UI and local
 * stress-test tooling.
 */
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class GameLogWriter implements AutoCloseable {
    private static final Path DEFAULT_LOG_DIRECTORY = Path.of("game-logs");
    private static final DateTimeFormatter FILE_TIMESTAMP =
        DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");
    private static final DateTimeFormatter LOG_TIMESTAMP =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Path path;
    private final String sessionTag;
    private final String dictionaryPath;
    private final BufferedWriter writer;

    private boolean initialStateLogged;
    private boolean finalStateLogged;
    private int lastLoggedTurnNumber;

    /**
     * Creates a logger that writes to an explicit file path.
     *
     * @param path target log file
     * @param sessionTag session label such as "human-vs-computer"
     * @param dictionaryPath dictionary used for the session
     * @throws IOException if the log file cannot be created
     */
    public GameLogWriter(Path path, String sessionTag, String dictionaryPath) throws IOException {
        if (path == null) {
            throw new IllegalArgumentException("path is required");
        }
        if (sessionTag == null || sessionTag.isBlank()) {
            throw new IllegalArgumentException("session tag is required");
        }
        if (dictionaryPath == null || dictionaryPath.isBlank()) {
            throw new IllegalArgumentException("dictionary path is required");
        }

        this.path = path;
        this.sessionTag = sessionTag;
        this.dictionaryPath = dictionaryPath;
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        this.writer = Files.newBufferedWriter(path);
        writeLine("SCRABBLE SESSION LOG");
        writeLine("session: " + sessionTag);
        writeLine("dictionary: " + dictionaryPath);
        writeLine("created: " + LOG_TIMESTAMP.format(LocalDateTime.now()));
        writeBlankLine();
        writer.flush();
    }

    /**
     * Creates a timestamped logger under the default ignored log directory.
     *
     * @param sessionTag session label such as "human-vs-computer"
     * @param dictionaryPath dictionary used for the session
     * @return opened log writer
     * @throws IOException if the file cannot be created
     */
    public static GameLogWriter createTimestamped(String sessionTag, String dictionaryPath) throws IOException {
        String filename = sanitizeFilename(sessionTag)
            + "-"
            + FILE_TIMESTAMP.format(LocalDateTime.now())
            + ".log";
        return new GameLogWriter(DEFAULT_LOG_DIRECTORY.resolve(filename), sessionTag, dictionaryPath);
    }

    /**
     * Returns the log file path.
     *
     * @return log file path
     */
    public Path path() {
        return path;
    }

    /**
     * Synchronizes the log with the latest game snapshot.
     *
     * This method can be called repeatedly after refreshes; it only appends new
     * content when the game state has advanced.
     *
     * @param snapshot latest game snapshot
     * @throws IOException if writing fails
     */
    public synchronized void sync(GameSnapshot snapshot) throws IOException {
        if (snapshot == null) {
            throw new IllegalArgumentException("snapshot is required");
        }

        if (!initialStateLogged) {
            logInitialState(snapshot);
        }

        TurnRecord latestTurn = snapshot.latestTurn();
        if (latestTurn != null && latestTurn.number() > lastLoggedTurnNumber) {
            logTurn(latestTurn, snapshot.board());
            lastLoggedTurnNumber = latestTurn.number();
        }

        if (snapshot.isGameOver() && !finalStateLogged) {
            logGameOver(snapshot);
        }

        writer.flush();
    }

    @Override
    public synchronized void close() throws IOException {
        writer.close();
    }

    // Appends the initial game state.
    private void logInitialState(GameSnapshot snapshot) throws IOException {
        writeSectionHeader("Initial State");
        writeLine("human turn: " + snapshot.isHumanTurn());
        writeLine("human score: " + snapshot.humanScore());
        writeLine("computer score: " + snapshot.computerScore());
        writeLine("bag remaining: " + snapshot.tileBagRemaining());
        writeLine("human rack: " + snapshot.humanRack());
        writeLine("computer rack: " + snapshot.computerRack());
        writeLine("status: " + snapshot.statusMessage());
        writeBoard(snapshot.board());
        writeBlankLine();
        initialStateLogged = true;
    }

    // Appends one completed turn snapshot.
    private void logTurn(TurnRecord turn, Board board) throws IOException {
        writeSectionHeader("Turn " + turn.number());
        writeLine("actor: " + turn.actor());
        writeLine("type: " + turn.type());
        writeLine("summary: " + turn.summary());
        writeLine("score delta: " + turn.scoreDelta());
        writeLine("human score: " + turn.humanScore());
        writeLine("computer score: " + turn.computerScore());
        writeLine("bag remaining: " + turn.tileBagRemaining());
        writeLine("human rack: " + turn.humanRack());
        writeLine("computer rack: " + turn.computerRack());

        ResolvedPlay play = turn.play();
        if (play != null) {
            writeLine("main word: " + play.move().mainWord());
            writeLine("orientation: " + (play.move().isHorizontal() ? "HORIZONTAL" : "VERTICAL"));
            writeLine("span: (" + play.move().startRow() + "," + play.move().startCol() + ") -> ("
                + play.move().endRow() + "," + play.move().endCol() + ")");
            writeLine("played tiles: " + formatPlayedTiles(play.playedTiles()));
            writeLine("formed words: " + formatFormedWords(play.formedWords(), play.resultBoard()));
        }

        writeBoard(board);
        writeBlankLine();
    }

    // Appends the final winner summary.
    private void logGameOver(GameSnapshot snapshot) throws IOException {
        writeSectionHeader("Game Over");
        writeLine("winner: " + snapshot.winnerSummary());
        writeLine("final human score: " + snapshot.humanScore());
        writeLine("final computer score: " + snapshot.computerScore());
        writeLine("bag remaining: " + snapshot.tileBagRemaining());
        writeBoard(snapshot.board());
        writeBlankLine();
        finalStateLogged = true;
    }

    // Writes one board block to the log.
    private void writeBoard(Board board) throws IOException {
        writeLine("board:");
        for (int row = 0; row < board.size(); row++) {
            writeLine("  " + board.rowString(row));
        }
    }

    // Formats newly placed tiles for one move.
    private String formatPlayedTiles(List<PlayedTile> playedTiles) {
        ArrayList<String> parts = new ArrayList<>();
        for (PlayedTile tile : playedTiles) {
            String tileText = tile.isBlank()
                ? "*->" + Character.toLowerCase(tile.letter())
                : String.valueOf(Character.toLowerCase(tile.letter()));
            parts.add(tileText + "@(" + tile.row() + "," + tile.col() + ")");
        }
        return String.join(", ", parts);
    }

    // Formats every formed word with its extracted text and span.
    private String formatFormedWords(List<WordPlacement> formedWords, Board board) {
        ArrayList<String> parts = new ArrayList<>();
        for (WordPlacement placement : formedWords) {
            int startRow = placement.isHorizontal() ? placement.fixed() : placement.start();
            int startCol = placement.isHorizontal() ? placement.start() : placement.fixed();
            int endRow = placement.isHorizontal() ? placement.fixed() : placement.end();
            int endCol = placement.isHorizontal() ? placement.end() : placement.fixed();
            parts.add(placement.text(board) + "@(" + startRow + "," + startCol + ")->(" + endRow + "," + endCol + ")");
        }
        return String.join(", ", parts);
    }

    // Writes a section header line.
    private void writeSectionHeader(String title) throws IOException {
        writeLine("== " + title + " ==");
    }

    // Writes a single line.
    private void writeLine(String line) throws IOException {
        writer.write(line);
        writer.newLine();
    }

    // Writes one empty line.
    private void writeBlankLine() throws IOException {
        writer.newLine();
    }

    // Normalizes a session tag into a filesystem-safe filename fragment.
    private static String sanitizeFilename(String raw) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < raw.length(); i++) {
            char ch = raw.charAt(i);
            if (Character.isLetterOrDigit(ch) || ch == '-' || ch == '_') {
                sb.append(ch);
            } else {
                sb.append('_');
            }
        }
        return sb.toString();
    }
}
