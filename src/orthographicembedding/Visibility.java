/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package orthographicembedding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import util.Pair;
import util.Permutations;

/**
 *
 * @author santi
 */
public class Visibility {
    public static int DEBUG = 0;
    
    public Random r = null;
    
    public int graph[][];
    public int nEdges;
    public int[][] edgeIndexes;    // the index of each of the edges
    public int edge_n1[];          // node 1 of each of the edges
    public int edge_n2[];          // node 2 of each of the edges
    
    // visibility results:
    public double horizontal_y[];   // graph nodes
    public double horizontal_x1[];
    public double horizontal_x2[];
    public double vertical_x[];     // graph edges
    public double vertical_y1[];
    public double vertical_y2[];    
    
    public Visibility(int [][]a_graph, Random a_r) {
        int n = a_graph.length;
        r = a_r;
        graph = a_graph;        
        nEdges = 0;
        edgeIndexes = new int[n][n];
        for(int i = 0;i<n;i++) {
            for(int j = i+1;j<n;j++) {
                if (graph[i][j]==0) {
                    edgeIndexes[i][j] = -1;
                    edgeIndexes[j][i] = -1;
                } else {
                    edgeIndexes[i][j] = nEdges;
                    edgeIndexes[j][i] = nEdges;
                    nEdges++;
                }
            }
        }
        edge_n1 = new int[nEdges];
        edge_n2 = new int[nEdges];
        for(int i = 0;i<n;i++) {
            for(int j = i+1;j<n;j++) {
                if (edgeIndexes[i][j]!=-1) {
                    edge_n1[edgeIndexes[i][j]] = i;
                    edge_n2[edgeIndexes[i][j]] = j;
                }
            }
        }
        if (DEBUG>=1) {
            System.out.println("Visibility calculator created for graph with adjacency matrix (n_edges = " + nEdges + "):");
            for(int i = 0;i<graph.length;i++) {
                for(int j = 0;j<graph.length;j++) {
                    System.out.print(graph[i][j] + " ");
                }        
                System.out.println("");
            }        
        }
    }
    
    
    public Visibility(Visibility v) {
        copy(v);
    }
    
    
    public void copy(Visibility v) {
        r = v.r;
        graph = v.graph;
//        graph = new int[v.graph.length][v.graph[0].length];
//        for(int i = 0;i<v.graph.length;i++) {
//            for(int j = 0;j<v.graph[0].length;j++) {
//                graph[i][j] = v.graph[i][j];
//            }
//        }      
        nEdges = v.nEdges;
        edgeIndexes = v.edgeIndexes;
        edge_n1 = v.edge_n1;
        edge_n2 = v.edge_n2;
        
        if (v.horizontal_y!=null) {
            horizontal_y = new double[v.horizontal_y.length];
            horizontal_x1 = new double[v.horizontal_x1.length];
            horizontal_x2 = new double[v.horizontal_x2.length];
            for(int i = 0;i<v.horizontal_y.length;i++) {
                horizontal_y[i] = v.horizontal_y[i];
                horizontal_x1[i] = v.horizontal_x1[i];
                horizontal_x2[i] = v.horizontal_x2[i];
            }
        }
        if (v.vertical_x!=null) {
            vertical_x = new double[v.vertical_x.length];
            vertical_y1 = new double[v.vertical_y1.length];
            vertical_y2 = new double[v.vertical_y2.length];
            for(int i = 0;i<v.vertical_x.length;i++) {
                vertical_x[i] = v.vertical_x[i];
                vertical_y1[i] = v.vertical_y1[i];
                vertical_y2[i] = v.vertical_y2[i];
            }
        }
    }
    
    /* 
    This method stretches a visibility representatino to make each point fall into a dot of a grid, 
    the grid step size is given in "step"
    */
    public void gridAlign(double step) {
        List<Double> xvalues = new LinkedList<Double>();
        List<Double> yvalues = new LinkedList<Double>();
        
        for(double y:horizontal_y) if (!yvalues.contains(y)) yvalues.add(y);
        for(double y:vertical_y1) if (!yvalues.contains(y)) yvalues.add(y);
        for(double y:vertical_y2) if (!yvalues.contains(y)) yvalues.add(y);
        for(double x:vertical_x) if (!xvalues.contains(x)) xvalues.add(x);
        for(double x:horizontal_x1) if (!xvalues.contains(x)) xvalues.add(x);
        for(double x:horizontal_x2) if (!xvalues.contains(x)) xvalues.add(x);
        
        Collections.sort(xvalues);
        Collections.sort(yvalues);
        
        // filter those that are too similar (proably the same but for precission errors):
        double threshold = 0.00001;
        List<Double> toDelete = new LinkedList<Double>();
        for(int i = 0;i<xvalues.size()-1;i++) {
            if (Math.abs(xvalues.get(i)-xvalues.get(i+1))<threshold) 
                toDelete.add(xvalues.get(i+1));
        }
        xvalues.removeAll(toDelete);
        toDelete.clear();
        for(int i = 0;i<yvalues.size()-1;i++) {
            if (Math.abs(yvalues.get(i)-yvalues.get(i+1))<threshold) 
                toDelete.add(yvalues.get(i+1));
        }
        yvalues.removeAll(toDelete);
        
        if (DEBUG>=1) System.out.println("gridAlign: X = " + xvalues);
        if (DEBUG>=1) System.out.println("gridAlign: Y = " + yvalues);
        
        for(int i = 0;i<horizontal_y.length;i++) {
            horizontal_y[i] = indexOfClosest(horizontal_y[i],yvalues)*step;
            horizontal_x1[i] = indexOfClosest(horizontal_x1[i],xvalues)*step;
            horizontal_x2[i] = indexOfClosest(horizontal_x2[i],xvalues)*step;
        }
        for(int i = 0;i<vertical_x.length;i++) {
            vertical_x[i] = indexOfClosest(vertical_x[i],xvalues)*step;
            vertical_y1[i] = indexOfClosest(vertical_y1[i],yvalues)*step;
            vertical_y2[i] = indexOfClosest(vertical_y2[i],yvalues)*step;
        }
    }
    
