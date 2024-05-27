import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Factory;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;



public class Graph {
    private static final DirectedGraph directedGraph = new DirectedGraph();
    private static final TextProcessor textProcessor = new TextProcessor();
    private static final GraphVisualizer graphVisualizer = new GraphVisualizer();
    private static final Scanner scanner = new Scanner(System.in);

    private static String filePath;
    private static int[] taskCounts = new int[5];

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

    public static void executeTask(int task) {
        taskCounts[task]++;
        switch (task) {
            case 1: queryBridgeWords(); break;
            case 2: processNewText(); break;
            case 3: calAllShortestPaths(); break;
            case 4: randomTraversal(); break;
            default: System.out.println("Invalid task. Enter a number 1-6."); break;
        }
    }

    public static void queryBridgeWords() {
        System.out.println("Input word1:");
        String w1 = scanner.nextLine().trim();
        System.out.println("Input word2:");
        String w2 = scanner.nextLine().trim();
        String result = directedGraph.queryBridgeWords(w1, w2);
        System.out.println(result);
    }

    public static void processNewText() {
        System.out.println("Input a new text:");
        String newText = scanner.nextLine();
        generateNewText(newText);
    }

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

        int pathCount = 1;
        for (List<String> shortestPath : allShortestPaths) {
            String distance = shortestPath.getFirst();  // Get the path length
            System.out.println("Path " + pathCount + ": The shortest distance from " + w1 + " to " + w2 + " is: " + distance + ", and the path is: " + String.join(" -> ", shortestPath.subList(1, shortestPath.size())) + ".");
            pathCount++;
        }

        String pathsPngPath = filePath + "/paths" + taskCounts[3] + ".png";
        try {
            graphVisualizer.showAllPaths(pathsPngPath, allShortestPaths);
        } catch (IOException e) {
            System.out.println("An error occurred while saving the shortest paths.");
        }
    }

    public static void randomTraversal() {
        String PngPath = filePath + "/travel" + taskCounts[4] + ".txt";
        try {
            directedGraph.randomTraversal(PngPath);
        } catch (IOException e) {
            System.out.println("An error occurred while saving the random traversal.");
        }
    }

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


class TextProcessor {
    public String processFile(String filepath) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (Stream<String> stream = Files.lines(Paths.get(filepath))) {
            stream.forEach(line -> stringBuilder.append(line).append(" "));
        }
        return processText(stringBuilder.toString());
    }

    private String processText(String text){
        text = text.replaceAll("[^a-zA-Z ]", " ");
        return text;
    }
}

class DirectedGraph {
    private  Map<String, Map<String,Integer>> graph = new ConcurrentHashMap<>();

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

    public Map<String, Map<String,Integer>> getGraph() {
        return this.graph;
    }

    public String queryBridgeWords(String word1, String word2) {
        if (!graph.containsKey(word1) || !graph.containsKey(word2)) {
            return "No " + word1 + " or " + word2 + " in the graph!";
        }

        List<String> bridgeWords = new ArrayList<>();
        Map<String, Integer> edges1 = graph.get(word1);
        for (String intermediate : edges1.keySet()) {
            Map<String, Integer> edges2 = graph.get(intermediate);
            if (edges2.containsKey(word2)) {
                bridgeWords.add(intermediate);
            }
        }

        if (bridgeWords.isEmpty()) {
            return "No bridge words from " + word1 + " to " + word2 + "!";
        } else {
            return "The bridge words from " + word1 + " to " + word2 + " are: " + String.join(", ", bridgeWords) + ".";
        }
    }

    public List<String> findBridgeWords(String word1, String word2) {
        if (!graph.containsKey(word1) || !graph.containsKey(word2)) {
            return new ArrayList<>();
        }

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
        backtrack(allPaths, new LinkedList<>(), parents, word2, word1,0);  // Pass the start node to the backtrack method

        return allPaths;
    }

    private void backtrack(List<List<String>> allPaths, LinkedList<String> path, Map<String, List<String>> parents, String node, String startNode, int pathWeight) {
        path.addFirst(node);

        if (node.equals(startNode)) {
            List<String> fullPath = new ArrayList<>(path);
            fullPath.addFirst( String.valueOf(pathWeight));
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

class GraphVisualizer {
    private StringBuilder dotGraph;
    private final String[] colors = {"red", "blue", "green", "purple", "orange", "brown", "pink", "black"};

    public void showDirectedGraph(String filePath, Map<String, Map<String,Integer>> graph) throws IOException {
        dotGraph = new StringBuilder("digraph G {");
        String PNG_path= convertTxtToPng(filePath);
        for (String node : graph.keySet()) {
            Map<String, Integer> edges = graph.get(node);
            for (String edge : edges.keySet()) {
                dotGraph.append(node).append(" -> ").append(edge).append(" [label=\"").append(edges.get(edge)).append("\"];");
            }
        }
        dotGraph.append("}");

        System.out.println(dotGraph.toString());
        MutableGraph g = new Parser().read(dotGraph.toString());
        Graphviz.fromGraph(g).width(700).render(Format.PNG).toFile(new File(PNG_path));
    }

    private boolean isInShortestPath(String node1, String node2, List<String> shortestPath) {
        int index1 = shortestPath.indexOf(node1);
        int index2 = shortestPath.indexOf(node2);
        return (index1 != -1 && index2 != -1 && Math.abs(index1 - index2) == 1 && index1 < index2);
    }

    public void showAllPaths(String PNG_path, List<List<String>> allShortestPaths) throws IOException {
        String[] lines = dotGraph.toString().trim().split(";");
        StringBuilder localDotGraph = new StringBuilder();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();

            if (line.contains("->")) {
                String[] parts = line.split("->");
                String node1 = parts[0].trim().split(" ")[parts[0].trim().split(" ").length - 1].replaceAll("[^a-zA-Z]", "");
                String node2 = parts[1].trim().split(" ")[0].replaceAll("[^a-zA-Z]", "");

                for (int j = 0; j < allShortestPaths.size(); j++) {
                    List<String> shortestPath = allShortestPaths.get(j).subList(1, allShortestPaths.get(j).size());  // Exclude the path length
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
        Graphviz.fromGraph(g).width(700).render(Format.PNG).toFile(new File(PNG_path));
    }



    private String convertTxtToPng(String filename) {
        if (filename.endsWith(".txt")) {
            return filename.substring(0, filename.length() - 4) + ".png";
        }
        return filename;
    }
}