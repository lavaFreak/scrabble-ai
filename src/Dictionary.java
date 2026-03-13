/**
 * Author: Garion
 *
 * File purpose: provide case-insensitive dictionary loading and trie-backed lookups.
 */
import java.io.IOException;

public class Dictionary {
    private final Trie trie;

    private Dictionary(Trie trie) {
        this.trie = trie;
    }

    /**
     * Loads dictionary entries from file into a lowercase trie.
     *
     * @param dictionaryPath path to dictionary text file
     * @return dictionary instance containing all non-empty lines
     * @throws IOException if dictionary file cannot be read
     */
    public static Dictionary fromFile(String dictionaryPath) throws IOException {
        Trie trie = new Trie();
        trie.loadFromFileOrThrow(dictionaryPath);
        return new Dictionary(trie);
    }

    /**
     * Checks whether a full word exists in the dictionary.
     *
     * @param word candidate word
     * @return true when present (case-insensitive)
     */
    public boolean contains(String word) {
        return trie.search(word);
    }

    /**
     * Checks whether a string is a prefix of at least one dictionary word.
     *
     * @param prefix candidate prefix
     * @return true when at least one word starts with the prefix
     */
    public boolean hasPrefix(String prefix) {
        return trie.startsWith(prefix);
    }

    /**
     * Returns a cursor positioned at the root trie node.
     *
     * @return root cursor for incremental dictionary walks
     */
    public Cursor rootCursor() {
        return new Cursor(trie.rootNode());
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
        Trie.TrieNode child = trie.advance(cursor.node, letter);
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

    /**
     * Immutable trie cursor for incremental solver search.
     */
    public static final class Cursor {
        private final Trie.TrieNode node;

        private Cursor(Trie.TrieNode node) {
            this.node = node;
        }
    }
}
