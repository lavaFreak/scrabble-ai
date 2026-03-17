# Project 3 Part 2: Scrabble Solver

This repository is for **Part 2** of CS 351 Project 3: building a Scrabble solver.

## Goal
Given a board state and a tray of letters, find the **highest-scoring legal move** and print:
- the input board,
- the tray,
- the selected solution word and score,
- the solution board after placing the move.

## Input and Output
Input is provided through **standard input** and may contain multiple test cases until EOF.

Each solver test case contains:
1. board size `N`
2. `N` board rows
3. one tray line (letters, possibly including `*` for blank tiles)

Output formatting must match the project examples exactly, including labels, spacing, and blank lines.

Reference files:
1. [`Resources/examples/example_input.txt`](/Users/garion/UNM/JavaFX/CS351/Scrabble/Resources/examples/example_input.txt)
2. [`Resources/examples/example_output.txt`](/Users/garion/UNM/JavaFX/CS351/Scrabble/Resources/examples/example_output.txt)

## Part 2 Requirements
- Enumerate legal placements that can be formed from the tray.
- Validate candidate moves using Scrabble legality rules.
- Score each legal move with board multipliers and tile values.
- Support blank tiles (`*`) correctly.
- Select the best move according to the Part 2 prompt tie-breaking rules.

## Project Notes
- Dictionaries are stored under `Resources/dictionaries/`.
- Board/tile configuration examples are under `Resources/config/`.
- The Part 1 scorer code in `src/` is intended to be reused/refactored as solver components are added.

## Build And Run
Build both required submission jars from the repository root:

```sh
sh build_submission_jars.sh
```

This creates both `Scorechecker.jar` and `Solver.jar` with the required prompt-facing main classes.

Run the scorer exactly in the required format:

```sh
java -jar Scorechecker.jar Resources/dictionaries/sowpods.txt < Resources/examples/example_score_input.txt
```

Run the solver exactly in the required format:

```sh
java -jar Solver.jar Resources/dictionaries/sowpods.txt < Resources/examples/example_input.txt
```

To verify the scorer output against the provided example:

```sh
java -jar Scorechecker.jar Resources/dictionaries/sowpods.txt < Resources/examples/example_score_input.txt > /tmp/scorechecker-output.txt
ruby -e 'expected = File.read("Resources/examples/example_score_output.txt").gsub("\r\n", "\n"); actual = File.read("/tmp/scorechecker-output.txt").gsub("\r\n", "\n"); abort("output mismatch") unless expected == actual'
```

To verify the solver output against the provided example:

```sh
java -jar Solver.jar Resources/dictionaries/sowpods.txt < Resources/examples/example_input.txt > /tmp/solver-output.txt
ruby -e 'expected = File.read("Resources/examples/example_output.txt").gsub("\r\n", "\n"); actual = File.read("/tmp/solver-output.txt").gsub("\r\n", "\n"); abort("output mismatch") unless expected == actual'
```

The solver uses a deterministic highest-score search. If multiple moves tie for best score, it keeps the first
highest-scoring move encountered, which is allowed by the prompt.

## Tests
Compile and run the current regression suites from the repository root:

```sh
javac -d build/classes src/*.java tests/*.java
java -cp build/classes ScorecheckerSoFarTests
java -cp build/classes Part2FoundationTests
```
