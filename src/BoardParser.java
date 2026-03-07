import java.io.BufferedReader;
import java.io.IOException;

public class BoardParser {

    public Board readBoard(BufferedReader in) throws IOException {
        String sizeLine = readNextNonEmptyLine(in);
        if (sizeLine == null) {
            return null;
        }

        int size = Integer.parseInt(sizeLine.trim());
        String[][] tokens = new String[size][size];

        for (int r = 0; r < size; r++) {
            String line = in.readLine();
            if (line == null) {
                return null;
            }

            String[] parts = line.trim().split("\\s+");
            if (parts.length != size) {
                throw new IllegalArgumentException(
                    "Invalid row " + r + ": expected " + size + " tokens, got " + parts.length
                );
            }

            System.arraycopy(parts, 0, tokens[r], 0, size);
        }

        return new Board(size, tokens);
    }

    private String readNextNonEmptyLine(BufferedReader in) throws IOException {
        String line;
        while ((line = in.readLine()) != null) {
            if (!line.trim().isEmpty()) {
                return line;
            }
        }
        return null;
    }
}
