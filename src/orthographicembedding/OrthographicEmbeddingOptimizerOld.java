/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package orthographicembedding;

import java.util.LinkedList;
import java.util.List;
import util.Pair;

/**
 *
 * @author santi
 */
public class OrthographicEmbeddingOptimizerOld {
    
    public static int DEBUG = 0;
    
    public static void optimize(OrthographicEmbeddingResult o, int graph[][]) {
        int n = graph.length;
        int n2 = o.x.length;
        int maxx = 0;
        int maxy = 0;
        int occupancyMatrix[][];

        o.gridAlign(1);

        for(int v = 0;v<n2;v++) {
            if (o.x[v]>maxx) maxx = (int)o.x[v];
            if (o.y[v]>maxy) maxy = (int)o.y[v];
        }
        maxx++;
        maxy++;
        occupancyMatrix = new int[maxx][maxy];

        // run twice over each vertex (this is just an "ad-hoc" solution,
        // but it's good enough, and it's fast :))
        for(int v = 0;v<n;v++) optimize(v, o, graph, occupancyMatrix);
        for(int v = 0;v<n;v++) optimize(v, o, graph, occupancyMatrix);
        
        
        o.gridAlign(1);
    }

    public static void optimize(int v, OrthographicEmbeddingResult o, int graph[][], int om[][]) {
        int n = graph.length;       // number of vertices
        int n2 = o.x.length;        // number of vertices (including the auxiliar ones (joints))
        int maxx = om.length;
        int maxy = om[0].length;

        if (DEBUG>=1) System.out.println("OrthographicEmbeddingOptimizer.optimize vertex " + v);
        
        // clear the matrix:
        for(int i = 0;i<maxy;i++) {
            for(int j = 0;j<maxx;j++) {
                om[j][i] = 0;
            }
        }

        // find all the edges that connect v to all the other graph nodes
        List<Integer> verticesToDelete = new LinkedList<Integer>();
        boolean [][]edgesToIgnore = new boolean[n2][n2];
        List<Pair<Integer,Integer>> open = new LinkedList<Pair<Integer,Integer>>();
        for(int i = 0;i<n2;i++) {
            if (o.edges[v][i] ||
                o.edges[i][v]) open.add(new Pair<Integer,Integer>(v,i));
        }
        while(!open.isEmpty()) {
            Pair<Integer,Integer> current = open.remove(0);
            edgesToIgnore[current.m_a][current.m_b] = true;
            edgesToIgnore[current.m_b][current.m_a] = true;
            if (DEBUG>=1) System.out.println("Ignore edge: " + current.m_a + "-" + current.m_b);
            
            if (current.m_b>=n) {
                verticesToDelete.add(current.m_b);
                for(int i = 0;i<n2;i++) {
                    if (o.edges[current.m_b][i] || o.edges[i][current.m_b]) {
                        Pair<Integer,Integer> next = new Pair<Integer,Integer>(current.m_b,i);
                        if (!open.contains(next) &&
                            !edgesToIgnore[current.m_b][i]) {
                            open.add(next);
                        }
                    }
                }
            }
        }
        
        // add all the edges (except the ones to ignore):
        for(int i1 = 0;i1<n2;i1++) {
            for(int i2 = 0;i2<n2;i2++) {
                if (o.edges[i1][i2] && !edgesToIgnore[i1][i2]) {
//                    System.out.println("adding " + i1 + "->" + i2);
                    int x1 = (int)o.x[i1];
                    int y1 = (int)o.y[i1];
                    int x2 = (int)o.x[i2];
                    int y2 = (int)o.y[i2];
                    om[x1][y1] = 1;
                    while(x1!=x2 || y1!=y2) {
                        if (x1<x2) x1++;
                        if (x1>x2) x1--;
                        if (y1<y2) y1++;
                        if (y1>y2) y1--;
                        if ((x1!=x2 || y1!=y2) && om[x1][y1]!=0) throw new Error("Some edges overlap ("+i1+" -> "+i2+"): ("+o.x[i1]+","+o.y[i1]+")->("+x2+","+y2+"), overlap is at ("+x1+","+y1+")!!!");
                        om[x1][y1] = 1;
                    }
                }
            }
        }
        // make sure we have added at least all the vertices:
        for(int i = 0;i<n;i++) {
            if (i==v) continue;
            om[(int)o.x[i]][(int)o.y[i]] = 1;
        }
      
        // determine the set of positions to try (all the positions along the paths):
        boolean [][]toTry = new boolean[maxx][maxy];
        for(int i1 = 0;i1<n2;i1++) {
            for(int i2 = 0;i2<n2;i2++) {
                if (o.edges[i1][i2] && edgesToIgnore[i1][i2]) {
                    int x1 = (int)o.x[i1];
                    int y1 = (int)o.y[i1];
                    int x2 = (int)o.x[i2];
                    int y2 = (int)o.y[i2];
                    if (om[x1][y1]==0) toTry[x1][y1] = true;
//                    if (x1>0 && om[x1-1][y1]==0) toTry[x1-1][y1] = true;
//                    if (y1>0 && om[x1][y1-1]==0) toTry[x1][y1-1] = true;
//                    if (x1<maxx-1 && om[x1+1][y1]==0) toTry[x1+1][y1] = true;
//                    if (y1<maxy-1 && om[x1][y1+1]==0) toTry[x1][y1+1] = true;
                    while(x1!=x2 || y1!=y2) {
                        if (x1<x2) x1++;
                        if (x1>x2) x1--;
                        if (y1<y2) y1++;
                        if (y1>y2) y1--;
                        if (om[x1][y1]==0) toTry[x1][y1] = true;
//                        if (x1>0 && om[x1-1][y1]==0) toTry[x1-1][y1] = true;
//                        if (y1>0 && om[x1][y1-1]==0) toTry[x1][y1-1] = true;
//                        if (x1<maxx-1 && om[x1+1][y1]==0) toTry[x1+1][y1] = true;
//                        if (y1<maxy-1 && om[x1][y1+1]==0) toTry[x1][y1+1] = true;
                    }
                }
            }
        }
        
        if (DEBUG>=1) {
            System.out.println("Cells occupied (1) and cells to try (2):");
            for(int y = 0;y<maxy;y++) {
                for(int x = 0;x<maxx;x++) {
                    if (toTry[x][y]) System.out.print("2");
                    else System.out.print(om[x][y]);
                }
                System.out.println("");
            }
        }
        

        // try each position:
        int idx = 2;
        int bestx = -1;
        int besty = -1;
        List<Pair<Integer,List<Pair<Integer,Integer>>>> best = null;
        Pair<Integer,Integer> bestScore = null;
        for(int x = 0;x<maxx;x++) {
            for(int y = 0;y<maxy;y++) {
                if (toTry[x][y]) {
                    List<Pair<Integer,List<Pair<Integer,Integer>>>> result = findConnections(v, x, y, o, om, idx, graph);
                    if (result!=null) {
                        Pair<Integer,Integer> s = resultScore(v, x,y, result, o);
                        if (DEBUG>=1) System.out.println("Solution has score: " + s);
                        if (best==null || betterScore(s, bestScore)) {
                            if (DEBUG>=1) System.out.println("better");
                            best = result;
                            bestScore = s;
                            bestx = x;
                            besty = y;
                        }
                    }
                    idx++;
                }
            }
        }
        
        if (best!=null) {
            if (DEBUG>=1) System.out.println("Best solution has score: " + bestScore);
            if (bestScore.m_b<=verticesToDelete.size()) {            
                // replace the current embedding with this solution:
                if (DEBUG>=1) {
                    System.out.println("Vertices to delete: " + verticesToDelete);
                }
                
                // create the new variables:
                int newn2 = n2 - verticesToDelete.size() + bestScore.m_b;
                int nodeIndexes2[] = new int[newn2];
                double x2[] = new double[newn2];
                double y2[] = new double[newn2];
                boolean edges2[][] = new boolean[newn2][newn2];
                
                // copy the previous values:
                int i1 = 0;
                for(int i = 0;i<n2;i++) {
                    if (!verticesToDelete.contains(i)) {
                        nodeIndexes2[i1] = o.nodeIndexes[i];
                        x2[i1] = o.x[i];
                        y2[i1] = o.y[i];
                        int j1 = 0;
                        for(int j = 0;j<n2;j++) {
                            if (!verticesToDelete.contains(j)) {
                                if (!edgesToIgnore[i][j]) edges2[i1][j1] = o.edges[i][j];
                                j1++;
                            }
                        }
                        i1++;
                    }
                }
                x2[v] = bestx;
                y2[v] = besty;
                
                if (DEBUG>=1) System.out.println("vertices count goes from: " + n2 + "->" + newn2 + " after copying next index is " + i1);
                // add the new values:
                for(Pair<Integer,List<Pair<Integer,Integer>>> path:best) {
                    int last = v;
                    for(Pair<Integer,Integer> point:path.m_b) {
                        nodeIndexes2[i1] = -1;
                        x2[i1] = point.m_a;
                        y2[i1] = point.m_b;
                        edges2[last][i1] = true;
//                        edges2[i1][last] = true;
                        if (DEBUG>=1) System.out.println("connecting (a): " + last + " -> " + i1);
                        last = i1;
                        i1++;
                    }
                    edges2[last][path.m_a] = true;
//                    edges2[path.m_a][last] = true;
                    if (DEBUG>=1) System.out.println("connecting (b): " + last + " -> " + path.m_a);
                }
                
                o.nodeIndexes = nodeIndexes2;
                o.x = x2;
                o.y = y2;
                o.edges = edges2;
            }
        }
    }

