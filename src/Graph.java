/**
 * Graph.java
 *
 * This class creates adjacency matrix of a graph from its g6 code.
 * Values of invariants values are obtained by calling appropriate functions.
 *
 * @author Dragan Stevanovic, Mohammad Ghebleh, Ali Kanso
 * @version April 2018
 */
import java.io.*;

public class Graph
{
    private int[] bits;         // Sequence of bits reconstructed from g6 code
    private int n;              // number of vertices (order)
    private int m;              // number of edges (size)
    private int[] degree;       // degree sequence
    private int[][] A;          // adjacency matrix

    /**
     * Empty constructor, needed for subclasses?
     */
    public Graph() {
        n=0;
    }

    /**
     * Constructor of a graph from g6 code
     */
    public Graph(String s) {
        n=s.charAt(0)-63;       // number of vertices is obtained from the first character of g6 code
        int firsti=1;
        if (s.charAt(0)>=126) {
            n=s.charAt(1)*4096 + s.charAt(2)*64 + s.charAt(3);
            firsti=4;
        }

        int bindex = 0;         // transform g6 code characters into bit sequence
        bits = new int[6*s.length()];
        for (int i=firsti; i<s.length(); i++) {
            int k = s.charAt(i)-63;
            for (int j=0; j<=5; j++) {
                bits[bindex+5-j] = k%2;
                k = k/2;
            }
            bindex += 6;
        }

        A = new int[n][n];        // initialize adjacency matrix, degree sequence and number of edges
        degree = new int[n];      // indexing always starts at 0
        for (int i=0; i<n; i++)
            degree[i] = 0;
        m = 0;

        bindex = 0;               // processes bit sequence to fill up adjacency matrix, degree sequence and number of edges
        for (int j=1; j<n; j++)
            for (int i=0; i<j; i++) {
                A[i][j] = bits[bindex];
                A[j][i] = bits[bindex];

                degree[i] += bits[bindex];
                degree[j] += bits[bindex];

                m += bits[bindex];

                bindex++;
            }
    }

    /**
     *  Constructor of a graph from provided adjacency matrix
     *  May be used to create graph complement or results of other graph operations
     *  Assumption: A is a symmetric, (0,1)-matrix
     */
    public Graph(int A[][]) {
        initializeGraph(A);
    }

    public void initializeGraph(int A[][]) {
        this.A = A;                 // adjacency matrix entries do not get copied,
        // only pointer to the matrix gets copied
        n = A.length;               // number of vertices

        degree = new int[n];        // initializes degrees and the number of edges
        for (int i=0; i<n; i++)
            degree[i] = 0;
        m = 0;

        for (int i=0; i<n; i++)     // processes degrees and the number of edges
            for (int j=0; j<i; j++)
                if (A[i][j]==1) {
                    degree[i]++;
                    degree[j]++;
                    m++;
                }
    }

    /**
     * Methods returning values of numbers of vertices, edges, degrees and adjacency matrix
     */
    public int n() {
        return n;
    }

    public int m() {
        return m;
    }

    public int[] degrees() {
        return degree;
    }

    public int[][] Amatrix() {
        return A;
    }

    /**
     * Laplacian matrix
     */
    private int[][] L;
    private boolean LExists = false;

    public int[][] Lmatrix() {
        if (LExists)
            return L;

        LExists = true;
        L = new int[n][n];

        for (int i=0; i<n; i++)     // off-diagonal entries are opposite of adjacency matrix entries
            for (int j=0; j<i; j++) {
                L[i][j] = -A[i][j];
                L[j][i] = -A[j][i];
            }

        for (int i=0; i<n; i++)     // diagonal entries are equal to corresponding degrees
            L[i][i] = degree[i];

        return L;
    }

    /**
     * Signless Laplacian matrix
     */
    private int[][] Q;
    private boolean QExists = false;

    public int[][] Qmatrix() {
        if (QExists)
            return Q;

        QExists = true;
        Q = new int[n][n];

        for (int i=0; i<n; i++)     // off-diagonal entries are equal to adjacency matrix entries
            for (int j=0; j<i; j++) {
                Q[i][j] = A[i][j];
                Q[j][i] = A[j][i];
            }

        for (int i=0; i<n; i++)     // diagonal entries are equal to corresponding degrees
            Q[i][i] = degree[i];

        return Q;
    }

    /**
     * Distance matrix by Floyd-Warshall algorithm
     */
    private int[][] D;
    private boolean DExists = false;

    public int[][] Dmatrix() {
        if (DExists)
            return D;

        DExists = true;
        D = new int[n][n];

        for (int i=0; i<n; i++)          // initializes distance matrix
            for (int j=0; j<n; j++)
                if (i==j) D[i][j]=0;
                else if (A[i][j]==1)
                    D[i][j]=1;
                else D[i][j]=n;

        for (int k=0; k<n; k++)          // the main loop of the Floyd-Warshall algorithm
            for (int i=0; i<n; i++)
                for (int j=0; j<n; j++)
                    if (D[i][j] > D[i][k] + D[k][j])
                        D[i][j] = D[i][k] + D[k][j];

        return D;
    }

