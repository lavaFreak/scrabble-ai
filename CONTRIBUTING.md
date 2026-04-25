# Contributing

## What To Read First
- [README.md](README.md) for the main run and verification commands
- [docs/REVIEW_GUIDE.md](docs/REVIEW_GUIDE.md) for the codebase walkthrough
- `src/ScrabbleGame.java` for the main game-state model
- `src/SolverEngine.java` and `src/WordFinder.java` for the solver pipeline

## Local Setup

For backend source compilation and the public regression suites, a standard JDK is enough.

```sh
javac -d build/classes $(find src -name '*.java' ! -name 'Scrabble.java' -print) tests/*.java
```

For UI work, use a JavaFX-enabled JDK and compile from the repository root:

```sh
javac -d build/classes src/*.java
java -cp build/classes Scrabble
```

## Before You Change Code
- Pick one area of behavior to touch at a time.
- Prefer focused source compilation unless your change is specifically in the UI.
- Keep dictionary and config file assumptions explicit in tests or examples.

## Validation Checklist

For source compilation:

```sh
javac -d build/classes $(find src -name '*.java' ! -name 'Scrabble.java' -print) tests/*.java
```

For example-file validation:

```sh
java -jar Scorechecker.jar Resources/dictionaries/sowpods.txt < Resources/examples/example_score_input.txt > /tmp/scorechecker-output.txt
java -jar Solver.jar Resources/dictionaries/sowpods.txt < Resources/examples/example_input.txt > /tmp/solver-output.txt
```

For the public regression suites:

```sh
java -cp build/classes ScorecheckerSoFarTests
java -cp build/classes Part2FoundationTests
java -cp build/classes Part3GameTests
```

## Suggested Contribution Areas
- scorer correctness and edge-case handling
- solver move generation and tie behavior
- backend legality checks, move application, and state transitions
- JavaFX interaction polish and move-history display
- test maintenance, new example cases, and expected-output updates

## Good Collaboration Ideas
- add more small reproducible scorer and solver cases under `Resources/examples/`
- improve README and review-guide explanations around the solver pipeline
- tighten backend edge-case coverage around passes, exchanges, and endgame scoring
- improve JavaFX quality-of-life details such as move feedback, status text, or history readability
- refactor large backend methods into smaller units without changing public behavior

## If You Want A Good First Contribution
Start with one of these:
- add a new example input/output pair and document what behavior it covers
- add a regression test for a scoring or legality edge case
- improve a focused piece of UI behavior without changing game rules

Small, well-scoped pull requests are the easiest to review in this project.

## Repo Conventions
- Keep generated logs local; `game-logs/` stays out of git.
- Prefer small, isolated changes over mixed refactors.
- When adding examples, update the matching expected-output files too.
