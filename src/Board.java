import java.util.ArrayList;
import java.util.List;

public class Board {
    private final int size;
    private final String[][] tokens;

    public Board(int size, String[][] tokens) {
        this.size = size;
        this.tokens = tokens;
    }

    public int size() {
        return size;
    }

    public String get(int row, int col) {
        return tokens[row][col];
    }

    public boolean inBounds(int row, int col) {
        return row >= 0 && row < size && col >= 0 && col < size;
    }

    public boolean isTile(int row, int col) {
        return isTileToken(get(row, col));
    }

    public char tileAt(int row, int col) {
        String token = get(row, col);
        if (!isTileToken(token)) {
            throw new IllegalStateException("No tile at (" + row + ", " + col + ")");
        }
        return Character.toLowerCase(extractTileChar(token));
    }

    public String rowString(int row) {
        StringBuilder sb = new StringBuilder();
        for (int c = 0; c < size; c++) {
            if (c > 0) {
                sb.append(' ');
            }
            sb.append(tokens[row][c]);
        }
        return sb.toString();
    }

    public boolean sameTileAt(Board other, int row, int col) {
        boolean mine = isTile(row, col);
        boolean theirs = other.isTile(row, col);
        if (!mine && !theirs) {
            return true;
        }
        if (mine != theirs) {
            return false;
        }
        return tileAt(row, col) == other.tileAt(row, col);
    }

    public boolean isEmptyBoard() {
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (isTile(r, c)) {
                    return false;
                }
            }
        }
        return true;
    }

    public List<int[]> changedPositions(Board other) {
        List<int[]> changed = new ArrayList<>();
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (!tokens[r][c].equals(other.tokens[r][c])) {
                    changed.add(new int[] {r, c});
                }
            }
        }
        return changed;
    }

    public int wordMultiplierAt(int row, int col) {
        String token = get(row, col);
        if (token.length() == 2 && Character.isDigit(token.charAt(0)) && token.charAt(1) == '.') {
            return Character.getNumericValue(token.charAt(0));
        }
        return 1;
    }

    public int letterMultiplierAt(int row, int col) {
        String token = get(row, col);
        if (token.length() == 2 && token.charAt(0) == '.' && Character.isDigit(token.charAt(1))) {
            return Character.getNumericValue(token.charAt(1));
        }
        return 1;
    }

    private static boolean isTileToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        if (token.length() == 1) {
            return Character.isLetter(token.charAt(0));
        }
        if (token.length() == 2) {
            return token.charAt(0) == '.' && Character.isLetter(token.charAt(1));
        }
        return false;
    }

    private static char extractTileChar(String token) {
        if (token.length() == 1) {
            return token.charAt(0);
        }
        return token.charAt(1);
    }
}
