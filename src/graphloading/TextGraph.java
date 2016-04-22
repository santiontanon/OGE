/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphloading;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.StringTokenizer;

/**
 *
 * @author santi
 */
public class TextGraph {
    public static int [][]loadGraph(String fileName) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        
        int n = 0;
        int i = 0;
        int graph[][] = null;
        while(true) {
            String line = br.readLine();
            if (line==null) return graph;
            if (graph==null) {
                StringTokenizer st = new StringTokenizer(line, ", \t");
                while(st.hasMoreTokens()) {
                    n++;
                    st.nextToken();
                }
                graph = new int[n][n];
            }
            StringTokenizer st = new StringTokenizer(line, ", \t");
            for(int j = 0;j<n;j++) {
                graph[i][j] = Integer.parseInt(st.nextToken());
            }
            i++;
        }
    }
}
