/**
 * Author: Garion
 *
 * File purpose: regression coverage for Part 2 groundwork.
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Part2FoundationTests {
    /**
     * Executes Part 2 foundation tests.
     *
     * @param args unused
     * @throws Exception if setup or assertions fail
     */
    public static void main(String[] args) throws Exception {
        testTrieInterfaceMethods();
        testTrieLoadsWordsFromFile();
        testDictionaryContainsAndPrefixesCaseInsensitively();
        testDictionaryCursorWalksTrieIncrementally();
        testSolverInputParserReadsExampleCases();
        testSolverInputParserNormalizesTray();
        testSolverInputParserRejectsInvalidTrayCharacters();

        System.out.println("All tests passed.");
    }

    private static void testTrieInterfaceMethods() {
        TrieInterface trie = new Trie();

        trie.insert("Car");
        trie.insert("cart");
        trie.insert("carbon");
        trie.insert("dog");

        check(trie.search("car"), "Expected search to find normalized word");
        check(trie.search("CART"), "Expected case-insensitive search");
        check(!trie.search("ca"), "Expected prefix not to count as a full word");
        check(trie.startsWith("ca"), "Expected valid prefix");
        check(trie.startsWith("CAR"), "Expected case-insensitive prefix");
        check(!trie.startsWith("cz"), "Expected invalid prefix to fail");

        Set<String> expected = new LinkedHashSet<>(List.of("car", "carbon", "cart"));
        check(expected.equals(trie.getWordsWithPrefix("car")),
            "Expected deterministic prefix word collection");
        check(trie.getWordsWithPrefix("zzz").isEmpty(), "Expected empty prefix result set");
    }

    private static void testTrieLoadsWordsFromFile() throws Exception {
        Path dict = writeTempDictionary(List.of("cat", "cater", "dog", "zoo"));
        TrieInterface trie = new Trie();
        trie.loadFromFile(dict.toString());

        check(trie.search("cat"), "Expected loaded word cat");
        check(trie.search("DOG"), "Expected case-insensitive loaded word dog");
        check(trie.startsWith("zo"), "Expected loaded prefix zo");
        check(new LinkedHashSet<>(List.of("cat", "cater")).equals(trie.getWordsWithPrefix("cat")),
            "Expected prefix words from loaded file");
    }

    private static void testDictionaryContainsAndPrefixesCaseInsensitively() throws Exception {
        Path dict = writeTempDictionary(List.of("cat", "cater", "dog", "zoo"));
        Dictionary dictionary = Dictionary.fromFile(dict.toString());

        check(dictionary.contains("cat"), "Expected exact lowercase word");
        check(dictionary.contains("CATER"), "Expected case-insensitive full-word lookup");
        check(!dictionary.contains("ca"), "Expected prefix alone not to count as a word");
        check(!dictionary.contains("cat1"), "Expected non-letter word lookup to fail");
        check(dictionary.hasPrefix("ca"), "Expected valid prefix");
        check(dictionary.hasPrefix("CAT"), "Expected case-insensitive prefix");
        check(dictionary.hasPrefix(""), "Expected empty prefix to match trie root");
        check(!dictionary.hasPrefix("cab"), "Expected missing prefix");
        check(!dictionary.hasPrefix("do-"), "Expected invalid prefix to fail");
    }

    private static void testDictionaryCursorWalksTrieIncrementally() throws Exception {
        Path dict = writeTempDictionary(List.of("cat", "cater", "dog"));
        Dictionary dictionary = Dictionary.fromFile(dict.toString());

        Dictionary.Cursor root = dictionary.rootCursor();
        Dictionary.Cursor c = dictionary.advance(root, 'c');
        Dictionary.Cursor ca = dictionary.advance(c, 'A');
        Dictionary.Cursor cat = dictionary.advance(ca, 't');
        Dictionary.Cursor cate = dictionary.advance(cat, 'e');

        check(c != null, "Expected trie branch for c");
        check(ca != null, "Expected trie branch for ca");
        check(cat != null, "Expected trie branch for cat");
        check(dictionary.isWord(cat), "Expected cat to be a full word");
        check(cate != null, "Expected trie branch for cate");
        check(!dictionary.isWord(cate), "Expected cate to be only a prefix");
        check(dictionary.advance(cat, 'x') == null, "Expected missing branch to return null");
    }

    private static void testSolverInputParserReadsExampleCases() throws Exception {
        String input = Files.readString(Path.of("Resources/examples/example_input.txt"));
        SolverInputParser parser = new SolverInputParser();

        try (BufferedReader in = new BufferedReader(new StringReader(input))) {
            SolverCase first = parser.readCase(in);
            SolverCase second = parser.readCase(in);
            SolverCase third = parser.readCase(in);
            SolverCase fourth = parser.readCase(in);
            SolverCase eof = parser.readCase(in);

            check(first != null && second != null && third != null && fourth != null,
                "Expected four solver cases");
            check(eof == null, "Expected EOF after the example cases");

            check(first.board().size() == 15, "Expected first example board size 15");
            check("le*mdoe".equals(first.tray()), "Expected first tray");

            check(second.board().size() == 7, "Expected second example board size 7");
            check("toloeri".equals(second.tray()), "Expected second tray");

            check(third.board().size() == 21, "Expected third example board size 21");
            check("ntnbtoi".equals(third.tray()), "Expected third tray");

            check(fourth.board().size() == 15, "Expected fourth example board size 15");
            check("dgos*ie".equals(fourth.tray()), "Expected fourth tray");
            check(fourth.board().isTile(7, 5), "Expected CAT tiles on fourth example board");
            check(fourth.board().tileAt(7, 5) == 'c', "Expected C on fourth example board");
        }
    }

    private static void testSolverInputParserNormalizesTray() throws Exception {
        String input = ""
            + "3\n"
            + ".. .. ..\n"
            + ".. .. ..\n"
            + ".. .. ..\n"
            + "Ab*Z\n";

        SolverInputParser parser = new SolverInputParser();
        try (BufferedReader in = new BufferedReader(new StringReader(input))) {
            SolverCase parsed = parser.readCase(in);
            check(parsed != null, "Expected parsed solver case");
            check("ab*z".equals(parsed.tray()), "Expected lowercase tray normalization");
        }
    }

    private static void testSolverInputParserRejectsInvalidTrayCharacters() throws Exception {
        String input = ""
            + "3\n"
            + ".. .. ..\n"
            + ".. .. ..\n"
            + ".. .. ..\n"
            + "ab-*\n";

        SolverInputParser parser = new SolverInputParser();
        try (BufferedReader in = new BufferedReader(new StringReader(input))) {
            boolean threw = false;
            try {
                parser.readCase(in);
            } catch (IllegalArgumentException ex) {
                threw = ex.getMessage().contains("Invalid tray character");
            }
            check(threw, "Expected invalid tray character rejection");
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static Path writeTempDictionary(List<String> words) throws IOException {
        Path path = Files.createTempFile("solver-foundation-dict-", ".txt");
        Files.writeString(path, String.join("\n", words) + "\n", StandardCharsets.UTF_8);
        path.toFile().deleteOnExit();
        return path;
    }
}
