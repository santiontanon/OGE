/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package orthographicembedding;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import util.Pair;

/**
 *
 * @author santi
 */
public class OrthographicEmbeddingResult {
    public static int DEBUG = 0;

    public OEVertex embedding[];
    public int nodeIndexes[];
    public double x[];
    public double y[];
    public boolean edges[][];
    
    public static double separation = 0.25;
    
    public OrthographicEmbeddingResult(int n)
    {
        embedding = new OEVertex[n];
        nodeIndexes = new int[n];
        x = new double[n];
        y = new double[n];
        edges = new boolean[n][n];        
    }
    

    public OrthographicEmbeddingResult(OEVertex a_embedding[], Visibility visibility, boolean fixNonOrthogonal) throws Exception {
        embedding = a_embedding;

        // count the number of nodes in the embedding:
        int n = a_embedding.length;
        for(int v = 0;v<embedding.length;v++) {
            for(OEElement e:embedding[v].embedding) {
                n+=e.bends;
            }
        }

        nodeIndexes = new int[n];
        x = new double[n];
        y = new double[n];
        edges = new boolean[n][n];

        // populate the list of corrdinates and edges:
        // add the basic nodes:
        int idx = 0;
        for(int v = 0;v<embedding.length;v++) {
//            System.out.println("index " + idx + " is node " + v);
            nodeIndexes[idx] = embedding[v].v;
            x[idx] = embedding[v].x;
            y[idx] = embedding[v].y;
            idx++;
        }

        // add the nodes + edges resulting from the "bends"
        for(int v = 0;v<embedding.length;v++) {
            // process all the bends:
            OEVertex ev = embedding[v];
            for(OEElement oev:ev.embedding) {
                int w = oev.dest;
                if (w>v) {
                    OEVertex ew = embedding[w];
                    OEElement oew = null;
                    for(OEElement tmp:ew.embedding) {
                        if (tmp.dest==v) {
                            oew = tmp;
                            break;
                        }
                    }
                    if (DEBUG>=1) System.out.println("Creating conector " + v + " -> " + w + " with bends " + oev.bends + ", " + oew.bends + " (" + OEElement.directionNames[oev.angle] + " - " + OEElement.directionNames[oew.angle] + ")");
                    if (oev.bends==0 && oew.bends==0) {
//                        System.out.println("  edge.");
                        edges[v][w] = true;
                    } else {
                        // handle the bends:
                        double startx = x[v];
                        double starty = y[v];
                        double endx = x[w];
                        double endy = y[w];

                        /*
                        double y_direction = 0;
                        if (starty>endy) {
                            y_direction = 1;
                        } if (starty<endy) {
                            y_direction = -1;
                        }
                        */
                        double intermediate_x = visibility.vertical_x[visibility.edgeIndexes[v][w]];

                        if (oev.bends+oew.bends==1) {
                            nodeIndexes[idx]=-1;
                            if (oev.angle==OEElement.LEFT || oev.angle==OEElement.RIGHT) {
//                                System.out.println("midpoints assigned to " + oev.v + " -> " + oew.v + " by method 1a");
                                x[idx] = endx;
                                if (oew.angle==OEElement.DOWN) {
                                    if (starty>endy+0.01) {
                                        y[idx] = starty;
                                    } else {
                                        y[idx] = endy+separation; // needs to be fixed by fixNonOrthogonalEdges
                                    }
                                } else {
                                    if (starty<endy-0.01) {
                                        y[idx] = starty;
                                    } else {
                                        y[idx] = endy-separation; // needs to be fixed by fixNonOrthogonalEdges
                                    }
                                }
                                y[idx] = starty;
                            } else {
//                                System.out.println("midpoints assigned to " + oev.v + " -> " + oew.v + " by method 1b");
                                x[idx] = startx;
                                if (oev.angle==OEElement.DOWN) {
                                    if (endy>starty+0.01) {
                                        y[idx] = endy;
                                    } else {
                                        y[idx] = starty+separation; // needs to be fixed by fixNonOrthogonalEdges
                                    }
                                } else {
                                    if (endy<starty-0.01) {
                                        y[idx] = endy;
                                    } else {
                                        y[idx] = starty-separation; // needs to be fixed by fixNonOrthogonalEdges
                                    }
                                }
                            }
                            edges[v][idx] = true;
                            edges[idx][w] = true;
                            idx++;
                        } else if (oev.bends+oew.bends==2) {
                            nodeIndexes[idx]=-1;
                            nodeIndexes[idx+1]=-1;
                            if (oev.angle==OEElement.LEFT) {
                                if (DEBUG>=1) System.out.println("  connector "+v+" to "+w+" with 2 bends (LEFT)");
                                if (intermediate_x>startx) {
                                    x[idx] = startx-separation;
                                    y[idx] = starty;
                                    x[idx+1] = startx-separation;
                                    y[idx+1] = endy;
                                } else {
                                    if (oew.angle==OEElement.LEFT && intermediate_x>endx) {
                                        x[idx] = endx-separation;
                                        y[idx] = starty;
                                        x[idx+1] = endx-separation;
                                        y[idx+1] = endy;
                                    } else {
                                        x[idx] = intermediate_x;
                                        y[idx] = starty;
                                        x[idx+1] = intermediate_x;
                                        y[idx+1] = endy;
                                    }
                                }
                                edges[v][idx] = true;
                                edges[idx][idx+1] = true;
                                edges[idx+1][w] = true;
                                idx+=2;
                            } else if (oev.angle==OEElement.RIGHT) {
                                if (intermediate_x<startx) {
                                    if (DEBUG>=1) System.out.println("  connector with 2 bends (RIGHT), case 1");
                                    x[idx] = startx+separation;
                                    y[idx] = starty;
                                    x[idx+1] = startx+separation;
                                    y[idx+1] = endy;
                                } else {
                                    if (oew.angle==OEElement.RIGHT && intermediate_x<endx) {                                    
                                        if (DEBUG>=1) System.out.println("  connector with 2 bends (RIGHT), case 2");
                                        x[idx] = endx+separation;
                                        y[idx] = starty;
                                        x[idx+1] = endx+separation;
                                        y[idx+1] = endy;
                                    } else {
                                        if (DEBUG>=1) System.out.println("  connector with 2 bends (RIGHT), case 3");
                                        x[idx] = intermediate_x;
                                        y[idx] = starty;
                                        x[idx+1] = intermediate_x;
                                        y[idx+1] = endy;
                                    }
                                }
                                edges[v][idx] = true;
                                edges[idx][idx+1] = true;
                                edges[idx+1][w] = true;
                                idx+=2;
                            } else {
                                if (Math.abs(startx-endx)<0.001) {
                                    edges[v][w] = true;
                                } else {
    //                                System.out.println(v + " -> " + w + " startx: " + startx + ", endx: " + endx);
                                    if (Math.abs(intermediate_x-startx)<0.001) {
                                        if (oew.angle==OEElement.UP) { // up from the end
    //                                        System.out.println("up from the end");
                                            x[idx] = startx;
                                            y[idx] = endy-separation;
                                            x[idx+1] = endx;
                                            y[idx+1] = endy-separation;
                                        } else {            // down from the end
                                            x[idx] = startx;
                                            y[idx] = endy+separation;
                                            x[idx+1] = endx;
                                            y[idx+1] = endy+separation;
                                        }
                                    } else {
                                        if (oev.angle==OEElement.UP) { // up from the start
    //                                        System.out.println("up from the start");
                                            x[idx] = startx;
                                            y[idx] = starty-separation;
                                            x[idx+1] = endx;
                                            y[idx+1] = starty-separation;
                                        } else {            // down from the start
    //                                        System.out.println("down from the start");
                                            x[idx] = startx;
                                            y[idx] = starty+separation;
                                            x[idx+1] = endx;
                                            y[idx+1] = starty+separation;
                                        }
                                    }
                                    edges[v][idx] = true;
                                    edges[idx][idx+1] = true;
                                    edges[idx+1][w] = true;
                                    idx+=2;
                                }
                            }
                        } else if (oev.bends+oew.bends==3) {
                            nodeIndexes[idx]=-1;
                            nodeIndexes[idx+1]=-1;
                            nodeIndexes[idx+2]=-1;
                            edges[v][idx] = true;
                            edges[idx][idx+1] = true;
                            edges[idx+1][idx+2] = true;
                            edges[idx+2][w] = true;
                            int nnewvertices = 3;
                            if (oev.angle==OEElement.LEFT) {
                                if (DEBUG>=1) System.out.println("  connector with 3 bends (LEFT)");
                                double tmpx = intermediate_x;
                                if (intermediate_x>startx) tmpx = startx-separation;
                                x[idx] = tmpx;
                                y[idx] = starty;
                                if (oew.angle==OEElement.DOWN) {
                                    x[idx+1] = tmpx;
                                    y[idx+1] = endy+separation;
                                    x[idx+2] = endx;
                                    y[idx+2] = endy+separation;
                                } else {
                                    x[idx+1] = tmpx;
                                    y[idx+1] = endy-separation;
                                    x[idx+2] = endx;
                                    y[idx+2] = endy-separation;
                                }
                            } else if (oev.angle==OEElement.RIGHT) {
                                if (DEBUG>=1) System.out.println("  connector with 3 bends (RIGHT)");
                                double tmpx = intermediate_x;
                                if (intermediate_x<startx) tmpx = startx+separation;
                                x[idx] = tmpx;
                                y[idx] = starty;
                                if (oew.angle==OEElement.DOWN) {
                                    x[idx+1] = tmpx;
                                    y[idx+1] = endy+separation;
                                    if (Math.abs(tmpx-endx)>0.001) {
                                        x[idx+2] = endx;
                                        y[idx+2] = endy+separation;
                                    } else {
                                        // the thrid vertex would be identical to the second, so, do not add it!:
                                        nnewvertices = 2;
                                        edges[idx+1][idx+2] = false;
                                        edges[idx+2][w] = false;
                                        edges[idx+1][w] = true;
                                    }
                                } else {
                                    x[idx+1] = tmpx;
                                    y[idx+1] = endy-separation;
                                    if (Math.abs(tmpx-endx)>0.001) {
                                        x[idx+2] = endx;
                                        y[idx+2] = endy-separation;
                                    } else {
                                        // the thrid vertex would be identical to the second, so, do not add it!:
                                        nnewvertices = 2;                                        
                                        edges[idx+1][idx+2] = false;
                                        edges[idx+2][w] = false;
                                        edges[idx+1][w] = true;
                                    }
                                }
                            } else {
                                if (DEBUG>=1) System.out.println("  connector with 3 bends (UP/DOWN)");
                                double tmpx = intermediate_x;
                                if (oew.angle==OEElement.LEFT) {
                                    if (intermediate_x>endx) tmpx = endx-separation;                                    
                                } 
                                if (oew.angle==OEElement.RIGHT) {
                                    if (intermediate_x<endx) tmpx = endx+separation;                                    
                                }
                                if (Math.abs(startx-tmpx)>0.001) {
                                    if (oev.angle==OEElement.DOWN) {
                                        x[idx] = startx;
                                        y[idx] = starty+separation;
                                        x[idx+1] = tmpx;
                                        y[idx+1] = starty+separation;
                                    } else {
                                        x[idx] = startx;
                                        y[idx] = starty-separation;
                                        x[idx+1] = tmpx;
                                        y[idx+1] = starty-separation;
                                    }
                                    x[idx+2] = tmpx;
                                    y[idx+2] = endy;
                                } else {
                                    // the thrid vertex would be identical to the second, so, do not add it!:
                                    nnewvertices = 2;                                        
                                    edges[idx+1][idx+2] = false;
                                    edges[idx+2][w] = false;
                                    edges[idx+1][w] = true;
                                    if (oev.angle==OEElement.DOWN) {
                                        x[idx] = startx;
                                        y[idx] = starty+separation;
                                    } else {
                                        x[idx] = startx;
                                        y[idx] = starty-separation;
                                    }
                                    x[idx+1] = tmpx;
                                    y[idx+1] = endy;                                    
                                }
                            }                            
                            idx+=nnewvertices;
                        } else {
                            //Connector with 4 bends:
                            nodeIndexes[idx]=-1;
                            nodeIndexes[idx+1]=-1;
                            nodeIndexes[idx+2]=-1;
                            nodeIndexes[idx+3]=-1;
                            if (oev.angle==OEElement.DOWN) {
                                x[idx] = startx;
                                y[idx] = starty+separation;
                                x[idx+1] = intermediate_x;
                                y[idx+1] = starty+separation;
                            } else {
                                x[idx] = startx;
                                y[idx] = starty-separation;
                                x[idx+1] = intermediate_x;
                                y[idx+1] = starty-separation;
                            }
                            if (oew.angle==OEElement.DOWN) {
                                x[idx+2] = intermediate_x;
                                y[idx+2] = endy+separation;
                                x[idx+3] = endx;
                                y[idx+3] = endy+separation;
                            } else {
                                x[idx+2] = intermediate_x;
                                y[idx+2] = endy-separation;
                                x[idx+3] = endx;
                                y[idx+3] = endy-separation;
                            }
                            edges[v][idx] = true;
                            edges[idx][idx+1] = true;
                            edges[idx+1][idx+2] = true;
                            edges[idx+2][idx+3] = true;
                            edges[idx+3][w] = true;
                            idx+=4;

                        }
                    }
                }
            }
        }
        
        if (idx<n) {
            int nodeIndexes2[] = new int[idx];
            double x2[] = new double[idx];
            double y2[] = new double[idx];
            boolean edges2[][] = new boolean[idx][idx];

            for(int i = 0;i<idx;i++) {
                nodeIndexes2[i] = nodeIndexes[i];
                x2[i] = x[i];
                y2[i] = y[i];
                for(int j = 0;j<idx;j++) {
                    edges2[i][j] = edges[i][j];
                }
            }
    
            nodeIndexes = nodeIndexes2;
            x = x2;
            y = y2;
            edges = edges2;
        }

        if (fixNonOrthogonal) fixNonOrthogonalEdges();
        gridAlign(1.0);
    }


