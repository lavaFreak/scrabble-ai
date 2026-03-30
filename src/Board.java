/**
 * Author: Garion
 *
 * File purpose: provide an immutable board view for scoring, legality, and UI state queries.
 *
 * Tokens preserve the input format for each square (multiplier or tile token),
 * while helper methods expose tile and multiplier semantics.
 * Use this class as the source of truth for board queries in parsing, legality, and scoring.
 */
import java.util.ArrayList;
import java.util.List;

public class Board {
    private final int size;
    private final String[][] tokens;

    /**
     * Creates an immutable board wrapper.
     *
     * @param size board size (N for an N x N board)
     * @param tokens parsed board tokens
     */
    public Board(int size, String[][] tokens) {
        this.size = size;
        this.tokens = tokens;
    }

    /**
     * Returns board size.
     *
     * @return board dimension
     */
    public int size() {
        return size;
    }

    /**
     * Returns the raw token at a coordinate.
     *
     * @param row row index
     * @param col column index
     * @return square token
     */
    public String get(int row, int col) {
        return tokens[row][col];
    }

    /**
     * Returns whether a coordinate is inside the board.
     *
     * @param row row index
     * @param col column index
     * @return true when in bounds
     */
    public boolean inBounds(int row, int col) {
        return row >= 0 && row < size && col >= 0 && col < size;
    }

    /**
     * Returns whether a square is occupied by a tile.
     *
     * @param row row index
     * @param col column index
     * @return true when square contains tile token
     */
    public boolean isTile(int row, int col) {
        return isTileToken(get(row, col));
    }

    /**
     * Returns the tile letter at a coordinate (lowercased).
     *
     * @param row row index
     * @param col column index
     * @return tile letter in lowercase
     */
    public char tileAt(int row, int col) {
        String token = get(row, col);
        if (!isTileToken(token)) {
            throw new IllegalStateException("No tile at (" + row + ", " + col + ")");
        }
        return Character.toLowerCase(extractTileChar(token));
    }

    /**
     * Returns whether the tile at a coordinate is a blank tile.
     *
     * @param row row index
     * @param col column index
     * @return true when tile token uses uppercase letter
     */
    public boolean isBlankTile(int row, int col) {
        String token = get(row, col);
        if (!isTileToken(token)) {
            return false;
        }
        char ch = extractTileChar(token);
        return Character.isUpperCase(ch);
    }

    /**
     * Returns row text with original token spacing.
     *
     * @param row row index
     * @return row string
     */
    public String rowString(int row) {
        return rowString(row, false);
    }

    /**
     * Returns row text with extra padding for single-letter tiles.
     *
     * @param row row index
     * @return padded row string for exact expected output alignment
     */
    public String rowStringPadded(int row) {
        return rowString(row, true);
    }

    // Builds one output row, optionally padding single-letter tokens.
    private String rowString(int row, boolean padSingleCharTokens) {
        StringBuilder sb = new StringBuilder();
        for (int c = 0; c < size; c++) {
            if (c > 0) {
                sb.append(' ');
            }
            String token = tokens[row][c];
            if (padSingleCharTokens && token.length() == 1) {
                sb.append(' ');
            }
            sb.append(token);
        }
        return sb.toString();
    }

    /**
     * Compares tile state at one coordinate across two boards.
     *
     * @param other board to compare against
     * @param row row index
     * @param col column index
     * @return true when tile occupancy/value match
     */
    public boolean sameTileAt(Board other, int row, int col) {
        boolean mine = isTile(row, col);
        boolean theirs = other.isTile(row, col);
        if (!mine && !theirs) {
            return true;
        }
        if (mine != theirs) {
            return false;
        }
        return tileAt(row, col) == other.tileAt(row, col);
    }

    /**
     * Returns whether no tiles are currently on the board.
     *
     * @return true when board has no tile tokens
     */
    public boolean isEmptyBoard() {
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (isTile(r, c)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Collects coordinates where raw tokens differ between boards.
     *
     * @param other board to compare against
     * @return list of [row, col] changes
     */
    public List<int[]> changedPositions(Board other) {
        List<int[]> changed = new ArrayList<>();
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (!tokens[r][c].equals(other.tokens[r][c])) {
                    changed.add(new int[] {r, c});
                }
            }
        }
        return changed;
    }

    /**
     * Returns word multiplier for a square.
     *
     * @param row row index
     * @param col column index
     * @return word multiplier (defaults to 1)
     */
    public int wordMultiplierAt(int row, int col) {
        String token = get(row, col);
        if (token.length() == 2 && Character.isDigit(token.charAt(0)) && token.charAt(1) == '.') {
            return Character.getNumericValue(token.charAt(0));
        }
        return 1;
    }

    /**
     * Returns letter multiplier for a square.
     *
     * @param row row index
     * @param col column index
     * @return letter multiplier (defaults to 1)
     */
    public int letterMultiplierAt(int row, int col) {
        String token = get(row, col);
        if (token.length() == 2 && token.charAt(0) == '.' && Character.isDigit(token.charAt(1))) {
            return Character.getNumericValue(token.charAt(1));
        }
        return 1;
    }

    // Detects tile tokens (.a, A, a) vs premium tokens.
    private static boolean isTileToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        if (token.length() == 1) {
            return Character.isLetter(token.charAt(0));
        }
        if (token.length() == 2) {
            return token.charAt(0) == '.' && Character.isLetter(token.charAt(1));
        }
        return false;
    }

    // Extracts letter char from either one-char or dotted two-char tile tokens.
    private static char extractTileChar(String token) {
        if (token.length() == 1) {
            return token.charAt(0);
        }
        return token.charAt(1);
    }
}
