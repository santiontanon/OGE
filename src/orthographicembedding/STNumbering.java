/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package orthographicembedding;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author santi
 */
public class STNumbering {
    public static int DEBUG = 0;
    
    // Implementation of the algorithm in:
    // S. Even and R. E. Tarjan, Computing an st-numbering, Theoret. Comput. Sci. 2, (1976), 339-344.
    public static int[] stNumbering(int graph[][]) throws Exception {
        int n = graph.length;
        
        if (DEBUG>=1) {
            System.out.println("Computing stNumebring of graph:");
            for(int i = 0;i<graph.length;i++) {            
                for(int j = 0;j<graph.length;j++) {
                    System.out.print(graph[i][j] + ",");
                }
                System.out.println("");
            }
        }
        
        // Part 1: Create a minimum spanning tree, preorder and L:
        int [][]treeGraph = new int[n][n];
        int [][]ancestors = new int[n][n];
        int T[] = new int[n];
        int preorder[] = new int[n];
        int L[] = new int[n];
        int t = 0;
        int s = -1;
        depthFirstSpanningTree(graph, treeGraph, ancestors, T, preorder, L, s, t);
        for(int i = 0;i<n;i++) {
            if (T[i]==t && i!=t) {
                s = i;
                break;
            }
        }
        if (s==-1) {
            System.err.println("WVisibility2Connected: spanning tree is malformed!");
            return null;
        }
        if (DEBUG>=1) System.out.println("(s,t) = (" + s + "," + t + ")");

        // Part 3: run the STNUMBER algorithm (part 2 is inside of STNUMBER)
        int stnumbers[] = STNUMBER(s,t,graph,treeGraph, ancestors, preorder, L);
        if (DEBUG>=1) System.out.println("st-numbers: " + Arrays.toString(stnumbers));
        
        if (DEBUG>=1) {
            if (verifySTNumbering(graph, stnumbers)) {
                System.out.println("STNumbering: st-numbering is correct!");
            } else {
                System.out.println("STNumbering: st-numbering has errors!");
            }
        }
        
        return stnumbers;
    }
    
    
    public static int[] stNumbering(int graph[][], int s) throws Exception {
        int t = -1;
        for(int i = 0;i<graph.length;i++) {
            if (graph[s][i]==1) {
                t = i;
                return stNumbering(graph, s, t);
            }       
        }
        return null;
    }
    
        
    public static int[] stNumbering(int graph[][], int s, int t) throws Exception {
        int n = graph.length;
        
        if (DEBUG>=1) {
            System.out.println("Computing stNumebring of graph: (s,t) = (" + s + "," + t + ")");
            for(int i = 0;i<graph.length;i++) {            
                for(int j = 0;j<graph.length;j++) {
                    System.out.print(graph[i][j] + ",");
                }
                System.out.println("");
            }
        }
        
        // Part 1: Create a minimum spanning tree, preorder and L:
        int [][]treeGraph = new int[n][n];
        int [][]ancestors = new int[n][n];
        int T[] = new int[n];
        int preorder[] = new int[n];
        int L[] = new int[n];
        depthFirstSpanningTree(graph, treeGraph, ancestors, T, preorder, L, s, t);
        if (DEBUG>=1) System.out.println("forced (s,t) = (" + s + "," + t + ")");

        // Part 3: run the STNUMBER algorithm (part 2 is inside of STNUMBER)
        int stnumbers[] = STNUMBER(s,t,graph,treeGraph, ancestors, preorder, L);
        if (DEBUG>=1) System.out.println("st-numbers: " + Arrays.toString(stnumbers));
        
        if (DEBUG>=1) {
            if (verifySTNumbering(graph, stnumbers)) {
                System.out.println("STNumbering: st-numbering is correct!");
            } else {
                System.out.println("STNumbering: st-numbering has errors!");
            }
        }        
        
        return stnumbers;
    }
    
    
    public static boolean verifySTNumbering(int graph[][], int stNumbering[]) {
        int n = graph.length;
        
        // find s and t:
        int s = -1, t = -1;
        for(int i = 0;i<n;i++) {
            if (stNumbering[i]==1) {
                if (s!=-1) return false;
                s = i;
            }
            if (stNumbering[i]==n) {
                if (t!=-1) return false;
                t = i;
            }
        }
        
        // verify that each other node has a lower and a higher neighbor:
        for(int v = 0;v<n;v++) {
            if (v!=s && v!=t) {
                boolean hasHigher = false;
                boolean hasLower = false;
                for(int w = 0;w<n;w++) {
                    if (graph[v][w]==1 && stNumbering[w]<stNumbering[v]) hasLower = true;
                    if (graph[v][w]==1 && stNumbering[w]>stNumbering[v]) hasHigher = true;
                }
                if (!hasLower || !hasHigher) return false;
            }
        }
        
        return true;
    }
    
    
    static int[] STNUMBER(int s, int t, int graph[][], int treeGraph[][], 
                         int ancestors[][], int preorder[], int L[]) throws Exception {
        int n = graph.length;
        int stnumber[] = new int[n];
        boolean [][]oldEdges = new boolean[n][n];
        boolean []oldNodes = new boolean[n];
        // initially all edges and nodes are "new"
        for(int v = 0;v<n;v++) {
            stnumber[v] = -1;
            oldNodes[v] = false;
            for(int w = 0;w<n;w++) {
                oldEdges[v][w] = false;
            }
        }
        
        oldNodes[s] = true;
        oldNodes[t] = true;
        oldEdges[s][t] = true;
        oldEdges[t][s] = true;
        List<Integer> stack = new LinkedList<>();
        stack.add(t);
        stack.add(s);
        int i = 0;
        while(!stack.isEmpty()) {
            if (DEBUG>=2) System.out.println("STNUMBER, stack: " + stack);
            int v = stack.remove(stack.size()-1);
            if (DEBUG>=2) System.out.println("STNUMBER: popped " + v);

            List<Integer> path = PATHFINDER(v, graph, treeGraph, oldEdges, oldNodes, ancestors, preorder, L);
            if (DEBUG>=2) System.out.println("STNUMBER: Path: " + path);
            if (DEBUG>=1 && path!=null) {
                // verify this is a "simple path" (no repeated vertices):
                for(int j = 0;j<path.size();j++) {
                    for(int k = j+1;k<path.size();k++) {
                        if (path.get(j).equals(path.get(k))) {
                            throw new Exception("PATHFINDER did not return a simple path!");
                        }
                    }
                }
            }
            if (path==null) {
                i++;
                stnumber[v] = i;
                if (DEBUG>=2) System.out.println("STNUMBER: stNumber(" + v + ") = " + i);
            } else {
                // add all the elements to the stack except the last:
                for(int j = path.size()-2;j>=0;j--) {
                    stack.add(path.get(j));
                }
            }
            if (DEBUG>=2) System.out.println();
        }
        
        return stnumber;
    }
    