    public void fixNonOrthogonalEdges() throws Exception {
        boolean repeat = true;
        boolean desperate = false;
        do {
            List<Pair<Integer,Integer>> edges = findNonOrthogonalEdges();
            repeat = false;
            for(Pair<Integer,Integer> edge:edges) {
                if (fixNonOrthogonalEdge(edge, desperate)) {
                    repeat = true;
                    break;
                }
            }
            if (!repeat && !edges.isEmpty() && !desperate) {
                // one more attempt in "desperate" mode
                repeat = true;
                desperate = true;
            }
        }while(repeat);
    }


    // if "desperate == true", when it cannot fix an edge, it will replace it by a 
    // bent connector.
    public boolean fixNonOrthogonalEdge(Pair<Integer,Integer> edge, boolean desperate) throws Exception {
        gridAlign(1.0);

        if (DEBUG>=1) System.out.println("fixNonOrthogonalEdges: " + edge);
        OEVertex v = (edge.m_a<embedding.length ? embedding[edge.m_a]:null);
        OEVertex w = (edge.m_b<embedding.length ? embedding[edge.m_b]:null);
        OEElement oev = null;
        OEElement oew = null;
        if (v!=null) {
            int target = -1;
            if (w!=null) {
                target = w.v;
            } else {
                List<Integer> visited = new LinkedList<Integer>();
                visited.add(v.v);
                target = edge.m_b;
                while(target>=embedding.length) {
                    for(int i = 0;i<edges.length;i++) {
                        if ((edges[target][i] || edges[i][target]) && !visited.contains(i)) {
                            visited.add(i);
                            target = i;
                            break;
                        }
                    }
                }
            }
            if (DEBUG>=1) System.out.println("  looking for " + v.v + " -> " + target);
            if (v.v==target) throw new Exception("fixNonOrthogonalEdges: looking for " + v.v + " -> " + target);
            for(OEElement tmp:v.embedding) {
                if (tmp.dest==target) {
                    oev = tmp;
                    break;
                }
            }
        }
        if (w!=null) {
            int target = -1;
            if (v!=null) {
                target = v.v;
            } else {
                List<Integer> visited = new LinkedList<Integer>();
                visited.add(w.v);
                target = edge.m_a;
                while(target>=embedding.length) {
                    if (DEBUG>=1) System.out.println("target: " + target);
                    for(int i = 0;i<edges.length;i++) {
                        if ((edges[target][i] || edges[i][target]) && !visited.contains(i)) {
                            visited.add(i);
                            target = i;
                            break;
                        }
                    }
                }
            }
            if (DEBUG>=1) System.out.println("  looking for " + w.v + " -> " + target);
            if (w.v==target) throw new Exception("fixNonOrthogonalEdges: looking for " + w.v + " -> " + target);
            for(OEElement tmp:w.embedding) {
                if (tmp.dest==target) {
                    oew = tmp;
                    break;
                }
            }
        }

        if (DEBUG>=1) System.out.println("  v: " + oev);
        if (DEBUG>=1) System.out.println("  w: " + oew);

        if ((oev==null || (oev.angle==OEElement.LEFT || oev.angle==OEElement.RIGHT)) &&
            (oew==null || (oew.angle==OEElement.LEFT || oew.angle==OEElement.RIGHT))) {
            // Find the vertical ranges of movement of each vertex:
            List<Integer> group_v = new LinkedList<Integer>();
            List<Integer> group_w = new LinkedList<Integer>();
            Pair<Double,Double> range_v = nodeVerticalWiggleRoom(edge.m_a, group_v);
            Pair<Double,Double> range_w = nodeVerticalWiggleRoom(edge.m_b, group_w);

            // if the ranges overlap, then move them to the center of the overlap:
            if (DEBUG>=1) System.out.println("  V range of " + edge.m_a + ": " + range_v);
            if (DEBUG>=1) System.out.println("  V range of " + edge.m_b + ": " + range_w);
            if (range_v.m_a<=range_w.m_b &&
                range_w.m_a<=range_w.m_b) {
                double overlap_y1 = Math.max(range_v.m_a,range_w.m_a);
                double overlap_y2 = Math.min(range_v.m_b,range_w.m_b);
                double new_y = (overlap_y1+overlap_y2)/2;

                // check if the edge would work:
                List<Integer> toIgnore = new LinkedList<Integer>();
                toIgnore.addAll(group_v);
                toIgnore.addAll(group_w);
                double min_x = x[edge.m_a];
                double max_x = x[edge.m_a];
                for(Integer tmp:group_v) {
                    if (x[tmp]<min_x) min_x = x[tmp];
                    if (x[tmp]>max_x) max_x = x[tmp];
                }
                for(Integer tmp:group_w) {
                    if (x[tmp]<min_x) min_x = x[tmp];
                    if (x[tmp]>max_x) max_x = x[tmp];
                }
                boolean fits = false;
                fits = edgeFitsIgnoring(min_x, new_y, max_x, new_y, toIgnore);
                if (!fits) {
                    for(double tmpy = overlap_y1;tmpy<=overlap_y2;tmpy+=0.25) {
                        if (edgeFitsIgnoring(min_x, new_y, max_x, new_y, toIgnore)) {
                            fits = true;
                            new_y = tmpy;
                            break;
                        }                        
                    }
                }
                if (fits) {                
                    for(int tmp:group_v) y[tmp] = new_y;
                    for(int tmp:group_w) y[tmp] = new_y;
                    if (DEBUG>=1) System.out.println("Fixed Y to " + new_y);
                    return true;
                }
            }
            if (desperate) {
                if (DEBUG>=1) System.out.println("Edge does not fit!");
                // restore a bent connector:
                edges[edge.m_a][edge.m_b] = false;
                edges[edge.m_b][edge.m_a] = false;
                int idx = x.length;
                x = Arrays.copyOf(x, x.length+2);
                y = Arrays.copyOf(y, y.length+2);
                nodeIndexes = Arrays.copyOf(nodeIndexes, nodeIndexes.length+2);
                boolean [][]newedges = new  boolean[edges.length+2][edges.length+2];
                for(int i = 0;i<edges.length;i++) {
                    for(int j = 0;j<edges.length;j++) {
                        newedges[i][j] = edges[i][j];
                    }
                }
                edges = newedges;
                double direction = separation;
                if (x[edge.m_b]<x[edge.m_a]) direction = -separation;
                if (edgeFits(x[edge.m_a], y[edge.m_a], x[edge.m_b]-direction, y[edge.m_a])) {
                    x[idx] = x[edge.m_b]-direction;
                    y[idx] = y[edge.m_a];
                    x[idx+1] = x[edge.m_b]-direction;
                    y[idx+1] = y[edge.m_b];
                } else {
                    x[idx] = x[edge.m_a]+direction;
                    y[idx] = y[edge.m_a];
                    x[idx+1] = x[edge.m_a]+direction;
                    y[idx+1] = y[edge.m_b];
                }
                nodeIndexes[idx] = -1;
                nodeIndexes[idx+1] = -1;
                edges[edge.m_a][idx] = true;
                edges[idx][idx+1] = true;
                edges[idx+1][edge.m_b] = true;
                return true;
            }
        }

        if ((oev==null || (oev.angle==OEElement.UP || oev.angle==OEElement.DOWN)) &&
            (oew==null || (oew.angle==OEElement.UP || oew.angle==OEElement.DOWN))) {
            // Find the horizontal ranges of movement of each vertex:
            List<Integer> group_v = new LinkedList<Integer>();
            List<Integer> group_w = new LinkedList<Integer>();
            Pair<Double,Double> range_v = nodeHorizontalWiggleRoom(edge.m_a, group_v);
            Pair<Double,Double> range_w = nodeHorizontalWiggleRoom(edge.m_b, group_w);

            // if the ranges overlap, then move them to the center of the overlap:
            if (DEBUG>=1) System.out.println("  H range of " + edge.m_a + ": " + range_v);
            if (DEBUG>=1) System.out.println("  H range of " + edge.m_b + ": " + range_w);
            if (range_v.m_a<=range_w.m_b &&
                range_w.m_a<=range_w.m_b) {
                double overlap_x1 = Math.max(range_v.m_a,range_w.m_a);
                double overlap_x2 = Math.min(range_v.m_b,range_w.m_b);
                double new_x = (overlap_x1+overlap_x2)/2;

                // check if the edge would work:
//                edges[edge.m_a][edge.m_b] = false;
//                edges[edge.m_b][edge.m_a] = false;
//                if (edgeFits(new_x,y[edge.m_a], new_x, y[edge.m_b])) {
                List<Integer> toIgnore = new LinkedList<Integer>();
                toIgnore.addAll(group_v);
                toIgnore.addAll(group_w);
                double min_y = y[edge.m_a];
                double max_y = y[edge.m_a];
                for(Integer tmp:group_v) {
                    if (y[tmp]<min_y) min_y = y[tmp];
                    if (y[tmp]>max_y) max_y = y[tmp];
                }
                for(Integer tmp:group_w) {
                    if (y[tmp]<min_y) min_y = y[tmp];
                    if (y[tmp]>max_y) max_y = y[tmp];
                }
                boolean fits = false;
                fits = edgeFitsIgnoring(new_x, min_y, new_x, max_y, toIgnore);
                if (!fits) {
                    for(double tmpx = overlap_x1;tmpx<=overlap_x2;tmpx+=separation) {
                        if (edgeFitsIgnoring(tmpx, min_y, tmpx, max_y, toIgnore)) {
                            fits = true;
                            new_x = tmpx;
                            break;
                        }                        
                    }
                }
                if (fits) {
                    for(int tmp:group_v) x[tmp] = new_x;
                    for(int tmp:group_w) x[tmp] = new_x;
                    if (DEBUG>=1) System.out.println("Fixed X to " + new_x);
                    return true;
                }
            }
            if (desperate) {
                if (DEBUG>=1) System.out.println("Edge does not fit!");
                // restore a bent connector:
                edges[edge.m_a][edge.m_b] = false;
                edges[edge.m_b][edge.m_a] = false;
                int idx = x.length;
                x = Arrays.copyOf(x, x.length+2);
                y = Arrays.copyOf(y, y.length+2);
                nodeIndexes = Arrays.copyOf(nodeIndexes, nodeIndexes.length+2);
                boolean [][]newedges = new  boolean[edges.length+2][edges.length+2];
                for(int i = 0;i<edges.length;i++) {
                    for(int j = 0;j<edges.length;j++) {
                        newedges[i][j] = edges[i][j];
                    }
                }
                edges = newedges;
                double direction = separation;
                if (y[edge.m_b]<y[edge.m_a]) direction = -separation;
                if (edgeFits(x[edge.m_a], y[edge.m_a], x[edge.m_a], y[edge.m_b]-direction)) {
                    x[idx] = x[edge.m_a];
                    y[idx] = y[edge.m_b]-direction;
                    x[idx+1] = x[edge.m_b];
                    y[idx+1] = y[edge.m_b]-direction;
                } else {
                    x[idx] = x[edge.m_a];
                    y[idx] = y[edge.m_a]+direction;
                    x[idx+1] = x[edge.m_b];
                    y[idx+1] = y[edge.m_a]+direction;
                }
                nodeIndexes[idx] = -1;
                nodeIndexes[idx+1] = -1;
                edges[edge.m_a][idx] = true;
                edges[idx][idx+1] = true;
                edges[idx+1][edge.m_b] = true;
                return true;
            }
        }

        if (DEBUG>=1) System.out.println("fixNonOrthogonalEdges: can't fix " + edge.m_a + "->" + edge.m_b);
        return false;
    }


