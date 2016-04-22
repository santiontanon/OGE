
import graphloading.DOTGraph;
import graphloading.GMLGraph;
import graphloading.GraphMLGraph;
import graphloading.TextGraph;
import java.util.Arrays;
import orthographicembedding.OrthographicEmbedding;
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

    static String inputFileName = null;
    static String outputFileName = null;
    static String outputPNGName = null;
    static boolean simplify = true;
    static boolean correct = true;

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
        
        OrthographicEmbeddingResult oe1 = OrthographicEmbedding.orthographicEmbedding(graph,simplify, correct); 
        if (!oe1.sanityCheck(false)) System.out.println("The orthographic projection without simplification contains errors!");
        System.out.println("Orthographic embedding (no simplification):");
        for(int i = 0;i<oe1.embedding.length;i++) {
            System.out.println(i + " : " + oe1.embedding[i]);
        }
        /*
        OEmbeddingResultVisualizer w = new OEmbeddingResultVisualizer(oe1);
        w.setSize(480,480);
        w.setVisible(true);
        w.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        */
        
        // save result:
        // ...
        
        if (outputPNGName!=null) {
            // save image:
            // ...
        }
    }
    
       
    public static boolean parseArguments(String args[]) {
        System.out.println(Arrays.toString(args));
        if (args.length<2) return false;
        inputFileName = args[0];
        outputFileName = args[0];
        for(int i = 2;i<args.length;i++) {
            if (args[i].startsWith("-png:")) {
                outputPNGName = args[i].substring(5);
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
        System.out.println("parameters: input-file output-file options");
        System.out.println("  input-file: a graph in .txt, .dot, .gml, .graphml format");
        System.out.println("  output-file: the desired output filename");
        System.out.println("Options:");
        System.out.println("  -png:filename : saves a graphical version of the output as a .png file");
        System.out.println("");
    }
    
    
    public static int[][]loadGraph(String fileName) throws Exception {
        
        if (fileName.endsWith(".txt")) {
            return TextGraph.loadGraph(fileName);
        } else if (fileName.endsWith(".dot")) {
            return DOTGraph.loadGraph(fileName);
        } else if (fileName.endsWith(".gml")) {
            return GMLGraph.loadGraph(fileName);
        } else if (fileName.endsWith(".graphml")) {
            return GraphMLGraph.loadGraph(fileName);
        }

        System.err.println("Unrecognized graph file format: " + fileName);
        return null;
    }
}
