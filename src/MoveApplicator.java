/**
 * Author: Garion
 *
 * File purpose: build a result board by applying a solver move candidate.
 */
public class MoveApplicator {

    /**
     * Applies a move candidate to an original board and returns the resulting board.
     *
     * @param original board before the move
     * @param candidate proposed move candidate
     * @return new board containing the applied move
     */
    public Board apply(Board original, MoveCandidate candidate) {
        if (original == null) {
            throw new IllegalArgumentException("original board is required");
        }
        if (candidate == null) {
            throw new IllegalArgumentException("move candidate is required");
        }

        validateCandidateAgainstBoard(original, candidate);

        String[][] copiedTokens = copyTokens(original);
        for (PlayedTile tile : candidate.playedTiles()) {
            copiedTokens[tile.row()][tile.col()] = tileToken(tile);
        }

        Board result = new Board(original.size(), copiedTokens);
        String materializedMainWord = candidate.mainWordPlacement().text(result);
        if (!materializedMainWord.equals(candidate.mainWord().toLowerCase())) {
            throw new IllegalStateException("applied board does not match candidate main word");
        }
        return result;
    }

    // Ensures the candidate fits on the board and agrees with existing fixed tiles.
    private void validateCandidateAgainstBoard(Board original, MoveCandidate candidate) {
        for (PlayedTile tile : candidate.playedTiles()) {
            if (!original.inBounds(tile.row(), tile.col())) {
                throw new IllegalArgumentException("played tile lies outside the board");
            }
            if (original.isTile(tile.row(), tile.col())) {
                throw new IllegalArgumentException("played tile overlaps an existing tile");
            }

            char expected = expectedMainWordChar(candidate, tile.row(), tile.col());
            if (Character.toLowerCase(tile.letter()) != expected) {
                throw new IllegalArgumentException("played tile does not match main word text");
            }
        }

        for (int offset = 0; offset < candidate.mainWord().length(); offset++) {
            int row = candidate.isHorizontal() ? candidate.fixed() : candidate.start() + offset;
            int col = candidate.isHorizontal() ? candidate.start() + offset : candidate.fixed();

            if (!original.inBounds(row, col)) {
                throw new IllegalArgumentException("main word placement lies outside the board");
            }

            if (original.isTile(row, col)) {
                char boardLetter = original.tileAt(row, col);
                char expected = Character.toLowerCase(candidate.mainWord().charAt(offset));
                if (boardLetter != expected) {
                    throw new IllegalArgumentException("existing board tile conflicts with main word text");
                }
            }
        }
    }

    // Returns the expected lowercase main-word letter for one coordinate.
    private char expectedMainWordChar(MoveCandidate candidate, int row, int col) {
        int offset;
        if (candidate.isHorizontal()) {
            offset = col - candidate.start();
        } else {
            offset = row - candidate.start();
        }
        return Character.toLowerCase(candidate.mainWord().charAt(offset));
    }

    // Copies the source board tokens into a fresh 2D array.
    private String[][] copyTokens(Board board) {
        String[][] copied = new String[board.size()][board.size()];
        for (int r = 0; r < board.size(); r++) {
            for (int c = 0; c < board.size(); c++) {
                copied[r][c] = board.get(r, c);
            }
        }
        return copied;
    }

    // Encodes a newly placed tile using the board's existing single-character tile format.
    private String tileToken(PlayedTile tile) {
        char letter = Character.toLowerCase(tile.letter());
        if (tile.isBlank()) {
            return String.valueOf(Character.toUpperCase(letter));
        }
        return String.valueOf(letter);
    }
}
