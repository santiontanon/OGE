/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package orthographicembedding;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author santi
 */
public class OrthographicEmbedding {
    public static int DEBUG = 0;
        
    public static OrthographicEmbeddingResult orthographicEmbedding(int graph[][], boolean simplify, boolean fixNonOrthogonal, Random r) throws Exception {
        int n = graph.length;
        OEVertex embedding[] = new OEVertex[n];
        
        // Algorithm from: "Planar Grid Embedding in Linear Time" Tamasia and Tollis
        // Step 1: Construct a visibility representation Gamma for the graph
        Visibility Gamma = new Visibility(graph, r);
        if (!Gamma.WVisibility()) return null;
        Gamma.reorganize();
        
        // from this point on, we assume that the result is aligned with a grid size od 1.0
        // Step 2: Transform Gamma into an orthogonal embedding G' by substituting each vertex segment
        //         with one of the structures shown in Fig. 5
        for(int v = 0;v<n;v++) {
            embedding[v] = vertexOrtographicEmbedding(v, graph, Gamma, embedding);
        }
        
//        if (simplify) attemptCompleteSimplification(embedding);
        if (simplify) {
            return cautiousSimplification(embedding, Gamma, fixNonOrthogonal);
        } else {
            // Step 4: Let H' be the orthogonal representation so obtained. 
            //         Construct from H' a grid embedding for G using the compaction algorithm of Lemma 1        
            // (I ignore that, and just provide my own algorihtm for it)
            return new OrthographicEmbeddingResult(embedding, Gamma, fixNonOrthogonal);
        }
    }        
    
    
    // Makes simplifications one by one, trying to generate an actual 2d representation, and only consider those for which my simple
    // 2d algorithm generates graphs that are correct:
    public static OrthographicEmbeddingResult cautiousSimplification(OEVertex embedding[], Visibility Gamma, boolean fixNonOrthogonal) throws Exception {
        int n = embedding.length;
        OrthographicEmbeddingResult best = new OrthographicEmbeddingResult(embedding,Gamma,fixNonOrthogonal);
        OrthographicEmbeddingResult current = null;
        
        // Step 3: Let H be the orthogonal representation of G'. 
        //         Simplify H by means of the bend-stretching transformations
        for(int v = 0;v<n;v++) {
            for(OEElement oev:embedding[v].embedding) {
                int w = oev.dest;
                OEElement oew = sym(oev,embedding);
                // Apply T1:
                if (oev.bends>=1 && oew.bends>=1) {
                    if (DEBUG>=1) System.out.println("T1: " + v + "->" + w);
                    int x = oev.bends;
                    int y = oew.bends;
                    int buffer1 = oev.bends;
                    int buffer2 = oew.bends;
                    oev.bends = Math.max(0,x-y);
                    oew.bends = Math.max(0,y-x);
                    current = new OrthographicEmbeddingResult(embedding,Gamma,fixNonOrthogonal);
                    if (current.sanityCheck(true)) {
                        best = current;
                    } else {
                        // undo!
                        oev.bends = buffer1;
                        oew.bends = buffer2;
                    }
                }
            }
        }
        
        for(int v = 0;v<n;v++) {
            // Apply T2: (case 1)
            int min = -1;
            for(OEElement oev:embedding[v].embedding) {
                if (min==-1 || oev.bends<min) {
                    min = oev.bends;
                }
            }
            if (min>0) {
                if (DEBUG>=1) System.out.println("T2(1): " + v);
                for(OEElement oev:embedding[v].embedding) {
                    oev.bends-=min;
                    OEElement oew = sym(oev,embedding);
                    // update the angle: (this was not in the original algorithm, 
                    // but it's necessary, since I store absolute angles, instead of relative ones
                    oew.angle = (oew.angle+min)%4;
                }
                current = new OrthographicEmbeddingResult(embedding,Gamma,fixNonOrthogonal);
                if (current.sanityCheck(true)) {
                    best = current;
                } else {
                    // undo!
                    for(OEElement oev:embedding[v].embedding) {
                        oev.bends+=min;
                        OEElement oew = sym(oev,embedding);
                        oew.angle = (oew.angle+min)%4;
                    }
                }
            }

            // Apply T2: (case 2)
            min = -1;
            for(OEElement oev:embedding[v].embedding) {
                OEElement oew = sym(oev,embedding);
                if (min==-1 || oew.bends<min) {
                    min = oew.bends;
                }
            }
            if (min>0) {
                if (DEBUG>=1) System.out.println("T2(2): " + v);
                for(OEElement oev:embedding[v].embedding) {
                    OEElement oew = sym(oev,embedding);
                    oew.bends-=min;
                    // update the angle: (this was not in the original algorithm, 
                    // but it's necessary, since I store absolute angles, instead of relative ones
                    oev.angle = (oev.angle+min)%4;
                }
                current = new OrthographicEmbeddingResult(embedding,Gamma,fixNonOrthogonal);
                if (current.sanityCheck(true)) {
                    best = current;
                } else {
                    // undo!
                    for(OEElement oev:embedding[v].embedding) {
                        OEElement oew = sym(oev,embedding);
                        oew.bends+=min;
                        oev.angle = (oev.angle+min)%4;
                    }
                }
            }
        }

        for(int v = 0;v<n;v++) {
            if (embedding[v].embedding.size()>1 && embedding[v].embedding.size()<=3) {
                for(int e = 0;e<embedding[v].embedding.size();e++) {
                    boolean tryagain = true;
                    while(tryagain) {
                        tryagain = false;
                        // Apply T3: (case 1)
                        int e2 = (e+1)%(embedding[v].embedding.size());
                        int e3 = (e+2)%(embedding[v].embedding.size());
                        OEElement oev = embedding[v].embedding.get(e);
                        OEElement oev2 = embedding[v].embedding.get(e2);
                        OEElement oev3 = embedding[v].embedding.get(e3);
                        int e_angle = oev2.angle - oev.angle;
                        int e2_angle = oev3.angle - oev2.angle;
                        if (e_angle<0) e_angle+=4;
                        if (e2_angle<0) e2_angle+=4;
                        if (e_angle>=2 && oev2.bends>=1) {
                            if (DEBUG>=1) System.out.println("T3(1): " + v + "->" + oev2.dest);
                            if (DEBUG>=1) System.out.println("e: " + v + "->" + oev.dest + ", e': " + oev2.v + "->" + oev2.dest + ", angle(e) = " + e_angle + ", bends(e') = " + oev2.bends);
                            int m = Math.min(e_angle-1, oev2.bends);
                            
                            int buffer1 = oev2.angle;
                            int buffer2 = oev2.bends;
                            oev2.angle-=m;
                            if (oev2.angle<0) oev2.angle+=4;
                            oev2.bends = oev2.bends - m;
                            if (DEBUG>=1) System.out.println("  result (e'): " + oev2);
                            if (DEBUG>=1) System.out.println("  result (sym(e')): " + sym(oev2,embedding));
                            current = new OrthographicEmbeddingResult(embedding,Gamma,fixNonOrthogonal);
                            if (current.sanityCheck(true)) {
                                tryagain = true;
                                best = current;
                            } else {
                                // undo!
                                oev2.angle = buffer1;
                                oev2.bends = buffer2;
                            }                        
                        }
                        if (!tryagain) {
                            // Apply T3: (case 2)
                            OEElement oew2 = sym(oev2, embedding);
                            if (e2_angle>=2 && oew2.bends>=1) {
                                if (DEBUG>=1) System.out.println("T3(2): " + v + "->" + oev2.dest);
                                if (DEBUG>=1) System.out.println("e: " + v + "->" + oev.dest + ", e': " + oev2.v + "->" + oev2.dest + ", angle(e') = " + e2_angle + ", bends(e') = " + oev2.bends);
                                int m = Math.min(e2_angle-1, oew2.bends);

                                int buffer1 = oev2.angle;
                                int buffer2 = oew2.bends;
                                oev2.angle+=m;
                                if (oev2.angle>=4) oev2.angle-=4;
                                oew2.bends = oew2.bends - m;
                                if (DEBUG>=1) System.out.println("  result (e'): " + oev2);
                                if (DEBUG>=1) System.out.println("  result (sym(e')): " + oew2);
                                current = new OrthographicEmbeddingResult(embedding,Gamma,fixNonOrthogonal);
                                if (current.sanityCheck(true)) {
                                    tryagain = true;
                                    best = current;
                                } else {
                                    // undo!
                                    oev2.angle = buffer1;
                                    oew2.bends = buffer2;
                                }                        
                            }
                        }
                    }
                }
            }
        }    
        
        return best;
    }    
    

