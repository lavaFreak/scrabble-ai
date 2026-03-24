/**
 * Author: Garion
 *
 * Immutable UI-facing snapshot of the current game state.
 */
public class GameSnapshot {
    private final Board board;
    private final String humanRack;
    private final String computerRack;
    private final int humanScore;
    private final int computerScore;
    private final int tileBagRemaining;
    private final boolean humanTurn;
    private final boolean gameOver;
    private final String statusMessage;
    private final TurnRecord latestTurn;

    /**
     * Creates one immutable game snapshot.
     *
     * @param board current board
     * @param humanRack human rack text
     * @param computerRack computer rack text
     * @param humanScore human score
     * @param computerScore computer score
     * @param tileBagRemaining remaining tile count
     * @param humanTurn true when the human acts next
     * @param gameOver true when the game is over
     * @param statusMessage latest status text
     * @param latestTurn latest completed turn, or null
     */
    public GameSnapshot(
        Board board,
        String humanRack,
        String computerRack,
        int humanScore,
        int computerScore,
        int tileBagRemaining,
        boolean humanTurn,
        boolean gameOver,
        String statusMessage,
        TurnRecord latestTurn
    ) {
        this.board = board;
        this.humanRack = humanRack;
        this.computerRack = computerRack;
        this.humanScore = humanScore;
        this.computerScore = computerScore;
        this.tileBagRemaining = tileBagRemaining;
        this.humanTurn = humanTurn;
        this.gameOver = gameOver;
        this.statusMessage = statusMessage;
        this.latestTurn = latestTurn;
    }

    /**
     * Returns the current board.
     *
     * @return board state
     */
    public Board board() {
        return board;
    }

    /**
     * Returns the human rack.
     *
     * @return human rack text
     */
    public String humanRack() {
        return humanRack;
    }

    /**
     * Returns the computer rack.
     *
     * @return computer rack text
     */
    public String computerRack() {
        return computerRack;
    }

    /**
     * Returns the human score.
     *
     * @return human score
     */
    public int humanScore() {
        return humanScore;
    }

    /**
     * Returns the computer score.
     *
     * @return computer score
     */
    public int computerScore() {
        return computerScore;
    }

    /**
     * Returns the remaining tile count.
     *
     * @return bag count
     */
    public int tileBagRemaining() {
        return tileBagRemaining;
    }

    /**
     * Returns whether the human acts next.
     *
     * @return true when it is the human player's turn
     */
    public boolean isHumanTurn() {
        return humanTurn;
    }

    /**
     * Returns whether the game is over.
     *
     * @return true when no more turns remain
     */
    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * Returns the latest status message.
     *
     * @return status text
     */
    public String statusMessage() {
        return statusMessage;
    }

    /**
     * Returns the latest completed turn record, or null when no turns have been taken.
     *
     * @return latest turn record or null
     */
    public TurnRecord latestTurn() {
        return latestTurn;
    }
}