    // true is s1 is better than s2:
    public static boolean betterScore(Pair<Integer,Integer> s1, Pair<Integer,Integer> s2) {
        if (s2==null) return true;
        if (s1==null) return false;
        if (s2.m_a<s1.m_a) return false;
        if (s2.m_a>s1.m_a) return true;
        if (s2.m_b<s1.m_b) return false;
        if (s2.m_b>s1.m_b) return true;
        return false;
    }
    

    // the score of a result is a pair with the accumulated length of the segments and the number of segments
    public static Pair<Integer,Integer> resultScore(int v, int vx, int vy,  List<Pair<Integer,List<Pair<Integer,Integer>>>> result, OrthographicEmbeddingResult o) {
        int nsegments = 0;
        int length = 0;

        if (result==null) return null;
        
        for(Pair<Integer,List<Pair<Integer,Integer>>> tmp:result) {
            nsegments+=tmp.m_b.size();

            int x = vx;
            int y = vy;
            for(Pair<Integer,Integer> next:tmp.m_b) {
                while(x!=next.m_a || y!=next.m_b) {
                    if (x<next.m_a) x++;
                    if (x>next.m_a) x--;
                    if (y<next.m_b) y++;
                    if (y>next.m_b) y--;
                    length++;
                }
            }
            int x2 = (int)o.x[tmp.m_a];
            int y2 = (int)o.y[tmp.m_a];
            while(x!=x2 || y!=y2) {
                if (x<x2) x++;
                if (x>x2) x--;
                if (y<y2) y++;
                if (y>y2) y--;
                length++;
            }
        }

        return new Pair<Integer,Integer>(length, nsegments);
    }
    

