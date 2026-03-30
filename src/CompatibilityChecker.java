/**
 * Author: Garion
 *
 * File purpose: validate that a proposed result board is structurally compatible with its original board.
 *
 * This check should run before any move extraction, legality, or scoring logic.
 * Use this check as the first gate before move extraction or legality tests.
 */
public class CompatibilityChecker {

    /**
     * Returns first incompatibility message between original and result boards.
     *
     * @param original board before move
     * @param result board after move
     * @return incompatibility description, or null when compatible
     */
    public String findIncompatibility(Board original, Board result) {
        if (original.size() != result.size()) {
            return "board size mismatch";
        }

        for (int r = 0; r < original.size(); r++) {
            for (int c = 0; c < original.size(); c++) {
                boolean originalTile = original.isTile(r, c);
                boolean resultTile = result.isTile(r, c);

                if (originalTile && !resultTile) {
                    return "tile removed at (" + r + ", " + c + ")";
                }

                if (originalTile && resultTile && original.tileAt(r, c) != result.tileAt(r, c)) {
                    return "tile changed at (" + r + ", " + c + ")";
                }

                if (!originalTile && !resultTile && !original.get(r, c).equals(result.get(r, c))) {
                    return "multiplier mismatch at (" + r + ", " + c + ")";
                }
            }
        }

        return null;
    }
}
