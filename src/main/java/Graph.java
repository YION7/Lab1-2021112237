import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * The main class of the program.
 * DirectedGraph object, used for storing the directed graph.
 * TextProcessor object, used for processing text.
 * GraphVisualizer object, used for visualizing the directed graph.
 * Scanner object, used for handling user input.
 */
public class Graph {
  private static final DirectedGraph directedGraph = new DirectedGraph();
  private static final TextProcessor textProcessor = new TextProcessor();
  private static final GraphVisualizer graphVisualizer = new GraphVisualizer();
  private static final Scanner scanner = new Scanner(System.in);

  private static String filePath;
  private static final int[] taskCounts = new int[5];

  /**
   * The entry point of the program, used to handle user input and execute corresponding tasks.
   * 1. Input new file
   * 2. Query bridge words
   * 3. Process new text
   * 4. Calculate all shortest paths
   * 5. Random traversal
   * 6. Exit
   */
  public static void main(String[] args) {
    while (true) {
      System.out.println("Enter task number (1-7):");
      System.out.println("1: Input New File");
      System.out.println("2: Query Bridge Words");
      System.out.println("3: Process New Text");
      System.out.println("4: Calculate All Shortest Paths");
      System.out.println("5: Random Traversal");
      System.out.println("6: Exit");

      int task = scanner.nextInt();
      scanner.nextLine();  // consume newline left-over

      if (task == 6) {
        return;
      } else if (task == 1) {
        changeFile(getInput("Input the new file path:"), getInput("Input the new file name:"));
      } else {
        executeTask(task - 1);
      }
    }
  }

  /**
   * Method to change the file path and file name, and reload the directed graph.
   *
   * @param newFilePath The new file path.
   * @param newFileName The new file name.
   */
  public static void changeFile(String newFilePath, String newFileName) {
    filePath = newFilePath;
    String fullFilePath = filePath + "/" + newFileName;
    try {
      directedGraph.clearGraph();
      directedGraph.createGraph(textProcessor.processFile(fullFilePath));
      graphVisualizer.showDirectedGraph(fullFilePath, directedGraph.getGraph());
      Arrays.fill(taskCounts, 0);
    } catch (IOException e) {
      System.out.println("An error occurred while processing the file. Please try again.");
    }
  }

  /**
   * Method to execute the corresponding task based on the user input.
   *
   * @param task The task number.
   */
  public static void executeTask(int task) {
    taskCounts[task]++;
    switch (task) {
      case 1: queryBridgeWords();
        break;
      case 2: processNewText();
        break;
      case 3: calAllShortestPaths();
        break;
      case 4: randomTraversal();
        break;
      default: System.out.println("Invalid task. Enter a number 1-6.");
        break;
    }
  }

  /**
   * Method to query the bridge words between two words.
   */
  public static void queryBridgeWords() {
    System.out.println("Input word1:");
    String w1 = scanner.nextLine().trim();
    System.out.println("Input word2:");
    String w2 = scanner.nextLine().trim();
    String result = directedGraph.queryBridgeWords(w1, w2);
    System.out.println(result);
  }

  /**
   * Method to process a new text and generate a new text with bridge words.
   */
  public static void processNewText() {
    System.out.println("Input a new text:");
    String newText = scanner.nextLine();
    generateNewText(newText);
  }

  /**
   * Method to calculate all shortest paths between two words.
   */
  public static void calAllShortestPaths() {
    System.out.println("Input word1:");
    String w1 = scanner.nextLine().trim();
    System.out.println("Input word2:");
    String w2 = scanner.nextLine().trim();

    if (!directedGraph.getGraph().containsKey(w1) || !directedGraph.getGraph().containsKey(w2)) {
      System.out.println("No " + w1 + " or " + w2 + " in the graph!");
      return;
    }

    List<List<String>> allShortestPaths = directedGraph.calAllShortestPath(w1, w2);

    if (allShortestPaths.isEmpty()) {
      System.out.println("No path found from " + w1 + " to " + w2);
      return;
    }

    //    int pathCount = 1;
    //    for (List<String> shortestPath : allShortestPaths) {
    //      String distance = shortestPath.getFirst();
    //      pathCount++;
    //    }

    String pathsPngPath = filePath + "/paths" + taskCounts[3] + ".png";
    try {
      graphVisualizer.showAllPaths(pathsPngPath, allShortestPaths);
    } catch (IOException e) {
      System.out.println("An error occurred while saving the shortest paths.");
    }
  }

  /**
   * Method to perform a random traversal of the directed graph.
   */
  public static void randomTraversal() {
    String pngPath = filePath + "/travel" + taskCounts[4] + ".txt";
    try {
      directedGraph.randomTraversal(pngPath);
    } catch (IOException e) {
      System.out.println("An error occurred while saving the random traversal.");
    }
  }

  /**
   * Method to generate a new text with bridge words.
   *
   * @param newText The new text.
   */
  public static void generateNewText(String newText) {
    String[] words = newText.split(" ");
    StringBuilder result = new StringBuilder();

    for (int i = 0; i < words.length - 1; i++) {
      String word1 = words[i];
      String word2 = words[i + 1];
      List<String> bridgeWords = directedGraph.findBridgeWords(word1, word2);

      result.append(word1).append(" ");
      if (!bridgeWords.isEmpty()) {
        int randomIndex = new Random().nextInt(bridgeWords.size());
        String bridgeWord = bridgeWords.get(randomIndex);
        result.append(bridgeWord).append(" ");
      }
    }
    result.append(words[words.length - 1]);
    System.out.println(result);
  }

  private static String getInput(String prompt) {
    System.out.println(prompt);
    return scanner.nextLine().trim();
  }
}
