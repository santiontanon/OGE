/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization;

import orthographicembedding.OrthographicEmbeddingResult;
import util.Pair;

/**
 *
 * @author santi
 */
public class SegmentLengthEmbeddingComparator implements EmbeddingComparator {

    @Override
    public int compare(OrthographicEmbeddingResult oer1, OrthographicEmbeddingResult oer2) {
        return compare(resultScore(oer1), resultScore(oer2));
    }
    
    
    // true is s1 is better than s2:
    public int compare(Pair<Double,Integer> s1, Pair<Double,Integer> s2) {
        
//        System.out.println("<" + s1.m_a + "," + s1.m_b + "> vs <" + s2.m_a + "," + s2.m_b + ">");
        
        int tmp = Double.compare(s1.m_a, s2.m_a);
        if (tmp!=0) return tmp;
        return Integer.compare(s1.m_b, s2.m_b);
    }
    

    // the score of a result is a pair with the accumulated length of the segments and the number of segments
    public Pair<Double,Integer> resultScore(OrthographicEmbeddingResult o) {
        double length = 0;
        int nsegments = 0;
        
        int nv = o.x.length;
        for(int i = 0;i<nv;i++) {
            for(int j = 0;j<nv;j++) {
                if (o.edges[i][j]) {
                    nsegments++;
                    length += (o.x[i] - o.x[j])*(o.x[i] - o.x[j]) + (o.y[i] - o.y[j])*(o.y[i] - o.y[j]);
                }
            }
        }

        return new Pair<Double,Integer>(length, nsegments);
    }
        
    
}
