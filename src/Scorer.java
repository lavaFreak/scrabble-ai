import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Scorer {

    public int computeScore(Board original, Board result, List<PlayedTile> played, List<WordPlacement> words) {
        Set<String> newlyPlayed = new HashSet<>();
        for (PlayedTile tile : played) {
            newlyPlayed.add(coordKey(tile.row, tile.col));
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

    private int letterPoints(Board board, int row, int col) {
        if (board.isBlankTile(row, col)) {
            return 0;
        }
        return switch (board.tileAt(row, col)) {
            case 'a', 'e', 'i', 'l', 'n', 'o', 'r', 's', 't', 'u' -> 1;
            case 'd', 'g' -> 2;
            case 'b', 'c', 'm', 'p' -> 3;
            case 'f', 'h', 'v', 'w', 'y' -> 4;
            case 'k' -> 5;
            case 'j', 'x' -> 8;
            case 'q', 'z' -> 10;
            default -> 0;
        };
    }

    private String coordKey(int row, int col) {
        return row + "," + col;
    }
}
