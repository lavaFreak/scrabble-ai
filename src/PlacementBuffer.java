/**
 * Author: Garion
 *
 * UI-facing buffer for one tentative human placement.
 *
 * This lets the future JavaFX layer stage letter placement interactively
 * without mutating the real game board until the move is submitted.
 */
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PlacementBuffer {
    private final Board baseBoard;
    private final ArrayList<PlayedTile> placements = new ArrayList<>();

    /**
     * Creates a placement buffer anchored to one board state.
     *
     * @param baseBoard board being edited
     */
    public PlacementBuffer(Board baseBoard) {
        if (baseBoard == null) {
            throw new IllegalArgumentException("base board is required");
        }
        this.baseBoard = baseBoard;
    }

    /**
     * Returns the board the buffer is based on.
     *
     * @return immutable base board
     */
    public Board baseBoard() {
        return baseBoard;
    }

    /**
     * Returns the number of pending placements.
     *
     * @return pending placement count
     */
    public int placementCount() {
        return placements.size();
    }

    /**
     * Returns whether any placements are buffered.
     *
     * @return true when the buffer is non-empty
     */
    public boolean hasPlacements() {
        return !placements.isEmpty();
    }

    /**
     * Places a normal tile at the given coordinate, replacing any existing tentative tile there.
     *
     * @param row board row
     * @param col board column
     * @param letter lowercase or uppercase letter tile
     */
    public void placeLetter(int row, int col, char letter) {
        placeTile(row, col, letter, false);
    }

    /**
     * Places a blank tile representing the given letter at the coordinate.
     *
     * @param row board row
     * @param col board column
     * @param letter represented letter
     */
    public void placeBlank(int row, int col, char letter) {
        placeTile(row, col, letter, true);
    }

    /**
     * Removes any pending placement at a coordinate.
     *
     * @param row board row
     * @param col board column
     */
    public void removeAt(int row, int col) {
        placements.removeIf(tile -> tile.row() == row && tile.col() == col);
    }

    /**
     * Clears all pending placements.
     */
    public void clear() {
        placements.clear();
    }

    /**
     * Returns the pending placements in row-major order.
     *
     * @return immutable played-tile list
     */
    public List<PlayedTile> playedTiles() {
        ArrayList<PlayedTile> sorted = new ArrayList<>(placements);
        sorted.sort(Comparator.comparingInt(PlayedTile::row).thenComparingInt(PlayedTile::col));
        return List.copyOf(sorted);
    }

    /**
     * Returns whether a rack can supply the current buffered placements.
     *
     * @param rack rack to validate against
     * @return true when the rack covers every buffered tile
     */
    public boolean canBeSuppliedBy(RackState rack) {
        if (rack == null) {
            throw new IllegalArgumentException("rack is required");
        }
        return rack.canSupply(playedTiles());
    }

    /**
     * Materializes the base board plus pending placements as a preview board.
     *
     * @return preview board
     */
    public Board previewBoard() {
        String[][] tokens = copyTokens(baseBoard);
        for (PlayedTile tile : playedTiles()) {
            tokens[tile.row()][tile.col()] = tile.isBlank()
                ? String.valueOf(Character.toUpperCase(tile.letter()))
                : String.valueOf(Character.toLowerCase(tile.letter()));
        }
        return new Board(baseBoard.size(), tokens);
    }

    // Adds or replaces one placement after validating it against the base board.
    private void placeTile(int row, int col, char letter, boolean blank) {
        if (!baseBoard.inBounds(row, col)) {
            throw new IllegalArgumentException("placement lies outside the board");
        }
        if (baseBoard.isTile(row, col)) {
            throw new IllegalArgumentException("cannot place over an existing board tile");
        }
        if (!Character.isLetter(letter)) {
            throw new IllegalArgumentException("tile letter is required");
        }

        removeAt(row, col);
        placements.add(new PlayedTile(Character.toLowerCase(letter), row, col, blank));
    }

    // Copies raw board tokens into a fresh array for preview construction.
    private String[][] copyTokens(Board board) {
        String[][] copied = new String[board.size()][board.size()];
        for (int row = 0; row < board.size(); row++) {
            for (int col = 0; col < board.size(); col++) {
                copied[row][col] = board.get(row, col);
            }
        }
        return copied;
    }
}