    public static List<Pair<Integer,List<Pair<Integer,Integer>>>> findConnections(int v, int x, int y, OrthographicEmbeddingResult o, int om[][], int idx, int graph[][]) {
        int n = graph.length;
        List<Pair<Integer,List<Pair<Integer,Integer>>>> result = new LinkedList<Pair<Integer,List<Pair<Integer,Integer>>>>();

        if (DEBUG>=1) System.out.println("OrthographicEmbeddingOptimizer.findConnections for " + v + " starting at " + x + "," + y);
        
        om[x][y] = idx;
        for(int i = 0;i<n;i++) {
            if (graph[v][i]!=0) {
                // try to find the shortest path and with the shortest bends from v to i:
                List<Pair<Integer,Integer>> path = shortestMinimumBendPath(x,y, (int)o.x[i], (int)o.y[i], om, idx);
                if (path==null) return null;
                
                int x1 = x;
                int y1 = y;
                if (om[x1][y1]!=1) om[x1][y1] = idx;
                for(Pair<Integer,Integer> next:path) {
                    while(x1!=next.m_a || y1!=next.m_b) {
                        if (x1<next.m_a) x1++;
                        if (x1>next.m_a) x1--;
                        if (y1<next.m_b) y1++;
                        if (y1>next.m_b) y1--;
                        if (om[x1][y1]!=1) om[x1][y1] = idx;
                    }
                }
                int x2 = (int)o.x[i];
                int y2 = (int)o.y[i];
                if (om[x1][y1]!=1) om[x1][y1] = idx;
                while(x1!=x2 || y1!=y2) {
                    if (x1<x2) x1++;
                    if (x1>x2) x1--;
                    if (y1<y2) y1++;
                    if (y1>y2) y1--;
                    if (om[x1][y1]!=1) om[x1][y1] = idx;
                }
                
                result.add(new Pair<Integer,List<Pair<Integer,Integer>>>(i,path));
            }
        }
        
        return result;
    }
    
