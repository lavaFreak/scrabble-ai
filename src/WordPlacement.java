public class WordPlacement {
    private final boolean horizontal;
    private final int fixed;
    private final int start;
    private final int end;

    public WordPlacement(boolean horizontal, int fixed, int start, int end) {
        this.horizontal = horizontal;
        this.fixed = fixed;
        this.start = start;
        this.end = end;
    }

    public int length() {
        return end - start + 1;
    }

    public String key() {
        return (horizontal ? "H" : "V") + ":" + fixed + ":" + start + ":" + end;
    }

    public boolean isHorizontal() {
        return horizontal;
    }

    public int fixed() {
        return fixed;
    }

    public int start() {
        return start;
    }

    public int end() {
        return end;
    }

    public String text(Board board) {
        StringBuilder sb = new StringBuilder();
        if (horizontal) {
            for (int col = start; col <= end; col++) {
                sb.append(board.tileAt(fixed, col));
            }
        } else {
            for (int row = start; row <= end; row++) {
                sb.append(board.tileAt(row, fixed));
            }
        }
        return sb.toString();
    }
}
