import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

class GraphVisualizer {
  private StringBuilder dotGraph;
  private final String[] colors =
        {"red", "blue", "green", "purple", "orange", "brown", "pink", "black"};

  public void showDirectedGraph(String filePath, Map<String,
                                Map<String, Integer>> graph) throws IOException {

    dotGraph = new StringBuilder("digraph G {");
    final String  png_path = convertTxtToPng(filePath);
    for (String node : graph.keySet()) {
      Map<String, Integer> edges = graph.get(node);
      for (String edge : edges.keySet()) {
        dotGraph.append(node).append(" -> ").append(edge).append(" [label=\"")
                .append(edges.get(edge)).append("\"];");
      }
    }
    dotGraph.append("}");

    System.out.println(dotGraph);
    MutableGraph g = new Parser().read(dotGraph.toString());
    Graphviz.fromGraph(g).width(700).render(Format.PNG).toFile(new File(png_path));
  }

  private boolean isInShortestPath(String node1, String node2, List<String> shortestPath) {
    int index1 = shortestPath.indexOf(node1);
    int index2 = shortestPath.indexOf(node2);
    return (index1 != -1 && index2 != -1 && Math.abs(index1 - index2) == 1 && index1 < index2);
  }

  public void showAllPaths(String filePath,
                           List<List<String>> allShortestPaths) throws IOException {
    String[] lines = dotGraph.toString().trim().split(";");
    StringBuilder localDotGraph = new StringBuilder();

    for (int i = 0; i < lines.length; i++) {
      String line = lines[i].trim();

      if (line.contains("->")) {
        String[] parts = line.split("->");
        String node1 = parts[0].trim().split(" ")[parts[0].trim().split(" ").length - 1]
                               .replaceAll("[^a-zA-Z]", "");
        String node2 = parts[1].trim().split(" ")[0].replaceAll("[^a-zA-Z]", "");

        for (int j = 0; j < allShortestPaths.size(); j++) {
          List<String> shortestPath =
                  allShortestPaths.get(j).subList(1, allShortestPaths.get(j).size());
          if (isInShortestPath(node1, node2, shortestPath)) {
            line = line.replace("]", ", color=" + colors[j % colors.length] + "]");
            break;
          }
        }
      }
      localDotGraph.append(line);
      if (i < lines.length - 1) {
        localDotGraph.append(";");
      }
    }
    MutableGraph g = new Parser().read(localDotGraph.toString());
    Graphviz.fromGraph(g).width(700).render(Format.PNG).toFile(new File(filePath));
  }

  private String convertTxtToPng(String filename) {
    if (filename.endsWith(".txt")) {
      return filename.substring(0, filename.length() - 4) + ".png";
    }
    return filename;
  }
}
