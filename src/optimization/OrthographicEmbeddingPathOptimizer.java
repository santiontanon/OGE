/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import orthographicembedding.OrthographicEmbeddingResult;
import util.Pair;
import util.SavePNG;

/**
 *
 * @author santi
 */
public class OrthographicEmbeddingPathOptimizer {
    public static int DEBUG = 0;


    public static OrthographicEmbeddingResult optimize(OrthographicEmbeddingResult o, int graph[][]) {
        return optimize(o, graph, new SegmentLengthEmbeddingComparator());
    }
   
    public static OrthographicEmbeddingResult optimize(OrthographicEmbeddingResult o, int graph[][], EmbeddingComparator comparator) {
        int n = graph.length;        
        boolean change;
        
        if (DEBUG == 1) {
            try {
                SavePNG.savePNG("embedding-beforeoptimization.png", o, 32, 32, true);
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
        
        do{
            change = false;
//            System.out.println("New optimization round...");
            List<Integer> verticesAlreadyConsidered = new ArrayList<Integer>();
            for(int v = 0;v<n;v++) {
                if (verticesAlreadyConsidered.contains(v)) continue;
                o.gridAlign(1);
                OrthographicEmbeddingResult o2 = optimizeVertex(v, o, graph, comparator, verticesAlreadyConsidered);
                if (o2!=o) {
                    change = true;
//                    System.out.println("Change in vertex " + v);
                }
                o = o2;
            }
        }while(change);                
        o.gridAlign(1);
        return o;
    }

    private static OrthographicEmbeddingResult optimizeVertex(int v, OrthographicEmbeddingResult o, int[][] graph, EmbeddingComparator comparator, List<Integer> verticesAlreadyConsidered) 
    {   
        if (!o.sanityCheck(false)) {
            System.out.println("Sanity check failed...");
            System.out.println("Saving image file:");
            try {
                SavePNG.savePNG("befpreOptimizeVertex.png", o, 32, 32, true);
            }catch(Exception e) {
                e.printStackTrace();
            }
            throw new Error("sanityCheck failed!");
        }
        
        // 1) find the whole path of the vertex 
//        System.out.println("1) o.nodeIndexes.length = " + o.nodeIndexes.length + ", o.embedding.length " + (o.embedding != null ? o.embedding.length : "-"));
        List<Integer> pathIndexes = new ArrayList<Integer>();
        List<Integer> open = new ArrayList<Integer>();
        List<Integer> pathNodesWithMoreThan2Neighbors = new ArrayList<Integer>();
        open.add(v);
        while(!open.isEmpty()) {
            int current = open.remove(0);
            pathIndexes.add(current);
            List<Integer> neighbors = new ArrayList<Integer>();
            for(int next = 0;next<o.edges.length;next++) {
                if (o.edges[current][next] ||
                    o.edges[next][current]) {
                    neighbors.add(next);
                }
            }
            if (DEBUG>=1) System.out.println("neighbors of " + current + ": " + neighbors);
            if (neighbors.size() == 2) {
                for(int next:neighbors) {
                    if (!open.contains(next) && !pathIndexes.contains(next)) {
                        open.add(next);
                    }
                }
            } else if (neighbors.size() > 2) {
                pathNodesWithMoreThan2Neighbors.add(current);
            }
        }
        verticesAlreadyConsidered.addAll(pathIndexes);
        if (pathIndexes.size()==1) return o;
        
        // 2) Sort the path:
//        System.out.println("2) o.nodeIndexes.length = " + o.nodeIndexes.length + ", o.embedding.length " + (o.embedding != null ? o.embedding.length : "-"));
        {
            List<Integer> sortedPath = new ArrayList<Integer>();
            sortedPath.add(pathIndexes.remove(0));
            while(!pathIndexes.isEmpty()) {
                // find for the left:
                int forLeft = -1;
                for(int v2:pathIndexes) {
                    if (o.edges[v2][sortedPath.get(0)] ||
                        o.edges[sortedPath.get(0)][v2]) {
                        forLeft = v2;
                        break;
                    }
                }
                if (forLeft != -1) sortedPath.add(0, forLeft);
                pathIndexes.remove((Integer)forLeft);
                // find for the right:
                int forRight = -1;
                for(int v2:pathIndexes) {
                    if (o.edges[v2][sortedPath.get(sortedPath.size()-1)] ||
                        o.edges[sortedPath.get(sortedPath.size()-1)][v2]) {
                        forRight = v2;
                        break;
                    }
                }
                if (forRight != -1) sortedPath.add(forRight);
                pathIndexes.remove((Integer)forRight);
            }
            pathIndexes = sortedPath;
        }
        if (!o.edges[pathIndexes.get(0)][pathIndexes.get(1)]) {
            if (DEBUG>=1) System.out.println("reversing path");
            Collections.reverse(pathIndexes);
        }
        
        // check if any node within the path has more than 2 neighbors:
        for(int i = 1;i<pathIndexes.size();i++) {
            if (pathNodesWithMoreThan2Neighbors.contains(pathIndexes.get(i))) {
                // path is no good!
                return o;
            }
        }

        // 3) Find all the elements in the path, and calculate path length:
//        System.out.println("3) o.nodeIndexes.length = " + o.nodeIndexes.length + ", o.embedding.length " + (o.embedding != null ? o.embedding.length : "-"));
        int pathLength = 0;
        List<Integer> necessaryElements = new ArrayList<Integer>();
        int nElements = 0;
        if (DEBUG>=1) System.out.print("path: ");
        for(int i = 0;i<pathIndexes.size();i++) {
            if (i>0) {
                pathLength += (int)(Math.abs(o.x[pathIndexes.get(i)] - o.x[pathIndexes.get(i-1)]) + 
                                    Math.abs(o.y[pathIndexes.get(i)] - o.y[pathIndexes.get(i-1)]));
            }
            int v2 = pathIndexes.get(i);
            if (i!=0 && i!=pathIndexes.size()-1 &&
                o.nodeIndexes[v2] != -1) {
                nElements++;
                necessaryElements.add(v2);
            }
            if (DEBUG>=1) System.out.print(o.nodeIndexes[v2] + " ");
        }
        if (DEBUG>=1) System.out.println("    ->    nElements: " + nElements);
        
        
        // 4) Find the shortest path that is equivalent
//        System.out.println("4) o.nodeIndexes.length = " + o.nodeIndexes.length + ", o.embedding.length " + (o.embedding != null ? o.embedding.length : "-"));
        int dx = 0;
        int dy = 0;
        for(int i = 0;i<o.x.length;i++) {
            if (DEBUG>=1) System.out.println("    " + o.x[i] + ", " + o.y[i] + " (" + o.nodeIndexes[i] + ")");
            if (o.x[i]>dx) dx = (int)o.x[i];
            if (o.y[i]>dy) dy = (int)o.y[i];
        }
        dx+=3;
        dy+=3;
        int map[][] = new int[dx][dy];
        for(int i = 0;i<o.x.length;i++) {
            if (!pathIndexes.contains(i)) map[(int)o.x[i]+1][(int)o.y[i]+1] = 1;
            for(int j = 0;j<o.x.length;j++) {
                if (o.edges[i][j] || o.edges[j][i]) {
                    if (pathIndexes.contains(i) && pathIndexes.contains(j) &&
                        Math.abs(pathIndexes.indexOf(i) - pathIndexes.indexOf(j)) == 1) continue;
                    // draw path:
                    int x = (int)o.x[i];
                    int y = (int)o.y[i];
                    while((x != (int)(o.x[j])) ||
                          (y != (int)(o.y[j]))) {
                        map[x+1][y+1] = 1;
                        if (x < (int)o.x[j]) x++;
                        if (x > (int)o.x[j]) x--;
                        if (y < (int)o.y[j]) y++;
                        if (y > (int)o.y[j]) y--;
                    }
                }
            }
        }
        map[(int)o.x[pathIndexes.get(0)]+1][(int)o.y[pathIndexes.get(0)]+1] = 2;
        map[(int)o.x[pathIndexes.get(pathIndexes.size()-1)]+1][(int)o.y[pathIndexes.get(pathIndexes.size()-1)]+1] = 3;
        if (DEBUG>=1) {
            for(int i = 0;i<dy;i++) {
                for(int j = 0;j<dx;j++) {
                    System.out.print(map[j][i]);
                }
                System.out.println("");
            }
        }
        
        // find the shortest path:
        if (DEBUG>=1) {
            System.out.println("Shortest path from " + ((int)o.x[pathIndexes.get(0)]+1) + ", " + ((int)o.y[pathIndexes.get(0)]+1) + " to " + 
                                                       ((int)o.x[pathIndexes.get(pathIndexes.size()-1)]+1) + ", " + ((int)o.y[pathIndexes.get(pathIndexes.size()-1)]+1));
            System.out.println("Previous path was:");
            for(int idx:pathIndexes) {
                System.out.println("    " + ((int)o.x[idx]+1) + ", " + ((int)o.y[idx]+1));
            }
        }
        List<Pair<Integer,Integer>> path2 = findShortestPath(map, 
                                                             (int)o.x[pathIndexes.get(0)]+1, (int)o.y[pathIndexes.get(0)]+1,
                                                             (int)o.x[pathIndexes.get(pathIndexes.size()-1)]+1, (int)o.y[pathIndexes.get(pathIndexes.size()-1)]+1);
        if (DEBUG>=1) { 
            System.out.println("Shortest path: " + path2);
            System.out.println("Length went from " + pathLength + " to " + (path2.size()-1));
        }
                
        // 5) if all the components fit, replace it!
//        System.out.println("5) o.nodeIndexes.length = " + o.nodeIndexes.length + ", o.embedding.length " + (o.embedding != null ? o.embedding.length : "-"));
        if (path2 == null) return o;
        if (path2.size()-2 < nElements) return o;
        if (pathLength <= (path2.size()-1)) return o;
        
        // 6) Remove the old auxiliary points, and the previous path:
//        System.out.println("6) o.nodeIndexes.length = " + o.nodeIndexes.length + ", o.embedding.length " + (o.embedding != null ? o.embedding.length : "-"));
        {
            List<Integer> toRemove = new ArrayList<Integer>();
            for(int i = 0;i<pathIndexes.size();i++) {
                int v2 = pathIndexes.get(i);
                if (o.nodeIndexes[v2] == -1) toRemove.add(v2);
                if (i>0) {
                    o.edges[pathIndexes.get(i-1)][pathIndexes.get(i)] = false;
                    o.edges[pathIndexes.get(i)][pathIndexes.get(i-1)] = false;
                }
            }
            Collections.sort(toRemove);
            Collections.reverse(toRemove);
            
            for(int v2:toRemove) {
                if (DEBUG>=1) System.out.println("removing vertex: " + v2 + " (out of " + o.nodeIndexes.length + ")");
                o = o.removeVertex(v2);
                for(int i = 0;i<pathIndexes.size();i++) {
                    if (pathIndexes.get(i) >= v2) {
                        pathIndexes.set(i, pathIndexes.get(i)-1);
                    }
                }
            }
        }
        
        // 7) Leave only the path important and elbow points:
//        System.out.println("7) o.nodeIndexes.length = " + o.nodeIndexes.length + ", o.embedding.length " + (o.embedding != null ? o.embedding.length : "-"));
        int nextAuxiliar = o.nodeIndexes.length;
        if (DEBUG>=1) System.out.println("nextAuxiliar: " + nextAuxiliar);
        List<Integer> path3Indexes = new ArrayList<Integer>();
        List<Pair<Integer,Integer>> path3 = new ArrayList<Pair<Integer,Integer>>();
        path3.add(path2.get(0));
        path3Indexes.add(pathIndexes.get(0));
        for(int i = 0;i<path2.size()-2;i++) {
            int dx1 = path2.get(i).m_a - path2.get(i+1).m_a;
            int dy1 = path2.get(i).m_b - path2.get(i+1).m_b;
            int dx2 = path2.get(i+1).m_a - path2.get(i+2).m_a;
            int dy2 = path2.get(i+1).m_b - path2.get(i+2).m_b;
            if (i<necessaryElements.size()) {
                path3Indexes.add(necessaryElements.get(i));
                path3.add(path2.get(i+1));
            } else {
                if (dx1 != dx2 || dy1 != dy2) {
                    // joint!
                    path3Indexes.add(nextAuxiliar);
                    path3.add(path2.get(i+1));
                    nextAuxiliar++;
                }
            }
        }
        path3.add(path2.get(path2.size()-1));
        path3Indexes.add(pathIndexes.get(pathIndexes.size()-1));
        if (DEBUG>=1) System.out.println("New path nodes: " + path3);
        if (DEBUG>=1) System.out.println("New path indexes: " + path3Indexes);
                
        // 8) create new arrays with less nodes, and replace the originals:
//        System.out.println("8) o.nodeIndexes.length = " + o.nodeIndexes.length + ", o.embedding.length " + (o.embedding != null ? o.embedding.length : "-"));
        int nAuxiliar = (path3.size() - 2) - nElements;
        if (nAuxiliar > 0) o = o.addVertices(nAuxiliar);
        for(int i = 1;i<path3.size()-1;i++) {
            o.x[path3Indexes.get(i)] = path3.get(i).m_a-1;
            o.y[path3Indexes.get(i)] = path3.get(i).m_b-1;
            if (DEBUG>=1) System.out.println(path3Indexes.get(i) + ": " + o.x[path3Indexes.get(i)] + ", " + o.y[path3Indexes.get(i)]);
            o.edges[path3Indexes.get(i-1)][path3Indexes.get(i)] = true;
            o.edges[path3Indexes.get(i)][path3Indexes.get(i+1)] = true;
            if (DEBUG>=1) System.out.println(path3Indexes.get(i-1) + " -> " + path3Indexes.get(i) + " -> " + path3Indexes.get(i+1));
        }
        
//        System.out.println("end) o.nodeIndexes.length = " + o.nodeIndexes.length + ", o.embedding.length " + (o.embedding != null ? o.embedding.length : "-"));
        return o;
    }

    private static List<Pair<Integer, Integer>> findShortestPath(int[][] map, int sx, int sy, int ex, int ey) {
        List<Integer> open = new ArrayList<Integer>();
        int w = map.length;
        int h = map[0].length;
        int closed[][] = new int[w][h];
        
        for(int i = 0;i<w;i++) {
            for(int j = 0;j<h;j++) {
                closed[i][j] = -2;
            }
        }
        
        open.add(sx+sy*w);
        closed[sx][sy] = -1;
        while(!open.isEmpty()) {
//            System.out.println("    " + open.size());
            int current = open.remove(0);
            int cx = current%w;
            int cy = current/w;
            
            if (cx == ex && cy == ey) {
                // found the end!
                break;
            }
            
            int offsx[] = {-1,0,1,0};
            int offsy[] = {0,1,0,-1};
            for(int i = 0;i<4;i++) {
                int nx = cx+offsx[i];
                int ny = cy+offsy[i];
                int next = nx + ny*w;
                if (nx>=0 && nx<w && ny>=0 && ny<h &&
                    closed[nx][ny] == -2 && !open.contains(next) &&
                    map[nx][ny] != 1) {
                    closed[nx][ny] = current;
                    open.add(next);
                }
            }
        }
//        System.out.println("    done");
        
        if (closed[ex][ey]==0) return null;
        List<Pair<Integer,Integer>> path = new ArrayList<Pair<Integer,Integer>>();
        int cx = ex;
        int cy = ey;
        while(closed[cx][cy] != -1) {
            path.add(0,new Pair<Integer,Integer>(cx,cy));
            int parent = closed[cx][cy];
            cx = parent%w;
            cy = parent/w;
            if (parent == -2) return null;  // no path!
//            System.out.println("    RP: " + parent);
        }
        
        path.add(0,new Pair<Integer,Integer>(sx,sy));
        
        return path;
    }
    
}
