/**
 * Author: Garion
 *
 * File purpose: compute legal move scores for scorer Part 1.
 */
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Scorer {

    /**
     * Computes total score for one legal move.
     *
     * @param original board before move (premium squares source)
     * @param result board after move (tile letters source)
     * @param played newly placed tiles
     * @param words all formed words for this move
     * @return total turn score
     */
    public int computeScore(Board original, Board result, List<PlayedTile> played, List<WordPlacement> words) {
        Set<String> newlyPlayed = new HashSet<>();
        for (PlayedTile tile : played) {
            newlyPlayed.add(coordKey(tile.row(), tile.col()));
        }

        int total = 0;
        for (WordPlacement word : words) {
            int wordSum = 0;
            int wordMultiplier = 1;

            if (word.isHorizontal()) {
                for (int col = word.start(); col <= word.end(); col++) {
                    int row = word.fixed();
                    int letterValue = letterPoints(result, row, col);

                    if (newlyPlayed.contains(coordKey(row, col))) {
                        letterValue *= original.letterMultiplierAt(row, col);
                        wordMultiplier *= original.wordMultiplierAt(row, col);
                    }
                    wordSum += letterValue;
                }
            } else {
                for (int row = word.start(); row <= word.end(); row++) {
                    int col = word.fixed();
                    int letterValue = letterPoints(result, row, col);

                    if (newlyPlayed.contains(coordKey(row, col))) {
                        letterValue *= original.letterMultiplierAt(row, col);
                        wordMultiplier *= original.wordMultiplierAt(row, col);
                    }
                    wordSum += letterValue;
                }
            }
            total += wordSum * wordMultiplier;
        }

        if (played.size() == 7) {
            total += 50;
        }
        return total;
    }

    // Returns standard face value for tile letter (blank tiles score 0).
    private int letterPoints(Board board, int row, int col) {
        if (board.isBlankTile(row, col)) {
            return 0;
        }
        return TileValues.faceValue(board.tileAt(row, col));
    }

    // Small coordinate key utility for quick set membership.
    private String coordKey(int row, int col) {
        return row + "," + col;
    }
}
