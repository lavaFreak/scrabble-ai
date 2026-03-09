/**
 * Author: Garion
 *
 * File purpose: validate move legality for scorer Part 1.
 */
import java.util.List;

public class LegalityChecker {
    private final Dictionary dictionary;

    /**
     * Creates legality checker with dictionary backing.
     *
     * @param dictionary dictionary used for formed-word validation
     */
    public LegalityChecker(Dictionary dictionary) {
        this.dictionary = dictionary;
    }

    /**
     * Evaluates move legality for Part 1 scorer rules.
     *
     * @param original board before move
     * @param result board after move
     * @param played newly placed tiles
     * @param words words formed by the move
     * @return true when move is legal
     */
    public boolean isLegalMove(Board original, Board result, List<PlayedTile> played, List<WordPlacement> words) {
        if (played.isEmpty()) {
            return false;
        }

        boolean sameRow = allSameRow(played);
        boolean sameCol = allSameCol(played);
        if (!sameRow && !sameCol) {
            return false;
        }

        if (!isContiguousOnLine(result, played, sameRow)) {
            return false;
        }

        if (original.isEmptyBoard()) {
            int center = original.size() / 2;
            boolean coversCenter = false;
            for (PlayedTile tile : played) {
                if (tile.row() == center && tile.col() == center) {
                    coversCenter = true;
                    break;
                }
            }
            if (!coversCenter) {
                return false;
            }
        } else if (!touchesExistingTile(original, played)) {
            return false;
        }

        if (words.isEmpty()) {
            return false;
        }

        for (WordPlacement word : words) {
            String text = word.text(result);
            if (!dictionary.contains(text)) {
                return false;
            }
        }
        return true;
    }

    // Checks one-line placement constraint (all tiles in one row).
    private boolean allSameRow(List<PlayedTile> played) {
        int row = played.get(0).row();
        for (PlayedTile tile : played) {
            if (tile.row() != row) {
                return false;
            }
        }
        return true;
    }

    // Checks one-line placement constraint (all tiles in one column).
    private boolean allSameCol(List<PlayedTile> played) {
        int col = played.get(0).col();
        for (PlayedTile tile : played) {
            if (tile.col() != col) {
                return false;
            }
        }
        return true;
    }

    // Ensures no holes along the played line after move application.
    private boolean isContiguousOnLine(Board result, List<PlayedTile> played, boolean sameRow) {
        if (played.size() <= 1) {
            return true;
        }

        if (sameRow) {
            int row = played.get(0).row();
            int minCol = Integer.MAX_VALUE;
            int maxCol = Integer.MIN_VALUE;
            for (PlayedTile tile : played) {
                minCol = Math.min(minCol, tile.col());
                maxCol = Math.max(maxCol, tile.col());
            }
            for (int c = minCol; c <= maxCol; c++) {
                if (!result.isTile(row, c)) {
                    return false;
                }
            }
            return true;
        }

        int col = played.get(0).col();
        int minRow = Integer.MAX_VALUE;
        int maxRow = Integer.MIN_VALUE;
        for (PlayedTile tile : played) {
            minRow = Math.min(minRow, tile.row());
            maxRow = Math.max(maxRow, tile.row());
        }
        for (int r = minRow; r <= maxRow; r++) {
            if (!result.isTile(r, col)) {
                return false;
            }
        }
        return true;
    }

    // Requires adjacency to existing board tiles after first move.
    private boolean touchesExistingTile(Board original, List<PlayedTile> played) {
        for (PlayedTile tile : played) {
            int r = tile.row();
            int c = tile.col();
            if (isOriginalTileAt(original, r - 1, c)
                || isOriginalTileAt(original, r + 1, c)
                || isOriginalTileAt(original, r, c - 1)
                || isOriginalTileAt(original, r, c + 1)) {
                return true;
            }
        }
        return false;
    }

    // Helper for in-bounds + occupied checks on original board.
    private boolean isOriginalTileAt(Board original, int row, int col) {
        return original.inBounds(row, col) && original.isTile(row, col);
    }
}