    /*
        This function returns the wiggle room of a node plus all the nodes (in "nodeGroup"),
        that need to be moved together with the node (those that have vertical connections with it)
    */
    public Pair<Double,Double> nodeHorizontalWiggleRoom(int vertex, List<Integer> nodeGroup) {
        nodeGroup.clear();
        List<Integer> open = new LinkedList<Integer>();
        open.add(vertex);

        // Find all the nodes that have vertical connections:
        while(!open.isEmpty()) {
            int v = open.remove(0);
            nodeGroup.add(v);

            // find neighbors:
            for(int w = 0;w<x.length;w++) {
                if (edges[v][w] || edges[w][v]) {
                    if (Math.abs(x[w]-x[v])<0.01 &&
                        !nodeGroup.contains(w) && !open.contains(w)) {
                        open.add(w);
                    }
                }
            }
        }
        if (DEBUG>=1) System.out.println("  Node " + vertex + "'s vertical connections:" + nodeGroup);

        // Find the wiggleRoom of all of them:
        List<Pair<Double,Double>> wiggleRooms = new LinkedList<Pair<Double,Double>>();
        for(Integer v:nodeGroup) wiggleRooms.add(nodeHorizontalWiggleRoomSingleNode(v));

        // Compute the intersection:
        Pair<Double,Double> result = wiggleRooms.remove(0);
        while(!wiggleRooms.isEmpty()) {
            Pair<Double,Double> tmp = wiggleRooms.remove(0);
            result.m_a = Math.max(result.m_a, tmp.m_a);
            result.m_b = Math.min(result.m_b, tmp.m_b);
        }
        result.m_a+=separation;
        result.m_b-=separation;

        return result;
    }