    int indexOfClosest(double v, List<Double> l) {
        int best = -1;
        double best_diff = 0;
        int i = 0;
        for(Double d:l) {
            double diff = Math.abs(v-d);
            if (best==-1 || diff<best_diff) {
                best = i;
                best_diff = diff;
            }
            i++;
        }
        return best;
    }
    
    
    // W-Visibility algorithm (reference)
    public boolean WVisibility() throws Exception {
        if (DEBUG>=1) System.out.println("Blocks and cutnodes (for a graph with " + graph.length + " nodes)");
        Pair<LinkedHashMap<Integer,List<Integer>>,LinkedHashMap<Integer,List<Integer>>> tmp = Blocks.blocks(graph);
        LinkedHashMap<Integer,List<Integer>> blocks = tmp.m_a;
        LinkedHashMap<Integer,List<Integer>> cutNodes = tmp.m_b;
        if (DEBUG>=1) {
            for(int blockID:blocks.keySet()) {
                System.out.println("block " + blockID + ": " + blocks.get(blockID));
            }
            for(int cutNode:cutNodes.keySet()) {
                System.out.println("cutnode " + cutNode + ": " + cutNodes.get(cutNode));
            }
        }
        
        if (blocks.size()==1) {
            return WVisibility2Connected();
        } else {
            // W-VISIBILITY2 algorithm
            // from: "A Unified Approach to Visibility Representations of Planar Graphs"
            // 1 find the blocks:
            List<Integer> T = new ArrayList<Integer>();
            List<Integer> S = new ArrayList<Integer>();
            T.addAll(blocks.keySet());
            
            // we randomize it (to get differnt outputs every time):
            Collections.shuffle(T, r);
            
            // 2 construct the visibility representation for B_1:
            int blockID1 = T.remove(0);
            List<Integer> block1 = blocks.get(blockID1);
            int [][]blockgraph1 = blockSubgraph(block1);
            if (DEBUG>=1) System.out.println("W-VISIBILITY2: computing visibility of first block: " + block1);
            Visibility block1Visibility = new Visibility(blockgraph1, r);
            if (!block1Visibility.WVisibility2Connected()) return false;
            S.add(blockID1);
            
            // initialize the visibility representation for the whole graph:
            int n = graph.length;
            horizontal_y = new double[n];
            horizontal_x1 = new double[n];
            horizontal_x2 = new double[n];
            vertical_x = new double[nEdges];
            vertical_y1 = new double[nEdges];
            vertical_y2 = new double[nEdges];
            for(int i = 0;i<block1.size();i++) {
                horizontal_y[block1.get(i)] = block1Visibility.horizontal_y[i];
                horizontal_x1[block1.get(i)] = block1Visibility.horizontal_x1[i];
                horizontal_x2[block1.get(i)] = block1Visibility.horizontal_x2[i];
            }
            for(int i = 0;i<block1Visibility.nEdges;i++) {
                int v1 = block1Visibility.edge_n1[i];
                int v2 = block1Visibility.edge_n2[i];
                int idx = edgeIndexes[block1.get(v1)][block1.get(v2)];
                vertical_x[idx] = block1Visibility.vertical_x[i];
                vertical_y1[idx] = block1Visibility.vertical_y1[i];
                vertical_y2[idx] = block1Visibility.vertical_y2[i];
            }

            
            // 3:
            while(!T.isEmpty()) {
                // Find all the blocks in T that have a cutpoint c in common with some blocks in S:
                if (DEBUG>=1) System.out.println("W-VISIBILITY2: T = " + T);
                if (DEBUG>=1) System.out.println("W-VISIBILITY2: S = " + S);
                for(int c:cutNodes.keySet()) {
                    List<Integer> blocksWithC = cutNodes.get(c);
                    List<Integer> newBlocks = new LinkedList<Integer>();
                    boolean intersectionWithS = false;
                    for(Integer bc:blocksWithC) {
                        if (S.contains(bc)) intersectionWithS = true;
                        if (T.contains(bc)) newBlocks.add(bc);
                    }
                    if (intersectionWithS && !newBlocks.isEmpty()) {
                        // Find min and max y values of the current visibility representation (to later reason
                        // on whether to add new blocks below or no top):
                        double currentMinY = 0;
                        double currentMaxY = 0;
                        for(int i = 0;i<n;i++) {
                            if (i==0 || horizontal_y[i]<currentMinY) currentMinY = horizontal_y[i];
                            if (i==0 || horizontal_y[i]>currentMaxY) currentMaxY = horizontal_y[i];
                        }
                        if (DEBUG>=1) System.out.println("W-VISIBILITY2: min/max y in the current representation: " + currentMinY + " / " + currentMaxY);

                        // ind a w-visibility representation for each Be, using algorithm W-VISIBILITY, 
                        // where in step 1, c is chosen to be the source vertex s:
                        if (DEBUG>=1) System.out.println("W-VISIBILITY2: next blocks via cutnode " + c + " are: " + newBlocks);

                        double leftx = horizontal_x1[c]+0.1;
                        double rightx = horizontal_x1[c]+0.9;
                        double topy = horizontal_y[c]-0.9;
                        double bottomy = horizontal_y[c];
                        double offset_step = (rightx - leftx)/newBlocks.size();
                        double offset = 0;
                        boolean flipY = false;
                        
                        if (bottomy == currentMaxY) flipY = true;

                        for(Integer blockID:newBlocks) {
                            if (DEBUG>=1) System.out.println("W-VISIBILITY2: computing visibility of block: " + blocks.get(blockID));
                            List<Integer> block = blocks.get(blockID);
                            int [][]blockgraph = blockSubgraph(block);
                            Visibility bv = new Visibility(blockgraph, r);
                            // get an st-numbering where s is the cutnode:
                            int blockSTNumbering[] = STNumbering.stNumbering(blockgraph, blocks.get(blockID).indexOf(c), r);
                            // compute the visibility:
                            if (!bv.WVisibility2Connected(blockSTNumbering)) return false;
                            
                            // scale down the above representations in such a way that they all fit 
                            // on the top of the vertex-segment corresponding to c in the w-visibility 
                            // representation already constructed for S;
                            double block_leftx = leftx + offset_step*offset;
                            double block_rightx = leftx + offset_step*(offset+0.9);
                            
                            if (DEBUG>=1) {
                                System.out.println("W-VISIBILITY2: adding new block at  " + block_leftx + " - " + block_rightx + " , " + topy + " - " + bottomy);
                            }
                            
                            offset++;
                            double minx = 0, maxx = 0;
                            double miny = 0, maxy = 0;
                            for(int j = 0;j<bv.horizontal_y.length;j++) {
                                if (j==0) {
                                    miny = maxy = bv.horizontal_y[j];
                                    minx = bv.horizontal_x1[j];
                                    maxx = bv.horizontal_x2[j];
                                } else {
                                    if (bv.horizontal_y[j]<miny) miny = bv.horizontal_y[j];
                                    if (bv.horizontal_y[j]>maxy) maxy = bv.horizontal_y[j];
                                    if (bv.horizontal_x1[j]<minx) minx = bv.horizontal_x1[j];
                                    if (bv.horizontal_x2[j]>maxx) maxx = bv.horizontal_x2[j];
                                }
                            }
                            for(int i = 0;i<block.size();i++) {
                                if (c!=block.get(i)) {
                                    if (flipY) {
                                        horizontal_y[block.get(i)] = bottomy + ((bv.horizontal_y[i] - miny)/(maxy-miny))*(bottomy-topy);
                                    } else {
                                        horizontal_y[block.get(i)] = bottomy - ((bv.horizontal_y[i] - miny)/(maxy-miny))*(bottomy-topy);
                                    }
                                    horizontal_x1[block.get(i)] = ((bv.horizontal_x1[i] - minx)/(maxx-minx))*(block_rightx-block_leftx)+block_leftx;
                                    horizontal_x2[block.get(i)] = ((bv.horizontal_x2[i] - minx)/(maxx-minx))*(block_rightx-block_leftx)+block_leftx;
                                    if (DEBUG>=1) {
                                        System.out.println("W-VISIBILITY2: adding new node " + block.get(i));
                                        System.out.println("  " + horizontal_x1[block.get(i)] + " - " + horizontal_x2[block.get(i)] + "," + horizontal_y[block.get(i)]);
                                    }
                                }
                            }
                            for(int i = 0;i<bv.nEdges;i++) {
                                int v1 = bv.edge_n1[i];
                                int v2 = bv.edge_n2[i];
                                int idx = edgeIndexes[block.get(v1)][block.get(v2)];
                                vertical_x[idx] = ((bv.vertical_x[i] - minx)/(maxx-minx))*(block_rightx-block_leftx)+block_leftx;
                                if (flipY) {
                                    vertical_y1[idx] = bottomy + ((bv.vertical_y1[i] - miny)/(maxy-miny))*(bottomy-topy);
                                    vertical_y2[idx] = bottomy + ((bv.vertical_y2[i] - miny)/(maxy-miny))*(bottomy-topy);
                                } else {
                                    vertical_y1[idx] = bottomy - ((bv.vertical_y1[i] - miny)/(maxy-miny))*(bottomy-topy);
                                    vertical_y2[idx] = bottomy - ((bv.vertical_y2[i] - miny)/(maxy-miny))*(bottomy-topy);
                                }
                                if (DEBUG>=1) {
                                    System.out.println("W-VISIBILITY2: adding new edge " + block.get(v1) + " - " + block.get(v2));
                                        System.out.println("  " + vertical_x[idx] + "," + vertical_y1[idx] + " - " + vertical_y2[idx]);
                                }
                            }                  
                        }
                        
                        T.removeAll(newBlocks);
                        S.addAll(newBlocks);
                        gridAlign(1.0);
//                        if (S.size()>3) return true;
                        break;
                    }
                }    
            }
            
            gridAlign(1.0);
            if (!sanityCheck()) {
                for(int i = 0;i<n;i++) {
                    System.err.print("{");
                    for(int j = 0;j<n;j++) {
                        System.err.print(graph[i][j] + ",");
                    }
                    System.err.println("},");
                }
                throw new Exception("WVisibility: Visibility representation is not consistent after merging!");
            }            
            return true;
        }
    } 
    
