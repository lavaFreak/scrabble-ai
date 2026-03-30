/**
 * Author: Garion
 *
 * File purpose: manage mutable tile-bag state for the Part 3 game flow.
 */
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TileBag {
    private final ArrayList<Character> tiles;
    private final Random random;

    /**
     * Creates a bag from an explicit tile sequence.
     *
     * The given order is preserved until tiles are returned to the bag.
     *
     * @param tiles lowercase letters or '*'
     * @param random randomness source for return/exchange shuffles
     */
    public TileBag(List<Character> tiles, Random random) {
        if (random == null) {
            throw new IllegalArgumentException("random is required");
        }

        this.random = random;
        this.tiles = new ArrayList<>();
        if (tiles != null) {
            for (char tile : tiles) {
                this.tiles.add(normalizeTile(tile));
            }
        }
    }

    /**
     * Creates a standard 100-tile Scrabble bag and shuffles it.
     *
     * @param random randomness source for shuffling
     * @return shuffled standard tile bag
     */
    public static TileBag standard(Random random) {
        ArrayList<Character> tiles = new ArrayList<>(100);
        addCopies(tiles, 'a', 9);
        addCopies(tiles, 'b', 2);
        addCopies(tiles, 'c', 2);
        addCopies(tiles, 'd', 4);
        addCopies(tiles, 'e', 12);
        addCopies(tiles, 'f', 2);
        addCopies(tiles, 'g', 3);
        addCopies(tiles, 'h', 2);
        addCopies(tiles, 'i', 9);
        addCopies(tiles, 'j', 1);
        addCopies(tiles, 'k', 1);
        addCopies(tiles, 'l', 4);
        addCopies(tiles, 'm', 2);
        addCopies(tiles, 'n', 6);
        addCopies(tiles, 'o', 8);
        addCopies(tiles, 'p', 2);
        addCopies(tiles, 'q', 1);
        addCopies(tiles, 'r', 6);
        addCopies(tiles, 's', 4);
        addCopies(tiles, 't', 6);
        addCopies(tiles, 'u', 4);
        addCopies(tiles, 'v', 2);
        addCopies(tiles, 'w', 2);
        addCopies(tiles, 'x', 1);
        addCopies(tiles, 'y', 2);
        addCopies(tiles, 'z', 1);
        addCopies(tiles, '*', 2);

        TileBag bag = new TileBag(tiles, random);
        Collections.shuffle(bag.tiles, random);
        return bag;
    }

    /**
     * Returns the number of tiles left in the bag.
     *
     * @return remaining tile count
     */
    public int remaining() {
        return tiles.size();
    }

    /**
     * Returns whether the bag can support exchanging the requested number of tiles.
     *
     * @param count exchange count
     * @return true when enough tiles remain to draw replacements first
     */
    public boolean canExchange(int count) {
        return count >= 0 && remaining() >= count;
    }

    /**
     * Draws up to the requested number of tiles from the front of the bag.
     *
     * @param count number of tiles requested
     * @return drawn tiles in draw order
     */
    public List<Character> draw(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("draw count cannot be negative");
        }

        int actual = Math.min(count, tiles.size());
        ArrayList<Character> drawn = new ArrayList<>(actual);
        for (int i = 0; i < actual; i++) {
            drawn.add(tiles.remove(0));
        }
        return drawn;
    }

    /**
     * Returns tiles to the bag and reshuffles.
     *
     * @param returned tiles to add back
     */
    public void returnTiles(List<Character> returned) {
        if (returned == null || returned.isEmpty()) {
            return;
        }

        for (char tile : returned) {
            tiles.add(normalizeTile(tile));
        }
        Collections.shuffle(tiles, random);
    }

    /**
     * Exchanges tiles by drawing replacements first and only then returning the old tiles.
     *
     * @param returned tiles to exchange out
     * @return replacement tiles
     */
    public List<Character> exchange(List<Character> returned) {
        if (returned == null || returned.isEmpty()) {
            throw new IllegalArgumentException("returned tiles are required");
        }
        if (!canExchange(returned.size())) {
            throw new IllegalArgumentException("not enough tiles remain to exchange");
        }

        List<Character> drawn = draw(returned.size());
        returnTiles(returned);
        return drawn;
    }

    // Adds repeated copies of one tile to a bag build list.
    private static void addCopies(List<Character> tiles, char tile, int count) {
        for (int i = 0; i < count; i++) {
            tiles.add(tile);
        }
    }

    // Normalizes bag tiles to lowercase letters or '*'.
    private static char normalizeTile(char tile) {
        if (tile == '*') {
            return '*';
        }
        if (!Character.isLetter(tile)) {
            throw new IllegalArgumentException("invalid bag tile: " + tile);
        }
        return Character.toLowerCase(tile);
    }
}
