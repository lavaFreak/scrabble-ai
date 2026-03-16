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
        testMoveCandidateNormalizesGeometryAndTileOrder();
        testMoveCandidateScoreKeyAndBlankHelpers();
        testMoveCandidateRejectsInvalidTiles();
        testMoveExtractorPreservesBlankTiles();
        testMoveApplicatorBuildsResultBoard();
        testMoveApplicatorPreservesBlankTiles();
        testMoveApplicatorRejectsConflictingPlacements();
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

    private static void testMoveCandidateNormalizesGeometryAndTileOrder() {
        MoveCandidate candidate = new MoveCandidate(
            new WordPlacement(true, 7, 3, 6),
            "cart",
            List.of(
                new PlayedTile('t', 7, 6),
                new PlayedTile('c', 7, 3),
                new PlayedTile('a', 7, 4)
            )
        );

        check(candidate.isHorizontal(), "Expected horizontal candidate");
        check("cart".equals(candidate.mainWord()), "Expected stored main word");
        check(candidate.tileCount() == 3, "Expected three played tiles");
        check(candidate.playedTiles().get(0).col() == 3, "Expected row-major tile ordering");
        check(candidate.startRow() == 7 && candidate.startCol() == 3, "Expected start coordinate helpers");
        check(candidate.endRow() == 7 && candidate.endCol() == 6, "Expected end coordinate helpers");
        check(candidate.coversSquare(7, 5), "Expected covered square on main word");
        check(!candidate.coversSquare(6, 5), "Expected uncovered square off main word");
        check(candidate.playsAt(7, 4), "Expected newly played tile at (7,4)");
        check(!candidate.playsAt(7, 5), "Expected existing-tile square not to count as newly played");
        check(!candidate.hasScore(), "Expected unscored candidate initially");
    }

    private static void testMoveCandidateScoreKeyAndBlankHelpers() {
        MoveCandidate candidate = new MoveCandidate(
            new WordPlacement(false, 8, 2, 5),
            "Bags",
            List.of(
                new PlayedTile('b', 2, 8, true),
                new PlayedTile('a', 3, 8),
                new PlayedTile('g', 4, 8)
            )
        );

        MoveCandidate scored = candidate.withScore(38);
        check(candidate.usesBlankTiles(), "Expected blank-tile detection");
        check(scored.hasScore(), "Expected scored candidate");
        check(scored.score() == 38, "Expected assigned score");
        check(scored.key().contains("V:8:2:5:Bags"), "Expected stable candidate key");
        check(scored.key().contains("2,8=B"), "Expected blank tile encoded distinctly in key");
    }

    private static void testMoveCandidateRejectsInvalidTiles() {
        boolean threw = false;
        try {
            new MoveCandidate(
                new WordPlacement(true, 4, 1, 3),
                "cat",
                List.of(
                    new PlayedTile('c', 4, 1),
                    new PlayedTile('a', 5, 2)
                )
            );
        } catch (IllegalArgumentException ex) {
            threw = ex.getMessage().contains("outside main word placement");
        }
        check(threw, "Expected tiles outside the placement to be rejected");
    }

    private static void testMoveExtractorPreservesBlankTiles() {
        Board original = boardFromRows(new String[] {
            ".. .. ..",
            ".. .. ..",
            ".. .. .."
        });
        Board result = boardFromRows(new String[] {
            ".. .. ..",
            ".. A ..",
            ".. .. .."
        });

        MoveExtractor extractor = new MoveExtractor();
        List<PlayedTile> played = extractor.collectNewTiles(original, result);
        check(played.size() == 1, "Expected one extracted played tile");
        check(played.get(0).letter() == 'a', "Expected lowercased extracted blank letter");
        check(played.get(0).isBlank(), "Expected blank-tile state to be preserved");
    }

    private static void testMoveApplicatorBuildsResultBoard() {
        Board original = boardFromRows(new String[] {
            ".. .. .. .. ..",
            ".. .. .. .. ..",
            ".. c .. .. ..",
            ".. .. .. .. ..",
            ".. .. .. .. .."
        });
        MoveCandidate candidate = new MoveCandidate(
            new WordPlacement(true, 2, 1, 3),
            "cat",
            List.of(
                new PlayedTile('a', 2, 2),
                new PlayedTile('t', 2, 3)
            )
        );

        MoveApplicator applicator = new MoveApplicator();
        Board result = applicator.apply(original, candidate);

        check(!original.isTile(2, 2), "Expected original board to remain unchanged");
        check(result.isTile(2, 1), "Expected original tile on main word to remain present");
        check(result.tileAt(2, 1) == 'c', "Expected original board letter to remain");
        check(result.tileAt(2, 2) == 'a', "Expected applied tile at (2,2)");
        check(result.tileAt(2, 3) == 't', "Expected applied tile at (2,3)");
        check("cat".equals(candidate.mainWordPlacement().text(result)), "Expected materialized main word");
    }

    private static void testMoveApplicatorPreservesBlankTiles() {
        Board original = boardFromRows(new String[] {
            ".. .. .. ..",
            ".. .. .. ..",
            ".. .. .. ..",
            ".. .. .. .."
        });
        MoveCandidate candidate = new MoveCandidate(
            new WordPlacement(false, 1, 0, 2),
            "Bag",
            List.of(
                new PlayedTile('b', 0, 1, true),
                new PlayedTile('a', 1, 1),
                new PlayedTile('g', 2, 1)
            )
        );

        MoveApplicator applicator = new MoveApplicator();
        Board result = applicator.apply(original, candidate);

        check("B".equals(result.get(0, 1)), "Expected uppercase token for blank tile");
        check(result.isBlankTile(0, 1), "Expected blank tile state on applied board");
        check(result.tileAt(0, 1) == 'b', "Expected lowercased letter lookup for blank tile");
    }

    private static void testMoveApplicatorRejectsConflictingPlacements() {
        Board original = boardFromRows(new String[] {
            ".. .. .. ..",
            ".. x .. ..",
            ".. .. .. ..",
            ".. .. .. .."
        });
        MoveCandidate candidate = new MoveCandidate(
            new WordPlacement(true, 1, 1, 3),
            "cat",
            List.of(
                new PlayedTile('a', 1, 2),
                new PlayedTile('t', 1, 3)
            )
        );

        MoveApplicator applicator = new MoveApplicator();
        boolean threw = false;
        try {
            applicator.apply(original, candidate);
        } catch (IllegalArgumentException ex) {
            threw = ex.getMessage().contains("conflicts with main word text");
        }
        check(threw, "Expected conflicting existing tile rejection");
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

    private static Board boardFromRows(String[] rows) {
        int size = rows.length;
        String[][] tokens = new String[size][size];
        for (int r = 0; r < size; r++) {
            String[] parts = rows[r].trim().split("\\s+");
            if (parts.length != size) {
                throw new IllegalArgumentException("Invalid row width for synthetic board at row " + r);
            }
            System.arraycopy(parts, 0, tokens[r], 0, size);
        }
        return new Board(size, tokens);
    }
}