    // Do the same thing for vertical:
    public Pair<Double,Double> nodeVerticalWiggleRoom(int vertex, List<Integer> nodeGroup) {
        nodeGroup.clear();
        List<Integer> open = new LinkedList<Integer>();
        open.add(vertex);

        // Find all the nodes that have horizontal connections:
        while(!open.isEmpty()) {
            int v = open.remove(0);
            nodeGroup.add(v);

            // find neighbors:
            for(int w = 0;w<x.length;w++) {
                if (edges[v][w] || edges[w][v]) {
                    if (Math.abs(y[w]-y[v])<0.01 &&
                        !nodeGroup.contains(w) && !open.contains(w)) {
                        open.add(w);
                    }
                }
            }
        }
        if (DEBUG>=1) System.out.println("  Node " + vertex + "'s horizontal connections:" + nodeGroup);

        // Find the wiggleRoom of all of them:
        List<Pair<Double,Double>> wiggleRooms = new LinkedList<Pair<Double,Double>>();
        for(Integer v:nodeGroup) wiggleRooms.add(nodeVerticalWiggleRoomSingleNode(v));

        // Compute the intersection:
        Pair<Double,Double> result = wiggleRooms.remove(0);
        if (DEBUG>=1) System.out.println("  Initial wiggle room: " + result);
        while(!wiggleRooms.isEmpty()) {
            Pair<Double,Double> tmp = wiggleRooms.remove(0);
            if (DEBUG>=1) System.out.println("  Merging with: " + tmp);
            result.m_a = Math.max(result.m_a, tmp.m_a);
            result.m_b = Math.min(result.m_b, tmp.m_b);
        }
        result.m_a+=separation;
        result.m_b-=separation;

        return result;
    }


