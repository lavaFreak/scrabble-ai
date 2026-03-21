/**
 * Author: Garion
 *
 * Fully resolved move information for the Part 3 game flow.
 */
import java.util.List;

public class ResolvedPlay {
    private final MoveCandidate move;
    private final Board resultBoard;
    private final List<WordPlacement> formedWords;

    /**
     * Creates one resolved play.
     *
     * @param move fully validated move
     * @param resultBoard resulting board after the move
     * @param formedWords words formed or modified by the move
     */
    public ResolvedPlay(MoveCandidate move, Board resultBoard, List<WordPlacement> formedWords) {
        if (move == null) {
            throw new IllegalArgumentException("move is required");
        }
        if (resultBoard == null) {
            throw new IllegalArgumentException("result board is required");
        }
        if (formedWords == null || formedWords.isEmpty()) {
            throw new IllegalArgumentException("formed words are required");
        }

        this.move = move;
        this.resultBoard = resultBoard;
        this.formedWords = List.copyOf(formedWords);
    }

    /**
     * Returns the validated move candidate.
     *
     * @return move candidate
     */
    public MoveCandidate move() {
        return move;
    }

    /**
     * Returns the board after the move.
     *
     * @return resulting board
     */
    public Board resultBoard() {
        return resultBoard;
    }

    /**
     * Returns the words formed or modified by the move.
     *
     * @return immutable formed-word list
     */
    public List<WordPlacement> formedWords() {
        return formedWords;
    }

    /**
     * Returns the move score.
     *
     * @return move score
     */
    public int score() {
        return move.score();
    }

    /**
     * Returns the newly played tiles.
     *
     * @return immutable played-tile list
     */
    public List<PlayedTile> playedTiles() {
        return move.playedTiles();
    }
}
