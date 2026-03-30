/**
 * Author: Garion
 *
 * File purpose: represent one immutable solver anchor square.
 */
public class AnchorSquare {
    private final int row;
    private final int col;

    /**
     * Creates one anchor coordinate.
     *
     * @param row row index
     * @param col column index
     */
    public AnchorSquare(int row, int col) {
        this.row = row;
        this.col = col;
    }

    /**
     * Returns anchor row.
     *
     * @return row index
     */
    public int row() {
        return row;
    }

    /**
     * Returns anchor column.
     *
     * @return column index
     */
    public int col() {
        return col;
    }

    /**
     * Returns a stable coordinate key for deduplication.
     *
     * @return coordinate key
     */
    public String key() {
        return row + "," + col;
    }
}
