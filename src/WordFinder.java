/**
 * Author: Garion
 *
 * File purpose: discover unique words formed by newly placed tiles.
 */
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class WordFinder {

    public List<WordPlacement> collectFormedWordPlacements(Board result, List<PlayedTile> played) {
        Set<String> seen = new LinkedHashSet<>();
        List<WordPlacement> words = new ArrayList<>();
        for (PlayedTile tile : played) {
            WordPlacement horizontal = horizontalWordPlacement(result, tile.row(), tile.col());
            if (horizontal.length() >= 2 && seen.add(horizontal.key())) {
                words.add(horizontal);
            }

            WordPlacement vertical = verticalWordPlacement(result, tile.row(), tile.col());
            if (vertical.length() >= 2 && seen.add(vertical.key())) {
                words.add(vertical);
            }
        }
        return words;
    }

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
}
