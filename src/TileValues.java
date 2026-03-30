/**
 * Author: Garion
 *
 * File purpose: provide shared standard Scrabble tile values.
 */
public final class TileValues {
    private TileValues() {
    }

    /**
     * Returns the standard face value for a rack or board tile.
     *
     * @param tile lowercase letter tile or '*'
     * @return standard Scrabble face value
     */
    public static int faceValue(char tile) {
        char normalized = Character.toLowerCase(tile);
        return switch (normalized) {
            case '*'
                -> 0;
            case 'a', 'e', 'i', 'l', 'n', 'o', 'r', 's', 't', 'u'
                -> 1;
            case 'd', 'g'
                -> 2;
            case 'b', 'c', 'm', 'p'
                -> 3;
            case 'f', 'h', 'v', 'w', 'y'
                -> 4;
            case 'k'
                -> 5;
            case 'j', 'x'
                -> 8;
            case 'q', 'z'
                -> 10;
            default
                -> 0;
        };
    }
}
