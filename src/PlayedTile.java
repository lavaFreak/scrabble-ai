/**
 * Author: Garion
 *
 * Represents one tile placed by the current move.
 * Use this class as the shared value type between extraction, legality, and scoring.
 */
public class PlayedTile {
    private final char letter;
    private final int row;
    private final int col;

    /**
     * Creates immutable played-tile record.
     *
     * @param letter tile letter
     * @param row row index
     * @param col column index
     */
    public PlayedTile(char letter, int row, int col) {
        this.letter = letter;
        this.row = row;
        this.col = col;
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
}
