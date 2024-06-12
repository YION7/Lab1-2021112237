import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

class DirectedGraphTest {
    private DirectedGraph directedGraph;

    @BeforeEach
    void setUp() {
        directedGraph = new DirectedGraph();
    }

    void writeTestResultToFile(int testCaseNumber, String expected, String result, PrintWriter writer) {
        writer.println("Test case " + testCaseNumber + ":");
        writer.println("Expected: " + expected);
        writer.println("Actual: " + result);
        writer.println(result.equals(expected) ? "Test case " + testCaseNumber + " passed\n" : "Test case " + testCaseNumber + " failed\n");
    }

    @Test
    void testQueryBridgeWords() throws FileNotFoundException {
        PrintWriter writer = new PrintWriter("src/test/resources/BlackBoxTesting/test_results.txt");

        String text = "word1 word2 word3 word1 word4 word3 ";
        directedGraph.createGraph(text);

        String result = directedGraph.queryBridgeWords("word1", "word3");
        String expected = "The bridge words from word1 to word3 : word2, word4.";
        assertEquals(expected, result);
        writeTestResultToFile(1, expected, result, writer);

        result = directedGraph.queryBridgeWords("word1", "word4");
        expected = "No bridge words from word1 to word4!";
        assertEquals(expected, result);
        writeTestResultToFile(2, expected, result, writer);

        result = directedGraph.queryBridgeWords("word5", "word1");
        expected = "No word5 in the graph!";
        assertEquals(expected, result);
        writeTestResultToFile(3, expected, result, writer);

        result = directedGraph.queryBridgeWords("word1", "word5");
        expected = "No word5 in the graph!";
        assertEquals(expected, result);
        writeTestResultToFile(4, expected, result, writer);

        result = directedGraph.queryBridgeWords("word5", "word5");
        expected = "No word5 in the graph!";
        assertEquals(expected, result);
        writeTestResultToFile(5, expected, result, writer);

        result = directedGraph.queryBridgeWords("word5", "word6");
        expected = "No word5 and word6 in the graph!";
        assertEquals(expected, result);
        writeTestResultToFile(6, expected, result, writer);

        result = directedGraph.queryBridgeWords("word1", "word1");
        expected = "The bridge words from word1 to word1 : word1.";
        assertEquals(expected, result);
        writeTestResultToFile(7, expected, result, writer);

        result = directedGraph.queryBridgeWords("word2", "word1");
        expected = "The bridge words from word2 to word1 : word3.";
        assertEquals(expected, result);
        writeTestResultToFile(8, expected, result, writer);

        writer.close();
    }


}