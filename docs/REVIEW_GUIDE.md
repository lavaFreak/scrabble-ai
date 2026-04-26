# Review Guide

This guide is for a quick technical walkthrough of the codebase.

## Fast Review Path

If you only have a few minutes, read in this order:
- [README.md](../README.md)
- `src/ScrabbleGame.java`
- `src/SolverEngine.java`
- `src/WordFinder.java`
- `tests/Part3GameTests.java`

That path shows the project at three levels: overall purpose, core game model, solver logic, and concrete regression coverage.

## What Lives Where
- `src/ScrabbleGame.java`
  Main game-state coordinator for turns, racks, scores, exchanges, passes, and endgame behavior
- `src/SolverEngine.java`
  High-level solver orchestration
- `src/WordFinder.java`
  Candidate search and highest-scoring move selection
- `src/Scorer.java` and `src/LegalityChecker.java`
  Move validation and scoring logic
- `src/Scrabble.java`
  JavaFX UI shell
- `tests/`
  Public regression coverage for scorer, solver foundations, and game-state behavior
- `Resources/examples/`
  Small reproducible scorer and solver cases used for validation
- root `.jar` files
  Prebuilt artifacts that make quick review easier

## Discussion Points
- how solver logic and game-state rules share one backend instead of drifting apart
- how legality checking, move application, and scoring stay synchronized
- how the project uses both focused regression suites and bundled example cases
- what tradeoffs come from keeping the UI thin and the backend stateful

## Commands Worth Running

Quick source compilation and regression:

```sh
javac -d build/classes $(find src -name '*.java' ! -name 'Scrabble.java' -print) tests/*.java
java -cp build/classes Part3GameTests
```

Example-driven solver check:

```sh
java -jar Solver.jar Resources/dictionaries/sowpods.txt < Resources/examples/example_input.txt
```

UI launch:

```sh
java -cp build/classes Scrabble
```

## If You Want To Go Deeper
- Read [CONTRIBUTING.md](../CONTRIBUTING.md) for the intended local workflow
- Open `docs/scrabble-architecture.jpg` for the high-level structure
- Look at `Resources/examples/` for small reproducible solver and scorer cases
