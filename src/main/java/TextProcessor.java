import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

class TextProcessor {
  /**
   * Process the text in the file and return the processed text.
   *
   * @param filepath The file path.
   */
  public String processFile(String filepath) throws IOException {
    StringBuilder stringBuilder = new StringBuilder();
    try (Stream<String> stream = Files.lines(Paths.get(filepath))) {
      stream.forEach(line -> stringBuilder.append(line).append(" "));
    }
    return processText(stringBuilder.toString());
  }

  /**
   * Process the text and return the processed text.
   *
   * @param text The text.
   */
  private String processText(String text) {
    text = text.replaceAll("[^a-zA-Z ]", " ");
    return text;
  }
}