    // breadth first search:
    public static List<Pair<Integer,Integer>> shortestMinimumBendPath(int x1, int y1, int x2, int y2, int om[][], int idx) {
        int dx = om.length;
        int dy = om[0].length;
        List<Integer> parents[][] = new List[dx][dy];
        int cost[][] = new int[dx][dy];
        int bends[][] = new int[dx][dy];
        int offx[] = {-1,0,1,0};
        int offy[] = {0,-1,0,1};

        // initialize:
        for(int i = 0;i<dy;i++) {
            for(int j = 0;j<dx;j++) {
                parents[j][i] = new LinkedList<Integer>();
                cost[j][i] = 0;
                bends[j][i] = 0;
            }
        }
        
        List<Integer> open = new LinkedList<Integer>();
        open.add(x1+y1*dx);
        while(!open.isEmpty()) {
            int current = open.remove(0);
            int cx = current%dx;    // current
            int cy = current/dx;
            List<Integer> currentParents = parents[cx][cy];
            /*
            int parentx = cx;
            int parenty = cy;
            if (parent!=-1) {
                parentx = parent%dx;
                parenty = parent/dx;
            }
            */
            
            if (cx!=x2 || cy!=y2) {
                int nextcost = cost[cx][cy] + 1;
                for(int i = 0;i<4;i++) {
                    int nextbends = bends[cx][cy] + 1;
                    int nx = cx+offx[i];
                    int ny = cy+offy[i];
                    for(int parent:currentParents) {
                        int parentx = parent%dx;
                        int parenty = parent/dx;
                        // there is one parent from where we don't have to change direction:
                        if (parentx==cx-offx[i] && parenty==cy-offy[i]) nextbends = bends[cx][cy];
                    }
                    if (nx>=0 && nx<dx && ny>=0 && ny<dy &&
                        ((nx==x2 && ny==y2) ||
                         (om[nx][ny]!=1 && om[nx][ny]!=idx))) {
                        if (parents[nx][ny].isEmpty() ||
                            nextcost<cost[nx][ny] ||
                            (nextcost==cost[nx][ny] && nextbends<bends[nx][ny])) {
                            parents[nx][ny].clear();
                            parents[nx][ny].add(current);
                            cost[nx][ny] = nextcost;
                            bends[nx][ny] = nextbends;
                            open.add(nx+ny*dx);
                        } else if (nextcost==cost[nx][ny] && nextbends==bends[nx][ny]) {
                            if (!parents[nx][ny].contains(current)) parents[nx][ny].add(current);
                        }
                    }
                }
            }
        }
        
        // reconstruct the path:
        List<Pair<Integer,Integer>> path = new LinkedList<Pair<Integer,Integer>>();
        if (parents[x2][y2].isEmpty()) return null;
        int current = x2+y2*dx;
        int start = x1+y1*dx;
        int offs = 0;
        while(current!=start) {
            int next = current;
            path.add(0,new Pair<Integer,Integer>(current%dx,current/dx));
            if (offs==0) {
                next = parents[current%dx][current/dx].get(0);
            } else {
                // try to find a parent that goes in the same direction:
                for(int p:parents[current%dx][current/dx]) {
                    if ((p - current)==offs) next = p;
                }
                if (next==current) next = parents[current%dx][current/dx].get(0);
            }
            offs = next - current;
            current = next;
        }
        path.add(0,new Pair<Integer,Integer>(current%dx,current/dx));
        if (DEBUG>=1) System.out.println("full path to (" + x2 + "," + y2 + ") cost " + cost[x2][y2] + "," + bends[x2][y2] + ": " + path);
                
        // only leave the bending points:
        List<Pair<Integer,Integer>> toDelete = new LinkedList<Pair<Integer,Integer>>();
        Pair<Integer,Integer> previous1 = null;
        Pair<Integer,Integer> previous2 = null;
        for(Pair<Integer,Integer> p:path) {
            if (previous1!=null && previous2!=null) {
                int offsx1 = p.m_a - previous1.m_a;
                int offsy1 = p.m_b - previous1.m_b;
                int offsx2 = previous1.m_a - previous2.m_a;
                int offsy2 = previous1.m_b - previous2.m_b;
                
                if (offsx1 == offsx2 && offsy1 == offsy2) {
                    toDelete.add(previous1);
                }
            }
            previous2 = previous1;
            previous1 = p;
        }
        path.removeAll(toDelete);
        // now remove the start and end points:
//        if (DEBUG>=1) System.out.println(path);
        path.remove(0);
        path.remove(path.size()-1);
        if (DEBUG>=1) System.out.println(path);
        
        return path;
    }
}