    int [][]blockSubgraph(List<Integer> block) {
        int bn = block.size();
        int [][]subgraph = new int[bn][bn];
        for(int i = 0;i<bn;i++) {
            for(int j = 0;j<bn;j++) {
                subgraph[i][j] = graph[block.get(i)][block.get(j)];
            }
        }
        return subgraph;
    }
    
    
    public boolean WVisibility2Connected() throws Exception {    
        // 1,2: select (s,t) and generate an st-order. 
        //      Generate the graph D induced by the st-ordering
        int stNumbering[] = STNumbering.stNumbering(graph, r);
        if (DEBUG>=1) {
            // verify the STNumbering:
            for(int i = 0;i<stNumbering.length;i++) {
                System.out.print("Node " + i + " -> " + stNumbering[i] + ": has neighbors");
                boolean smaller = false;
                boolean larger = false;
                if (stNumbering[i]==1) smaller = true;
                if (stNumbering[i]==stNumbering.length) larger = true;
                for(int j = 0;j<stNumbering.length;j++) {
                    if (graph[i][j]==1 || graph[j][i]==1) {
                        System.out.print(" " + j);
                        if (stNumbering[j]<stNumbering[i]) smaller = true;
                        if (stNumbering[j]>stNumbering[i]) larger = true;
                    }
                }
                System.out.println("");
                if (!smaller || !larger) throw new Exception("stNumbering is not correct!");
            }
        }
        return WVisibility2Connected(stNumbering);
    }
    
    
    public List<Visibility> allPossibleWVisibility2Connected() throws Exception {
        List<Visibility> l = new ArrayList<Visibility>();
        List<int []> stNumberings = STNumbering.allSTNumberings(graph);
        for(int []stNumbering:stNumberings) {
            if (!STNumbering.verifySTNumbering(graph, stNumbering)) {
                System.err.println("Wrong STNumbering!");
                System.err.println(Arrays.toString(stNumbering));
                throw new Exception("Wrong STNumbering!");
            }
            Visibility v = new Visibility(this);
            v.WVisibility2Connected(stNumbering);
            l.add(v);
        }
        return l;
    }
    
    
    public boolean WVisibility2Connected(int stNumbering[]) throws Exception {    
        int n = graph.length;        
        int s = -1;
        int t = -1;
        for(int i = 0;i<n;i++) {
            if (stNumbering[i]==1) s = i;
            if (stNumbering[i]==n) t = i;
        }
        if (DEBUG>=1) System.out.println("WVisibility2Connected, st-numbering: " + Arrays.toString(stNumbering));
        if (DEBUG>=1) System.out.println("WVisibility2Connected: (s,t) = (" + s + "," + t + ")");

        if (DEBUG>=1) {
            System.out.println("Graph indexed by stNumbers:");
            for(int i = 0;i<n;i++) {
                int v = -1;
                for(int tmp = 0;tmp<n;tmp++) if (stNumbering[tmp]==i+1) v = tmp;
                for(int j = 0;j<n;j++) {
                    int w = -1;
                    for(int tmp = 0;tmp<n;tmp++) if (stNumbering[tmp]==j+1) w = tmp;
                    System.out.print(graph[v][w] + " ");
                }        
                System.out.println("");
            }        
        }
        
        
        // 3: Find a planar representation of D such that the arc [s,t] is on the external face
        //    Use the planar representation to construct a new digraph DStar:
        int D_star[][];
        List<Integer> embedding[] = PlanarEmbedding.planarEmbedding2Connected(graph,stNumbering);
        if (embedding==null) return false;        
        if (DEBUG>=1) {
            System.out.println("Embedding of D:");
            for(int i = 0;i<embedding.length;i++) {
                System.out.println(i + " : " + embedding[i]);
            }
        }
        List<List<Integer>> faces = PlanarEmbedding.faces(embedding);
        int nFaces = faces.size();
        if (DEBUG>=1) {
            System.out.println("Faces of D:");
            int i = 0;
            for(List<Integer> face:PlanarEmbedding.faces(embedding)) {
                System.out.println((i++) + " : " + face);
            }
            System.out.println("Faces of D (in st-numbers):");
            i = 0;
            for(List<Integer> face:PlanarEmbedding.faces(embedding)) {
                System.out.print((i++) + " : ");
                for(Integer v:face) {
                    System.out.print(stNumbering[v] + " ");
                }
                System.out.println("");
            }

        }
        if (faces.size()==1 && n==2) {
            // this can only happen if the graph has only 2 nodes (special case)
            horizontal_y = new double[n];
            horizontal_x1 = new double[n];
            horizontal_x2= new double[n];
            for(int i = 0;i<n;i++) {
                horizontal_y[i] = stNumbering[i];
                horizontal_x1[i] = -1;
                horizontal_x2[i] = 0;
            }            
            
            // edge segments
            vertical_x = new double[nEdges];
            vertical_y1= new double[nEdges];
            vertical_y2= new double[nEdges];
            for(int i = 0;i<nEdges;i++) {
                vertical_x[i] = 0;
                vertical_y1[i] = stNumbering[s];
                vertical_y2[i] = stNumbering[t];
            }            
            return true;
        }
        
        
        D_star = new int[nFaces][nFaces];
        int s_star = -1;
        int t_star = -1;
        for(int face1=0;face1<nFaces;face1++) {
            int idxs = faces.get(face1).indexOf(s);
            int idxt = faces.get(face1).indexOf(t);
            int fn = faces.get(face1).size();
            if (idxs>=0 && idxt>=0) {
                // we want to make sure we get the two faces that are separated
                // by the s -> t edge
                if (((idxs+1)%fn) == idxt ||
                    ((idxt+1)%fn) == idxs) {
                    if (t_star==-1) t_star = face1;
                               else s_star = face1;
                }
            }
            for(int face2=face1+1;face2<nFaces;face2++) {
                Pair<Integer,Integer> edge = adjacentFaces(faces.get(face1),faces.get(face2));
                if (edge!=null) {
                    if (DEBUG>=1) System.out.println("Adjacent faces: " + face1 + " - " + face2 + " (via " + edge.m_a + " - " + edge.m_b + ", with st-numbers " + stNumbering[edge.m_a] + " - " + stNumbering[edge.m_b] + ")");
                    if (stNumbering[edge.m_a]>stNumbering[edge.m_b]) {
                        D_star[face1][face2]=2;                    
                    } else {                        
                        D_star[face2][face1]=2;
                    }
                }
            }
        }
        D_star[s_star][t_star]=0;
        D_star[t_star][s_star]=0;

        if (DEBUG>=1) {
            System.out.println("D*: (s*,t*) = ("  + s_star + "," + t_star + ")");
            for(int i = 0;i<D_star.length;i++) {
                for(int j = 0;j<D_star.length;j++) {
                    System.out.print(D_star[i][j] + " ");
                }        
                System.out.println("");
            }        
        }
        // 4: Apply the critical path medhod to D* with all arc-lengths equal to 2. 
        //    This gives the function alpha(f) for each vertex f of D*.
        int alpha[] = criticalPathCosts(D_star);
        if (DEBUG>=1) {
            System.out.println("Critical Path, alpha = " + Arrays.toString(alpha));
        }
                
        // 5: Construct the w-visibility representation:
        {
            // vertex segments:
            horizontal_x1 = new double[n];
            horizontal_x2= new double[n];
            List<Integer> vertexEdges[] = new List[n];  // to easily keep track of vertices in each node
            
            // edge segments
            vertical_x = new double[nEdges];
            vertical_y1= new double[nEdges];
            vertical_y2= new double[nEdges];
            
            // 5.1:  Use the st-numbering computed in step 2 to assign y-coordinates to horizontal vertex-segments.
            horizontal_y = new double[n];
            for(int i = 0;i<n;i++) horizontal_y[i] = stNumbering[i];
            
            // 5.2: Set the x-coordinate of arc [s, t] equal to -1.
            int st_index = edgeIndexes[s][t];
            vertical_x[st_index] = -1;
            
            for(int i = 0;i<n;i++) {
                for(int j = 0;j<n;j++) {
                    int idx = edgeIndexes[i][j];
                    if (idx!=-1 && stNumbering[i]<stNumbering[j]) {
                        if (idx!=st_index) {
                            int face1 = -1;
                            int face2 = -1;
                            for(List<Integer> face:faces) {
                                int idxi = face.indexOf(i);
                                int idxj = face.indexOf(j);
                                int fn = face.size();
                                if (idxi>=0 && idxj>=0 &&
                                    (((idxi+1)%fn)==idxj ||
                                     ((idxj+1)%fn)==idxi)) {
                                    if (face1==-1) {
                                        face1 = faces.indexOf(face);
                                    } else {
                                        face2 = faces.indexOf(face); 
                                        break;
                                    }
                                }
                            }
                            // 5.3: ...
                            vertical_x[idx] = (alpha[face1] + alpha[face2])/2;
                            if (DEBUG>=1) System.out.println("Visibility, faces for v segment (" + idx + ": " + i + "->" + j + "): " + face1 + " , " + face2 + " -> " + vertical_x[idx]);
                        }
                        // 5.4: 
                        vertical_y1[idx] = Math.min(horizontal_y[i],horizontal_y[j]);
                        vertical_y2[idx] = Math.max(horizontal_y[i],horizontal_y[j]);
                        
                        if (vertexEdges[i]==null) vertexEdges[i]=new LinkedList<Integer>();
                        vertexEdges[i].add(idx);
                        if (vertexEdges[j]==null) vertexEdges[j]=new LinkedList<Integer>();
                        vertexEdges[j].add(idx);
                    }
                }
            }
            
            // 5.5: 
            for(int v = 0;v<n;v++) {
                horizontal_x1[v] = 0;
                horizontal_x2[v] = 0;
                boolean first = true;
                for(Integer edge:vertexEdges[v]) {
                    // System.out.println(v + " considering edge " + edge);
                    if (first) {
                        first = false;
                        horizontal_x1[v] = horizontal_x2[v] = vertical_x[edge];
                    } else {
                        if (vertical_x[edge]<horizontal_x1[v]) horizontal_x1[v] = vertical_x[edge];
                        if (vertical_x[edge]>horizontal_x2[v]) horizontal_x2[v] = vertical_x[edge];
                    }
                }
                if (horizontal_x1[v] == horizontal_x2[v]) {
                    horizontal_x1[v]-=0.5;
                }
            }
        }
        gridAlign(1.0);
        
        if (!sanityCheck()) {
            System.err.println("WVisibility2Connected: Visibility representation is not consistent!");
            for(int i = 0;i<n;i++) {
                System.err.print("{");
                for(int j = 0;j<n;j++) {
                    System.err.print(graph[i][j] + ",");
                }
                System.err.println("},");
            }
            return false;
        }
        
        return true;
    }    
    
    
    static int[] criticalPathCosts(int [][]graph) {
        int n = graph.length;
        int distance[] = new int[n];
        int indegree[] = new int[n];
        
        for(int i=0;i<n;i++) {
            distance[i] = 0;
            indegree[i] = 0;
            for(int j = 0;j<n;j++) 
                if (graph[j][i]!=0) indegree[i]++;
        }
        
        List<Integer> Q = new LinkedList<Integer>();
        for(int v = 0;v<n;v++) {
            if (indegree[v]==0) Q.add(v);
        }
        
        while(!Q.isEmpty()) {
            int v = Q.remove(0);
            if (DEBUG>=2) System.out.println("criticalPathCosts: " + v);
            for(int u = 0;u<n;u++) {
                if (graph[v][u]!=0) {
                    distance[u] = Math.max(distance[u],distance[v]+graph[v][u]);
                    indegree[u]--;
                    if (indegree[u]==0) Q.add(u);
                }
            }
        }
        
        return distance;
    }
    
    
    // looks if two faces are adjacent (they are if the share any edge)
    static Pair<Integer,Integer> adjacentFaces(List<Integer> face1, List<Integer> face2) {
        for(int idx1 = 0;idx1<face1.size();idx1++) {
            int idx1b = idx1+1;
            if (idx1b>=face1.size()) idx1b=0;
            for(int idx2 = 0;idx2<face2.size();idx2++) {
                if (face1.get(idx1).equals(face2.get(idx2))) {
                    int idx2b = idx2-1;
                    if (idx2b<0) idx2b=face2.size()-1;
                    if (face1.get(idx1b).equals(face2.get(idx2b))) {
                            return new Pair<Integer,Integer>(face1.get(idx1),face1.get(idx1b));
                    }
                }
            }
        }
        return null;
    }
    
    
    /* 
        This method tries to realign the vertical segments, in order to minimize the 
        number of contact points of each horitonzal edges with vertical edges
    */
    public void reorganize() {
        gridAlign(1.0);
        
        for(int i = 0;i<horizontal_y.length;i++) {
            if (horizontal_x1[i]>horizontal_x2[i]) {
                double tmp = horizontal_x1[i];
                horizontal_x1[i] = horizontal_x2[i];
                horizontal_x2[i] = tmp;
            }
        }
        for(int i = 0;i<vertical_x.length;i++) {
            if (vertical_y1[i]>vertical_y2[i]) {
                double tmp = vertical_y1[i];
                vertical_y1[i] = vertical_y2[i];
                vertical_y2[i] = tmp;
            }
        }

        /*
        boolean anotherRound;
        do {
            anotherRound = false;
            Visibility v = new Visibility(this);
            if (v.reorganizeAttempt() && v.sanityCheck()) {
                copy(v);
                anotherRound = true;
            }
        }while(anotherRound);        
        */
        boolean []alreadyMoved = new boolean[vertical_x.length];
        for(int i = 0;i<alreadyMoved.length;i++) alreadyMoved[i] = false;
        while(reorganizeAttempt(alreadyMoved));
        gridAlign(1.0);
    }
     
