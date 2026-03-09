import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class Dictionary {
    private final Set<String> words;

    private Dictionary(Set<String> words) {
        this.words = words;
    }

    public static Dictionary fromFile(String dictionaryPath) throws IOException {
        Set<String> loaded = new HashSet<>();
        try (BufferedReader in = new BufferedReader(new FileReader(dictionaryPath))) {
            String line;
            while ((line = in.readLine()) != null) {
                String word = line.trim().toLowerCase();
                if (!word.isEmpty()) {
                    loaded.add(word);
                }
            }
        }
        return new Dictionary(loaded);
    }

    public boolean contains(String word) {
        return words.contains(word.toLowerCase());
    }
}
