/**
 * Author: Garion
 *
 * Mutable rack model used during solver backtracking.
 */
public class Rack {
    private final int[] counts = new int[26];
    private int blanks;
    private int tilesRemaining;

    private Rack() {
    }

    /**
     * Builds a rack from a normalized tray string.
     *
     * @param tray lowercase letters plus optional '*'
     * @return parsed rack instance
     */
    public static Rack fromTray(String tray) {
        if (tray == null) {
            throw new IllegalArgumentException("tray is required");
        }

        Rack rack = new Rack();
        for (int i = 0; i < tray.length(); i++) {
            char ch = Character.toLowerCase(tray.charAt(i));
            if (ch == '*') {
                rack.blanks++;
                rack.tilesRemaining++;
            } else if (ch >= 'a' && ch <= 'z') {
                rack.counts[ch - 'a']++;
                rack.tilesRemaining++;
            } else {
                throw new IllegalArgumentException("invalid rack character: " + tray.charAt(i));
            }
        }
        return rack;
    }

    /**
     * Returns the number of tiles still available.
     *
     * @return remaining tiles
     */
    public int tilesRemaining() {
        return tilesRemaining;
    }

    /**
     * Returns whether the rack contains a concrete letter tile.
     *
     * @param letter candidate letter
     * @return true when available
     */
    public boolean hasLetter(char letter) {
        int index = index(letter);
        return index >= 0 && counts[index] > 0;
    }

    /**
     * Returns whether the rack contains a blank tile.
     *
     * @return true when a blank is available
     */
    public boolean hasBlank() {
        return blanks > 0;
    }

    /**
     * Consumes one concrete letter tile.
     *
     * @param letter letter to consume
     */
    public void useLetter(char letter) {
        int index = index(letter);
        if (index < 0 || counts[index] == 0) {
            throw new IllegalArgumentException("letter tile is not available: " + letter);
        }
        counts[index]--;
        tilesRemaining--;
    }

    /**
     * Restores one concrete letter tile after backtracking.
     *
     * @param letter letter to restore
     */
    public void restoreLetter(char letter) {
        int index = index(letter);
        if (index < 0) {
            throw new IllegalArgumentException("invalid rack letter: " + letter);
        }
        counts[index]++;
        tilesRemaining++;
    }

    /**
     * Consumes one blank tile.
     */
    public void useBlank() {
        if (blanks == 0) {
            throw new IllegalArgumentException("blank tile is not available");
        }
        blanks--;
        tilesRemaining--;
    }

    /**
     * Restores one blank tile after backtracking.
     */
    public void restoreBlank() {
        blanks++;
        tilesRemaining++;
    }

    // Maps a lowercase English letter to its rack index.
    private int index(char letter) {
        char lowered = Character.toLowerCase(letter);
        if (lowered < 'a' || lowered > 'z') {
            return -1;
        }
        return lowered - 'a';
    }
}
