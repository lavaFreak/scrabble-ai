import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Scorechecker {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("usage: java -jar Scorechecker.jar <dictionary-file>");
            return;
        }

        String dictionaryPath = args[0];
        // TODO: Load dictionary from dictionaryPath.

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        BoardParser parser = new BoardParser();
        boolean firstCase = true;

        while (true) {
            Board original = parser.readBoard(in);
            if (original == null) {
                break;
            }

            Board result = parser.readBoard(in);
            if (result == null) {
                break;
            }

            if (!firstCase) {
                System.out.println();
            }
            firstCase = false;

            printBoard("original board:", original);
            printBoard("result board:", result);
            String incompatibility = findIncompatibility(original, result);
            if (incompatibility != null) {
                System.out.println("Incompatible boards: " + incompatibility);
            } else {
                System.out.println(formatPlay(original, result));
            }
        }
    }

    private static void printBoard(String label, Board board) {
        System.out.println(label);
        for (int r = 0; r < board.size(); r++) {
            System.out.println(board.rowStringPadded(r));
        }
    }

    private static String formatPlay(Board original, Board result) {
        List<PlayedTile> played = collectNewTiles(original, result);
        if (played.isEmpty()) {
            return "play is empty";
        }

        StringBuilder sb = new StringBuilder("play is ");
        for (int i = 0; i < played.size(); i++) {
            PlayedTile tile = played.get(i);
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(tile.letter)
              .append(" at (")
              .append(tile.row)
              .append(", ")
              .append(tile.col)
              .append(")");
        }
        return sb.toString();
    }

    private static List<PlayedTile> collectNewTiles(Board original, Board result) {
        List<PlayedTile> played = new ArrayList<>();
        for (int r = 0; r < original.size(); r++) {
            for (int c = 0; c < original.size(); c++) {
                if (!original.isTile(r, c) && result.isTile(r, c)) {
                    played.add(new PlayedTile(result.tileAt(r, c), r, c));
                }
            }
        }
        return played;
    }

    private static String findIncompatibility(Board original, Board result) {
        if (original.size() != result.size()) {
            return "board size mismatch";
        }

        for (int r = 0; r < original.size(); r++) {
            for (int c = 0; c < original.size(); c++) {
                boolean originalTile = original.isTile(r, c);
                boolean resultTile = result.isTile(r, c);

                if (originalTile && !resultTile) {
                    return "tile removed at (" + r + ", " + c + ")";
                }

                if (originalTile && resultTile && original.tileAt(r, c) != result.tileAt(r, c)) {
                    return "tile changed at (" + r + ", " + c + ")";
                }

                if (!originalTile && !resultTile && !original.get(r, c).equals(result.get(r, c))) {
                    return "multiplier mismatch at (" + r + ", " + c + ")";
                }
            }
        }

        return null;
    }

    private static class PlayedTile {
        private final char letter;
        private final int row;
        private final int col;

        private PlayedTile(char letter, int row, int col) {
            this.letter = letter;
            this.row = row;
            this.col = col;
        }
    }
}
