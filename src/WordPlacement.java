/**
 * Author: Garion
 *
 * File purpose: describe the coordinate span of one formed word on the board.
 *
 * A placement is represented as either horizontal (fixed row, varying columns)
 * or vertical (fixed column, varying rows).
 * Use this class as a reusable word geometry descriptor across legality and scoring.
 */
public class WordPlacement {
    private final boolean horizontal;
    private final int fixed;
    private final int start;
    private final int end;

    /**
     * Creates a word placement descriptor.
     *
     * @param horizontal true for horizontal, false for vertical placement
     * @param fixed fixed row (horizontal) or column (vertical)
     * @param start start index along varying axis
     * @param end end index along varying axis
     */
    public WordPlacement(boolean horizontal, int fixed, int start, int end) {
        this.horizontal = horizontal;
        this.fixed = fixed;
        this.start = start;
        this.end = end;
    }

    /**
     * Returns word length.
     *
     * @return number of squares in this placement
     */
    public int length() {
        return end - start + 1;
    }

    /**
     * Returns unique key for deduplication.
     *
     * @return stable placement key string
     */
    public String key() {
        return (horizontal ? "H" : "V") + ":" + fixed + ":" + start + ":" + end;
    }

    /**
     * Returns orientation flag.
     *
     * @return true if horizontal
     */
    public boolean isHorizontal() {
        return horizontal;
    }

    /**
     * Returns fixed index for this placement.
     *
     * @return fixed row/column index
     */
    public int fixed() {
        return fixed;
    }

    /**
     * Returns start index on the varying axis.
     *
     * @return start index
     */
    public int start() {
        return start;
    }

    /**
     * Returns end index on the varying axis.
     *
     * @return end index
     */
    public int end() {
        return end;
    }

    /**
     * Materializes word text from board tiles.
     *
     * @param board board containing placed tiles
     * @return extracted word text
     */
    public String text(Board board) {
        StringBuilder sb = new StringBuilder();
        if (horizontal) {
            for (int col = start; col <= end; col++) {
                sb.append(board.tileAt(fixed, col));
            }
        } else {
            for (int row = start; row <= end; row++) {
                sb.append(board.tileAt(row, fixed));
            }
        }
        return sb.toString();
    }
}
