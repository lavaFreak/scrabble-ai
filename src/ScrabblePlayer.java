/**
 * Author: Garion
 *
 * File purpose: hold mutable player state for the Part 3 game flow.
 */
public class ScrabblePlayer {
    private final String name;
    private final boolean human;
    private final RackState rack;
    private int score;

    /**
     * Creates one game player.
     *
     * @param name display name
     * @param human true for the human player
     * @param rack player's rack
     */
    public ScrabblePlayer(String name, boolean human, RackState rack) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("player name is required");
        }
        if (rack == null) {
            throw new IllegalArgumentException("player rack is required");
        }

        this.name = name;
        this.human = human;
        this.rack = rack;
    }

    /**
     * Returns player display name.
     *
     * @return player name
     */
    public String name() {
        return name;
    }

    /**
     * Returns whether this is the human player.
     *
     * @return true when human-controlled
     */
    public boolean isHuman() {
        return human;
    }

    /**
     * Returns the player's current rack.
     *
     * @return mutable rack state
     */
    public RackState rack() {
        return rack;
    }

    /**
     * Returns the player's current score.
     *
     * @return score total
     */
    public int score() {
        return score;
    }

    /**
     * Adjusts the player's score by a delta.
     *
     * @param delta score change
     */
    public void addScore(int delta) {
        score += delta;
    }
}