    public boolean reorganizeAttempt(boolean []alreadyMoved) {        
        for(int i = 0;i<graph.length;i++) {
            // find the number of different contact points:
            List<Integer> upContacts = new LinkedList<Integer>();
            List<Integer> downContacts = new LinkedList<Integer>();
            List<Integer> upContactsIdx = new LinkedList<Integer>();
            List<Integer> downContactsIdx = new LinkedList<Integer>();
            List<Integer> allContacts = new LinkedList<Integer>();
            int leftMostUp = -1;
            int rightMostUp = -1;
            int leftMostDown = -1;
            int rightMostDown = -1;
            for(int j = 0;j<graph.length;j++) {
                if (graph[i][j]!=0) {
                    int idx = edgeIndexes[i][j];
                    int x = (int)vertical_x[idx];
                    if (!allContacts.contains(x)) allContacts.add(x);
                    if (horizontal_y[j]>horizontal_y[i]) {
                        downContacts.add(x);
                        downContactsIdx.add(idx);
                        if (leftMostDown==-1 || x<vertical_x[leftMostDown]) leftMostDown = idx;
                        if (rightMostDown==-1 || x>vertical_x[rightMostDown]) rightMostDown = idx;
                    } else {
                        upContacts.add(x);
                        upContactsIdx.add(idx);
                        if (leftMostUp==-1 || x<vertical_x[leftMostUp]) leftMostUp = idx;
                        if (rightMostUp==-1 || x>vertical_x[rightMostUp]) rightMostUp = idx;
                    }
                }
            }
                        
            if (DEBUG>=1) {
                System.out.println("Vertex " + i + " has the following contacts: " + allContacts);
                System.out.println("   up: " + upContacts);
                System.out.println("   down: " + downContacts);
            }
            if (allContacts.size()>Math.max(upContacts.size(),downContacts.size())) {
                if (DEBUG>=1) System.out.println("Vertex " + i + " might require reorganizing.");

                // try to move the leftMostUp over the leftMostDown:
                if (!alreadyMoved[leftMostUp] && ((int)vertical_x[leftMostUp])>((int)vertical_x[leftMostDown])) {
                    int upx = (int)vertical_x[leftMostUp];
                    int downx = (int)vertical_x[leftMostDown];                         
                    if (DEBUG>=1) System.out.println("  Trying to fix it by moving the leftMostUp over the leftMostDown edge " + leftMostUp + " (x = " + upx + ")");                    
                    if (moveSegment(leftMostUp, upx, downx)) {
                        alreadyMoved[leftMostUp] = true;
                        return true;
                    }
                }
                // try to move the leftMostDown over the leftMostUp:
                if (!alreadyMoved[leftMostDown] && ((int)vertical_x[leftMostUp])<((int)vertical_x[leftMostDown])) {
                    int upx = (int)vertical_x[leftMostUp];
                    int downx = (int)vertical_x[leftMostDown];                         
                    if (DEBUG>=1) System.out.println("  Trying to fix it by moving the leftMostDown over the leftMostUp edge " + leftMostDown + " (x = " + downx + ")");                        
                    if (moveSegment(leftMostDown, downx, upx)) {
                        alreadyMoved[leftMostDown] = true;
                        return true;
                    }
                }

                // try to move the rightMostUp over the rightMostDown:
                if (!alreadyMoved[rightMostUp] && ((int)vertical_x[rightMostUp])<((int)vertical_x[rightMostDown])) {
                    int upx = (int)vertical_x[rightMostUp];
                    int downx = (int)vertical_x[rightMostDown];                         
                    if (DEBUG>=1) System.out.println("  Trying to fix it by moving the rightMostUp over the rightMostDown edge " + rightMostUp + " (x = " + upx + ")");                        
                    if (moveSegment(rightMostUp, upx, downx)) {
                        alreadyMoved[rightMostUp] = true;
                        return true;
                    }
                }
                // try to move the rightMostDown over the rightMostUp:
                if (!alreadyMoved[rightMostDown] && ((int)vertical_x[rightMostUp])>((int)vertical_x[rightMostDown])) {
                    int upx = (int)vertical_x[rightMostUp];
                    int downx = (int)vertical_x[rightMostDown];                         
                    if (DEBUG>=1) System.out.println("  Trying to fix it by moving the rightMostDown over the rightMostUp edge " + rightMostDown + " (x = " + downx + ")");                        
                    if (moveSegment(rightMostDown, downx, upx)) {
                        alreadyMoved[rightMostDown] = true;
                        return true;
                    }
                }
            
            }
        }
        return false;
    }
    
