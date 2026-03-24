# Project 3: Scrabble

This repository contains the shared code for CS 351 Project 3:
- `Scorechecker` for the Part 1 scorer
- `Solver` for the Part 2 highest-scoring move finder
- the Part 3 game backend that the JavaFX UI will sit on

## Current Status
- Part 1 scorer is implemented and packaged as [Scorechecker.jar](/Users/garion/UNM/JavaFX/CS351/Scrabble/Scorechecker.jar).
- Part 2 solver is implemented and packaged as [Solver.jar](/Users/garion/UNM/JavaFX/CS351/Scrabble/Solver.jar).
- Part 3 backend is implemented in shared model/controller classes such as [ScrabbleGame.java](/Users/garion/UNM/JavaFX/CS351/Scrabble/src/ScrabbleGame.java), [MoveResolver.java](/Users/garion/UNM/JavaFX/CS351/Scrabble/src/MoveResolver.java), [PlacementBuffer.java](/Users/garion/UNM/JavaFX/CS351/Scrabble/src/PlacementBuffer.java), and [GameSnapshot.java](/Users/garion/UNM/JavaFX/CS351/Scrabble/src/GameSnapshot.java).
- The JavaFX `Scrabble.java` UI entry point is still to be built on top of that backend.

## Project Layout
- Dictionaries live under [Resources/dictionaries](/Users/garion/UNM/JavaFX/CS351/Scrabble/Resources/dictionaries).
- Board and tile configuration files live under [Resources/config](/Users/garion/UNM/JavaFX/CS351/Scrabble/Resources/config).
- Example scorer and solver inputs/outputs live under [Resources/examples](/Users/garion/UNM/JavaFX/CS351/Scrabble/Resources/examples).
- The current architecture snapshot is documented in [scrabble-architecture.drawio](/Users/garion/UNM/JavaFX/CS351/Scrabble/docs/scrabble-architecture.drawio).

## Scorer And Solver
Both console programs read repeated cases from standard input until EOF.

Run the scorer:

```sh
java -jar Scorechecker.jar Resources/dictionaries/sowpods.txt < Resources/examples/example_score_input.txt
```

Run the solver:

```sh
java -jar Solver.jar Resources/dictionaries/sowpods.txt < Resources/examples/example_input.txt
```

Verify scorer output against the provided example:

```sh
java -jar Scorechecker.jar Resources/dictionaries/sowpods.txt < Resources/examples/example_score_input.txt > /tmp/scorechecker-output.txt
ruby -e 'expected = File.read("Resources/examples/example_score_output.txt").gsub("\r\n", "\n"); actual = File.read("/tmp/scorechecker-output.txt").gsub("\r\n", "\n"); abort("output mismatch") unless expected == actual'
```

Verify solver output against the provided example:

```sh
java -jar Solver.jar Resources/dictionaries/sowpods.txt < Resources/examples/example_input.txt > /tmp/solver-output.txt
ruby -e 'expected = File.read("Resources/examples/example_output.txt").gsub("\r\n", "\n"); actual = File.read("/tmp/solver-output.txt").gsub("\r\n", "\n"); abort("output mismatch") unless expected == actual'
```

The solver keeps the first highest-scoring move it encounters when scores tie, which is allowed by the prompt.

## Part 3 Backend
The current game backend supports:
- human and computer players with independent racks and scores
- a mutable tile bag with standard Scrabble tile frequencies
- solver-driven computer turns
- validated human moves through board transitions or a [PlacementBuffer.java](/Users/garion/UNM/JavaFX/CS351/Scrabble/src/PlacementBuffer.java)
- passes, exchanges, turn history, and endgame leave scoring
- immutable UI snapshots through [GameSnapshot.java](/Users/garion/UNM/JavaFX/CS351/Scrabble/src/GameSnapshot.java)

This is intended to let the JavaFX layer focus on rendering and interaction instead of re-implementing game rules.

## Java Setup
IntelliJ is configured to use the `zulu-25` JDK, which includes JavaFX modules.

If you want terminal `java` and `javac` to use the same JDK as the IDE, use:

```sh
export JAVA_HOME=/Library/Java/JavaVirtualMachines/zulu-25.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"
```

This matters for Part 3, because plain shell `javac` against a non-FX JDK will fail on `javafx.*` imports.

## Tests
Compile and run the current regression suites from the repository root:

```sh
javac -d build/classes src/*.java tests/*.java
java -cp build/classes ScorecheckerSoFarTests
java -cp build/classes Part2FoundationTests
java -cp build/classes Part3GameTests
```
