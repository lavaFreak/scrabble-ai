# Project 3 Part 1: Scrabble Scorer

This repository contains **Part 1** of CS 351 Project 3: the scorer program.

## Goal
Given an initial board and a resulting board (after a move), determine:
- the letters played and their coordinates,
- whether the play is legal,
- the score (only if legal).

## Required Program Behavior
- Program name/entry: `Scorechecker` (packaged as a jar).
- Run format:
  - `java -jar Scorechecker.jar <dictionary-file>`
- Input source: standard input (not command-line move arguments).
- Must process **multiple test cases** until EOF.
- Output must match expected formatting exactly (including whitespace/punctuation).

## Part 1 Output Includes
- original board,
- result board,
- play details (`letter at (row, col)` for each placed tile),
- legality (`play is legal` / `play is not legal`),
- score if legal.

## Notes
- Board and dictionary formats follow the Project 3 prompt (`prompt3.pdf`).
- This repo is intentionally scoped to the scorer stage of the full Scrabble project.