    public boolean moveSegment(int segIdx, double oldx, double newx) {
        double tolerance = 0.1;

        // see if we can move upx to downx:
        // check for collision with the vertical segments:
        for(int j = 0;j<vertical_x.length;j++) {
            if (j!=segIdx) {
                if ((vertical_x[j]>=newx-tolerance && vertical_x[j]<=oldx+tolerance) ||
                    (vertical_x[j]>=oldx-tolerance && vertical_x[j]<=newx+tolerance)) {
                    if (vertical_y1[j]+tolerance<vertical_y2[segIdx] &&
                        vertical_y2[j]-tolerance>vertical_y1[segIdx]) {
                        if (DEBUG>=1) System.out.println("Can't because of vertical segment " + j);
                        return false;
                    }
                }
            }
        }
        // check for collision with the horizontal segments:
        for(int i = 0;i<horizontal_y.length;i++) {
            if (i==edge_n1[segIdx] || i==edge_n2[segIdx]) continue;
            if (Math.abs(horizontal_y[i]-horizontal_y[edge_n1[segIdx]])<tolerance) {
                if (horizontal_x1[i]+tolerance<horizontal_x2[edge_n1[segIdx]] &&
                    horizontal_x2[i]-tolerance>horizontal_x1[edge_n1[segIdx]]) {
                    if (DEBUG>=1) System.out.println("Can't because of horizontal segment " + i);
                    return false;
                }
            }
            if (Math.abs(horizontal_y[i]-horizontal_y[edge_n2[segIdx]])<tolerance) {
                if (horizontal_x1[i]+tolerance<horizontal_x2[edge_n2[segIdx]] &&
                    horizontal_x2[i]-tolerance>horizontal_x1[edge_n2[segIdx]]) {
                    if (DEBUG>=1) System.out.println("Can't because of horizontal segment " + i);
                    return false;
                }
            }
        }

        vertical_x[segIdx] = newx;
        horizontal_x1[edge_n1[segIdx]] = Math.min(horizontal_x1[edge_n1[segIdx]],newx);
        horizontal_x2[edge_n1[segIdx]] = Math.max(horizontal_x2[edge_n1[segIdx]],newx);                                
        horizontal_x1[edge_n2[segIdx]] = Math.min(horizontal_x1[edge_n2[segIdx]],newx);
        horizontal_x2[edge_n2[segIdx]] = Math.max(horizontal_x2[edge_n2[segIdx]],newx);
        if (DEBUG>=1) System.out.println("  Edge " + segIdx + " moved from " + oldx + " to " + newx);
        return true;
    }
    
    
    public void printResult() {
        for(int i = 0;i<horizontal_y.length;i++) {
            System.out.println("Hsegment: (" + horizontal_x1[i] + "-" + horizontal_x2[i] + ", " + horizontal_y[i] + ")");
        }
        for(int i = 0;i<vertical_x.length;i++) {
            System.out.println("Vsegment: (" + vertical_x[i] + ", " + vertical_y1[i] + "-" + vertical_y2[i] + ")");
        }
    }
    
