
import java.util.ArrayList;
import java.util.List;
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
public class DisconnectedGraphs {
    public static List<List<Integer>> findDisconnectedGraphs(int graph[][]) {
        int n = graph.length;
        List<List<Integer>> graphs = new ArrayList<>();
        List<Integer> closedAccum = new ArrayList<>();
        do {
            List<Integer> open = new ArrayList<>();
            List<Integer> closed = new ArrayList<>();
            
            for(int i = 0;i<n;i++) {
                if (!closedAccum.contains(i)) {
                    open.add(i);
                    break;
                }
            }

            while(!open.isEmpty()) {
                int current = open.remove(0);
                for(int i = 0;i<n;i++) {
                    if (graph[current][i] != 0 || graph[i][current] != 0) {
                        if (!closed.contains(i) && !open.contains(i)) open.add(i);
                    }
                }
                closed.add(current);
            }
            if (!closed.isEmpty()) {
                graphs.add(closed);
            }
            closedAccum.addAll(closed);
        }while(closedAccum.size()<n);
        
        return graphs;
    }
    
    
    static int[][]subgraph(int graph[][], List<Integer> vertexIndexes) {
        int n2 = vertexIndexes.size();
        int graph2[][] = new int[n2][n2];
        for(int i2 = 0;i2<n2;i2++) {
            int i = vertexIndexes.get(i2);
            for(int j2 = 0;j2<n2;j2++) {
                int j = vertexIndexes.get(j2);
                graph2[i2][j2] = graph[i][j];
            }
        }
        return graph2;
    }
    

    static OrthographicEmbeddingResult mergeDisconnectedEmbeddingsSideBySide(List<OrthographicEmbeddingResult> disconnectedEmbeddings, 
                                                                   List<List<Integer>> vertexIndexes,
                                                                   double separation) {
        if (disconnectedEmbeddings.size()==1) {
            return disconnectedEmbeddings.get(0);
        } else {
            int embeddingSizes[] = new int[disconnectedEmbeddings.size()];
            int n = 0;
            
            for(int i = 0;i<disconnectedEmbeddings.size();i++) {
                embeddingSizes[i] = disconnectedEmbeddings.get(i).nodeIndexes.length;
                n += embeddingSizes[i];
            }
            
            OrthographicEmbeddingResult aggregated = new OrthographicEmbeddingResult(n);
            
            /*
    public OEVertex embedding[];
    public int nodeIndexes[];
    public double x[];
    public double y[];
    public boolean edges[][];            
            */

            int startIndex = 0;
            double startX = 0;
            double nextStartX = 0;
            for(int i = 0;i<disconnectedEmbeddings.size();i++) {
                OrthographicEmbeddingResult er = disconnectedEmbeddings.get(i);
                List<Integer> vi = vertexIndexes.get(i);
                for(int j = 0;j<embeddingSizes[i];j++) {
                    // node indexes:
                    if (er.nodeIndexes[j]>=0) {
                        aggregated.nodeIndexes[startIndex+j] = vi.get(er.nodeIndexes[j]);
                    } else {
                        aggregated.nodeIndexes[startIndex+j] = -1;
                    }
                    
                    // coordinates:
                    if (er.x[j] + startX > nextStartX) nextStartX = er.x[j] + startX;
                    aggregated.x[startIndex+j] = startX + er.x[j];
                    aggregated.y[startIndex+j] = er.y[j];
                    
                    // edges:
                    for(int k = 0;k<embeddingSizes[i];k++) {
                        aggregated.edges[startIndex+j][startIndex+k] = er.edges[j][k];
                    }
                }
                startX = nextStartX + separation; 
                startIndex += embeddingSizes[i];
            }
            
            return aggregated;
        }
    }
}
