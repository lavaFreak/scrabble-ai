#!/bin/sh

set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
CLASS_DIR="$ROOT_DIR/build/submission/classes"

rm -rf "$CLASS_DIR"
mkdir -p "$CLASS_DIR"

javac -d "$CLASS_DIR" "$ROOT_DIR"/src/*.java
jar cfe "$ROOT_DIR/Scorechecker.jar" Scorechecker -C "$CLASS_DIR" .
jar cfe "$ROOT_DIR/Solver.jar" Solver -C "$CLASS_DIR" .

echo "Built $ROOT_DIR/Scorechecker.jar"
echo "Built $ROOT_DIR/Solver.jar"