    /*
    // visibility results:
    public double horizontal_y[];   // graph nodes
    public double horizontal_x1[];
    public double horizontal_x2[];
    public double vertical_x[];     // graph edges
    public double vertical_y1[];
    public double vertical_y2[];    
    
    */
    
    public boolean sanityCheck() {
        for(int i = 0;i<vertical_x.length;i++) {
            if (vertical_y2[i]==vertical_y1[i]) {
                System.err.println("vertical edge with negative or zero length: " + i);
                System.err.println(i + ": " + vertical_x[i] + ", [" + vertical_y1[i] + "," + vertical_y2[i] + "]");
                return false;
            }
            for(int j = i+1;j<vertical_x.length;j++) {
                if (Math.abs(vertical_x[i] - vertical_x[j])<0.01) {
                    if (vertical_y1[i]<vertical_y2[j] &&
                        vertical_y1[j]<vertical_y2[i]) {
                        System.err.println("vertical edges cross: " + i + ", " + j);
                        System.err.println(i + ": " + vertical_x[i] + ", [" + vertical_y1[i] + "," + vertical_y2[i] + "]");
                        System.err.println(j + ": " + vertical_x[j] + ", [" + vertical_y1[j] + "," + vertical_y2[i] + "]");
                        return false;
                    }
                }
            }        
        }
        for(int i = 0;i<horizontal_y.length;i++) {
            if (horizontal_x2[i]==horizontal_x1[i]) {
                System.err.println("horizontal vertex with negative or zero length: " + i);
                System.err.println(i + ": " + horizontal_y[i] + ", [" + horizontal_x1[i] + "," + horizontal_x2[i] + "]");
                return false;
            }
            for(int j = i+1;j<horizontal_y.length;j++) {
                if (Math.abs(horizontal_y[i] - horizontal_y[j])<0.01) {
                    if (horizontal_x1[i]<horizontal_x2[j] &&
                        horizontal_x1[j]<horizontal_x2[i]) {
                        System.err.println("horizontal vertices cross: " + i + ", " + j);
                        System.err.println(i + ": " + horizontal_y[i] + ", [" + horizontal_x1[i] + "," + horizontal_x2[i] + "]");
                        System.err.println(j + ": " + horizontal_y[j] + ", [" + horizontal_x1[j] + "," + horizontal_x2[i] + "]");
                        return false;
                    }
                }
            }        
        }
        
        return true;
    }
     
}
