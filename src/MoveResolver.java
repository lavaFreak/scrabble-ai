/**
 * Author: Garion
 *
 * Resolves a board transition into one validated, scored move.
 *
 * This is the backend bridge between a UI-generated board proposal and the
 * existing legality/scoring pipeline from the scorer and solver work.
 */
import java.util.List;

public class MoveResolver {
    private final CompatibilityChecker compatibilityChecker = new CompatibilityChecker();
    private final MoveExtractor moveExtractor = new MoveExtractor();
    private final WordFinder wordFinder = new WordFinder();
    private final LegalityChecker legalityChecker;
    private final Scorer scorer = new Scorer();

    /**
     * Creates a resolver backed by the shared dictionary.
     *
     * @param dictionary dictionary used for legality checks
     */
    public MoveResolver(Dictionary dictionary) {
        if (dictionary == null) {
            throw new IllegalArgumentException("dictionary is required");
        }
        this.legalityChecker = new LegalityChecker(dictionary);
    }

    /**
     * Resolves a board transition into one legal, rack-backed move.
     *
     * @param original board before the move
     * @param proposed board after the move
     * @param rack rack that must supply the new tiles
     * @return resolved play
     */
    public ResolvedPlay resolve(Board original, Board proposed, RackState rack) {
        if (original == null) {
            throw new IllegalArgumentException("original board is required");
        }
        if (proposed == null) {
            throw new IllegalArgumentException("proposed board is required");
        }
        if (rack == null) {
            throw new IllegalArgumentException("rack is required");
        }

        String incompatibility = compatibilityChecker.findIncompatibility(original, proposed);
        if (incompatibility != null) {
            throw new IllegalArgumentException("incompatible boards: " + incompatibility);
        }

        List<PlayedTile> played = moveExtractor.collectNewTiles(original, proposed);
        if (played.isEmpty()) {
            throw new IllegalArgumentException("move must place at least one tile");
        }
        if (!rack.canSupply(played)) {
            throw new IllegalArgumentException("rack does not contain the played tiles");
        }

        List<WordPlacement> words = wordFinder.collectFormedWordPlacements(proposed, played);
        if (!legalityChecker.isLegalMove(original, proposed, played, words)) {
            throw new IllegalArgumentException("move is not legal");
        }

        WordPlacement mainWordPlacement = resolveMainWordPlacement(proposed, played);
        MoveCandidate move = new MoveCandidate(mainWordPlacement, mainWordPlacement.text(proposed), played)
            .withScore(scorer.computeScore(original, proposed, played, words));
        return new ResolvedPlay(move, proposed, words);
    }

    // Infers the main-word span from the played tiles and resulting board state.
    private WordPlacement resolveMainWordPlacement(Board board, List<PlayedTile> played) {
        PlayedTile first = played.get(0);
        if (played.size() == 1) {
            WordPlacement horizontal = horizontalWordPlacement(board, first.row(), first.col());
            WordPlacement vertical = verticalWordPlacement(board, first.row(), first.col());
            return horizontal.length() >= vertical.length() ? horizontal : vertical;
        }

        if (allSameRow(played)) {
            return horizontalWordPlacement(board, first.row(), first.col());
        }
        if (allSameCol(played)) {
            return verticalWordPlacement(board, first.row(), first.col());
        }
        throw new IllegalArgumentException("played tiles must lie on one row or one column");
    }

    // Finds the horizontal word passing through one coordinate.
    private WordPlacement horizontalWordPlacement(Board board, int row, int col) {
        int start = col;
        while (start - 1 >= 0 && board.isTile(row, start - 1)) {
            start--;
        }
        int end = col;
        while (end + 1 < board.size() && board.isTile(row, end + 1)) {
            end++;
        }
        return new WordPlacement(true, row, start, end);
    }

    // Finds the vertical word passing through one coordinate.
    private WordPlacement verticalWordPlacement(Board board, int row, int col) {
        int start = row;
        while (start - 1 >= 0 && board.isTile(start - 1, col)) {
            start--;
        }
        int end = row;
        while (end + 1 < board.size() && board.isTile(end + 1, col)) {
            end++;
        }
        return new WordPlacement(false, col, start, end);
    }

    // Checks whether all tiles share one row.
    private boolean allSameRow(List<PlayedTile> played) {
        int row = played.get(0).row();
        for (PlayedTile tile : played) {
            if (tile.row() != row) {
                return false;
            }
        }
        return true;
    }

    // Checks whether all tiles share one column.
    private boolean allSameCol(List<PlayedTile> played) {
        int col = played.get(0).col();
        for (PlayedTile tile : played) {
            if (tile.col() != col) {
                return false;
            }
        }
        return true;
    }
}
