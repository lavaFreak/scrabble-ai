/**
 * Author: Garion
 *
 * File purpose: represent one tile placed by the current move.
 *
 * Use this class as the shared value type between extraction, legality, and scoring.
 */
public class PlayedTile {
    private final char letter;
    private final int row;
    private final int col;
    private final boolean blank;

    /**
     * Creates immutable played-tile record.
     *
     * @param letter tile letter
     * @param row row index
     * @param col column index
     */
    public PlayedTile(char letter, int row, int col) {
        this(letter, row, col, false);
    }

    /**
     * Creates immutable played-tile record with explicit blank-tile state.
     *
     * @param letter tile letter
     * @param row row index
     * @param col column index
     * @param blank true when the tile is a blank standing in for the given letter
     */
    public PlayedTile(char letter, int row, int col, boolean blank) {
        this.letter = letter;
        this.row = row;
        this.col = col;
        this.blank = blank;
    }

    /**
     * Returns played tile letter.
     *
     * @return tile letter
     */
    public char letter() {
        return letter;
    }

    /**
     * Returns played tile row.
     *
     * @return row index
     */
    public int row() {
        return row;
    }

    /**
     * Returns played tile column.
     *
     * @return column index
     */
    public int col() {
        return col;
    }

    /**
     * Returns whether this played tile is a blank tile.
     *
     * @return true when blank-backed
     */
    public boolean isBlank() {
        return blank;
    }
}
