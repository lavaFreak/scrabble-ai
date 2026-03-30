/**
 * Author: Garion
 *
 * File purpose: launch and render the JavaFX Part 3 Scrabble UI.
 */
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

public class Scrabble extends Application {
    private static final String DEFAULT_DICTIONARY = "Resources/dictionaries/dictionary.txt";
    private static final double CELL_SIZE = 42.0;
    private static final double RACK_TILE_WIDTH = 58.0;
    private static final double RACK_TILE_HEIGHT = 76.0;
    private static final String SESSION_TAG = "human-vs-computer";
    private static final String UI_FONT = "System";

    private ScrabbleGame game;
    private GameLogWriter gameLogWriter;
    private String dictionaryPath = DEFAULT_DICTIONARY;

    private GridPane boardGrid;
    private FlowPane rackPane;
    private ListView<String> historyList;
    private Label turnLabel;
    private Label statusLabel;
    private Label interactionLabel;
    private Label humanScoreLabel;
    private Label computerScoreLabel;
    private Label bagLabel;
    private Label computerRackLabel;
    private ToggleButton exchangeModeButton;
    private Button playMoveButton;
    private Button clearButton;
    private Button passButton;
    private Button exchangeButton;
    private Button computerTurnButton;

    private final LinkedHashMap<String, RackPlacement> stagedPlacements = new LinkedHashMap<>();
    private final LinkedHashSet<Integer> exchangeIndexes = new LinkedHashSet<>();
    private Integer selectedRackIndex;
    private boolean computerTurnRunning;
    private String interactionOverride;

    /**
     * Launches the JavaFX game.
     *
     * @param args optional dictionary path
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Initializes the stage and shows the game window.
     *
     * @param stage primary JavaFX stage
     */
    @Override
    public void start(Stage stage) {
        try {
            dictionaryPath = resolveDictionaryPath();
            game = ScrabbleGame.createStandardGame(loadDictionary(), new Random());
            gameLogWriter = GameLogWriter.createTimestamped(SESSION_TAG, dictionaryPath);
        } catch (IOException ex) {
            showFatalError("Unable to start Scrabble", ex.getMessage());
            return;
        }

        BorderPane root = buildRoot();
        Scene scene = new Scene(root, 1280, 940);

        stage.setTitle("Scrabble");
        stage.setScene(scene);
        stage.show();

        refreshView();
    }

    /**
     * Closes any active game log before shutdown.
     *
     * @throws Exception if JavaFX shutdown handling fails
     */
    @Override
    public void stop() throws Exception {
        if (gameLogWriter != null) {
            gameLogWriter.close();
        }
        super.stop();
    }

    // Loads the dictionary from the first CLI argument or falls back to dictionary.txt.
    private Dictionary loadDictionary() throws IOException {
        return Dictionary.fromFile(dictionaryPath);
    }

    // Resolves the active dictionary path from CLI args.
    private String resolveDictionaryPath() {
        List<String> raw = getParameters().getRaw();
        return raw.isEmpty() ? DEFAULT_DICTIONARY : raw.get(0);
    }