    public Pair<Double,Double> nodeHorizontalWiggleRoomSingleNode(int vertex) {
        double min = 0;
        double max = 0;
        for(int i = 0;i<x.length;i++) {
            if (i==0 || x[i]<min) min = x[i];
            if (i==0 || x[i]>max) max = x[i];
        }
        for(int w = 0;w<x.length;w++) {
            if (edges[vertex][w] || edges[w][vertex]) {
                if (Math.abs(y[w]-y[vertex])<0.01) {
                    if (x[w]<x[vertex]-0.01) {
                        if (x[w]>min) min = x[w];
                    } else if (x[w]>x[vertex]+0.01) {
                        if (x[w]<max) max = x[w];
                    } else {
                        return new Pair<Double,Double>(x[vertex],x[vertex]);
                    }
                }
            }
        }

        if (nodeIndexes[vertex]>=0) {
            for(OEElement e:embedding[vertex].embedding) {
                if (e.angle==OEElement.LEFT || e.angle==OEElement.RIGHT) {
                    int w = e.dest;
                    if (edges[vertex][w] || edges[w][vertex]) {
                        if (x[w]<x[vertex]-0.01 && x[w]>min) {
                            min = x[w];
                        }
                        if (x[w]>x[vertex]+0.01 && x[w]<max) {
                            max = x[w];
                        }
                    }
                }
            }
        }

        return new Pair<Double,Double>(min,max);
    }