    /**
     * Modularity matrix
     */
    private double[][] M;
    private boolean MExists = false;

    public double[][] Mmatrix() {
        if (MExists)
            return M;

        MExists = true;
        for (int i=0; i<n; i++)
            for (int j=0; j<n; j++)
                M[i][j] = ((double) A[i][j]) - ((double) degree[i]*degree[j])/(2*m);

        return M;
    }











    /**
     * Auxiliary function for calculation of energies
     */
    public static double deviation(double[] eigs) {
        double average = 0.0;
        for (int i=0; i < eigs.length; i++)
            average += eigs[i];
        average = average / eigs.length;

        double deviation = 0.0;
        for (int i=0; i< eigs.length; i++)
            deviation += Math.abs(eigs[i] - average);

        return deviation;
    }



    /**
     * LEL, Laplacian-like energy
     */



    /**
     * Diameter
     */
    public int diameter() {
        Dmatrix();
        int diameter = 0;
        for (int i=0; i<n; i++)
            for (int j=0; j<i; j++)
                if (D[i][j]>diameter)
                    diameter = D[i][j];
        return diameter;
    }

    /**
     * Radius
     */
    public int radius() {
        Dmatrix();
        int radius = n;
        for (int i=0; i<n; i++) {
            int ecc = 0;
            for (int j=0; j<n; j++)
                if (D[i][j]>ecc)
                    ecc = D[i][j];
            if (ecc<radius)
                radius=ecc;
        }
        return radius;
    }

    /**
     * Wiener index
     */
    public int wiener() {
        Dmatrix();
        int wiener = 0;
        for (int i=0; i<n; i++)
            for (int j=0; j<i; j++)
                wiener += D[i][j];
        return wiener;
    }

    /**
     * Randic index
     */
    public double randic() {
        double randic = 0.0;
        for (int i=0; i<n; i++)
            for (int j=0; j<i; j++)
                if (A[i][j]==1)
                    randic += 1/Math.sqrt(degree[i]*degree[j]);
        return randic;
    }

    /**
     * First Zagreb index
     */
    public int zagreb1() {
        int zagreb1 = 0;
        for (int i=0; i<n; i++)
            zagreb1 += degree[i]*degree[i];
        return zagreb1;
    }

    /**
     * Second Zagreb index
     */
    public int zagreb2() {
        int zagreb2 = 0;
        for (int i=0; i<n; i++)
            for (int j=0; j<i; j++)
                if (A[i][j]==1)
                    zagreb2 += degree[i]*degree[j];
        return zagreb2;
    }

    /**
     * Distance-sum heterogeneity index is defined by Estrada and Vargas-Estrada
     * in Appl. Math. Comput. 218 (2012), 10393-10405 as
     * dshi = \sum_{i=1}^n \frac{d_i}{s_i} - 2\sum_{ij\in E} (s_is_j)^{-1/2},
     * where d_i is the degree of vertex i, while s_i is the sum of distances from i to all other vertices.
     */
    public double dshi() {
        Dmatrix();
        int[] s = new int[n];
        for (int i=0; i<n; i++) {
            s[i]=0;
            for (int j=0; j<n; j++)
                s[i] += D[i][j];
        }

        double dshi = 0.0;
        for (int i=0; i<n; i++)
            dshi += ((double)degree[i])/s[i];

        for (int i=0; i<n; i++)
            for (int j=0; j<i; j++)
                if (A[i][j]==1)
                    dshi -= 2/Math.sqrt(s[i]*s[j]);

        return dshi;
    }

    /**
     * Eigenvectors are placed in columns, so we need
     * an auxiliary function to extract a column from a matrix.
     */
    public static int[] extractColumn(int[][] mat, int column) {
        int[] excol = new int[mat.length];

        for (int i=0; i<mat.length; i++)
            excol[i] = mat[i][column];

        return excol;
    }

    public static double[] extractColumn(double[][] mat, int column) {
        double[] excol = new double[mat.length];

        for (int i=0; i<mat.length; i++)
            excol[i] = mat[i][column];

        return excol;
    }

    /**
     * Output printing formats for graph, its vectors and its matrices
     * Returns multiline string representing integer matrix
     * delims is a three-character string,
     * where character at position 0 is put at the beginning of a matrix,
     * character at position 1 is put between entries,
     * and character at position 2 is put at the end of a matrix (think of "[,]").
     */
    public static String printVector(int[] vec, String delims) {
        StringBuffer buf = new StringBuffer("");

        buf.append(delims.charAt(0));
        for (int i=0; i<vec.length; i++) {
            buf.append("" + vec[i]);
            if (i!=vec.length-1)    // was it the last entry?
                buf.append(delims.charAt(1) + " ");
        }
        buf.append(delims.charAt(2));

        return buf.toString();
    }

    public static String printVector(int[] vec) {
        return Graph.printVector(vec, "[,]");
    }

