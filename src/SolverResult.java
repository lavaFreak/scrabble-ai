/**
 * Author: Garion
 *
 * File purpose: represent one immutable solver output.
 */
public class SolverResult {
    private final MoveCandidate move;
    private final Board resultBoard;

    /**
     * Creates one solver result.
     *
     * @param move selected move candidate
     * @param resultBoard board after applying the move
     */
    public SolverResult(MoveCandidate move, Board resultBoard) {
        this.move = move;
        this.resultBoard = resultBoard;
    }

    /**
     * Returns the selected move.
     *
     * @return chosen move candidate
     */
    public MoveCandidate move() {
        return move;
    }

    /**
     * Returns the board after applying the selected move.
     *
     * @return result board
     */
    public Board resultBoard() {
        return resultBoard;
    }
}
