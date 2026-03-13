/**
 * Author: Garion
 *
 * File purpose: trie implementation used by the solver dictionary and standalone trie exercises.
 */
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.LinkedHashSet;
import java.util.Set;

public class Trie implements TrieInterface {
    private final TrieNode root = new TrieNode();

    /**
     * Inserts one word into the trie.
     *
     * @param word the word to insert
     */
    @Override
    public void insert(String word) {
        String normalized = normalize(word);
        if (normalized == null || normalized.isEmpty()) {
            return;
        }

        TrieNode current = root;
        for (int i = 0; i < normalized.length(); i++) {
            int childIndex = childIndex(normalized.charAt(i));
            if (childIndex < 0) {
                return;
            }
            if (current.children[childIndex] == null) {
                current.children[childIndex] = new TrieNode();
            }
            current = current.children[childIndex];
        }
        current.isWord = true;
    }

    /**
     * Returns whether the trie contains a full word.
     *
     * @param word the word to search for
     * @return true if found, false otherwise
     */
    @Override
    public boolean search(String word) {
        TrieNode node = traverse(word);
        return node != null && node.isWord;
    }

    /**
     * Returns whether the trie contains any words with the given prefix.
     *
     * @param prefix the prefix to check
     * @return true if prefix exists, false otherwise
     */
    @Override
    public boolean startsWith(String prefix) {
        if (prefix == null) {
            return false;
        }
        if (prefix.isEmpty()) {
            return true;
        }
        return traverse(prefix) != null;
    }

    /**
     * Collects all complete words below a prefix node.
     *
     * @param prefix the prefix to search by
     * @return matching words in deterministic DFS order
     */
    @Override
    public Set<String> getWordsWithPrefix(String prefix) {
        String normalized = normalize(prefix);
        if (normalized == null) {
            return Set.of();
        }

        TrieNode startNode;
        String seed;
        if (normalized.isEmpty()) {
            startNode = root;
            seed = "";
        } else {
            startNode = traverse(normalized);
            seed = normalized;
        }

        if (startNode == null) {
            return Set.of();
        }

        LinkedHashSet<String> matches = new LinkedHashSet<>();
        collectWords(startNode, new StringBuilder(seed), matches);
        return matches;
    }

    /**
     * Loads and inserts each non-empty line from a file.
     *
     * @param filename path to the file
     */
    @Override
    public void loadFromFile(String filename) {
        try {
            loadFromFileOrThrow(filename);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    /**
     * Loads words from a file with checked I/O behavior for project code.
     *
     * @param filename path to the file
     * @throws IOException if the file cannot be read
     */
    public void loadFromFileOrThrow(String filename) throws IOException {
        try (BufferedReader in = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = in.readLine()) != null) {
                insert(line);
            }
        }
    }

    // Walks the trie for a normalized word/prefix string.
    TrieNode traverse(String text) {
        String normalized = normalize(text);
        if (normalized == null || normalized.isEmpty()) {
            return null;
        }

        TrieNode current = root;
        for (int i = 0; i < normalized.length(); i++) {
            int childIndex = childIndex(normalized.charAt(i));
            if (childIndex < 0) {
                return null;
            }
            TrieNode child = current.children[childIndex];
            if (child == null) {
                return null;
            }
            current = child;
        }
        return current;
    }

    // Exposes the root node for incremental search helpers.
    TrieNode rootNode() {
        return root;
    }

    // Advances one trie edge for a lowercase English letter.
    TrieNode advance(TrieNode node, char letter) {
        if (node == null) {
            return null;
        }
        int childIndex = childIndex(letter);
        if (childIndex < 0) {
            return null;
        }
        return node.children[childIndex];
    }

    // Collects all terminal words below one node.
    private void collectWords(TrieNode node, StringBuilder prefix, Set<String> matches) {
        if (node.isWord) {
            matches.add(prefix.toString());
        }

        for (int i = 0; i < 26; i++) {
            TrieNode child = node.children[i];
            if (child == null) {
                continue;
            }
            prefix.append((char) ('a' + i));
            collectWords(child, prefix, matches);
            prefix.deleteCharAt(prefix.length() - 1);
        }
    }

    // Normalizes input to lowercase text while preserving the empty-string case.
    private String normalize(String text) {
        if (text == null) {
            return null;
        }
        return text.trim().toLowerCase();
    }

    // Converts one ASCII letter to a 0..25 trie slot.
    private static int childIndex(char ch) {
        char lowered = Character.toLowerCase(ch);
        if (lowered < 'a' || lowered > 'z') {
            return -1;
        }
        return lowered - 'a';
    }

    // Fixed-size trie node for lowercase English letters.
    static final class TrieNode {
        final TrieNode[] children = new TrieNode[26];
        boolean isWord;
    }
}
