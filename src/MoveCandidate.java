/**
 * Author: Garion
 *
 * File purpose: represent one immutable solver-side move candidate.
 *
 * A candidate describes one proposed placement geometry and the newly added
 * tiles that would realize that placement on the board.
 */
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MoveCandidate {
    private final WordPlacement mainWordPlacement;
    private final String mainWord;
    private final List<PlayedTile> playedTiles;
    private final Integer score;

    /**
     * Creates an unscored move candidate from placement geometry, word text, and newly played tiles.
     *
     * @param mainWordPlacement span of the candidate's main word
     * @param mainWord text of the candidate's main word
     * @param playedTiles new tiles placed by the move
     */
    public MoveCandidate(WordPlacement mainWordPlacement, String mainWord, List<PlayedTile> playedTiles) {
        this(mainWordPlacement, mainWord, playedTiles, null);
    }

    // Internal constructor used by withScore().
    private MoveCandidate(
        WordPlacement mainWordPlacement,
        String mainWord,
        List<PlayedTile> playedTiles,
        Integer score
    ) {
        if (mainWordPlacement == null) {
            throw new IllegalArgumentException("main word placement is required");
        }
        if (mainWord == null || mainWord.isEmpty()) {
            throw new IllegalArgumentException("main word text is required");
        }
        if (playedTiles == null || playedTiles.isEmpty()) {
            throw new IllegalArgumentException("at least one played tile is required");
        }
        if (mainWord.length() != mainWordPlacement.length()) {
            throw new IllegalArgumentException("main word text must match placement length");
        }

        List<PlayedTile> sortedTiles = new ArrayList<>(playedTiles);
        sortedTiles.sort(Comparator
            .comparingInt(PlayedTile::row)
            .thenComparingInt(PlayedTile::col));

        validateTilesOnPlacement(mainWordPlacement, sortedTiles);

        this.mainWordPlacement = mainWordPlacement;
        this.mainWord = mainWord;
        this.playedTiles = List.copyOf(sortedTiles);
        this.score = score;
    }

    /**
     * Returns the main word span for this candidate.
     *
     * @return main word placement
     */
    public WordPlacement mainWordPlacement() {
        return mainWordPlacement;
    }

    /**
     * Returns the newly played tiles in row-major order.
     *
     * @return immutable tile list
     */
    public List<PlayedTile> playedTiles() {
        return playedTiles;
    }

    /**
     * Returns the main word text for this candidate.
     *
     * @return main word text
     */
    public String mainWord() {
        return mainWord;
    }

    /**
     * Returns whether the main word is horizontal.
     *
     * @return true when horizontal
     */
    public boolean isHorizontal() {
        return mainWordPlacement.isHorizontal();
    }

    /**
     * Returns the fixed row/column index for the main word placement.
     *
     * @return fixed placement index
     */
    public int fixed() {
        return mainWordPlacement.fixed();
    }

    /**
     * Returns the start index of the main word on its varying axis.
     *
     * @return start index
     */
    public int start() {
        return mainWordPlacement.start();
    }

    /**
     * Returns the end index of the main word on its varying axis.
     *
     * @return end index
     */
    public int end() {
        return mainWordPlacement.end();
    }

    /**
     * Returns the number of newly played tiles in the move.
     *
     * @return count of placed tiles
     */
    public int tileCount() {
        return playedTiles.size();
    }

    /**
     * Returns the row of the main word's first square.
     *
     * @return starting row
     */
    public int startRow() {
        return isHorizontal() ? fixed() : start();
    }

    /**
     * Returns the column of the main word's first square.
     *
     * @return starting column
     */
    public int startCol() {
        return isHorizontal() ? start() : fixed();
    }

    /**
     * Returns the row of the main word's last square.
     *
     * @return ending row
     */
    public int endRow() {
        return isHorizontal() ? fixed() : end();
    }

    /**
     * Returns the column of the main word's last square.
     *
     * @return ending column
     */
    public int endCol() {
        return isHorizontal() ? end() : fixed();
    }

    /**
     * Returns whether this candidate has been scored.
     *
     * @return true when a score has been assigned
     */
    public boolean hasScore() {
        return score != null;
    }

    /**
     * Returns the assigned score for this candidate.
     *
     * @return score value
     */
    public int score() {
        if (score == null) {
            throw new IllegalStateException("candidate score has not been assigned");
        }
        return score;
    }

    /**
     * Returns a new candidate with the provided score attached.
     *
     * @param score score for the candidate
     * @return new scored move candidate
     */
    public MoveCandidate withScore(int score) {
        return new MoveCandidate(mainWordPlacement, mainWord, playedTiles, score);
    }

    /**
     * Returns whether the main-word span covers a given board square.
     *
     * @param row row index
     * @param col column index
     * @return true when the square lies on the main word span
     */
    public boolean coversSquare(int row, int col) {
        if (isHorizontal()) {
            return row == fixed() && col >= start() && col <= end();
        }
        return col == fixed() && row >= start() && row <= end();
    }

    /**
     * Returns whether this move places a new tile at a given coordinate.
     *
     * @param row row index
     * @param col column index
     * @return true when a newly played tile occupies the coordinate
     */
    public boolean playsAt(int row, int col) {
        for (PlayedTile tile : playedTiles) {
            if (tile.row() == row && tile.col() == col) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether any newly placed tile is a blank tile.
     *
     * @return true when the move uses a blank
     */
    public boolean usesBlankTiles() {
        for (PlayedTile tile : playedTiles) {
            if (tile.isBlank()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a stable key for deduplicating equivalent move candidates.
     *
     * @return candidate key
     */
    public String key() {
        StringBuilder sb = new StringBuilder();
        sb.append(mainWordPlacement.key()).append(':').append(mainWord);
        for (PlayedTile tile : playedTiles) {
            sb.append(':')
                .append(tile.row())
                .append(',')
                .append(tile.col())
                .append('=')
                .append(tile.isBlank() ? Character.toUpperCase(tile.letter()) : tile.letter());
        }
        return sb.toString();
    }

    // Validates that each new tile lies on the proposed main-word placement.
    private void validateTilesOnPlacement(WordPlacement placement, List<PlayedTile> tiles) {
        Set<String> seen = new HashSet<>();
        for (PlayedTile tile : tiles) {
            if (!seen.add(tile.row() + "," + tile.col())) {
                throw new IllegalArgumentException("duplicate played tile coordinate");
            }

            boolean onPlacement;
            if (placement.isHorizontal()) {
                onPlacement = tile.row() == placement.fixed()
                    && tile.col() >= placement.start()
                    && tile.col() <= placement.end();
            } else {
                onPlacement = tile.col() == placement.fixed()
                    && tile.row() >= placement.start()
                    && tile.row() <= placement.end();
            }

            if (!onPlacement) {
                throw new IllegalArgumentException("played tile lies outside main word placement");
            }
        }
    }
}
