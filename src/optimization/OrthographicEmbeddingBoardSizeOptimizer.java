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
public class OrthographicEmbeddingBoardSizeOptimizer {
    public static int DEBUG = 0;
    
    
    public static OrthographicEmbeddingResult optimize(OrthographicEmbeddingResult o, int graph[][]) {
        return optimize(o, graph, new SegmentLengthEmbeddingComparator());
    }
   
    
    public static OrthographicEmbeddingResult optimize(OrthographicEmbeddingResult o, int graph[][], EmbeddingComparator comparator) {             
        o.gridAlign(1);
        while(removeAHorizontalPath(o, graph, comparator));
        return o;
    }
    

    public static boolean removeAHorizontalPath(OrthographicEmbeddingResult o, int graph[][], EmbeddingComparator comparator) {             

        // 1) construct the board:
        int dx = 0;
        int dy = 0;
        for(int i = 0;i<o.x.length;i++) {
            if (o.x[i]>dx) dx = (int)o.x[i];
            if (o.y[i]>dy) dy = (int)o.y[i];
        }
        dx+=1;
        dy+=1;
        int map[][] = new int[dx][dy];
        
        for(int i = 0;i<o.x.length;i++) {
            map[(int)o.x[i]][(int)o.y[i]] = 1;
            for(int j = 0;j<o.x.length;j++) {
                if (o.edges[i][j] || o.edges[j][i]) {
                    // draw path:
                    int x = (int)o.x[i];
                    int y = (int)o.y[i];
                    if (x == (int)o.x[j]) continue;
                    while(x != (int)(o.x[j])) {
                        map[x][y] = 1;
                        if (x < (int)o.x[j]) x++;
                        if (x > (int)o.x[j]) x--;
                    }
                }
            }
        }
        if (DEBUG>=1) {
            for(int i = 0;i<dy;i++) {
                for(int j = 0;j<dx;j++) {
                    System.out.print(map[j][i]);
                }
                System.out.println("");
            }
        }
        
        // 2) find a path of 0s from left to right:
        Pair<Integer,List<Integer>> path = findPathToTheRight(map, 0, dy-1, 0);
        if (path != null) {
//            System.out.println("Found path (width "+path.m_a+"): " + path.m_b); 
            removePath(o, path);
            return true;
        }
        
        return false;
    }    

    private static Pair<Integer, List<Integer>> findPathToTheRight(int[][] map, int y1, int y2, int x) {
//        System.out.println(x + ":" + y1 + "-" + y2);
        if (x == map.length) {
            return new Pair<Integer,List<Integer>>(map[0].length,new ArrayList<Integer>());
        }
        int last0 = -1;
        while(y1>0 && map[x][y1-1] == 0) y1--;
        while(y2<map[0].length-1 && map[x][y2+1] == 0) y2++;
        for(int i = y1;i<y2+1;i++) {
            if (map[x][i] == 1) {
                if (last0!=-1) {
                    // found one from last0 -> i-1:
                    Pair<Integer,List<Integer>> path = findPathToTheRight(map, last0, i-1, x+1);
                    if (path != null) {
                        path.m_a = Math.min(path.m_a, ((i-1)-last0)+1);
                        path.m_b.add(0, last0);
                        return path;
                    }
                    last0 = -1;
                }
            } else {
                if (last0 == -1) last0 = i;
            }
        }
        if (last0!=-1) {
            // found one from last0 -> i-1:
            Pair<Integer,List<Integer>> path = findPathToTheRight(map, last0, y2, x+1);
            if (path != null) {
                path.m_a = Math.min(path.m_a, (y2-last0)+1);
                path.m_b.add(0, last0);
                return path;
            }
        }        
        return null;
    }

    private static void removePath(OrthographicEmbeddingResult o, Pair<Integer, List<Integer>> path) {
        for(int x = 0;x<path.m_b.size();x++) {
            for(int j = 0;j<o.y.length;j++) {
                if (((int)o.x[j]) == x && 
                    ((int)o.y[j]) >= path.m_b.get(x)) {  
                    o.y[j]-= path.m_a;
                }
            }
        }   
    }
}