    public Pair<Double,Double> nodeVerticalWiggleRoomSingleNode(int vertex) {
        double min = 0;
        double max = 0;
        for(int i = 0;i<y.length;i++) {
            if (i==0 || y[i]<min) min = y[i];
            if (i==0 || y[i]>max) max = y[i];
        }

        for(int w = 0;w<y.length;w++) {
            if (edges[vertex][w] || edges[w][vertex]) {
                if (Math.abs(x[w]-x[vertex])<0.01) {
                    if (y[w]<y[vertex]-0.01) {
                        if (y[w]>min) min = y[w];
                    } else if (y[w]>y[vertex]+0.01) {
                        if (y[w]<max) max = y[w];
                    } else {
                        return new Pair<Double,Double>(y[vertex],y[vertex]);
                    }
                }
            }
        }

        if (nodeIndexes[vertex]>=0) {
            for(OEElement e:embedding[vertex].embedding) {
                if (e.angle==OEElement.UP || e.angle==OEElement.DOWN) {
                    int w = e.dest;
                    if (edges[vertex][w] || edges[w][vertex]) {
                        if (y[w]<y[vertex]-0.01 && y[w]>min) {
                            min = y[w];
                        }
                        if (y[w]>y[vertex]+0.01 && y[w]<max) {
                            max = y[w];
                        }
                    }
                }
            }
        }

        return new Pair<Double,Double>(min,max);
    }


    public Pair<Integer,Integer> findFirstNonOrthogonalEdge() {
        for(int i = 0;i<edges.length;i++) {
            for(int j = 0;j<edges.length;j++) {
                if (edges[i][j] &&
                    Math.abs(x[i]-x[j])>0.01 &&
                    Math.abs(y[i]-y[j])>0.01) return new Pair<Integer,Integer>(i,j);
            }
        }
        return null;
    }


    public List<Pair<Integer,Integer>> findNonOrthogonalEdges() {
        List<Pair<Integer,Integer>> l = new LinkedList<Pair<Integer,Integer>>();
        for(int i = 0;i<edges.length;i++) {
            for(int j = 0;j<edges.length;j++) {
                if (edges[i][j] &&
                    Math.abs(x[i]-x[j])>0.01 &&
                    Math.abs(y[i]-y[j])>0.01) {
                    l.add(new Pair<Integer,Integer>(i,j));
                }
            }
        }
        return l;
    }


