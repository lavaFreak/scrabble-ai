/**
 * Author: Garion
 *
 * File purpose: generate legal solver move candidates from a board and rack.
 */
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class CandidateGenerator {
    private static final Comparator<MoveCandidate> CANDIDATE_ORDER = Comparator
        .comparingInt(MoveCandidate::score)
        .reversed()
        .thenComparing(MoveCandidate::key);

    private final Dictionary dictionary;
    private final AnchorFinder anchorFinder = new AnchorFinder();
    private final MoveApplicator moveApplicator = new MoveApplicator();
    private final WordFinder wordFinder = new WordFinder();
    private final LegalityChecker legalityChecker;
    private final Scorer scorer = new Scorer();

    /**
     * Creates a candidate generator backed by the shared solver dictionary.
     *
     * @param dictionary solver dictionary
     */
    public CandidateGenerator(Dictionary dictionary) {
        if (dictionary == null) {
            throw new IllegalArgumentException("dictionary is required");
        }
        this.dictionary = dictionary;
        this.legalityChecker = new LegalityChecker(dictionary);
    }

    /**
     * Generates all legal move candidates that can be formed from a rack on the given board.
     *
     * @param board current board
     * @param tray normalized rack letters plus optional '*'
     * @return scored legal candidates in deterministic order
     */
    public List<MoveCandidate> generateLegalCandidates(Board board, String tray) {
        CandidateListCollector collector = new CandidateListCollector();
        generateCandidates(board, tray, collector);
        collector.generated.sort(CANDIDATE_ORDER);
        return collector.generated;
    }

    /**
     * Generates and returns only the best legal move candidate for a board and rack.
     *
     * @param board current board
     * @param tray normalized rack letters plus optional '*'
     * @return highest-ranked legal candidate or null when none exists
     */
    public MoveCandidate findBestLegalCandidate(Board board, String tray) {
        BestCandidateCollector collector = new BestCandidateCollector();
        generateCandidates(board, tray, collector);
        return collector.best;
    }

    // Drives anchor-based generation with a caller-provided result collector.
    private void generateCandidates(Board board, String tray, CandidateCollector collector) {
        if (board == null) {
            throw new IllegalArgumentException("board is required");
        }

        Rack rack = Rack.fromTray(tray);
        CrossCheckCache crossChecks = new CrossCheckCache(board, dictionary);

        for (AnchorSquare anchor : anchorFinder.collectAnchors(board)) {
            generateForAnchor(board, anchor, true, rack, crossChecks, collector);
            generateForAnchor(board, anchor, false, rack, crossChecks, collector);
        }
    }

    // Enumerates all start/end segments for one anchor and orientation.
    private void generateForAnchor(
        Board board,
        AnchorSquare anchor,
        boolean horizontal,
        Rack rack,
        CrossCheckCache crossChecks,
        CandidateCollector collector
    ) {
        int fixed = horizontal ? anchor.row() : anchor.col();
        int anchorIndex = horizontal ? anchor.col() : anchor.row();

        for (int start = 0; start <= anchorIndex; start++) {
            if (hasFixedTileBefore(board, horizontal, fixed, start)) {
                continue;
            }

            for (int end = anchorIndex; end < board.size(); end++) {
                if (hasFixedTileAfter(board, horizontal, fixed, end)) {
                    continue;
                }

                int emptySquares = countEmptySquares(board, horizontal, fixed, start, end);
                if (emptySquares == 0 || emptySquares > rack.tilesRemaining()) {
                    continue;
                }

                if (!segmentContainsAnchor(start, end, anchorIndex)) {
                    continue;
                }

                fillSegment(board, horizontal, fixed, start, end, rack, crossChecks, collector);
            }
        }
    }

    // Builds candidates for one segment using trie backtracking and cross-check pruning.
    private void fillSegment(
        Board board,
        boolean horizontal,
        int fixed,
        int start,
        int end,
        Rack rack,
        CrossCheckCache crossChecks,
        CandidateCollector collector
    ) {
        buildCandidatesOnSegment(
            board,
            horizontal,
            fixed,
            start,
            end,
            start,
            rack,
            dictionary.rootCursor(),
            new StringBuilder(),
            new ArrayList<>(),
            crossChecks,
            collector
        );
    }

    // Recursively fills one candidate segment with fixed board letters and rack tiles.
    private void buildCandidatesOnSegment(
        Board board,
        boolean horizontal,
        int fixed,
        int start,
        int end,
        int index,
        Rack rack,
        Dictionary.Cursor cursor,
        StringBuilder word,
        List<PlayedTile> playedTiles,
        CrossCheckCache crossChecks,
        CandidateCollector collector
    ) {
        if (index > end) {
            if (!dictionary.isWord(cursor)) {
                return;
            }

            MoveCandidate candidate = new MoveCandidate(
                new WordPlacement(horizontal, fixed, start, end),
                word.toString(),
                playedTiles
            );

            Board result;
            try {
                result = moveApplicator.apply(board, candidate);
            } catch (IllegalArgumentException | IllegalStateException ex) {
                return;
            }

            List<WordPlacement> words = wordFinder.collectFormedWordPlacements(result, candidate.playedTiles());
            if (!legalityChecker.isLegalMove(board, result, candidate.playedTiles(), words)) {
                return;
            }

            MoveCandidate scored = candidate.withScore(
                scorer.computeScore(board, result, candidate.playedTiles(), words)
            );
            collector.accept(scored);
            return;
        }

        int row = horizontal ? fixed : index;
        int col = horizontal ? index : fixed;

        if (board.isTile(row, col)) {
            char boardLetter = board.tileAt(row, col);
            Dictionary.Cursor next = dictionary.advance(cursor, boardLetter);
            if (next == null) {
                return;
            }

            char displayLetter = board.isBlankTile(row, col)
                ? Character.toUpperCase(boardLetter)
                : boardLetter;
            word.append(displayLetter);
            buildCandidatesOnSegment(
                board,
                horizontal,
                fixed,
                start,
                end,
                index + 1,
                rack,
                next,
                word,
                playedTiles,
                crossChecks,
                collector
            );
            word.deleteCharAt(word.length() - 1);
            return;
        }

        for (char letter = 'a'; letter <= 'z'; letter++) {
            Dictionary.Cursor next = dictionary.advance(cursor, letter);
            if (next == null) {
                continue;
            }
            if (!crossChecks.allowsLetter(horizontal, row, col, letter)) {
                continue;
            }

            if (rack.hasLetter(letter)) {
                rack.useLetter(letter);
                word.append(letter);
                playedTiles.add(new PlayedTile(letter, row, col));

                buildCandidatesOnSegment(
                    board,
                    horizontal,
                    fixed,
                    start,
                    end,
                    index + 1,
                    rack,
                    next,
                    word,
                    playedTiles,
                    crossChecks,
                    collector
                );

                playedTiles.remove(playedTiles.size() - 1);
                word.deleteCharAt(word.length() - 1);
                rack.restoreLetter(letter);
            }

            if (rack.hasBlank()) {
                rack.useBlank();
                word.append(Character.toUpperCase(letter));
                playedTiles.add(new PlayedTile(letter, row, col, true));

                buildCandidatesOnSegment(
                    board,
                    horizontal,
                    fixed,
                    start,
                    end,
                    index + 1,
                    rack,
                    next,
                    word,
                    playedTiles,
                    crossChecks,
                    collector
                );

                playedTiles.remove(playedTiles.size() - 1);
                word.deleteCharAt(word.length() - 1);
                rack.restoreBlank();
            }
        }
    }

    // Returns whether a segment boundary is immediately preceded by an existing tile.
    private boolean hasFixedTileBefore(Board board, boolean horizontal, int fixed, int start) {
        int row = horizontal ? fixed : start - 1;
        int col = horizontal ? start - 1 : fixed;
        return board.inBounds(row, col) && board.isTile(row, col);
    }

    // Returns whether a segment boundary is immediately followed by an existing tile.
    private boolean hasFixedTileAfter(Board board, boolean horizontal, int fixed, int end) {
        int row = horizontal ? fixed : end + 1;
        int col = horizontal ? end + 1 : fixed;
        return board.inBounds(row, col) && board.isTile(row, col);
    }

    // Counts empty squares on a line segment, i.e. tiles that must come from the rack.
    private int countEmptySquares(Board board, boolean horizontal, int fixed, int start, int end) {
        int emptySquares = 0;
        for (int index = start; index <= end; index++) {
            int row = horizontal ? fixed : index;
            int col = horizontal ? index : fixed;
            if (!board.isTile(row, col)) {
                emptySquares++;
            }
        }
        return emptySquares;
    }

    // Returns whether a numeric segment range includes the anchor index.
    private boolean segmentContainsAnchor(int start, int end, int anchorIndex) {
        return anchorIndex >= start && anchorIndex <= end;
    }

    // Collects deduplicated candidates during generation.
    private interface CandidateCollector {
        void accept(MoveCandidate candidate);
    }

    // Materializes every unique candidate for callers that need the full list.
    private static final class CandidateListCollector implements CandidateCollector {
        private final Set<String> seen = new LinkedHashSet<>();
        private final List<MoveCandidate> generated = new ArrayList<>();

        @Override
        public void accept(MoveCandidate candidate) {
            if (seen.add(candidate.key())) {
                generated.add(candidate);
            }
        }
    }

    // Tracks only the highest-ranked unique candidate during generation.
    private static final class BestCandidateCollector implements CandidateCollector {
        private final Set<String> seen = new LinkedHashSet<>();
        private MoveCandidate best;

        @Override
        public void accept(MoveCandidate candidate) {
            if (!seen.add(candidate.key())) {
                return;
            }
            if (best == null || CANDIDATE_ORDER.compare(candidate, best) < 0) {
                best = candidate;
            }
        }
    }
}
