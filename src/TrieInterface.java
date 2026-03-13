import java.util.Set;

public interface TrieInterface {

    /**
     * Inserts a word into the trie.
     *
     * @param word the word to insert
     */
    void insert(String word);

    /**
     * Returns true if the word exists in the trie as a complete word.
     *
     * @param word the word to search for
     * @return true if found, false otherwise
     */
    boolean search(String word);

    /**
     * Returns true if any word in the trie starts with the given prefix.
     *
     * @param prefix the prefix to check
     * @return true if prefix exists, false otherwise
     */
    boolean startsWith(String prefix);

    /**
     * Returns all words in the trie that begin with the given prefix.
     *
     * @param prefix the prefix to search by
     * @return Set of matching words, empty set if none found
     */
    Set<String> getWordsWithPrefix(String prefix);

    /**
     * Loads words from a file, inserting each line as a word.
     *
     * @param filename path to the file
     */
    void loadFromFile(String filename);
}
