/**
 * Template for reporting values of invariants for a set of graphs.
 *
 * How to use the template:
 * In your OS:
 * - make a copy of the whole framework folder in a new location
 * - copy (or generate) required sets of graphs to the same folder
 *
 * In BlueJ:
 * - modify the template according to your needs
 * - right-click on the template, select 'New ReporterTemplate' and press Enter
 * - at the bottom of the window, right-click a newly created instance of the template
 *        and select 'run(String inputFileName)'
 * - in a dialog that appears, enter the filename of the graph set
 *        within quotation marks "" (inputFileName), and
 *        0 if you do NOT want to create Graphviz .dot files,
 *        1 if you do want to create Graphviz .dot files for further visualisation
 * - study your results!
 */
import java.io.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static java.lang.Math.abs;
import static java.lang.Math.max;

public class ReporterTemplate {

    public static final int READ_AMOUNT = 100000;
    public static final int THREADS_AMOUNT = 8;
    private Worker[] workers = new Worker[THREADS_AMOUNT];
    private BufferedReader in;
    private PrintWriter outResults;
    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();

    public static boolean hasGraphsToRead = true;



    public ReporterTemplate() {
    }

    static boolean graphExists(LinkedList<Integer> a) {
        while (true) {
            a.sort(Collections.reverseOrder());

            if (a.get(0) == 0)
                return true;

            int v = a.get(0);
            a.remove(0);


            if (v > a.size())
                return false;

            for(int i = 0; i < v; i++) {
                a.set(i, a.get(i) - 1);

                if (a.get(i) < 0)
                    return false;
            }
        }
    }

    /**
     * The main method whose argument inputFileName
     * points to a file containing graphs in g6 format,
     * while createDotFiles instructs whether to write Graphviz .dot files for g6codes
     */
    public void run(String inputFileName) throws IOException {
        long startTime = System.currentTimeMillis();

        in = new BufferedReader(new FileReader(inputFileName));
        //outResults = new PrintWriter(new BufferedWriter(new FileWriter(inputFileName + ".results.csv")));
        System.out.println("Starting the algorithm");
        in.lines().parallel().forEach(this::checkImbalanceHypothesis);
        in.close();
        // Testing done, close the files
        //outResults.close();
        long totalTime = System.currentTimeMillis() - startTime;    // Report elapsed time
        System.out.println("Time elapsed: " +
                (totalTime / 60000) + " min, " + ((double) (totalTime % 60000) / 1000) + " sec");
    }

    private void checkGraph(String s) {
        Graph g = new Graph(s);
        int m = 0;
        int[][] matrix = g.Amatrix();
        int[] degrees = g.degrees();
        Integer[] edgeImbalances = new Integer[g.m()];
        for (int i = 0; m < g.m(); i++) {

            for (int j = i + 1; j < g.n(); j++) {
                if (matrix[i][j] == 1) {
                    edgeImbalances[m++] = abs(degrees[i] - degrees[j]);
                }
            }
        }

        LinkedList<Integer> a = new LinkedList<>(Arrays.asList(edgeImbalances));
        if (graphExists(a))
            return;
        System.out.println("Non-graphical");
        Graph.printMatrix(g.Amatrix());
    }

    private void checkImbalanceHypothesis(final String s) {
        Graph g = new Graph(s);
        //Checking the imbalance hypothesis
        //Assuming that the graph is not empty

        int m = 0;
        int[][] matrix = g.Amatrix();
        int[] degrees = g.degrees();
        Integer[] edgeImbalances = new Integer[g.m()];
        for (int i = 0; m < g.m(); i++) {
            //If there is a universal vertex, we can skip this graph (assuming that the conjecture has been verified for all
            // graphs on n - 1 and less vertices)

            if (degrees[i] == g.n() - 1)
                return;

            for (int j = i + 1; j < g.n(); j++) {
                if (matrix[i][j] == 1) {
                    int imbalance = abs(degrees[i] - degrees[j]);
                    if (imbalance == 0)
                        return;
                    edgeImbalances[m++] = imbalance;
                }
            }
        }


        LinkedList<Integer> a = new LinkedList<>(Arrays.asList(edgeImbalances));
        if (graphExists(a))
            return;
        System.out.println("Non-graphical!");
        System.out.println("------------------------------------------------------\n\n\n");
        System.out.println("The adjacency matrix:");
        for (int i = 0; i < m; i++)
            System.out.print(edgeImbalances[i] + " ");
        for (int i = 0; m < g.m(); i++) {
            for (int j = i + 1; j < g.n(); j++)
                System.out.print(matrix[i][j] + " ");
            System.out.println();
        }
        System.out.println("------------------------------------------------------\n\n\n");
    }

    private boolean addComplementAndCheck(final LinkedList<Integer> a, final int[] degrees, final int n, final int k) {
        /*IntStream stream = Arrays.stream(degrees).parallel();
        stream.forEach(d -> );*/
        for (int d : degrees) {
            int value = abs(n - d - k);
            for (int i = 0; i < k; i++)
                a.add(value);
        }
        return graphExists(a);
    }


    // This function may be used to run the template from out of BlueJ
    public static void main(String[] args) throws IOException, NumberFormatException, InterruptedException {
        new ReporterTemplate().run(args[0]);
    }
}
