/**
 * Author: Garion
 *
 * Mutable rack state for the Part 3 game flow.
 */
import java.util.ArrayList;
import java.util.List;

public class RackState {
    private final List<Character> tiles;

    /**
     * Creates an empty rack.
     */
    public RackState() {
        this.tiles = new ArrayList<>();
    }

    /**
     * Creates a rack from an explicit tile list.
     *
     * @param tiles rack tiles as lowercase letters or '*'
     */
    public RackState(List<Character> tiles) {
        this();
        addTiles(tiles);
    }

    /**
     * Creates a rack from tray text.
     *
     * @param tray lowercase letters plus optional '*'
     * @return rack populated from the tray string
     */
    public static RackState fromTray(String tray) {
        RackState rack = new RackState();
        if (tray == null) {
            return rack;
        }

        for (int i = 0; i < tray.length(); i++) {
            rack.tiles.add(normalizeTile(tray.charAt(i)));
        }
        return rack;
    }

    /**
     * Returns the number of tiles in the rack.
     *
     * @return rack size
     */
    public int size() {
        return tiles.size();
    }

    /**
     * Returns whether the rack is empty.
     *
     * @return true when no tiles remain
     */
    public boolean isEmpty() {
        return tiles.isEmpty();
    }

    /**
     * Returns an immutable view of the current tiles.
     *
     * @return rack tiles in current display order
     */
    public List<Character> tiles() {
        return List.copyOf(tiles);
    }

    /**
     * Returns tray text for solver and UI display.
     *
     * @return rack text
     */
    public String trayString() {
        StringBuilder sb = new StringBuilder();
        for (char tile : tiles) {
            sb.append(tile);
        }
        return sb.toString();
    }

    /**
     * Adds tiles to the rack.
     *
     * @param newTiles lowercase letters or '*'
     */
    public void addTiles(List<Character> newTiles) {
        if (newTiles == null) {
            return;
        }

        for (char tile : newTiles) {
            tiles.add(normalizeTile(tile));
        }
    }

    /**
     * Draws tiles until the rack reaches the target size or the bag empties.
     *
     * @param bag tile bag
     * @param targetSize desired rack size
     * @return tiles drawn from the bag
     */
    public List<Character> fillToSize(TileBag bag, int targetSize) {
        if (bag == null) {
            throw new IllegalArgumentException("tile bag is required");
        }
        if (targetSize < 0) {
            throw new IllegalArgumentException("target size cannot be negative");
        }

        int needed = Math.max(0, targetSize - tiles.size());
        List<Character> drawn = bag.draw(needed);
        addTiles(drawn);
        return drawn;
    }

    /**
     * Returns whether the rack can supply an exact set of played tiles.
     *
     * Blank tiles must be marked explicitly in the played tile list.
     *
     * @param played tiles placed onto the board
     * @return true when the rack contains every required tile
     */
    public boolean canSupply(List<PlayedTile> played) {
        List<Character> available = new ArrayList<>(tiles);
        for (PlayedTile tile : played) {
            char required = tile.isBlank() ? '*' : Character.toLowerCase(tile.letter());
            if (!removeOne(available, required)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Removes the exact played tiles from the rack.
     *
     * @param played tiles that were placed onto the board
     */
    public void removePlayedTiles(List<PlayedTile> played) {
        if (!canSupply(played)) {
            throw new IllegalArgumentException("rack does not contain the played tiles");
        }

        for (PlayedTile tile : played) {
            char required = tile.isBlank() ? '*' : Character.toLowerCase(tile.letter());
            removeOne(tiles, required);
        }
    }

    /**
     * Removes explicit tray letters from the rack, used for exchanges.
     *
     * @param letters letters to remove
     * @return removed tiles in removal order
     */
    public List<Character> removeLetters(String letters) {
        if (letters == null || letters.isEmpty()) {
            throw new IllegalArgumentException("letters are required");
        }

        List<Character> removed = new ArrayList<>();
        for (int i = 0; i < letters.length(); i++) {
            char tile = normalizeTile(letters.charAt(i));
            if (!removeOne(tiles, tile)) {
                throw new IllegalArgumentException("rack does not contain tile '" + tile + "'");
            }
            removed.add(tile);
        }
        return removed;
    }

    /**
     * Removes and returns every tile from the rack.
     *
     * @return removed rack contents in current order
     */
    public List<Character> removeAllTiles() {
        List<Character> removed = new ArrayList<>(tiles);
        tiles.clear();
        return removed;
    }

    // Removes one matching tile from a working list.
    private boolean removeOne(List<Character> available, char tile) {
        for (int i = 0; i < available.size(); i++) {
            if (available.get(i) == tile) {
                available.remove(i);
                return true;
            }
        }
        return false;
    }

    // Normalizes rack tiles to lowercase letters or '*'.
    private static char normalizeTile(char tile) {
        if (tile == '*') {
            return '*';
        }
        if (!Character.isLetter(tile)) {
            throw new IllegalArgumentException("invalid rack tile: " + tile);
        }
        return Character.toLowerCase(tile);
    }
}