    public void gridAlign(double step) {
        List<Double> xvalues = new LinkedList<Double>();
        List<Double> yvalues = new LinkedList<Double>();

        for(double tmp:y) if (!yvalues.contains(tmp)) yvalues.add(tmp);
        for(double tmp:x) if (!xvalues.contains(tmp)) xvalues.add(tmp);

        Collections.sort(xvalues);
        Collections.sort(yvalues);

        // filter those that are too similar (proably the same but for precission errors):
        double threshold = 0.01;
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

//        if (DEBUG>=1) System.out.println("gridAlign: X = " + xvalues);
//        if (DEBUG>=1) System.out.println("gridAlign: Y = " + yvalues);

        for(int i = 0;i<y.length;i++) {
            y[i] = indexOfClosest(y[i],yvalues)*step;
        }
        for(int i = 0;i<x.length;i++) {
            x[i] = indexOfClosest(x[i],xvalues)*step;
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


    public boolean edgeFits(double x1, double y1,
                            double x2, double y2) {
        double tolerance = 0.01;
        double tolerance2 = 0.005;
        double minx1 = Math.min(x1,x2) + tolerance;
        double maxx1 = Math.max(x1,x2) - tolerance;
        double miny1 = Math.min(y1,y2) + tolerance;
        double maxy1 = Math.max(y1,y2) - tolerance;
        int isPoint = 0;
        if (Math.abs(x1-x2)<tolerance) {
            minx1 = Math.min(x1,x2) - tolerance2;
            maxx1 = Math.max(x1,x2) + tolerance2;
            isPoint++;
        }
        if (Math.abs(y1-y2)<tolerance) {
            miny1 = Math.min(y1,y2) - tolerance2;
            maxy1 = Math.max(y1,y2) + tolerance2;
            isPoint++;
        }
        if (isPoint>=2) return true;
        
        for(int i2 = 0;i2<edges.length;i2++) {
            for(int j2 = i2+1;j2<edges.length;j2++) {
                if (edges[i2][j2] || edges[j2][i2]) {
                    // test for intersection:
                    double minx2 = Math.min(x[i2],x[j2]) + tolerance;
                    double maxx2 = Math.max(x[i2],x[j2]) - tolerance;
                    double miny2 = Math.min(y[i2],y[j2]) + tolerance;
                    double maxy2 = Math.max(y[i2],y[j2]) - tolerance;
                    int isPoint2 = 0;
                    if (Math.abs(x[i2]-x[j2])<tolerance) {
                        minx2 = Math.min(x[i2],x[j2]) - tolerance2;
                        maxx2 = Math.max(x[i2],x[j2]) + tolerance2;
                        isPoint2++;
                    }
                    if (Math.abs(y[i2]-y[j2])<tolerance) {
                        miny2 = Math.min(y[i2],y[j2]) - tolerance2;
                        maxy2 = Math.max(y[i2],y[j2]) + tolerance2;
                        isPoint2++;
                    }
                    if (isPoint2>=2) continue;

                    if (minx1<=maxx2 &&
                        minx2<=maxx1 &&
                        miny1<=maxy2 &&
                        miny2<=maxy1) {
                        // intersection!
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    public boolean edgeFitsIgnoring(double x1, double y1,
                                    double x2, double y2, List<Integer> l) {
        double tolerance = 0.01;
        double tolerance2 = 0.005;
        double minx1 = Math.min(x1,x2) + tolerance;
        double maxx1 = Math.max(x1,x2) - tolerance;
        double miny1 = Math.min(y1,y2) + tolerance;
        double maxy1 = Math.max(y1,y2) - tolerance;
        int isPoint = 0;
        if (Math.abs(x1-x2)<tolerance) {
            minx1 = Math.min(x1,x2) - tolerance2;
            maxx1 = Math.max(x1,x2) + tolerance2;
            isPoint++;
        }
        if (Math.abs(y1-y2)<tolerance) {
            miny1 = Math.min(y1,y2) - tolerance2;
            maxy1 = Math.max(y1,y2) + tolerance2;
            isPoint++;
        }
        if (isPoint>=2) return true;
        for(int i2 = 0;i2<edges.length;i2++) {
            for(int j2 = i2+1;j2<edges.length;j2++) {
                if (edges[i2][j2] || edges[j2][i2]) {
                    if (!l.contains(i2) && !l.contains(j2)) {
                        // test for intersection:
                        double minx2 = Math.min(x[i2],x[j2]) + tolerance;
                        double maxx2 = Math.max(x[i2],x[j2]) - tolerance;
                        double miny2 = Math.min(y[i2],y[j2]) + tolerance;
                        double maxy2 = Math.max(y[i2],y[j2]) - tolerance;
                        int isPoint2 = 0;
                        if (Math.abs(x[i2]-x[j2])<tolerance) {
                            minx2 = Math.min(x[i2],x[j2]) - tolerance2;
                            maxx2 = Math.max(x[i2],x[j2]) + tolerance2;
                            isPoint2++;
                        }
                        if (Math.abs(y[i2]-y[j2])<tolerance) {
                            miny2 = Math.min(y[i2],y[j2]) - tolerance2;
                            maxy2 = Math.max(y[i2],y[j2]) + tolerance2;
                            isPoint2++;
                        }
                        if (isPoint2>=2) continue;

                        if (minx1<=maxx2 &&
                            minx2<=maxx1 &&
                            miny1<=maxy2 &&
                            miny2<=maxy1) {
                            // intersection!
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }    
    
    
    public int findEdgeEnd1(int n1, int n2) {
        if (n1<embedding.length) return n1;
        for(int i = 0;i<edges.length;i++) {
            if (i!=n2 && edges[i][n1]) return findEdgeEnd1(i,n1);
        }
        return -1;
    }

    
    public int findEdgeEnd2(int n1, int n2) {
        if (n2<embedding.length) return n2;
        for(int i = 0;i<edges.length;i++) {
            if (i!=n1 && edges[n2][i]) return findEdgeEnd2(n2,i);
        }
        return -1;
    }

    
    public boolean sanityCheck(boolean silent) {
        double toleranceNode = 0.02;
        double toleranceEdge1 = 0.01;
        double toleranceEdge2 = 0.005;
        // verify that there are no intersections:
        for(int i1 = 0;i1<edges.length;i1++) {
            for(int j1 = i1+1;j1<edges.length;j1++) {
                if (edges[i1][j1] || edges[j1][i1]) {
                    double minx1 = Math.min(x[i1],x[j1]) + toleranceEdge1;
                    double maxx1 = Math.max(x[i1],x[j1]) - toleranceEdge1;
                    double miny1 = Math.min(y[i1],y[j1]) + toleranceEdge1;
                    double maxy1 = Math.max(y[i1],y[j1]) - toleranceEdge1;
                    int isPoint = 0;
                    if (Math.abs(x[i1]-x[j1])<toleranceEdge1) {
                        minx1 = Math.min(x[i1],x[j1]) - toleranceEdge2;
                        maxx1 = Math.max(x[i1],x[j1]) + toleranceEdge2;
                        isPoint++;
                    }
                    if (Math.abs(y[i1]-y[j1])<toleranceEdge1) {
                        miny1 = Math.min(y[i1],y[j1]) - toleranceEdge2;
                        maxy1 = Math.max(y[i1],y[j1]) + toleranceEdge2;
                        isPoint++;
                    }
                    if (isPoint>=2) continue;
                    // edge - edge intersection
                    for(int i2 = i1;i2<edges.length;i2++) {
                        for(int j2 = (i2==i1 ? j1+1:i2+1);j2<edges.length;j2++) {
                            if (edges[i2][j2] || edges[j2][i2]) {
                                // test for intersection:
                                double minx2 = Math.min(x[i2],x[j2]) + toleranceEdge1;
                                double maxx2 = Math.max(x[i2],x[j2]) - toleranceEdge1;
                                double miny2 = Math.min(y[i2],y[j2]) + toleranceEdge1;
                                double maxy2 = Math.max(y[i2],y[j2]) - toleranceEdge1;
                                int isPoint2 = 0;
                                if (Math.abs(x[i2]-x[j2])<toleranceEdge1) {
                                    minx2 = Math.min(x[i2],x[j2]) - toleranceEdge2;
                                    maxx2 = Math.max(x[i2],x[j2]) + toleranceEdge2;
                                    isPoint2++;
                                }
                                if (Math.abs(y[i2]-y[j2])<toleranceEdge1) {
                                    miny2 = Math.min(y[i2],y[j2]) - toleranceEdge2;
                                    maxy2 = Math.max(y[i2],y[j2]) + toleranceEdge2;
                                    isPoint2++;
                                }
                                if (isPoint2>=2) continue;
                                if (minx1<=maxx2 &&
                                    minx2<=maxx1 &&
                                    miny1<=maxy2 &&
                                    miny2<=maxy1) {
                                    // intersection!
                                    if (!silent) {
                                        System.err.println("edge " + i1 + "->" + j1 + " crosses with " + i2 + "->" + j2);
                                        System.err.println("  ("+x[i1] +","+y[i1]+")-("+x[j1]+","+y[j1]+")  crosses  ("+x[i2] +","+y[i2]+")-("+x[j2]+","+y[j2]+")");
                                    }
                                    return false;
                                }
                            }
                        }
                    }
                    
                    // edge-node intersections:
                    for(int i2 = 0;i2<edges.length;i2++) {
                        if (i2==i1 || i2==j1) continue;
                        double minx2 = x[i2] - toleranceNode;
                        double maxx2 = x[i2] + toleranceNode;
                        double miny2 = y[i2] - toleranceNode;
                        double maxy2 = y[i2] + toleranceNode;
                                if (minx1<=maxx2 &&
                                    minx2<=maxx1 &&
                                    miny1<=maxy2 &&
                                    miny2<=maxy1) {
                                    // intersection!
                                    if (!silent) {
                                        System.err.println("edge " + i1 + "->" + j1 + " crosses with node " + i2);
                                        System.err.println("  ("+x[i1] +","+y[i1]+")-("+x[j1]+","+y[j1]+")  crosses  ("+x[i2] +","+y[i2]+")");
                                    }
                                    return false;
                                }
                    }
                }
            }            
        }

        return true;
    }
    
    
    public String toString()
    {
        String tmp = "";
        for(int i = 0;i<nodeIndexes.length;i++) {    
            for(int j = 0;j<nodeIndexes.length;j++) {
                if (edges[i][j] || edges[j][i]) tmp+="1" + ", ";
                                                 else tmp+="0" + ", ";
            }
            tmp+="\n";
        }
        for(int i = 0;i<nodeIndexes.length;i++) {    
            tmp+=i + ", " + nodeIndexes[i] + ", " + x[i] + ", " + y[i] + "\n";
        }
        
        return tmp;
    }
 }
