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
        // 1) find the whole path of the vertex 
        List<Integer> path = new ArrayList<Integer>();
        List<Integer> open = new ArrayList<Integer>();
        open.add(v);
        while(!open.isEmpty()) {
            int current = open.remove(0);
            path.add(current);
            List<Integer> neighbors = new ArrayList<Integer>();
            for(int next = 0;next<o.edges.length;next++) {
                if (o.edges[current][next] ||
                    o.edges[next][current]) {
                    neighbors.add(next);
                }
            }
            if (neighbors.size() == 2) {
                for(int next:neighbors) {
                    if (!open.contains(next) && !path.contains(next)) {
                        open.add(next);
                    }
                }
            }
        }
        verticesAlreadyConsidered.addAll(path);
        if (path.size()==1) return o;
        
        // 2) Sort the path:
        {
            List<Integer> sortedPath = new ArrayList<Integer>();
            sortedPath.add(path.remove(0));
            while(!path.isEmpty()) {
                // find for the left:
                int forLeft = -1;
                for(int v2:path) {
                    if (o.edges[v2][sortedPath.get(0)] ||
                        o.edges[sortedPath.get(0)][v2]) {
                        forLeft = v2;
                        break;
                    }
                }
                if (forLeft != -1) sortedPath.add(0, forLeft);
                path.remove((Integer)forLeft);
                // find for the right:
                int forRight = -1;
                for(int v2:path) {
                    if (o.edges[v2][sortedPath.get(sortedPath.size()-1)] ||
                        o.edges[sortedPath.get(sortedPath.size()-1)][v2]) {
                        forRight = v2;
                        break;
                    }
                }
                if (forRight != -1) sortedPath.add(forRight);
                path.remove((Integer)forRight);
            }
            path = sortedPath;
        }
        if (!o.edges[path.get(0)][path.get(1)]) {
            if (DEBUG>=1) System.out.println("reversing path");
            Collections.reverse(path);
        }

        // 3) Find all the elements in the path, and calculate path length:
        int pathLength = 0;
        List<Integer> necessaryElements = new ArrayList<Integer>();
        int nElements = 0;
        if (DEBUG>=1) System.out.print("path: ");
        for(int i = 0;i<path.size();i++) {
            if (i>0) {
                pathLength += (int)(Math.abs(o.x[path.get(i)] - o.x[path.get(i-1)]) + 
                                    Math.abs(o.y[path.get(i)] - o.y[path.get(i-1)]));
            }
            int v2 = path.get(i);
            if (i!=0 && i!=path.size()-1 &&
                o.nodeIndexes[v2] != -1) {
                nElements++;
                necessaryElements.add(v2);
            }
            if (DEBUG>=1) System.out.print(o.nodeIndexes[v2] + " ");
        }
        if (DEBUG>=1) System.out.println("    ->    nElements: " + nElements);
        
        
        // 4) Find the shortest path that is equivalent
        int dx = 0;
        int dy = 0;
        for(int i = 0;i<o.x.length;i++) {
            if (o.x[i]>dx) dx = (int)o.x[i];
            if (o.y[i]>dy) dy = (int)o.y[i];
        }
        dx+=3;
        dy+=3;
        int map[][] = new int[dx][dy];
        for(int i = 0;i<o.x.length;i++) {
            if (!path.contains(i)) map[(int)o.x[i]+1][(int)o.y[i]+1] = 1;
            for(int j = 0;j<o.x.length;j++) {
                if (o.edges[i][j] || o.edges[j][i]) {
                    if (path.contains(i) && path.contains(j) &&
                        Math.abs(path.indexOf(i) - path.indexOf(j)) == 1) continue;
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
        map[(int)o.x[path.get(0)]+1][(int)o.y[path.get(0)]+1] = 2;
        map[(int)o.x[path.get(path.size()-1)]+1][(int)o.y[path.get(path.size()-1)]+1] = 3;
        if (DEBUG>=1) {
            for(int i = 0;i<dy;i++) {
                for(int j = 0;j<dx;j++) {
                    System.out.print(map[j][i]);
                }
                System.out.println("");
            }
        }
        
        // find the shortest path:
        List<Pair<Integer,Integer>> path2 = findShortestPath(map, 
                                                             (int)o.x[path.get(0)]+1, (int)o.y[path.get(0)]+1,
                                                             (int)o.x[path.get(path.size()-1)]+1, (int)o.y[path.get(path.size()-1)]+1);
        if (DEBUG>=1) System.out.println("Shortest path: " + path2);
        if (DEBUG>=1) System.out.println("Length went from " + pathLength + " to " + (path2.size()-1));
                
        // 5) if all the components fit, replace it!
        if (path2 == null) return o;
        if (path2.size()-2 < nElements) return o;
        if (pathLength <= (path2.size()-1)) return o;
        
        // 6) Remove the old auxiliary points, and the previous path:
        {
            List<Integer> toRemove = new ArrayList<Integer>();
            for(int i = 0;i<path.size();i++) {
                int v2 = path.get(i);
                if (o.nodeIndexes[v2] == -1) toRemove.add(v2);
                if (i>0) {
                    o.edges[path.get(i-1)][path.get(i)] = false;
                    o.edges[path.get(i)][path.get(i-1)] = false;
                }
            }
            Collections.sort(toRemove);
            Collections.reverse(toRemove);
            
            for(int v2:toRemove) {
                if (DEBUG>=1) System.out.println("removing vertex: " + v2);
                o = o.removeVertex(v2);
            }
        }
        
        // 7) Leave only the path important and elbow points:
        int nextAuxiliar = o.nodeIndexes.length;
        if (DEBUG>=1) System.out.println("nextAuxiliar: " + nextAuxiliar);
        List<Integer> path3Indexes = new ArrayList<Integer>();
        List<Pair<Integer,Integer>> path3 = new ArrayList<Pair<Integer,Integer>>();
        path3.add(path2.get(0));
        path3Indexes.add(path.get(0));
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
        path3Indexes.add(path.get(path.size()-1));
        if (DEBUG>=1) System.out.println("New path nodes: " + path3);
        if (DEBUG>=1) System.out.println("New path indexes: " + path3Indexes);
                
        // 8) create new arrays with less nodes, and replace the originals:
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
        
        return o;
    }

    private static List<Pair<Integer, Integer>> findShortestPath(int[][] map, int sx, int sy, int ex, int ey) {
        List<Integer> open = new ArrayList<Integer>();
        int w = map.length;
        int h = map[0].length;
        int closed[][] = new int[w][h];
        
        open.add(sx+sy*w);
        closed[sx][sy] = -1;
        while(!open.isEmpty()) {
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
                    closed[nx][ny] == 0 && !open.contains(next) &&
                    map[nx][ny] != 1) {
                    closed[nx][ny] = current;
                    open.add(next);
                }
            }
        }
        
        if (closed[ex][ey]==0) return null;
        List<Pair<Integer,Integer>> path = new ArrayList<Pair<Integer,Integer>>();
        int cx = ex;
        int cy = ey;
        while(closed[cx][cy] != -1) {
            path.add(0,new Pair<Integer,Integer>(cx,cy));
            int parent = closed[cx][cy];
            cx = parent%w;
            cy = parent/w;
        }
        
        path.add(0,new Pair<Integer,Integer>(sx,sy));
        
        return path;
    }
    
}
