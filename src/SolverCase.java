/**
 * Author: Garion
 *
 * Immutable Part 2 solver test case: one input board plus one tray.
 */
public class SolverCase {
    private final Board board;
    private final String tray;

    /**
     * Creates one solver input case.
     *
     * @param board board before the move
     * @param tray normalized tray string
     */
    public SolverCase(Board board, String tray) {
        this.board = board;
        this.tray = tray;
    }

    /**
     * Returns the input board.
     *
     * @return solver board
     */
    public Board board() {
        return board;
    }

    /**
     * Returns the normalized tray contents.
     *
     * @return lowercase tray letters with optional '*'
     */
    public String tray() {
        return tray;
    }
}
