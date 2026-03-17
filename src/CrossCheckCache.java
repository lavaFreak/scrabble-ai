/**
 * Author: Garion
 *
 * File purpose: cache perpendicular cross-check legality for solver placements.
 */
public class CrossCheckCache {
    private static final int ALL_LETTERS_MASK = (1 << 26) - 1;

    private final int[][] horizontalMasks;
    private final int[][] verticalMasks;

    /**
     * Precomputes allowed letters for every empty square on the board in both orientations.
     *
     * @param board current board
     * @param dictionary solver dictionary
     */
    public CrossCheckCache(Board board, Dictionary dictionary) {
        if (board == null) {
            throw new IllegalArgumentException("board is required");
        }
        if (dictionary == null) {
            throw new IllegalArgumentException("dictionary is required");
        }

        int size = board.size();
        horizontalMasks = new int[size][size];
        verticalMasks = new int[size][size];

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (board.isTile(row, col)) {
                    continue;
                }
                horizontalMasks[row][col] = buildMask(board, dictionary, true, row, col);
                verticalMasks[row][col] = buildMask(board, dictionary, false, row, col);
            }
        }
    }

    /**
     * Returns whether a letter can be placed at a square without breaking the perpendicular word.
     *
     * @param horizontalMainWord true when the main word is horizontal
     * @param row row index
     * @param col column index
     * @param letter candidate lowercase letter
     * @return true when the perpendicular cross word is valid
     */
    public boolean allowsLetter(boolean horizontalMainWord, int row, int col, char letter) {
        int letterIndex = letterIndex(letter);
        if (letterIndex < 0) {
            return false;
        }

        int mask = horizontalMainWord ? horizontalMasks[row][col] : verticalMasks[row][col];
        return (mask & (1 << letterIndex)) != 0;
    }

    // Builds a 26-bit mask of allowed letters for one empty square and main-word orientation.
    private int buildMask(Board board, Dictionary dictionary, boolean horizontalMainWord, int row, int col) {
        int rowStep = horizontalMainWord ? 1 : 0;
        int colStep = horizontalMainWord ? 0 : 1;

        String prefix = collectBackward(board, row, col, rowStep, colStep);
        String suffix = collectForward(board, row, col, rowStep, colStep);
        if (prefix.isEmpty() && suffix.isEmpty()) {
            return ALL_LETTERS_MASK;
        }

        int mask = 0;
        StringBuilder word = new StringBuilder(prefix.length() + 1 + suffix.length());
        word.append(prefix).append(' ').append(suffix);

        for (char letter = 'a'; letter <= 'z'; letter++) {
            word.setCharAt(prefix.length(), letter);
            if (dictionary.contains(word.toString())) {
                mask |= 1 << (letter - 'a');
            }
        }
        return mask;
    }

    // Collects contiguous existing tiles immediately before a square on one axis.
    private String collectBackward(Board board, int row, int col, int rowStep, int colStep) {
        StringBuilder letters = new StringBuilder();
        int currentRow = row - rowStep;
        int currentCol = col - colStep;

        while (board.inBounds(currentRow, currentCol) && board.isTile(currentRow, currentCol)) {
            letters.insert(0, board.tileAt(currentRow, currentCol));
            currentRow -= rowStep;
            currentCol -= colStep;
        }

        return letters.toString();
    }

    // Collects contiguous existing tiles immediately after a square on one axis.
    private String collectForward(Board board, int row, int col, int rowStep, int colStep) {
        StringBuilder letters = new StringBuilder();
        int currentRow = row + rowStep;
        int currentCol = col + colStep;

        while (board.inBounds(currentRow, currentCol) && board.isTile(currentRow, currentCol)) {
            letters.append(board.tileAt(currentRow, currentCol));
            currentRow += rowStep;
            currentCol += colStep;
        }

        return letters.toString();
    }

    // Maps an English lowercase letter to its 0..25 bit index.
    private int letterIndex(char letter) {
        char lowered = Character.toLowerCase(letter);
        if (lowered < 'a' || lowered > 'z') {
            return -1;
        }
        return lowered - 'a';
    }
}
