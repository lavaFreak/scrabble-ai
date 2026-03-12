/**
 * Author: Garion
 *
 * File purpose: provide case-insensitive dictionary loading and trie-backed lookups.
 */
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Dictionary {
    private final TrieNode trieRoot;

    private Dictionary(TrieNode trieRoot) {
        this.trieRoot = trieRoot;
    }

    /**
     * Loads dictionary entries from file into a lowercase trie.
     *
     * @param dictionaryPath path to dictionary text file
     * @return dictionary instance containing all non-empty lines
     * @throws IOException if dictionary file cannot be read
     */
    public static Dictionary fromFile(String dictionaryPath) throws IOException {
        TrieNode root = new TrieNode();
        try (BufferedReader in = new BufferedReader(new FileReader(dictionaryPath))) {
            String line;
            while ((line = in.readLine()) != null) {
                String word = line.trim().toLowerCase();
                if (!word.isEmpty()) {
                    insert(root, word);
                }
            }
        }
        return new Dictionary(root);
    }

    /**
     * Checks whether a full word exists in the dictionary.
     *
     * @param word candidate word
     * @return true when present (case-insensitive)
     */
    public boolean contains(String word) {
        TrieNode node = traverse(word);
        return node != null && node.isWord;
    }

    /**
     * Checks whether a string is a prefix of at least one dictionary word.
     *
     * @param prefix candidate prefix
     * @return true when at least one word starts with the prefix
     */
    public boolean hasPrefix(String prefix) {
        if (prefix == null) {
            return false;
        }
        if (prefix.isEmpty()) {
            return true;
        }
        return traverse(prefix) != null;
    }

    /**
     * Returns a cursor positioned at the root trie node.
     *
     * @return root cursor for incremental dictionary walks
     */
    public Cursor rootCursor() {
        return new Cursor(trieRoot);
    }

    /**
     * Advances one trie step for the given letter.
     *
     * @param cursor current cursor position
     * @param letter next letter to consume
     * @return advanced cursor or null when no matching branch exists
     */
    public Cursor advance(Cursor cursor, char letter) {
        if (cursor == null) {
            return null;
        }
        int childIndex = childIndex(letter);
        if (childIndex < 0) {
            return null;
        }
        TrieNode child = cursor.node.children[childIndex];
        if (child == null) {
            return null;
        }
        return new Cursor(child);
    }

    /**
     * Reports whether the cursor currently sits on a complete word.
     *
     * @param cursor trie cursor to inspect
     * @return true when the consumed letters form a dictionary word
     */
    public boolean isWord(Cursor cursor) {
        return cursor != null && cursor.node.isWord;
    }

    // Adds one normalized lowercase word to the trie.
    private static void insert(TrieNode root, String word) {
        TrieNode current = root;
        for (int i = 0; i < word.length(); i++) {
            int childIndex = childIndex(word.charAt(i));
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

    // Walks the trie for a normalized word/prefix string.
    private TrieNode traverse(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        TrieNode current = trieRoot;
        for (int i = 0; i < text.length(); i++) {
            int childIndex = childIndex(text.charAt(i));
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

    // Converts one ASCII letter to a 0..25 trie slot.
    private static int childIndex(char ch) {
        char lowered = Character.toLowerCase(ch);
        if (lowered < 'a' || lowered > 'z') {
            return -1;
        }
        return lowered - 'a';
    }

    /**
     * Immutable trie cursor for incremental solver search.
     */
    public static final class Cursor {
        private final TrieNode node;

        private Cursor(TrieNode node) {
            this.node = node;
        }
    }

    // Fixed-size trie node for lowercase English letters.
    private static final class TrieNode {
        private final TrieNode[] children = new TrieNode[26];
        private boolean isWord;
    }
}
