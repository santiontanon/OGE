
import graphloading.TextGraph;
import java.io.FileWriter;
import orthographicembedding.OrthographicEmbedding;
import orthographicembedding.OrthographicEmbeddingOptimizer;
import orthographicembedding.OrthographicEmbeddingResult;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author santi
 */
public class Main {

    public static String output_type_txt = "txt";
    
    static String inputFileName = null;
    static String outputFileName = null;
    static boolean simplify = true;
    static boolean correct = true;
    static boolean optimize = true;
    static String output_type = output_type_txt;

    static String outputPNGName = null;
    static int PNGcellWidth = 48;
    static int PNGcellHeight = 48;
    static boolean PNGlabelVertices = true;
    
    public static void main(String args[]) throws Exception {
        if (!parseArguments(args)) {
            printInstructions();
            System.exit(0);
        }
        
        int graph[][] = loadGraph(inputFileName);
        if (graph==null) {
            System.err.println("Cannot load input-file " + inputFileName);
            System.exit(1);
        }

        // calculate the embedding:
        OrthographicEmbeddingResult oe = OrthographicEmbedding.orthographicEmbedding(graph,simplify, correct); 
        if (oe==null) {
            System.err.println("No orthographic projection could be found! verify the input graph is planar.");
            System.exit(1);
        }
        OrthographicEmbeddingOptimizer.optimize(oe, graph);
        if (!oe.sanityCheck(false)) System.err.println("The orthographic projection without simplification contains errors!");
        
        // save the results:
        saveEmbedding(outputFileName, oe);
        
        // save image:
        if (outputPNGName!=null) SavePNG.savePNG(outputPNGName, oe, PNGcellWidth, PNGcellHeight, PNGlabelVertices);
    }
    
       
    public static boolean parseArguments(String args[]) {
        //System.out.println(Arrays.toString(args));
        if (args.length<2) return false;
        inputFileName = args[0];
        outputFileName = args[1];
        for(int i = 2;i<args.length;i++) {
            if (args[i].startsWith("-png:")) {
                outputPNGName = args[i].substring(5);
            } else if (args[i].startsWith("-simplify")) {
                if (args[i].equals("-simplify:true")) simplify = true;
                if (args[i].equals("-simplify:false")) simplify = false;
            } else if (args[i].startsWith("-optimize")) {
                if (args[i].equals("-optimize:true")) optimize = true;
                if (args[i].equals("-optimize:false")) optimize = false;
            } else {
                System.err.println("Unrecognized parameter " + args[i]);
                return false;
            }
        }
        
        return true;
    }
    
    
    public static void printInstructions() {
        System.out.println("Orthographic Graphic Embedder (OGE) v1.0 by Santiago Ontañón (2016)");
        System.out.println("");
        System.out.println("This tool computes an orthographic embedding of a plannar input graph. " + 
                           "Although the tool was originally designed to be part of a procedural-content generation (PCG) " + 
                           "module for a game, it is designed to be usable to find orthographic embeddings for any planar " + 
                           "input graphs via the use of PQ-trees.");
        System.out.println("");
        System.out.println("Example usage: java -classpath OGE.jar Main examples/graph1 examples/oe1.txt -png:examples/oe1.png");
        System.out.println("");
        System.out.println("parameters: input-file output-file options");
        System.out.println("  input-file: a file containing the adjacency matrix of a graph");
        System.out.println("  output-file: the desired output filename");
        System.out.println("Options:");
        System.out.println("  -output:[type] : the type of output desired, which can be:");
        System.out.println("        txt (default): a text file with the connectivity matrix, and then a list of vertices, with their mapping to the original vertices, and their coordinates in the orthographic embedding.");
        System.out.println("        (more output types might be added in the future)");
        System.out.println("  -png:filename : saves a graphical version of the output as a .png file");
        System.out.println("  -simplify:true/false : defaults to true, applies a filter to try to reduce unnecessary auxiliary vertices.");
        System.out.println("  -optimize:true/false : defaults to true, postprocesses the output to try to make it more compact.");
        System.out.println("");
    }
    
    
    public static int[][]loadGraph(String fileName) throws Exception {
        
        if (fileName.endsWith(".txt")) {
            return TextGraph.loadGraph(fileName);
        }

        System.err.println("Unrecognized graph file format: " + fileName);
        return null;
    }

    private static void saveEmbedding(String fileName, OrthographicEmbeddingResult oe) throws Exception {
        FileWriter fw = new FileWriter(fileName);
        if (output_type.equals(output_type_txt)) {
            for(int i = 0;i<oe.nodeIndexes.length;i++) {    
                for(int j = 0;j<oe.nodeIndexes.length;j++) {
                    if (oe.edges[i][j] || oe.edges[j][i]) fw.write("1" + ", ");
                                                     else fw.write("0" + ", ");
                }
                fw.write("\n");
            }
            for(int i = 0;i<oe.nodeIndexes.length;i++) {    
                fw.write(i + ", " + oe.nodeIndexes[i] + ", " + oe.x[i] + ", " + oe.y[i] + "\n");
            }
            
        } else {
            System.err.println("Unknown output type: " + output_type);
        }
        fw.close();
    }


}
