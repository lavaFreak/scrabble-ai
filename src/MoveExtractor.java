import java.util.ArrayList;
import java.util.List;

public class MoveExtractor {

    public List<PlayedTile> collectNewTiles(Board original, Board result) {
        List<PlayedTile> played = new ArrayList<>();
        for (int r = 0; r < original.size(); r++) {
            for (int c = 0; c < original.size(); c++) {
                if (!original.isTile(r, c) && result.isTile(r, c)) {
                    played.add(new PlayedTile(result.tileAt(r, c), r, c));
                }
            }
        }
        return played;
    }

    public String formatPlay(List<PlayedTile> played) {
        if (played.isEmpty()) {
            return "play is empty";
        }

        StringBuilder sb = new StringBuilder("play is ");
        for (int i = 0; i < played.size(); i++) {
            PlayedTile tile = played.get(i);
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(tile.letter)
                .append(" at (")
                .append(tile.row)
                .append(", ")
                .append(tile.col)
                .append(")");
        }
        return sb.toString();
    }
}