    static List<Integer> PATHFINDER(int v, int graph[][], int treeGraph[][], 
                                    boolean [][]oldEdges, boolean []oldNodes, 
                                    int ancestors[][], int preorder[], int L[]) throws Exception {
        List<Integer> path = new LinkedList<>();
        int n = graph.length;
        // if there is a new cycle edge {v,w} with w -*-> v:
        for(int w = 0;w<n;w++) {
            /*
            if (graph[v][w]==1)
                System.out.println("a) {" + v + "," + w + "} : " + 
                                   (oldEdges[v][w] ? "old":"new") + " " + 
                                   (ancestors[w][v]==1 ? (w + "-*->" + "v"):""));
            */
            if (!oldEdges[v][w] && graph[v][w]==1 && ancestors[w][v]==1) {
                if (DEBUG>=2) System.out.println("* PATHFINDER a)");
                oldEdges[v][w] = true;
                oldEdges[w][v] = true;
                path.add(v);
                path.add(w);
                return path;
            }
        }
        // if there is a new tree edge v->w:
        for(int w = 0;w<n;w++) {
            if (!oldEdges[v][w] && treeGraph[v][w]==1) {
                if (DEBUG>=2) System.out.println("* PATHFINDER b)");
                oldEdges[v][w] = true;
                oldEdges[w][v] = true;
                path.add(v);
                path.add(w);
                while(!oldNodes[w]) {
                    boolean found = false;
                    for(int x = 0;x<n;x++) {
                        // the condition "!path.contains(x)" was not in the original
                        // Even & Tarjan paper, but I had to add it, otherwise, the algorithm
                        // sometimes returned paths that were not simple!
                        if (!path.contains(x)) {
                            if (!oldEdges[w][x] && graph[w][x]==1 && 
                                (preorder[x]==L[w] || L[x]==L[w])) {
                                oldNodes[w] = true;
                                oldEdges[w][x] = true;
                                oldEdges[x][w] = true;
                                path.add(x);
                                w = x;
                                found = true;
                                break;
                            }
                        }
                    }
                    if (!found) {
                        for(int i = 0;i<graph.length;i++) {            
                            System.err.print("{");
                            for(int j = 0;j<graph.length;j++) {
                                System.err.print(graph[i][j] + ",");
                            }
                            System.err.println("},");
                        }                        
                        throw new Exception("PAHFINDER: inside case (b), couldn't find next node in path.");
                    }
                }
                return path;
            }
        }
        // if there is a new cycle edge {v,w} with v -*-> w:
        for(int w = 0;w<n;w++) {
            if (!oldEdges[v][w] && graph[v][w]==1 && 
                treeGraph[v][w]==0 && ancestors[v][w]==1) {
                if (DEBUG>=2) System.out.println("* PATHFINDER c)");
                oldEdges[v][w] = true;
                oldEdges[w][v] = true;
                path.add(v);
                path.add(w);
                while(!oldNodes[w]) {
                    boolean found = false;
                    for(int x = 0;x<n;x++) {
                        if (!oldEdges[w][x] && treeGraph[x][w]==1) {
                            oldNodes[w] = true;
                            oldEdges[w][x] = true;
                            oldEdges[x][w] = true;
                            path.add(x);
                            w = x;
                            found = true;
                            break;
                        }
                    }                    
                    if (!found) if (!found) throw new Exception("PAHFINDER: inside case (c), couldn't find next node in path.");
                }
                return path;
            }
        }
        return null;
    }
    
    
    // Computes a depth-first spanning tree of the graph, and outputs:
    //  treeGraph: the tree represented as a graph
    //  ancestors: ancestors[i][j]==1 if i is an ancestor or j
    //  T: the parent of each node in the spanning tree 
    //  preorder: the nodes sorted in preorder according to the spanning tree
    //  L: the L values for each node (see paper referred above)
    //  s, and t are the s and t nodes: s can be set to -1 if you want it to be determined automatically
    public static void depthFirstSpanningTree(int graph[][], int treeGraph[][], int ancestors[][], int T[], int preorder[], int L[], int s, int t) {
        int n = graph.length;
        List<Integer> stack = new LinkedList<>();
        List<Integer> parents = new LinkedList<>();
        for(int i = 0;i<graph.length;i++) T[i] = -1;        
        stack.add(t);
        parents.add(t);
        // if (s!=-1) System.out.println("s->t: " + graph[t][s]);
        while(!stack.isEmpty()) {
            int current = stack.remove(stack.size()-1);
            int parent = parents.remove(parents.size()-1);
            // System.out.println("DFST: " + current + " -> " + parent);
            if (T[current]==-1) {
                T[current] = parent;
                for(int next = graph.length-1;next>=0;next--) {
                    if (graph[current][next]==1) {
                        if (next==s && current==t) {
                            // wait, to add it at the end (and make sure it's taken the first)
                        } else {
                            if (T[next]==-1) {
                                stack.add(next);
                                parents.add(current);
                            }
                        }
                    }
                }
                if (s!=-1 && current==t && graph[current][s]==1) {
                    stack.add(s);
                    parents.add(current);
                }
            }
        }
        if (DEBUG>=1) System.out.println("Spanning tree: " + Arrays.toString(T));
        
        // Compute the preorder:
        int counter = 1;
        stack = new LinkedList<>();
        stack.add(t);
        while(!stack.isEmpty()) {
            int current = stack.remove(0);
            if (DEBUG>=1) System.out.println("  computing preorder: " + current);
            preorder[current] = counter++;
            int tmp = 0;
            for(int i = 0;i<n;i++) {
                if (T[i]==current && i!=current) {
                    stack.add(tmp,i);
                    tmp++;
                }
            }
        }
        if (DEBUG>=1) System.out.println("Preorder: " + Arrays.toString(preorder));
        
        // Compute the L numbers (not the most efficient way to do it):
        for(int v = 0;v<n;v++) {
            for(int w = 0;w<n;w++) {
                treeGraph[v][w] = 0;
                ancestors[v][w] = 0;
            }
        }
        for(int v = 0;v<n;v++) {
            if (T[v]!=v) treeGraph[T[v]][v] = 1;
            ancestors[v][v] = 1;
            int tmp = v;
            while(T[tmp]!=tmp) {
                ancestors[T[tmp]][v] = 1;
                tmp = T[tmp];
            }
        }
        for(int v = 0;v<n;v++) {
            L[v] = preorder[v];
            for(int w = 0;w<n;w++) {
                // if (w is a descendant of v)
                // and w is connected to u through an edge that is NOT in the spanning tree
                if (ancestors[v][w]==1) {
//                    System.out.println(w + " is a descendant of " + v);
                    for(int u = 0;u<n;u++) {
                        if (graph[w][u]==1 && treeGraph[w][u]==0) {
                            if (preorder[u]<L[v]) L[v] = preorder[u];
                        }
                    }
                }
            }
        }
        if (DEBUG>=1) System.out.println("L: " + Arrays.toString(L));
    }           
}