    static OEElement sym(OEElement oev, OEVertex embedding[]) {
        int w = oev.dest;
        OEVertex ew = embedding[w];
        for(OEElement tmp:ew.embedding) {
            if (tmp.dest==oev.v) return tmp;
        }                    
        return null;
    }

    
    static OEVertex vertexOrtographicEmbedding(int v, int graph[][], Visibility Gamma, OEVertex embedding[]) {
        List<OEElement> vertexEmbedding = new ArrayList<OEElement>();
        double x = -1,y = -1;
        int n = graph.length;
        double tolerance = 0.1;
        
        if (DEBUG>=1) System.out.println("Generating ortographic embedding for node " + v + ":");
        List<Integer> edgesOnTop = new ArrayList<Integer>();
        List<Integer> edgesBelow = new ArrayList<Integer>();
        double vertexY = Gamma.horizontal_y[v];
        for(int w = 0;w<n;w++) {
            if (graph[v][w]!=0) {
                int vIndex = Gamma.edgeIndexes[v][w];
                if (Gamma.vertical_y1[vIndex]<vertexY-0.5 || Gamma.vertical_y2[vIndex]<vertexY-0.5) {
                    // insert in the right spot:
                    boolean inserted = false;
                    for(int i = 0;i<edgesOnTop.size();i++) {
                        if (Gamma.vertical_x[vIndex]<Gamma.vertical_x[Gamma.edgeIndexes[v][edgesOnTop.get(i)]]) {
                            edgesOnTop.add(i,w);
                            inserted = true;
                            break;
                        }
                    }
                    if (!inserted) edgesOnTop.add(w);
                } else {
                    // insert in the right spot:
                    boolean inserted = false;
                    for(int i = 0;i<edgesBelow.size();i++) {
                        if (Gamma.vertical_x[vIndex]<Gamma.vertical_x[Gamma.edgeIndexes[v][edgesBelow.get(i)]]) {
                            edgesBelow.add(i,w);
                            inserted = true;
                            break;
                        }
                    }
                    if (!inserted) edgesBelow.add(w);
                }
            }
        }
        if (DEBUG>=1) {
            System.out.println("Edges on top " + edgesOnTop);
            for(int i:edgesOnTop) System.out.println("  " + Gamma.vertical_x[Gamma.edgeIndexes[v][i]]);
        }
        if (DEBUG>=1) {
            System.out.println("Edges below " + edgesBelow);
            for(int i:edgesBelow) System.out.println("  " + Gamma.vertical_x[Gamma.edgeIndexes[v][i]]);
        }

        // possible cases:
        int ntop = edgesOnTop.size();
        int nbelow = edgesBelow.size();
        if (ntop==1 && nbelow==0) { // (a)
            int w = edgesOnTop.get(0);
            OEElement e = new OEElement(v, w, OEElement.UP, 0);
            y = Gamma.horizontal_y[v];
            x = Gamma.vertical_x[Gamma.edgeIndexes[v][w]];
            findSymmetric(e,embedding);
            vertexEmbedding.add(e);
            if (DEBUG>=1) System.out.println("Node " + v + " processed with pattern (a).1: " + x + "," + y);
            
        } else if (ntop==0 && nbelow==1) {  // (a)
            int w = edgesBelow.get(0);
            OEElement e = new OEElement(v, w, OEElement.DOWN, 0);
            y = Gamma.horizontal_y[v];
            x = Gamma.vertical_x[Gamma.edgeIndexes[v][w]];
            findSymmetric(e,embedding);
            vertexEmbedding.add(e);
            if (DEBUG>=1) System.out.println("Node " + v + " processed with pattern (a).2: " + x + "," + y);

        } else if (ntop==2 && nbelow==0) {  // (b)
            int w = edgesOnTop.get(0);
            OEElement e = new OEElement(v, w, OEElement.UP, 0);
            y = Gamma.horizontal_y[v];
            x = Gamma.vertical_x[Gamma.edgeIndexes[v][w]];
            findSymmetric(e,embedding);
            vertexEmbedding.add(e);

            w = edgesOnTop.get(1);
            e = new OEElement(v, w, OEElement.RIGHT, 1);
            findSymmetric(e,embedding);
            vertexEmbedding.add(e);
            if (DEBUG>=1) System.out.println("Node " + v + " processed with pattern (b).1: " + x + "," + y);
                        
        } else if (ntop==1 && nbelow==1) {  // (c)
            if (DEBUG>=1) System.out.println("Node " + v + " processed with pattern (c).1");
            double xtop = Gamma.vertical_x[Gamma.edgeIndexes[v][edgesOnTop.get(0)]];
            double xbot = Gamma.vertical_x[Gamma.edgeIndexes[v][edgesBelow.get(0)]];
            int w = edgesOnTop.get(0);
            y = Gamma.horizontal_y[v];
            x = (xtop+xbot)/2;
            if (Math.abs(xtop-xbot)<tolerance) {
                OEElement e = new OEElement(v, w, OEElement.UP, 0);
                findSymmetric(e,embedding);
                vertexEmbedding.add(e);
                
                w = edgesBelow.get(0);
                e = new OEElement(v, w, OEElement.DOWN, 0);
                findSymmetric(e,embedding);
                vertexEmbedding.add(e);
            } else if (xtop<xbot) {
                OEElement e = new OEElement(v, w, OEElement.LEFT, 0);
                e.bendsToAddToSymmetric = 1;
                findSymmetric(e,embedding);
                vertexEmbedding.add(e);
                
                w = edgesBelow.get(0);
                e = new OEElement(v, w, OEElement.RIGHT, 0);
                e.bendsToAddToSymmetric = 1;
                findSymmetric(e,embedding);
                vertexEmbedding.add(e);                
            } else {
                OEElement e = new OEElement(v, w, OEElement.RIGHT, 1);
                findSymmetric(e,embedding);
                vertexEmbedding.add(e);
                
                w = edgesBelow.get(0);
                e = new OEElement(v, w, OEElement.LEFT, 1);
                findSymmetric(e,embedding);
                vertexEmbedding.add(e);                
            }
        } else if (ntop==0 && nbelow==2) {  // (b)
            int w = edgesBelow.get(1);
            OEElement e = new OEElement(v, w, OEElement.DOWN, 0);
            y = Gamma.horizontal_y[v];
            x = Gamma.vertical_x[Gamma.edgeIndexes[v][w]];
            findSymmetric(e,embedding);
            vertexEmbedding.add(e);

            w = edgesBelow.get(0);
            e = new OEElement(v, w, OEElement.LEFT, 1);
            findSymmetric(e,embedding);
            vertexEmbedding.add(e);
            if (DEBUG>=1) System.out.println("Node " + v + " processed with pattern (b).2: " + x + "," + y);
                        
        } else if (ntop==3 && nbelow==0) {  // (d)
            if (DEBUG>=1) System.out.println("Node " + v + " processed with pattern (d).1");
            int w = edgesOnTop.get(0);
            OEElement e = new OEElement(v, w, OEElement.LEFT, 0);
            e.bendsToAddToSymmetric = 1;
            findSymmetric(e,embedding);
            vertexEmbedding.add(e);

            w = edgesOnTop.get(1);
            e = new OEElement(v, w, OEElement.UP, 0);
            y = Gamma.horizontal_y[v];
            x = Gamma.vertical_x[Gamma.edgeIndexes[v][w]];
            findSymmetric(e,embedding);
            vertexEmbedding.add(e);

            w = edgesOnTop.get(2);
            e = new OEElement(v, w, OEElement.RIGHT, 1);
            findSymmetric(e,embedding);
            vertexEmbedding.add(e);

        } else if (ntop==2 && nbelow==1) {  // (e)
            if (DEBUG>=1) System.out.println("Node " + v + " processed with pattern (e).1");
            int w0 = edgesOnTop.get(0);
            int w1 = edgesOnTop.get(1);
            int w2 = edgesBelow.get(0);
            OEElement e = new OEElement(v, w0, OEElement.UP, 0);
            y = Gamma.horizontal_y[v];
            x = Gamma.vertical_x[Gamma.edgeIndexes[v][w0]];
            findSymmetric(e,embedding);
            vertexEmbedding.add(e);

            e = new OEElement(v, w1, OEElement.RIGHT, 1);
            findSymmetric(e,embedding);
            vertexEmbedding.add(e);
            
            if (Gamma.vertical_x[Gamma.edgeIndexes[v][w2]] == Gamma.vertical_x[Gamma.edgeIndexes[v][w0]]) {
                e = new OEElement(v, w2, OEElement.DOWN, 0);
                findSymmetric(e,embedding);
                vertexEmbedding.add(e);            
            } else {
                e = new OEElement(v, w2, OEElement.DOWN, 1);
                e.bendsToAddToSymmetric = 1;
                findSymmetric(e,embedding);
                vertexEmbedding.add(e);            
            } 
                        
        } else if (ntop==1 && nbelow==2) {  // (e)
            if (DEBUG>=1) System.out.println("Node " + v + " processed with pattern (e).2");
            int w0 = edgesBelow.get(0);
            int w1 = edgesBelow.get(1);
            int w2 = edgesOnTop.get(0);
            if (Gamma.vertical_x[Gamma.edgeIndexes[v][w2]] == Gamma.vertical_x[Gamma.edgeIndexes[v][w1]]) {
                OEElement e = new OEElement(v, w2, OEElement.UP, 0);
                findSymmetric(e,embedding);
                vertexEmbedding.add(e);
            } else {
                OEElement e = new OEElement(v, w2, OEElement.UP, 1);
                e.bendsToAddToSymmetric = 1;
                findSymmetric(e,embedding);
                vertexEmbedding.add(e);
            } 
            
            OEElement e = new OEElement(v, w1, OEElement.DOWN, 0);
            y = Gamma.horizontal_y[v];
            x = Gamma.vertical_x[Gamma.edgeIndexes[v][w1]];
            findSymmetric(e,embedding);
            vertexEmbedding.add(e);

            e = new OEElement(v, w0, OEElement.LEFT, 1);
            findSymmetric(e,embedding);
            vertexEmbedding.add(e);            
        
        } else if (ntop==0 && nbelow==3) {  // (d)
            if (DEBUG>=1) System.out.println("Node " + v + " processed with pattern (d).2");
            int w = edgesBelow.get(2);
            OEElement e = new OEElement(v, w, OEElement.RIGHT, 0);
            e.bendsToAddToSymmetric = 1;
            findSymmetric(e,embedding);
            vertexEmbedding.add(e);

            w = edgesBelow.get(1);
            e = new OEElement(v, w, OEElement.DOWN, 0);
            y = Gamma.horizontal_y[v];
            x = Gamma.vertical_x[Gamma.edgeIndexes[v][w]];
            findSymmetric(e,embedding);
            vertexEmbedding.add(e);

            w = edgesBelow.get(0);
            e = new OEElement(v, w, OEElement.LEFT, 1);
            findSymmetric(e,embedding);
            vertexEmbedding.add(e);

        } else if (ntop==4 && nbelow==0) {  // (f)
            if (DEBUG>=1) System.out.println("Node " + v + " processed with pattern (f).1");
            int w = edgesOnTop.get(0);
            OEElement e = new OEElement(v, w, OEElement.LEFT, 0);
            e.bendsToAddToSymmetric = 1;
            findSymmetric(e,embedding);
            vertexEmbedding.add(e);
            if (DEBUG>=1) System.out.println("   left:"  + w);

            w = edgesOnTop.get(1);
            e = new OEElement(v, w, OEElement.UP, 0);
            y = Gamma.horizontal_y[v];
            x = Gamma.vertical_x[Gamma.edgeIndexes[v][w]];
            findSymmetric(e,embedding);
            vertexEmbedding.add(e);
            if (DEBUG>=1) System.out.println("   up:"  + w);

            w = edgesOnTop.get(2);
            e = new OEElement(v, w, OEElement.RIGHT, 1);
            findSymmetric(e,embedding);
            vertexEmbedding.add(e);
            if (DEBUG>=1) System.out.println("   right:"  + w);
                        
            w = edgesOnTop.get(3);
            e = new OEElement(v, w, OEElement.DOWN, 2);
            findSymmetric(e,embedding);
            vertexEmbedding.add(e);
            if (DEBUG>=1) System.out.println("   down:"  + w);

        } else if (ntop==3 && nbelow==1) {  // (g)
            if (DEBUG>=1) System.out.println("Node " + v + " processed with pattern (g).1");
            int w = edgesOnTop.get(0);
            OEElement e = new OEElement(v, w, OEElement.LEFT, 0);
            e.bendsToAddToSymmetric = 1;
            findSymmetric(e,embedding);
            vertexEmbedding.add(e);

            w = edgesOnTop.get(1);
            e = new OEElement(v, w, OEElement.UP, 0);
            y = Gamma.horizontal_y[v];
            x = Gamma.vertical_x[Gamma.edgeIndexes[v][w]];
            findSymmetric(e,embedding);
            vertexEmbedding.add(e);

            w = edgesOnTop.get(2);
            e = new OEElement(v, w, OEElement.RIGHT, 1);
            findSymmetric(e,embedding);
            vertexEmbedding.add(e);
            
            w = edgesBelow.get(0);
            e = new OEElement(v, w, OEElement.DOWN, 1);
            e.bendsToAddToSymmetric = 1;
            findSymmetric(e,embedding);
            vertexEmbedding.add(e);            

        } else if (ntop==2 && nbelow==2) {  // (h) or (i)
            if (DEBUG>=1) System.out.println("Node " + v + " processed with pattern (h/i).1");
            
            if (Gamma.vertical_x[Gamma.edgeIndexes[v][edgesOnTop.get(1)]] > 
                Gamma.vertical_x[Gamma.edgeIndexes[v][edgesBelow.get(0)]]) {
                int w = edgesOnTop.get(0);
                OEElement e = new OEElement(v, w, OEElement.UP, 1);
                e.bendsToAddToSymmetric = 1;
                findSymmetric(e,embedding);
                vertexEmbedding.add(e);

                w = edgesOnTop.get(1);
                e = new OEElement(v, w, OEElement.RIGHT, 1);
                findSymmetric(e,embedding);
                vertexEmbedding.add(e);

                w = edgesBelow.get(1);
                e = new OEElement(v, w, OEElement.DOWN, 1);
                e.bendsToAddToSymmetric = 1;
                findSymmetric(e,embedding);
                vertexEmbedding.add(e);

                w = edgesBelow.get(0);
                e = new OEElement(v, w, OEElement.LEFT, 1);
                findSymmetric(e,embedding);
                vertexEmbedding.add(e);

                y = Gamma.horizontal_y[v];
                x = (Gamma.vertical_x[Gamma.edgeIndexes[v][edgesOnTop.get(1)]] + 
                     Gamma.vertical_x[Gamma.edgeIndexes[v][edgesBelow.get(0)]])/2;
            } else {
                int w = edgesOnTop.get(0);
                OEElement e = new OEElement(v, w, OEElement.LEFT, 0);
                e.bendsToAddToSymmetric = 1;
                findSymmetric(e,embedding);
                vertexEmbedding.add(e);

                w = edgesOnTop.get(1);
                e = new OEElement(v, w, OEElement.UP, 1);
                e.bendsToAddToSymmetric = 1;
                findSymmetric(e,embedding);
                vertexEmbedding.add(e);

                w = edgesBelow.get(1);
                e = new OEElement(v, w, OEElement.RIGHT, 0);
                e.bendsToAddToSymmetric = 1;
                findSymmetric(e,embedding);
                vertexEmbedding.add(e);

                w = edgesBelow.get(0);
                e = new OEElement(v, w, OEElement.DOWN, 1);
                e.bendsToAddToSymmetric = 1;
                findSymmetric(e,embedding);
                vertexEmbedding.add(e);

                y = Gamma.horizontal_y[v];
                x = (Gamma.vertical_x[Gamma.edgeIndexes[v][edgesOnTop.get(0)]] + 
                     Gamma.vertical_x[Gamma.edgeIndexes[v][edgesBelow.get(1)]])/2;
                
            }
            

        } else if (ntop==1 && nbelow==3) {  // (g)
            if (DEBUG>=1) System.out.println("Node " + v + " processed with pattern (g).2");
            int w = edgesBelow.get(2);
            OEElement e = new OEElement(v, w, OEElement.RIGHT, 0);
            e.bendsToAddToSymmetric = 1;
            findSymmetric(e,embedding);
            vertexEmbedding.add(e);

            w = edgesBelow.get(1);
            e = new OEElement(v, w, OEElement.DOWN, 0);
            y = Gamma.horizontal_y[v];
            x = Gamma.vertical_x[Gamma.edgeIndexes[v][w]];
            findSymmetric(e,embedding);
            vertexEmbedding.add(e);

            w = edgesBelow.get(0);
            e = new OEElement(v, w, OEElement.LEFT, 1);
            findSymmetric(e,embedding);
            vertexEmbedding.add(e);
            
            w = edgesOnTop.get(0);
            e = new OEElement(v, w, OEElement.UP, 1);
            e.bendsToAddToSymmetric = 1;
            findSymmetric(e,embedding);
            vertexEmbedding.add(e);            

        } else if (ntop==0 && nbelow==4) {  // (f)
            if (DEBUG>=1) System.out.println("Node " + v + " processed with pattern (f).2");
            int w = edgesBelow.get(3);
            OEElement e = new OEElement(v, w, OEElement.RIGHT, 0);
            e.bendsToAddToSymmetric = 1;
            findSymmetric(e,embedding);
            vertexEmbedding.add(e);

            w = edgesBelow.get(2);
            e = new OEElement(v, w, OEElement.DOWN, 0);
            y = Gamma.horizontal_y[v];
            x = Gamma.vertical_x[Gamma.edgeIndexes[v][w]];
            findSymmetric(e,embedding);
            vertexEmbedding.add(e);

            w = edgesBelow.get(1);
            e = new OEElement(v, w, OEElement.LEFT, 1);
            findSymmetric(e,embedding);
            vertexEmbedding.add(e);
                        
            w = edgesBelow.get(0);
            e = new OEElement(v, w, OEElement.UP, 2);
            findSymmetric(e,embedding);
            vertexEmbedding.add(e);
        }
        
        return new OEVertex(vertexEmbedding, v, x, y);
    }
    
    public static void findSymmetric(OEElement e, OEVertex embedding[]) {
        if (embedding[e.dest]!=null) {
            for(OEElement e_sym:embedding[e.dest].embedding) {
                if (e_sym.dest == e.v) {
                    e_sym.sym = e;
                    e.sym = e_sym;
                    e.bends += e_sym.bendsToAddToSymmetric;
                    e_sym.bendsToAddToSymmetric = 0;
                    e_sym.bends += e.bendsToAddToSymmetric;
                    e.bendsToAddToSymmetric = 0;
                    break;
                }
            }
        }
    }
}