    public static String printMatrix(int[][] mat, String delims) {
        StringBuffer buf = new StringBuffer("");

        buf.append(delims.charAt(0));
        for (int i=0; i<mat.length; i++) {
            buf.append(delims.charAt(0));
            for (int j=0; j<mat[i].length; j++) {
                buf.append("" + mat[i][j]);

                if (j!=mat[i].length-1)         // was it the last column?
                    buf.append(delims.charAt(1) + " ");
            }
            buf.append(delims.charAt(2));

            if (i!=mat.length-1)                // was it the last row?
                buf.append(delims.charAt(1) + " ");
        }
        buf.append(delims.charAt(2));

        return buf.toString();
    }

    public static String printMatrix(int[][] mat) {
        return Graph.printMatrix(mat, "[,]");
    }

    /**
     * Returns multiline string representing double vector or double matrix
     */
    public static String printVector(double[] vec, String delims) {
        StringBuffer buf = new StringBuffer("");

        buf.append(delims.charAt(0));
        for (int i=0; i<vec.length; i++) {
            buf.append("" + vec[i]);
            if (i!=vec.length-1)    // was it the last entry?
                buf.append(delims.charAt(1) + " ");
        }
        buf.append(delims.charAt(2));

        return buf.toString();
    }

    public static String printVector(double[] vec) {
        return Graph.printVector(vec, "[,]");
    }

    public static String printMatrix(double[][] dmat, String delims) {
        StringBuffer buf = new StringBuffer("");

        buf.append(delims.charAt(0));
        for (int i=0; i<dmat.length; i++) {
            buf.append(delims.charAt(0));
            for (int j=0; j<dmat[i].length; j++) {
                buf.append("" + dmat[i][j]);

                if (j!=dmat[i].length-1)         // was it the last column?
                    buf.append(delims.charAt(1) + " ");
            }
            buf.append(delims.charAt(2));

            if (i!=dmat.length-1)               // was it the last row?
                buf.append(delims.charAt(1) + " ");
        }
        buf.append(delims.charAt(2));

        return buf.toString();
    }

    public static String printMatrix(double[][] dmat) {
        return Graph.printMatrix(dmat, "[,]");
    }

    /**
     * String representing adjacency matrix
     */
    public String printAmatrix() {
        return Graph.printMatrix(A);
    }

    /**
     * String representing Laplacian matrix
     */
    public String printLmatrix() {
        Lmatrix();
        return Graph.printMatrix(L);
    }

    /**
     * String representing signless Laplacian matrix
     */
    public String printQmatrix() {
        Qmatrix();
        return Graph.printMatrix(Q);
    }

    /**
     * String representing distance matrix
     */
    public String printDmatrix() {
        Dmatrix();
        return Graph.printMatrix(D);
    }

    /**
     * String representing modularity matrix
     */
    public String printMmatrix() {
        Mmatrix();
        return Graph.printMatrix(M);
    }

    /**
     * Returns string containing list of edges
     */
    public String printEdgeList() {
        StringBuffer buf = null;

        for (int i=0; i<n; i++)
            for (int j=i+1; j<n; j++)
                if (A[i][j]==1) {
                    if (buf == null) {
                        buf = new StringBuffer("");
                    }
                    else
                        buf.append(", ");
                    buf.append("" + i + " " + j);
                }

        return buf.toString();
    }

    /**
     * Returns string describing graph in a .dot format,
     * needed for visualisation with Graphviz.
     */
    public String printDotFormat() {
        StringBuffer buf = new StringBuffer("Graph {\n");

        for (int i=0; i<n; i++)
            buf.append("" + i + " [shape=circle]\n");

        for (int i=0; i<n; i++)
            for (int j=i+1; j<n; j++)
                if (A[i][j]==1)
                    buf.append("" + i + " -- " + j + "\n");

        buf.append("}\n");
        return buf.toString();
    }

    /**
     * Together with the graph, you can visualise additional data
     * by placing them in a data string.
     * This string is put as a label of a separate isolated vertex,
     * and visualised by Graphviz in the same image next to the graph itself.
     */
    public String printDotFormat(String data) {
        StringBuffer buf = new StringBuffer("Graph {\n");

        for (int i=0; i<n; i++)
            buf.append("" + i + " [shape=circle]\n");

        for (int i=0; i<n; i++)
            for (int j=i+1; j<n; j++)
                if (A[i][j]==1)
                    buf.append("" + i + " -- " + j + "\n");

        buf.append("data [shape=box, label=\"" + data + "\"]\n");
        buf.append("}\n");
        return buf.toString();
    }

    /**
     * Writes the .dot format description of a graph to the file
     */
    public void saveDotFormat(String filename) throws IOException {
        PrintWriter outfile = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
        outfile.println(printDotFormat());
        outfile.close();
    }

    /**
     * Writes the .dot format description of a graph to the file,
     * together with additional data placed as a label of a separate isolated vertex
     */
    public void saveDotFormat(String filename, String data) throws IOException {
        PrintWriter outfile = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
        outfile.println(printDotFormat(data));
        outfile.close();
    }
}