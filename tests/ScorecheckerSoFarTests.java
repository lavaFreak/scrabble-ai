import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ScorecheckerSoFarTests {
    public static void main(String[] args) throws Exception {
        testBoardParserReadsMultipleBoards();
        testScorecheckerPrintsExpectedPlayLinesSoFar();
        testScorecheckerPrintsCaseBreaks();

        System.out.println("All tests passed.");
    }

    private static void testBoardParserReadsMultipleBoards() throws IOException {
        BoardParser parser = new BoardParser();

        try (BufferedReader in = new BufferedReader(new FileReader("Resources/examples/example_score_input.txt"))) {
            Board b1 = parser.readBoard(in);
            Board b2 = parser.readBoard(in);
            Board b3 = parser.readBoard(in);
            Board b4 = parser.readBoard(in);

            check(b1 != null && b2 != null && b3 != null && b4 != null, "Expected four parsed boards");
            check(b1.size() == 7, "Expected first board size 7");
            check(b1.isEmptyBoard(), "Expected first board to be empty");
            check(b2.isEmptyBoard(), "Expected second board to be empty");
            check(b3.isEmptyBoard(), "Expected third board to be empty");
            check(!b4.isEmptyBoard(), "Expected fourth board to contain played tiles");
            check(b4.isTile(3, 3), "Expected tile at (3,3) on fourth board");
            check(b4.tileAt(3, 3) == 'c', "Expected 'c' at (3,3)");
            check(b4.tileAt(3, 4) == 'a', "Expected 'a' at (3,4)");
            check(b4.tileAt(3, 5) == 't', "Expected 't' at (3,5)");
        }
    }

    private static void testScorecheckerPrintsExpectedPlayLinesSoFar() throws Exception {
        String input = Files.readString(Path.of("Resources/examples/example_score_input.txt"));
        String output = runScorechecker(input);

        check(output.contains("play is empty"), "Expected empty play line");
        check(output.contains("play is c at (3, 3), a at (3, 4), t at (3, 5)"),
            "Expected CAT play line");
        check(output.contains("play is d at (0, 6), o at (1, 6), g at (2, 6), s at (3, 6)"),
            "Expected DOGS play line");
    }

    private static void testScorecheckerPrintsCaseBreaks() throws Exception {
        String input = Files.readString(Path.of("Resources/examples/example_score_input.txt"));
        String output = runScorechecker(input);

        check(output.startsWith("original board:"), "Output should start with original board label");
        check(output.contains("\n\noriginal board:\n"), "Expected blank line between cases");
    }

    private static String runScorechecker(String stdinText) throws Exception {
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;

        ByteArrayInputStream fakeIn = new ByteArrayInputStream(stdinText.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream capturedBytes = new ByteArrayOutputStream();
        PrintStream fakeOut = new PrintStream(capturedBytes, true, StandardCharsets.UTF_8);

        try {
            System.setIn(fakeIn);
            System.setOut(fakeOut);
            Scorechecker.main(new String[] {"Resources/dictionaries/animals.txt"});
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
            fakeOut.close();
        }

        return capturedBytes.toString(StandardCharsets.UTF_8);
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
