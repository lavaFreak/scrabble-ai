/**
 * Author: Garion
 *
 * File purpose: parse Part 2 solver cases from stdin or example files.
 */
import java.io.BufferedReader;
import java.io.IOException;

public class SolverInputParser {
    private final BoardParser boardParser = new BoardParser();

    /**
     * Reads one solver case consisting of a board followed by one tray line.
     *
     * @param in source reader
     * @return parsed solver case or null at EOF before the next case
     * @throws IOException if stream read fails
     */
    public SolverCase readCase(BufferedReader in) throws IOException {
        Board board = boardParser.readBoard(in);
        if (board == null) {
            return null;
        }

        String trayLine = readNextNonEmptyLine(in);
        if (trayLine == null) {
            return null;
        }

        return new SolverCase(board, normalizeTray(trayLine));
    }

    // Normalizes a tray line to lowercase letters and '*' blanks.
    private String normalizeTray(String trayLine) {
        String tray = trayLine.trim().toLowerCase();
        for (int i = 0; i < tray.length(); i++) {
            char ch = tray.charAt(i);
            if (ch != '*' && (ch < 'a' || ch > 'z')) {
                throw new IllegalArgumentException("Invalid tray character: " + ch);
            }
        }
        return tray;
    }

    // Skips blank lines and returns the next tray line or null at EOF.
    private String readNextNonEmptyLine(BufferedReader in) throws IOException {
        String line;
        while ((line = in.readLine()) != null) {
            if (!line.trim().isEmpty()) {
                return line;
            }
        }
        return null;
    }
}
