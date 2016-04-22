/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphloading;

import java.io.BufferedReader;
import java.io.FileReader;

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
            String []tokens = line.split("(,| |\t)");
            if (graph==null) {
                n = tokens.length;
                graph = new int[n][n];
            } else {
                if (n!=tokens.length) throw new Exception("number of elements is different than in the previous line in line " + i + " when reading file " + fileName);
            }
            for(int j = 0;j<n;j++) {
                graph[i][j] = Integer.parseInt(tokens[j]);
            }
            i++;
        }
    }
}
