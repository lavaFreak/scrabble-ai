/**
 * Author: Garion
 *
 * File purpose: regression coverage for scorer Part 1 behavior.
 */
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
import java.util.List;

public class ScorecheckerSoFarTests {
    public static void main(String[] args) throws Exception {
        testBoardParserReadsMultipleBoards();
        testScorecheckerPrintsExpectedPlayLinesSoFar();
        testScorecheckerPrintsCaseBreaks();
        testScorecheckerPrintsIncompatibleBoardMessages();
        testScorecheckerPrintsLegalAndIllegalLines();
        testScoreLinesForKnownCases();
        testRiskCasesWithSyntheticBoards();
        testCompatibilitySizeMismatch();
        testMoveExtractorOrdering();
        testScorerBranchCoverage();
        testClassProvidedScoreAndIllegalFiles();

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

    private static void testScorecheckerPrintsIncompatibleBoardMessages() throws Exception {
        String input = Files.readString(Path.of("Resources/examples/example_score_input.txt"));
        String output = runScorechecker(input);

        check(output.contains("Incompatible boards: multiplier mismatch at (0, 7)"),
            "Expected multiplier mismatch incompatibility message");
        check(output.contains("Incompatible boards: tile removed at (3, 14)"),
            "Expected tile removed incompatibility message");
    }

    private static void testScorecheckerPrintsLegalAndIllegalLines() throws Exception {
        String input = Files.readString(Path.of("Resources/examples/example_score_input.txt"));
        String output = runScorechecker(input);

        check(output.contains("play is c at (3, 3), a at (3, 4), t at (3, 5)\nplay is legal"),
            "Expected CAT move to be legal");
        check(output.contains("play is d at (0, 6), o at (1, 6), g at (2, 6), x at (3, 6)\nplay is not legal"),
            "Expected DOGX move to be illegal");
        check(output.contains("play is empty\nplay is not legal"),
            "Expected empty play to be illegal");
    }

    private static void testRiskCasesWithSyntheticBoards() throws Exception {
        Path dict = writeTempDictionary(List.of("cat", "cats", "dog", "at"));

        // 1) Incompatibility: tile changed.
        String inputTileChanged = singleCaseInput(
            new String[] {
                ".. .. .. .. ..",
                ".. .. .. .. ..",
                ".. .. c .. ..",
                ".. .. .. .. ..",
                ".. .. .. .. .."
            },
            new String[] {
                ".. .. .. .. ..",
                ".. .. .. .. ..",
                ".. .. d .. ..",
                ".. .. .. .. ..",
                ".. .. .. .. .."
            }
        );
        String out1 = runScorechecker(inputTileChanged, dict.toString());
        check(out1.contains("Incompatible boards: tile changed at (2, 2)"),
            "Expected tile-changed incompatibility");

        // 2) Illegal: diagonal placement (not one line).
        String inputDiagonal = singleCaseInput(
            empty5(),
            new String[] {
                ".. .. .. .. ..",
                ".. .. .. .. ..",
                ".. .. c .. ..",
                ".. .. .. a ..",
                ".. .. .. .. .."
            }
        );
        String out2 = runScorechecker(inputDiagonal, dict.toString());
        check(out2.contains("play is c at (2, 2), a at (3, 3)\nplay is not legal"),
            "Expected diagonal placement to be illegal");

        // 3) Illegal: non-contiguous placement with gap.
        String inputGap = singleCaseInput(
            empty5(),
            new String[] {
                ".. .. .. .. ..",
                ".. .. .. .. ..",
                ".c .. .t .. ..",
                ".. .. .. .. ..",
                ".. .. .. .. .."
            }
        );
        String out3 = runScorechecker(inputGap, dict.toString());
        check(out3.contains("play is c at (2, 0), t at (2, 2)\nplay is not legal"),
            "Expected gapped placement to be illegal");

        // 4) Illegal: first move does not cover center.
        String inputMissCenter = singleCaseInput(
            empty5(),
            new String[] {
                "a t .. .. ..",
                ".. .. .. .. ..",
                ".. .. .. .. ..",
                ".. .. .. .. ..",
                ".. .. .. .. .."
            }
        );
        String out4 = runScorechecker(inputMissCenter, dict.toString());
        check(out4.contains("play is a at (0, 0), t at (0, 1)\nplay is not legal"),
            "Expected first move missing center to be illegal");

        // 5) Illegal: move disconnected from existing tiles.
        String inputDisconnected = singleCaseInput(
            new String[] {
                ".. .. .. .. ..",
                ".. .. .. .. ..",
                ".. c a t ..",
                ".. .. .. .. ..",
                ".. .. .. .. .."
            },
            new String[] {
                "d o g .. ..",
                ".. .. .. .. ..",
                ".. c a t ..",
                ".. .. .. .. ..",
                ".. .. .. .. .."
            }
        );
        String out5 = runScorechecker(inputDisconnected, dict.toString());
        check(out5.contains("play is d at (0, 0), o at (0, 1), g at (0, 2)\nplay is not legal"),
            "Expected disconnected move to be illegal");

        // 6) Illegal: dictionary rejection (word not in dict).
        String inputBadWord = singleCaseInput(
            empty5(),
            new String[] {
                ".. .. .. .. ..",
                ".. .. .. .. ..",
                ".. z z z ..",
                ".. .. .. .. ..",
                ".. .. .. .. .."
            }
        );
        String out6 = runScorechecker(inputBadWord, dict.toString());
        check(out6.contains("play is z at (2, 1), z at (2, 2), z at (2, 3)\nplay is not legal"),
            "Expected unknown word to be illegal");

        // 7) Illegal: cross word formed is invalid.
        String inputBadCross = singleCaseInput(
            new String[] {
                ".. .. .. .. ..",
                ".. .. .. .. .q",
                ".. c a t ..",
                ".. .. .. .. .z",
                ".. .. .. .. .."
            },
            new String[] {
                ".. .. .. .. ..",
                ".. .. .. .. .q",
                ".. c a t s",
                ".. .. .. .. .z",
                ".. .. .. .. .."
            }
        );
        String out7 = runScorechecker(inputBadCross, dict.toString());
        check(out7.contains("play is s at (2, 4)\nplay is not legal"),
            "Expected invalid cross word to be illegal");

        // 8) Legal: add one connecting tile to complete CAT.
        String inputLegalBridge = singleCaseInput(
            new String[] {
                ".. .. .. .. ..",
                ".. .. .. .. ..",
                ".. c .. t ..",
                ".. .. .. .. ..",
                ".. .. .. .. .."
            },
            new String[] {
                ".. .. .. .. ..",
                ".. .. .. .. ..",
                ".. c a t ..",
                ".. .. .. .. ..",
                ".. .. .. .. .."
            }
        );
        String out8 = runScorechecker(inputLegalBridge, dict.toString());
        check(out8.contains("play is a at (2, 2)\nplay is legal"),
            "Expected bridge move to be legal");
    }

    private static void testScoreLinesForKnownCases() throws Exception {
        String input = Files.readString(Path.of("Resources/examples/example_score_input.txt"));
        String output = runScorechecker(input, "Resources/dictionaries/dictionary.txt");

        check(output.contains("play is c at (3, 3), a at (3, 4), t at (3, 5)\nplay is legal\nscore is 10"),
            "Expected CAT score to be 10");
        check(output.contains("play is d at (0, 6), o at (1, 6), g at (2, 6), s at (3, 6)\nplay is legal\nscore is 48"),
            "Expected DOGS score to be 48");
    }

    private static void testCompatibilitySizeMismatch() {
        Board original = boardFromRows(new String[] {
            ".. .. ..",
            ".. .. ..",
            ".. .. .."
        });
        Board result = boardFromRows(new String[] {
            ".. .. .. .. ..",
            ".. .. .. .. ..",
            ".. .. .. .. ..",
            ".. .. .. .. ..",
            ".. .. .. .. .."
        });

        CompatibilityChecker checker = new CompatibilityChecker();
        String mismatch = checker.findIncompatibility(original, result);
        check("board size mismatch".equals(mismatch), "Expected board size mismatch branch");
    }

    private static void testMoveExtractorOrdering() {
        Board original = boardFromRows(new String[] {
            ".. .. .. .. ..",
            ".. .. .. .. ..",
            ".. .. .. .. ..",
            ".. .. .. .. ..",
            ".. .. .. .. .."
        });
        Board result = boardFromRows(new String[] {
            ".. b .. .. ..",
            ".. .. .. c ..",
            ".. .. a .. ..",
            ".. .. .. .. ..",
            ".. .. .. .. .."
        });

        MoveExtractor extractor = new MoveExtractor();
        List<PlayedTile> played = extractor.collectNewTiles(original, result);
        check(played.size() == 3, "Expected 3 played tiles");
        check(played.get(0).row() == 0 && played.get(0).col() == 1 && played.get(0).letter() == 'b',
            "Expected row-major first tile");
        check(played.get(1).row() == 1 && played.get(1).col() == 3 && played.get(1).letter() == 'c',
            "Expected row-major second tile");
        check(played.get(2).row() == 2 && played.get(2).col() == 2 && played.get(2).letter() == 'a',
            "Expected row-major third tile");
    }

    private static void testScorerBranchCoverage() {
        MoveExtractor extractor = new MoveExtractor();
        WordFinder finder = new WordFinder();
        Scorer scorer = new Scorer();

        // Letter multiplier branch.
        Board originalLetterMult = boardFromRows(new String[] {
            ".. .. .. .. ..",
            ".. .. .. .. ..",
            ".. .. .2 .. ..",
            ".. .. .. .. ..",
            ".. .. .. .. .."
        });
        Board resultLetterMult = boardFromRows(new String[] {
            ".. .. .. .. ..",
            ".. .. .. .. ..",
            ".. .. a t ..",
            ".. .. .. .. ..",
            ".. .. .. .. .."
        });
        int scoreLetterMult = scoreFor(originalLetterMult, resultLetterMult, extractor, finder, scorer);
        check(scoreLetterMult == 3, "Expected letter-multiplier score 3");

        // Word multiplier branch.
        Board originalWordMult = boardFromRows(new String[] {
            ".. .. .. .. ..",
            ".. .. .. .. ..",
            ".. .. 2. .. ..",
            ".. .. .. .. ..",
            ".. .. .. .. .."
        });
        Board resultWordMult = boardFromRows(new String[] {
            ".. .. .. .. ..",
            ".. .. .. .. ..",
            ".. .. a t ..",
            ".. .. .. .. ..",
            ".. .. .. .. .."
        });
        int scoreWordMult = scoreFor(originalWordMult, resultWordMult, extractor, finder, scorer);
        check(scoreWordMult == 4, "Expected word-multiplier score 4");

        // Blank tile branch.
        Board originalBlank = boardFromRows(new String[] {
            ".. .. .. .. ..",
            ".. .. .. .. ..",
            ".. .. .. .. ..",
            ".. .. .. .. ..",
            ".. .. .. .. .."
        });
        Board resultBlank = boardFromRows(new String[] {
            ".. .. .. .. ..",
            ".. .. .. .. ..",
            ".. .. A t ..",
            ".. .. .. .. ..",
            ".. .. .. .. .."
        });
        int scoreBlank = scoreFor(originalBlank, resultBlank, extractor, finder, scorer);
        check(scoreBlank == 1, "Expected blank-tile score 1");

        // Multiple words with shared newly-played tile and letter multiplier on that tile.
        Board originalCross = boardFromRows(new String[] {
            ".. .. .. .. ..",
            ".. .. c .. ..",
            ".. d .2 g ..",
            ".. .. t .. ..",
            ".. .. .. .. .."
        });
        Board resultCross = boardFromRows(new String[] {
            ".. .. .. .. ..",
            ".. .. c .. ..",
            ".. d o g ..",
            ".. .. t .. ..",
            ".. .. .. .. .."
        });
        int scoreCross = scoreFor(originalCross, resultCross, extractor, finder, scorer);
        check(scoreCross == 12, "Expected cross-word combined score 12");

        // Bingo branch (+50).
        Board originalBingo = boardFromRows(new String[] {
            ".. .. .. .. .. .. ..",
            ".. .. .. .. .. .. ..",
            ".. .. .. .. .. .. ..",
            ".. .. .. .. .. .. ..",
            ".. .. .. .. .. .. ..",
            ".. .. .. .. .. .. ..",
            ".. .. .. .. .. .. .."
        });
        Board resultBingo = boardFromRows(new String[] {
            ".. .. .. .. .. .. ..",
            ".. .. .. .. .. .. ..",
            ".. .. .. .. .. .. ..",
            "a a a a a a a",
            ".. .. .. .. .. .. ..",
            ".. .. .. .. .. .. ..",
            ".. .. .. .. .. .. .."
        });
        int scoreBingo = scoreFor(originalBingo, resultBingo, extractor, finder, scorer);
        check(scoreBingo == 57, "Expected bingo score 57");
    }

    private static void testClassProvidedScoreAndIllegalFiles() throws Exception {
        String scoreInput = Files.readString(Path.of("Resources/examples/score.txt"));
        String scoreOutput = runScorechecker(scoreInput, "Resources/dictionaries/dictionary.txt");
        check(!scoreOutput.contains("Incompatible boards:"), "score.txt should not contain incompatible boards");
        check(!scoreOutput.contains("play is not legal"), "score.txt should contain only legal plays");
        check(scoreOutput.contains("score is "), "score.txt should include scoring output");

        String illegalInput = Files.readString(Path.of("Resources/examples/illegal.txt"));
        String illegalOutput = runScorechecker(illegalInput, "Resources/dictionaries/dictionary.txt");
        check(!illegalOutput.contains("play is legal"), "illegal.txt should not contain legal plays");
        check(!illegalOutput.contains("score is "), "illegal.txt should not include scoring output");
    }

    private static String runScorechecker(String stdinText) throws Exception {
        return runScorechecker(stdinText, "Resources/dictionaries/animals.txt");
    }

    private static String runScorechecker(String stdinText, String dictionaryPath) throws Exception {
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;

        ByteArrayInputStream fakeIn = new ByteArrayInputStream(stdinText.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream capturedBytes = new ByteArrayOutputStream();
        PrintStream fakeOut = new PrintStream(capturedBytes, true, StandardCharsets.UTF_8);

        try {
            System.setIn(fakeIn);
            System.setOut(fakeOut);
            Scorechecker.main(new String[] {dictionaryPath});
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

    private static Path writeTempDictionary(List<String> words) throws IOException {
        Path path = Files.createTempFile("scorechecker-test-dict-", ".txt");
        Files.writeString(path, String.join("\n", words) + "\n", StandardCharsets.UTF_8);
        path.toFile().deleteOnExit();
        return path;
    }

    private static String[] empty5() {
        return new String[] {
            ".. .. .. .. ..",
            ".. .. .. .. ..",
            ".. .. .. .. ..",
            ".. .. .. .. ..",
            ".. .. .. .. .."
        };
    }

    private static String singleCaseInput(String[] originalRows, String[] resultRows) {
        int n = originalRows.length;
        StringBuilder sb = new StringBuilder();
        sb.append(n).append('\n');
        for (String row : originalRows) {
            sb.append(row).append('\n');
        }
        sb.append(n).append('\n');
        for (String row : resultRows) {
            sb.append(row).append('\n');
        }
        return sb.toString();
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

    private static int scoreFor(
        Board original,
        Board result,
        MoveExtractor extractor,
        WordFinder finder,
        Scorer scorer
    ) {
        List<PlayedTile> played = extractor.collectNewTiles(original, result);
        List<WordPlacement> words = finder.collectFormedWordPlacements(result, played);
        return scorer.computeScore(original, result, played, words);
    }
}
