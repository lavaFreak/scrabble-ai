/**
 * Author: Garion
 *
 * File purpose: capture one immutable snapshot of a completed game turn.
 */
public class TurnRecord {
    private final int number;
    private final TurnActor actor;
    private final TurnType type;
    private final String summary;
    private final int scoreDelta;
    private final int humanScore;
    private final int computerScore;
    private final int tileBagRemaining;
    private final String humanRack;
    private final String computerRack;
    private final ResolvedPlay play;

    /**
     * Creates one recorded turn snapshot.
     *
     * @param number sequential turn number
     * @param actor player who took the turn
     * @param type kind of turn
     * @param summary human-readable turn summary
     * @param scoreDelta points earned on this turn
     * @param humanScore human score after the turn
     * @param computerScore computer score after the turn
     * @param tileBagRemaining remaining bag count after the turn
     * @param humanRack human rack after the turn
     * @param computerRack computer rack after the turn
     * @param play resolved play for move turns, otherwise null
     */
    public TurnRecord(
        int number,
        TurnActor actor,
        TurnType type,
        String summary,
        int scoreDelta,
        int humanScore,
        int computerScore,
        int tileBagRemaining,
        String humanRack,
        String computerRack,
        ResolvedPlay play
    ) {
        this.number = number;
        this.actor = actor;
        this.type = type;
        this.summary = summary;
        this.scoreDelta = scoreDelta;
        this.humanScore = humanScore;
        this.computerScore = computerScore;
        this.tileBagRemaining = tileBagRemaining;
        this.humanRack = humanRack;
        this.computerRack = computerRack;
        this.play = play;
    }

    /**
     * Returns the sequential turn number.
     *
     * @return turn number
     */
    public int number() {
        return number;
    }

    /**
     * Returns which player acted.
     *
     * @return acting player
     */
    public TurnActor actor() {
        return actor;
    }

    /**
     * Returns the high-level turn type.
     *
     * @return turn type
     */
    public TurnType type() {
        return type;
    }

    /**
     * Returns the display summary for the turn.
     *
     * @return summary text
     */
    public String summary() {
        return summary;
    }

    /**
     * Returns the points earned on this turn.
     *
     * @return score delta
     */
    public int scoreDelta() {
        return scoreDelta;
    }

    /**
     * Returns the human score after the turn.
     *
     * @return human total score
     */
    public int humanScore() {
        return humanScore;
    }

    /**
     * Returns the computer score after the turn.
     *
     * @return computer total score
     */
    public int computerScore() {
        return computerScore;
    }

    /**
     * Returns the bag count after the turn.
     *
     * @return remaining bag count
     */
    public int tileBagRemaining() {
        return tileBagRemaining;
    }

    /**
     * Returns the human rack after the turn.
     *
     * @return human rack text
     */
    public String humanRack() {
        return humanRack;
    }

    /**
     * Returns the computer rack after the turn.
     *
     * @return computer rack text
     */
    public String computerRack() {
        return computerRack;
    }

    /**
     * Returns the resolved play for move turns.
     *
     * @return resolved play or null
     */
    public ResolvedPlay play() {
        return play;
    }
}
