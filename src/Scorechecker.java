import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Scorechecker {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("usage: java -jar Scorechecker.jar <dictionary-file>");
            return;
        }

        String dictionaryPath = args[0];
        // TODO: Load dictionary from dictionaryPath.

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            // TODO: Read one scorer test case:
            // 1) original board
            // 2) result board
            // Stop at EOF.
            String firstLine = in.readLine();
            if (firstLine == null) {
                break;
            }

            // TODO: Parse and process the case, then print exact required output.
        }
    }
}
