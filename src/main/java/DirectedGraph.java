import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Factory;
import guru.nidi.graphviz.model.MutableGraph;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

class DirectedGraph {
  private  final Map<String, Map<String, Integer>> graph = new ConcurrentHashMap<>();

  public void clearGraph() {
    graph.clear();  // 清空有向图
  }

  public void createGraph(String text) {
    String[] words = text.split(" ");
    for (int i = 0; i < words.length; i++) {
      String word1 = words[i].toLowerCase();
      graph.computeIfAbsent(word1, k -> new ConcurrentHashMap<>());
      if (i < words.length - 1) {
        String word2 = words[i + 1].toLowerCase();
        Map<String, Integer> edges = graph.get(word1);
        edges.put(word2, edges.getOrDefault(word2, 0) + 1);
      }
    }
  }

  public final Map<String, Map<String, Integer>> getGraph() {
    return this.graph;
  }

  public List<String> bridgeWords(String word1, String word2) {

    List<String> bridgeWords = new ArrayList<>();
    Map<String, Integer> edges1 = graph.get(word1);
    for (String intermediate : edges1.keySet()) {
      Map<String, Integer> edges2 = graph.get(intermediate);
      if (edges2.containsKey(word2)) {
        bridgeWords.add(intermediate);
      }
    }
    return bridgeWords;
  }

  public String queryBridgeWords(String word1, String word2) {
    if (word1.equals(word2)) {
      if (!graph.containsKey(word1)) {
        return "No " + word1 + " in the graph!";
      } else {
        return "The bridge words from " + word1 + " to " + word1 + " : "
                + word1 + ".";
      }
    } else {
      int flag1 = 0;
      int flag2 = 0;
      if (!graph.containsKey(word1)) {
        flag1 = 1;
      }
      if (!graph.containsKey(word2)) {
        flag2 = 1;
      }

      if (flag1 == 1 && flag2 == 1) {
        return "No " + word1 + " and " + word2 + " in the graph!";
      } else if (flag1 == 1) {
        return "No " + word1 + " in the graph!";
      } else if (flag2 == 1) {
        return "No " + word2 + " in the graph!";
      }
    }

    List<String> bridgeWords = bridgeWords(word1, word2);

    if (bridgeWords.isEmpty()) {
      return "No bridge words from " + word1 + " to " + word2 + "!";
    } else {
      return "The bridge words from " + word1 + " to " + word2 + " : "
              + String.join(", ", bridgeWords) + ".";
    }
  }

  public List<String> findBridgeWords(String word1, String word2) {
    if (!graph.containsKey(word1) || !graph.containsKey(word2)) {
      return new ArrayList<>();
    }
    return bridgeWords(word1, word2);
  }

  public List<List<String>> calAllShortestPath(String word1, String word2) {
    if (!graph.containsKey(word1) || !graph.containsKey(word2)) {
      return new ArrayList<>();
    }

    Map<String, Integer> distances = new HashMap<>();
    Map<String, List<String>> parents = new HashMap<>();
    PriorityQueue<String> queue = new PriorityQueue<>(Comparator.comparingInt(distances::get));

    for (String node : graph.keySet()) {
      distances.put(node, node.equals(word1) ? 0 : Integer.MAX_VALUE);
      parents.put(node, new ArrayList<>());
    }

    queue.add(word1);

    while (!queue.isEmpty()) {
      String current = queue.poll();

      for (String neighbor : graph.get(current).keySet()) {
        int oldDistance = distances.get(neighbor);
        int newDistance = distances.get(current) + graph.get(current).get(neighbor);

        if (newDistance < oldDistance) {
          distances.put(neighbor, newDistance);
          queue.add(neighbor);
          parents.get(neighbor).clear();
          parents.get(neighbor).add(current);
        } else if (newDistance == oldDistance) {
          parents.get(neighbor).add(current);
        }
      }
    }
    List<List<String>> allPaths = new ArrayList<>();
    backtrack(allPaths, new LinkedList<>(), parents, word2, word1, 0);
    return allPaths;
  }

  private void backtrack(List<List<String>> allPaths, LinkedList<String> path, Map<String,
                         List<String>> parents, String node, String startNode, int pathWeight) {
    path.addFirst(node);

    if (node.equals(startNode)) {
      List<String> fullPath = new ArrayList<>(path);
      fullPath.addFirst(String.valueOf(pathWeight));
      allPaths.add(fullPath);
    } else {
      for (String parent : parents.get(node)) {
        int edgeWeight = graph.get(parent).get(node);
        backtrack(allPaths, path, parents, parent, startNode, pathWeight + edgeWeight);
      }
    }
    path.removeFirst();
  }

  public void randomTraversal(String filePath) throws IOException {
    List<String> visitedNodes = new ArrayList<>();
    Set<String> visitedEdges = new HashSet<>();
    Random random = new Random();

    List<String> nodes = new ArrayList<>(graph.keySet());
    String currentNode = nodes.get(random.nextInt(nodes.size()));
    visitedNodes.add(currentNode);

    MutableGraph g = Factory.mutGraph().setDirected(true);

    while (true) {
      Map<String, Integer> edges = graph.get(currentNode);
      if (edges == null || edges.isEmpty()) {
        break;
      }

      List<String> edgeNodes = new ArrayList<>(edges.keySet());
      String nextNode = edgeNodes.get(random.nextInt(edgeNodes.size()));
      String edge = currentNode + "->" + nextNode;

      if (visitedEdges.contains(edge)) {
        break;
      }
      g.add(Factory.mutNode(currentNode).addLink(nextNode));

      visitedEdges.add(edge);
      visitedNodes.add(nextNode);
      currentNode = nextNode;
    }
    Files.write(Paths.get(filePath), visitedNodes, StandardCharsets.UTF_8);

    String pngPath = filePath.substring(0, filePath.lastIndexOf('.')) + ".png";
    Graphviz.fromGraph(g).width(700).render(Format.PNG).toFile(new File(pngPath));
  }
}