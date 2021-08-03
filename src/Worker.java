import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.Math.abs;

public class Worker extends Thread{

    private final BlockingQueue<String> blockingQueue;
    //private final BlockingQueue<int[][]> matrices = new LinkedBlockingQueue<>();
    long counter;

    public Worker(final BlockingQueue<String> queue) {
        this.blockingQueue = queue;
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


    @Override
    public void run() {
        while(ReporterTemplate.hasGraphsToRead || blockingQueue.isEmpty()) {
            System.out.println("checked");
            Graph g = null;
            try {
                g = new Graph(blockingQueue.take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //Checking the imbalance hypothesis
            //Assuming that the graph is not empty
            if (g.m() == 0)
                continue;
            counter++;
            printMatrix(g.Amatrix());
            System.out.println("\n\n");
            boolean isInteresting = true;

            int m = 0;
            int[][] matrix = g.Amatrix();
            int[] degrees = g.degrees();
            Integer[] edgeImbalances = new Integer[g.m()];
            for (int i = 0; m < g.m() && isInteresting; i++) {

                for (int j = i + 1; j < g.n() && isInteresting; j++) {
                    if (matrix[i][j] == 1) {
                        edgeImbalances[m++] = abs(degrees[i] - degrees[j]);
                    }
                }
            }

            LinkedList<Integer> a = new LinkedList<>(Arrays.asList(edgeImbalances));
            if (graphExists(a))
                    continue;
            System.out.println("Non-graphical");
            printMatrix(g.Amatrix());

        }

        /*
        StringBuilder builder = new StringBuilder(1000);
        builder.append("------------------------------------------\n");
        builder.append("Found some interesting graphs\n");
        for (int[][] a: matrices){
            for (int i = 0; i < a.length; i++) {
                for (int j = 0; j < a[0].length; j++)
                    builder.append(a[i][j]).append(" ");
                builder.append("\n");
            }
            builder.append("------------------------------------------").append("\n");
        }
        System.out.println(builder);
        */
    }

    private void printMatrix(final int[][] a) {
        StringBuilder builder = new StringBuilder(1000);
        for (int[] ints : a) {
            for (int j = 0; j < a[0].length; j++)
                builder.append(ints[j]).append(" ");
            builder.append("\n");
        }
        System.out.println(builder);
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

}
