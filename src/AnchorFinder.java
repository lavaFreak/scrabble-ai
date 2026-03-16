/**
 * Author: Garion
 *
 * File purpose: discover solver anchor squares for move generation.
 */
import java.util.ArrayList;
import java.util.List;

public class AnchorFinder {

    /**
     * Collects all anchor squares on the board in row-major order.
     *
     * @param board board to inspect
     * @return anchor coordinates for candidate generation
     */
    public List<AnchorSquare> collectAnchors(Board board) {
        if (board == null) {
            throw new IllegalArgumentException("board is required");
        }

        List<AnchorSquare> anchors = new ArrayList<>();
        if (board.isEmptyBoard()) {
            int center = board.size() / 2;
            anchors.add(new AnchorSquare(center, center));
            return anchors;
        }

        for (int r = 0; r < board.size(); r++) {
            for (int c = 0; c < board.size(); c++) {
                if (!board.isTile(r, c) && touchesExistingTile(board, r, c)) {
                    anchors.add(new AnchorSquare(r, c));
                }
            }
        }
        return anchors;
    }

    // Returns whether an empty square is orthogonally adjacent to an existing tile.
    private boolean touchesExistingTile(Board board, int row, int col) {
        return isTile(board, row - 1, col)
            || isTile(board, row + 1, col)
            || isTile(board, row, col - 1)
            || isTile(board, row, col + 1);
    }

    // Bounds-safe board tile lookup.
    private boolean isTile(Board board, int row, int col) {
        return board.inBounds(row, col) && board.isTile(row, col);
    }
}