    // Builds the top-level scene layout.
    private BorderPane buildRoot() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(22));
        root.setStyle("-fx-background-color: #d7d7d7;");

        Label titleLabel = new Label("SCRABBLE");
        titleLabel.setFont(Font.font(UI_FONT, FontWeight.BOLD, 28));
        titleLabel.setTextFill(Color.web("#222222"));

        Label subtitleLabel = new Label("Human vs Computer");
        subtitleLabel.setFont(Font.font(UI_FONT, 14));
        subtitleLabel.setTextFill(Color.web("#333333"));

        VBox titleBox = new VBox(2, titleLabel, subtitleLabel);

        turnLabel = createChipLabel();
        humanScoreLabel = createMetricLabel();
        computerScoreLabel = createMetricLabel();
        bagLabel = createMetricLabel();
        computerRackLabel = createMetricLabel();

        HBox metrics = new HBox(10, turnLabel, humanScoreLabel, computerScoreLabel, bagLabel);
        metrics.setAlignment(Pos.CENTER_RIGHT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(16, titleBox, spacer, metrics);
        header.setAlignment(Pos.CENTER_LEFT);

        boardGrid = new GridPane();
        boardGrid.setAlignment(Pos.CENTER);
        boardGrid.setHgap(3);
        boardGrid.setVgap(3);

        StackPane boardPanel = new StackPane(boardGrid);
        boardPanel.setPadding(new Insets(16));
        boardPanel.setStyle("-fx-background-color: #b8b8b8;");

        statusLabel = new Label();
        statusLabel.setWrapText(true);
        statusLabel.setFont(Font.font(UI_FONT, 15));
        statusLabel.setTextFill(Color.web("#222222"));

        interactionLabel = new Label();
        interactionLabel.setWrapText(true);
        interactionLabel.setFont(Font.font(UI_FONT, 14));
        interactionLabel.setTextFill(Color.web("#333333"));

        VBox statusBox = new VBox(6, statusLabel, interactionLabel);
        statusBox.setPadding(new Insets(0, 4, 0, 4));

        playMoveButton = createActionButton("Play Move", this::handlePlayMove);
        clearButton = createActionButton("Clear Staged", this::handleClearSelections);
        passButton = createActionButton("Pass", this::handlePassTurn);
        exchangeModeButton = new ToggleButton("Exchange Mode");
        exchangeModeButton.setFont(Font.font(UI_FONT, FontWeight.BOLD, 13));
        exchangeModeButton.setOnAction(event -> handleExchangeModeToggle());
        exchangeModeButton.setStyle(actionButtonStyle("#666666"));

        exchangeButton = createActionButton("Exchange Selected", this::handleExchange);
        computerTurnButton = createActionButton("Computer Turn", this::handleComputerTurnButton);

        HBox actionRow = new HBox(10,
            playMoveButton,
            clearButton,
            passButton,
            exchangeModeButton,
            exchangeButton,
            computerTurnButton
        );
        actionRow.setAlignment(Pos.CENTER_LEFT);

        VBox boardSection = new VBox(14, boardPanel, statusBox, actionRow);
        boardSection.setAlignment(Pos.TOP_CENTER);
        boardSection.setPrefWidth(780);

        Label rackTitle = new Label("Your Rack");
        rackTitle.setFont(Font.font(UI_FONT, FontWeight.BOLD, 20));
        rackTitle.setTextFill(Color.web("#222222"));

        rackPane = new FlowPane();
        rackPane.setHgap(10);
        rackPane.setVgap(10);
        rackPane.setAlignment(Pos.CENTER_LEFT);
        rackPane.setPadding(new Insets(12));
        rackPane.setStyle("-fx-background-color: #c6c6c6;");

        VBox rackSection = new VBox(10, rackTitle, rackPane);

        Label sideTitle = new Label("Match Panel");
        sideTitle.setFont(Font.font(UI_FONT, FontWeight.BOLD, 20));
        sideTitle.setTextFill(Color.web("#222222"));

        historyList = new ListView<>();
        historyList.setFocusTraversable(false);
        historyList.setPrefWidth(320);
        historyList.setStyle("-fx-background-color: #f3f3f3;");

        Label latestTitle = new Label("Computer Rack");
        latestTitle.setFont(Font.font(UI_FONT, FontWeight.BOLD, 14));
        latestTitle.setTextFill(Color.web("#222222"));

        VBox sidePanel = new VBox(
            12,
            sideTitle,
            computerRackLabel,
            latestTitle,
            createInfoCard(
                "Instructions",
                "Select a rack tile, then click board squares to stage a move. "
                    + "Click a staged square to remove it. Toggle exchange mode to choose rack tiles for exchange."
            ),
            createInfoCard("Turn History", null),
            historyList
        );
        sidePanel.setPrefWidth(330);
        VBox.setVgrow(historyList, Priority.ALWAYS);

        HBox center = new HBox(24, boardSection, sidePanel);
        center.setAlignment(Pos.TOP_CENTER);

        VBox content = new VBox(18, header, center, rackSection);
        VBox.setVgrow(center, Priority.ALWAYS);

        root.setCenter(content);
        return root;
    }

    // Re-renders the full UI from current game and staging state.
    private void refreshView() {
        GameSnapshot snapshot = game.snapshot();
        renderBoard(snapshot);
        renderRack(snapshot);
        renderHistory(snapshot);
        updateLabels(snapshot);
        updateControls(snapshot);
        syncGameLog(snapshot);
    }

    // Appends any newly completed game state to the session log.
    private void syncGameLog(GameSnapshot snapshot) {
        if (gameLogWriter == null) {
            return;
        }
        try {
            gameLogWriter.sync(snapshot);
        } catch (IOException ex) {
            System.err.println("Unable to write game log: " + ex.getMessage());
            gameLogWriter = null;
        }
    }

    // Rebuilds the board grid, using staged placements when present.
    private void renderBoard(GameSnapshot snapshot) {
        boardGrid.getChildren().clear();
        Board baseBoard = snapshot.board();
        Board displayBoard = stagedPlacements.isEmpty() ? baseBoard : buildPlacementBuffer(baseBoard).previewBoard();

        for (int row = 0; row < baseBoard.size(); row++) {
            for (int col = 0; col < baseBoard.size(); col++) {
                StackPane cell = new StackPane();
                cell.setPrefSize(CELL_SIZE, CELL_SIZE);
                cell.setMaxSize(CELL_SIZE, CELL_SIZE);
                cell.setAlignment(Pos.CENTER);
                cell.setStyle(boardCellStyle(baseBoard, row, col));

                Label label = new Label(boardCellText(displayBoard, row, col));
                label.setMouseTransparent(true);
                label.setTextFill(boardCellTextColor(baseBoard, row, col));
                label.setFont(Font.font(
                    UI_FONT,
                    baseBoard.isTile(row, col) || isStaged(row, col) ? FontWeight.BOLD : FontWeight.SEMI_BOLD,
                    baseBoard.isTile(row, col) || isStaged(row, col) ? 16 : 11
                ));
                cell.getChildren().add(label);

                int targetRow = row;
                int targetCol = col;
                cell.setOnMouseClicked(event -> handleBoardClick(targetRow, targetCol));
                boardGrid.add(cell, col, row);
            }
        }
    }

    // Rebuilds the human rack buttons.
    private void renderRack(GameSnapshot snapshot) {
        rackPane.getChildren().clear();
        String rack = snapshot.humanRack();

        for (int index = 0; index < rack.length(); index++) {
            char tile = rack.charAt(index);
            int rackIndex = index;
            Button tileButton = new Button(rackButtonText(tile));
            tileButton.setFont(Font.font(UI_FONT, FontWeight.BOLD, 15));
            tileButton.setPrefSize(RACK_TILE_WIDTH, RACK_TILE_HEIGHT);
            tileButton.setWrapText(true);
            tileButton.setDisable(computerTurnRunning || snapshot.isGameOver());
            tileButton.setStyle(rackButtonStyle(rackIndex, tile, snapshot));
            tileButton.setOnAction(event -> handleRackClick(rackIndex));
            rackPane.getChildren().add(tileButton);
        }
    }

    // Rebuilds the turn-history list.
    private void renderHistory(GameSnapshot snapshot) {
        ArrayList<String> lines = new ArrayList<>();
        for (TurnRecord turn : game.turnHistory()) {
            lines.add(String.format("%02d. %s", turn.number(), turn.summary()));
        }
        historyList.getItems().setAll(lines);
        if (!lines.isEmpty()) {
            historyList.scrollTo(lines.size() - 1);
        }
    }

    // Updates score, status, and instruction labels.
    private void updateLabels(GameSnapshot snapshot) {
        turnLabel.setText(resolveTurnLabel(snapshot));
        humanScoreLabel.setText("Human: " + snapshot.humanScore());
        computerScoreLabel.setText("Computer: " + snapshot.computerScore());
        bagLabel.setText("Bag: " + snapshot.tileBagRemaining());
        computerRackLabel.setText("Computer rack: " + snapshot.computerRack().length() + " tile(s)");
        statusLabel.setText(snapshot.statusMessage());
        interactionLabel.setText(resolveInteractionMessage(snapshot));
    }

    // Enables and disables action buttons based on current turn state.
    private void updateControls(GameSnapshot snapshot) {
        boolean humanTurn = snapshot.isHumanTurn();
        boolean moveBuffered = !stagedPlacements.isEmpty();
        boolean exchangeSelected = !exchangeIndexes.isEmpty();
        boolean exchangeMode = exchangeModeButton.isSelected();
        boolean selectionActive = selectedRackIndex != null;
        boolean exchangeAvailable = game.canExchangeCount(1);
        boolean exchangeSelectionAllowed = game.canExchangeCount(exchangeIndexes.size());

        exchangeModeButton.setDisable(snapshot.isGameOver() || computerTurnRunning || !humanTurn || !exchangeAvailable);
        exchangeModeButton.setStyle(exchangeMode ? actionButtonStyle("#4f8a4f") : actionButtonStyle("#666666"));
        playMoveButton.setDisable(snapshot.isGameOver() || computerTurnRunning || !humanTurn || exchangeMode || !moveBuffered);
        clearButton.setDisable(
            snapshot.isGameOver() || computerTurnRunning || (!moveBuffered && !exchangeSelected && !selectionActive)
        );
        passButton.setDisable(snapshot.isGameOver() || computerTurnRunning || !humanTurn);
        exchangeButton.setDisable(
            snapshot.isGameOver()
                || computerTurnRunning
                || !humanTurn
                || !exchangeMode
                || !exchangeSelected
                || !exchangeSelectionAllowed
        );
        computerTurnButton.setDisable(snapshot.isGameOver() || computerTurnRunning || humanTurn);
    }

    // Handles rack tile clicks for either placement or exchange mode.
    private void handleRackClick(int rackIndex) {
        GameSnapshot snapshot = game.snapshot();
        if (!snapshot.isHumanTurn() || snapshot.isGameOver() || computerTurnRunning) {
            return;
        }

        interactionOverride = null;
        if (isRackIndexUsed(rackIndex)) {
            interactionOverride = "That rack tile is already staged on the board.";
            refreshView();
            return;
        }

        if (exchangeModeButton.isSelected()) {
            if (exchangeIndexes.contains(rackIndex)) {
                exchangeIndexes.remove(rackIndex);
            } else {
                exchangeIndexes.add(rackIndex);
            }
            selectedRackIndex = null;
            refreshView();
            return;
        }

        exchangeIndexes.clear();
        selectedRackIndex = Objects.equals(selectedRackIndex, rackIndex) ? null : rackIndex;
        refreshView();
    }

    // Handles board clicks for staging and removing tentative tiles.
    private void handleBoardClick(int row, int col) {
        GameSnapshot snapshot = game.snapshot();
        if (!snapshot.isHumanTurn() || snapshot.isGameOver() || computerTurnRunning) {
            return;
        }

        interactionOverride = null;
        if (exchangeModeButton.isSelected()) {
            interactionOverride = "Exchange mode is active. Use rack tiles to choose exchange tiles.";
            refreshView();
            return;
        }

        String key = coordKey(row, col);
        if (stagedPlacements.containsKey(key)) {
            stagedPlacements.remove(key);
            refreshView();
            return;
        }

        if (game.board().isTile(row, col)) {
            interactionOverride = "That square is already occupied.";
            refreshView();
            return;
        }

        if (selectedRackIndex == null) {
            interactionOverride = "Select a rack tile before clicking the board.";
            refreshView();
            return;
        }

        char rackTile = snapshot.humanRack().charAt(selectedRackIndex);
        char placedLetter = rackTile;
        boolean blank = rackTile == '*';

        if (blank) {
            Optional<Character> chosen = chooseBlankLetter();
            if (chosen.isEmpty()) {
                interactionOverride = "Blank placement cancelled.";
                refreshView();
                return;
            }
            placedLetter = chosen.get();
        }

        stagedPlacements.put(key, new RackPlacement(selectedRackIndex, rackTile, placedLetter, blank, row, col));
        selectedRackIndex = null;
        refreshView();
    }

    // Submits the staged human move and then advances to the computer turn.
    private void handlePlayMove() {
        GameSnapshot snapshot = game.snapshot();
        if (!snapshot.isHumanTurn() || snapshot.isGameOver() || computerTurnRunning) {
            return;
        }
        if (stagedPlacements.isEmpty()) {
            interactionOverride = "Stage at least one tile before playing a move.";
            refreshView();
            return;
        }

        try {
            PlacementBuffer buffer = buildPlacementBuffer(game.board());
            game.playHumanMove(buffer);
            resetLocalSelections();
            interactionOverride = null;
            refreshView();
            runComputerTurnIfNeeded();
        } catch (IllegalArgumentException ex) {
            interactionOverride = ex.getMessage();
            refreshView();
        }
    }

    // Clears all staged tiles and exchange selections.
    private void handleClearSelections() {
        resetLocalSelections();
        interactionOverride = null;
        refreshView();
    }

    // Passes the human turn and advances to the computer.
    private void handlePassTurn() {
        GameSnapshot snapshot = game.snapshot();
        if (!snapshot.isHumanTurn() || snapshot.isGameOver() || computerTurnRunning) {
            return;
        }

        game.passHumanTurn();
        resetLocalSelections();
        interactionOverride = null;
        refreshView();
        runComputerTurnIfNeeded();
    }

    // Exchanges the selected rack tiles and advances to the computer.
    private void handleExchange() {
        GameSnapshot snapshot = game.snapshot();
        if (!snapshot.isHumanTurn() || snapshot.isGameOver() || computerTurnRunning) {
            return;
        }
        if (!exchangeModeButton.isSelected() || exchangeIndexes.isEmpty()) {
            interactionOverride = "Select one or more rack tiles while exchange mode is active.";
            refreshView();
            return;
        }

        ArrayList<Integer> indices = new ArrayList<>(exchangeIndexes);
        indices.sort(Comparator.naturalOrder());
        if (!game.canExchangeCount(indices.size())) {
            interactionOverride = "Not enough tiles remain in the bag to exchange " + indices.size() + " tile(s).";
            refreshView();
            return;
        }

        StringBuilder letters = new StringBuilder();
        for (int index : indices) {
            letters.append(snapshot.humanRack().charAt(index));
        }

        try {
            game.exchangeHumanTiles(letters.toString());
            resetLocalSelections();
            interactionOverride = null;
            refreshView();
            runComputerTurnIfNeeded();
        } catch (IllegalArgumentException ex) {
            interactionOverride = ex.getMessage();
            refreshView();
        }
    }

    // Toggles exchange mode and clears incompatible local state.
    private void handleExchangeModeToggle() {
        interactionOverride = null;
        if (exchangeModeButton.isSelected()) {
            stagedPlacements.clear();
            selectedRackIndex = null;
        } else {
            exchangeIndexes.clear();
        }
        refreshView();
    }

    // Lets the UI manually trigger the computer turn when needed.
    private void handleComputerTurnButton() {
        interactionOverride = null;
        refreshView();
        runComputerTurnIfNeeded();
    }

    // Runs the computer turn on a background thread so the window stays responsive.
    private void runComputerTurnIfNeeded() {
        if (game.isGameOver() || game.isHumanTurn() || computerTurnRunning) {
            refreshView();
            return;
        }

        computerTurnRunning = true;
        refreshView();

        Task<Void> computerTurnTask = new Task<>() {
            @Override
            protected Void call() {
                game.playComputerTurn();
                return null;
            }
        };

        computerTurnTask.setOnSucceeded(event -> {
            computerTurnRunning = false;
            interactionOverride = null;
            resetLocalSelections();
            refreshView();
        });

        computerTurnTask.setOnFailed(event -> {
            computerTurnRunning = false;
            resetLocalSelections();
            Throwable error = computerTurnTask.getException();
            interactionOverride = error == null ? "Computer turn failed." : error.getMessage();
            refreshView();
        });

        Thread worker = new Thread(computerTurnTask, "scrabble-computer-turn");
        worker.setDaemon(true);
        worker.start();
    }

    // Resets local staging and selection state after a completed turn.
    private void resetLocalSelections() {
        stagedPlacements.clear();
        exchangeIndexes.clear();
        selectedRackIndex = null;
        if (exchangeModeButton != null) {
            exchangeModeButton.setSelected(false);
        }
    }

    // Builds a placement buffer from the currently staged rack placements.
    private PlacementBuffer buildPlacementBuffer(Board baseBoard) {
        PlacementBuffer buffer = new PlacementBuffer(baseBoard);
        ArrayList<RackPlacement> placements = new ArrayList<>(stagedPlacements.values());
        placements.sort(Comparator.comparingInt(RackPlacement::row).thenComparingInt(RackPlacement::col));
        for (RackPlacement placement : placements) {
            if (placement.blank()) {
                buffer.placeBlank(placement.row(), placement.col(), placement.placedLetter());
            } else {
                buffer.placeLetter(placement.row(), placement.col(), placement.placedLetter());
            }
        }
        return buffer;
    }

    // Prompts for the represented letter when placing a blank tile.
    private Optional<Character> chooseBlankLetter() {
        ArrayList<String> choices = new ArrayList<>(26);
        for (char letter = 'A'; letter <= 'Z'; letter++) {
            choices.add(String.valueOf(letter));
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>("A", choices);
        dialog.setTitle("Blank Tile");
        dialog.setHeaderText("Choose the letter for the blank tile");
        dialog.setContentText("Letter:");
        return dialog.showAndWait().map(choice -> Character.toLowerCase(choice.charAt(0)));
    }

    // Returns whether a rack slot is already consumed by a staged board placement.
    private boolean isRackIndexUsed(int rackIndex) {
        for (RackPlacement placement : stagedPlacements.values()) {
            if (placement.rackIndex() == rackIndex) {
                return true;
            }
        }
        return false;
    }

    // Returns whether a board coordinate currently holds a staged tile.
    private boolean isStaged(int row, int col) {
        return stagedPlacements.containsKey(coordKey(row, col));
    }

    // Resolves the visible turn chip text.
    private String resolveTurnLabel(GameSnapshot snapshot) {
        if (snapshot.isGameOver()) {
            return "Game Over";
        }
        if (computerTurnRunning) {
            return "Computer Thinking";
        }
        return snapshot.isHumanTurn() ? "Human Turn" : "Computer Turn";
    }

    // Builds the current instruction text from selection and turn state.
    private String resolveInteractionMessage(GameSnapshot snapshot) {
        if (interactionOverride != null && !interactionOverride.isBlank()) {
            return interactionOverride;
        }
        if (snapshot.isGameOver()) {
            return snapshot.winnerSummary();
        }
        if (computerTurnRunning) {
            return "Computer is searching for the best move...";
        }
        if (!snapshot.isHumanTurn()) {
            return "Computer turn is ready. Use the button if it has not started yet.";
        }
        if (!game.canExchangeCount(1)) {
            return "Select a rack tile, then click empty board squares to stage your move. Exchanges are unavailable because the bag is empty.";
        }
        if (exchangeModeButton.isSelected()) {
            if (exchangeIndexes.isEmpty()) {
                return "Exchange mode: click rack tiles to choose which ones to swap.";
            }
            if (!game.canExchangeCount(exchangeIndexes.size())) {
                return "Exchange mode: only " + snapshot.tileBagRemaining() + " tile(s) remain in the bag.";
            }
            return "Exchange mode: " + exchangeIndexes.size() + " tile(s) selected.";
        }
        if (!stagedPlacements.isEmpty()) {
            return "Move staged: click a staged square to remove it, or play the move.";
        }
        if (selectedRackIndex != null) {
            char tile = snapshot.humanRack().charAt(selectedRackIndex);
            if (tile == '*') {
                return "Blank selected: click an empty square, then choose its letter.";
            }
            return "Tile " + Character.toUpperCase(tile) + " selected. Click an empty square to place it.";
        }
        return "Select a rack tile, then click empty board squares to stage your move.";
    }

    // Maps the board state to the visible text for one square.
    private String boardCellText(Board displayBoard, int row, int col) {
        if (displayBoard.isTile(row, col)) {
            return String.valueOf(Character.toUpperCase(displayBoard.tileAt(row, col)));
        }

        String token = game.board().get(row, col);
        return switch (token) {
            case "3." -> "TW";
            case "2." -> "DW";
            case ".3" -> "TL";
            case ".2" -> "DL";
            default -> "";
        };
    }

    // Returns the text color for one square.
    private Color boardCellTextColor(Board baseBoard, int row, int col) {
        if (isStaged(row, col)) {
            return Color.web("#111111");
        }
        if (baseBoard.isTile(row, col)) {
            return Color.web("#111111");
        }

        return switch (baseBoard.get(row, col)) {
            case "3.", "2.", ".3", ".2" -> Color.web("#222222");
            default -> Color.web("#555555");
        };
    }

    // Returns the background style for one square.
    private String boardCellStyle(Board baseBoard, int row, int col) {
        if (isStaged(row, col)) {
            return squareStyle("#d2b55b");
        }
        if (baseBoard.isTile(row, col)) {
            return squareStyle("#d8c48e");
        }

        return switch (baseBoard.get(row, col)) {
            case "3." -> squareStyle("#e47c7c");
            case "2." -> squareStyle("#f0b46a");
            case ".3" -> squareStyle("#77b6e8");
            case ".2" -> squareStyle("#9dcf74");
            default -> squareStyle("#ece8df");
        };
    }

    // Builds the visible text for a rack tile button.
    private String rackButtonText(char tile) {
        return Character.toUpperCase(tile) + "\n" + TileValues.faceValue(tile);
    }

    // Returns the style for a rack tile button based on local UI state.
    private String rackButtonStyle(int rackIndex, char tile, GameSnapshot snapshot) {
        if (isRackIndexUsed(rackIndex)) {
            return rackStyle("#c6c6c6", "#555555");
        }
        if (exchangeIndexes.contains(rackIndex)) {
            return rackStyle("#4f8a4f", "#ffffff");
        }
        if (Objects.equals(selectedRackIndex, rackIndex)) {
            return rackStyle("#4f8a4f", "#ffffff");
        }
        if (!snapshot.isHumanTurn() || computerTurnRunning || snapshot.isGameOver()) {
            return rackStyle("#dddddd", "#666666");
        }
        return rackStyle("#e0cfa3", "#111111");
    }

    // Creates the turn label.
    private Label createChipLabel() {
        Label label = createMetricLabel();
        label.setStyle(
            "-fx-background-color: #666666;"
                + "-fx-padding: 8 14 8 14;"
                + "-fx-text-fill: white;"
        );
        return label;
    }

    // Creates one metric label.
    private Label createMetricLabel() {
        Label label = new Label();
        label.setFont(Font.font(UI_FONT, FontWeight.BOLD, 14));
        label.setStyle(
            "-fx-background-color: #efefef;"
                + "-fx-padding: 8 14 8 14;"
                + "-fx-text-fill: #222222;"
        );
        return label;
    }

    // Creates one action button with shared styling.
    private Button createActionButton(String text, Runnable action) {
        Button button = new Button(text);
        button.setFont(Font.font(UI_FONT, FontWeight.BOLD, 13));
        button.setOnAction(event -> action.run());
        button.setStyle(actionButtonStyle("#666666"));
        return button;
    }

    // Builds a small info card for the side panel.
    private VBox createInfoCard(String title, String body) {
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font(UI_FONT, FontWeight.BOLD, 14));
        titleLabel.setTextFill(Color.web("#222222"));

        VBox box = new VBox(6, titleLabel);
        if (body != null) {
            Label bodyLabel = new Label(body);
            bodyLabel.setWrapText(true);
            bodyLabel.setFont(Font.font(UI_FONT, 13));
            bodyLabel.setTextFill(Color.web("#333333"));
            box.getChildren().add(bodyLabel);
        }
        box.setPadding(new Insets(12));
        box.setStyle("-fx-background-color: #efefef;");
        return box;
    }

    // Returns a compact square style string.
    private String squareStyle(String background) {
        return "-fx-background-color: " + background + ";";
    }

    // Returns a rack tile style string.
    private String rackStyle(String background, String textColor) {
        return "-fx-background-color: " + background + ";"
            + "-fx-background-radius: 0;"
            + "-fx-border-radius: 0;"
            + "-fx-border-color: transparent;"
            + "-fx-text-fill: " + textColor + ";";
    }

    // Returns a shared action button style string.
    private String actionButtonStyle(String background) {
        return "-fx-background-color: " + background + ";"
            + "-fx-text-fill: white;"
            + "-fx-background-radius: 0;"
            + "-fx-border-radius: 0;"
            + "-fx-border-color: transparent;"
            + "-fx-padding: 10 16 10 16;";
    }

    // Shows a fatal startup error and stops the app.
    private void showFatalError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
        Platform.exit();
    }

    // Produces the stable coordinate key used by local staging state.
    private String coordKey(int row, int col) {
        return row + "," + col;
    }

    // UI-local description of one staged rack tile placement.
    private static final class RackPlacement {
        private final int rackIndex;
        private final char rackTile;
        private final char placedLetter;
        private final boolean blank;
        private final int row;
        private final int col;

        private RackPlacement(int rackIndex, char rackTile, char placedLetter, boolean blank, int row, int col) {
            this.rackIndex = rackIndex;
            this.rackTile = rackTile;
            this.placedLetter = Character.toLowerCase(placedLetter);
            this.blank = blank;
            this.row = row;
            this.col = col;
        }

        private int rackIndex() {
            return rackIndex;
        }

        private char placedLetter() {
            return placedLetter;
        }

        private boolean blank() {
            return blank;
        }

        private int row() {
            return row;
        }

        private int col() {
            return col;
        }
    }
}
