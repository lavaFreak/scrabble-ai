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

    public PlayedTile(char letter, int row, int col) {
        this.letter = letter;
        this.row = row;
        this.col = col;
    }

    public char letter() {
        return letter;
    }

    public int row() {
        return row;
    }

    public int col() {
        return col;
    }
}
