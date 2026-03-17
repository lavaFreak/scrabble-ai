/**
 * Author: Garion
 *
 * File purpose: choose the best available move for one solver case.
 */

public class SolverEngine {
    private final CandidateGenerator candidateGenerator;
    private final MoveApplicator moveApplicator = new MoveApplicator();

    /**
     * Creates a solver engine backed by the shared dictionary.
     *
     * @param dictionary solver dictionary
     */
    public SolverEngine(Dictionary dictionary) {
        this.candidateGenerator = new CandidateGenerator(dictionary);
    }

    /**
     * Solves one board+tray case by selecting the highest-scoring generated move.
     *
     * @param board current board
     * @param tray normalized rack string
     * @return solver result or null when no legal move is found
     */
    public SolverResult solve(Board board, String tray) {
        MoveCandidate best = candidateGenerator.findBestLegalCandidate(board, tray);
        if (best == null) {
            return null;
        }

        Board resultBoard = moveApplicator.apply(board, best);
        return new SolverResult(best, resultBoard);
    }
}
