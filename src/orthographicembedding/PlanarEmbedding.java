/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package orthographicembedding;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author santi
 */
public class PlanarEmbedding {
    public static int DEBUG = 0;
    
    // This function assumes that the given graph is 2-connected
    public static List<Integer>[] planarEmbedding2Connected(int graph[][], Random r) throws Exception {
        int stNumbering[] = STNumbering.stNumbering(graph, r);
        if (DEBUG>=1) System.out.println("stNumbering: " + Arrays.toString(stNumbering));
        return planarEmbedding2Connected(graph, stNumbering);
    }

    public static List<Integer>[] planarEmbedding2Connected(int graph[][], int stNumbering[]) throws Exception {
        List<Integer> upwardEmbedding[] = planarUpwardEmbedding(graph,stNumbering);        
        return translateEmbeddingToNodeIndexes(
                    extendUpwardEmbedding(upwardEmbedding),
                    stNumbering);
    }
    
    // This function assumes that the given graph is 2-connected
    // It returns an embedding indexed by the st-numbers
    public static List<Integer>[] planarUpwardEmbedding(int graph[][], int stNumbering[]) throws Exception {
        int n = graph.length;
        List<Integer> embedding[] = new List[n];
        HashMap<PQTree,Integer> nodeParent = new LinkedHashMap<PQTree,Integer>();
        int nodeWithNumber[] = new int[n];
        int s = -1;
        int t = -1;
        for(int i = 0;i<n;i++) {
            if (stNumbering[i]==1) s = i;
            if (stNumbering[i]==n) t = i;
            nodeWithNumber[stNumbering[i]-1] = i;
        }
        if (DEBUG>=1) System.out.println("st-numbering: " + Arrays.toString(stNumbering));
        if (DEBUG>=1) System.out.println("s: " + s + ", t: " + t);

        // Create the initial PQ-tree as a P node (representing "s") with all the connections of "s" as leaves:
        PQTree pqTree = new PQTree(stNumbering[s],PQTree.P_NODE, null);
        for(int i = 0;i<graph.length;i++) {
            if (graph[s][i]==1 && stNumbering[i]>stNumbering[s]) {
                PQTree leaf = new PQTree(stNumbering[i], PQTree.LEAF_NODE, pqTree);
                nodeParent.put(leaf,stNumbering[s]);
            }
        }
        
        if (DEBUG>=1) System.out.println("PQ-tree at the beggining:\n" + pqTree.toString(2,nodeParent));
        
        for(int i = 1;i<n;i++) {
            int vNumber = i+1;
            int vNode = nodeWithNumber[i];
            
            if (DEBUG>=1) System.out.println("\n- Next is node " + vNode + " with st-number " + vNumber);
            
            // reduction step: 
            // - "gather" all the virtual vertices that have index vNumber+1
            //   (this means that all the vertices with index "nNumber" should 
            //    appear consecutively as leaves in the PQ-tree)
            //   (if the set of nodes with "vNumber" is "S", this operation corresponds to REDUCE(T,S))
            if (!pqTree.reduce(vNumber)) {
                if (DEBUG>=1) System.out.println("Cannot reduce the PQ-tree!!!");
                if (DEBUG>=1) System.out.println(pqTree.toString(nodeParent));                
                return null;
            }
            
            if (DEBUG>=1) System.out.println("  PQ-tree after reduction:\n" + pqTree.toString(2,nodeParent));
                                  
            // Vertex addition step:
            // - get the pertiment root (what I used to call the "fullParent"
            // - get all the "pertinent leaves" (full leaves) + direction indicators: l1, ..., lk
            List<PQTree> fullNodes = new LinkedList<PQTree>(); 
            List<PQTree> pertinentLeaves = new LinkedList<PQTree>();
            List<PQTree> directionIndicators = new LinkedList<PQTree>();
            PQTree fullParent = null;
            PQTree pertinentRoot = null;
            {
                List<PQTree> stack = new LinkedList<PQTree>();
                stack.add(pqTree);
                while(!stack.isEmpty()) {
                    PQTree current = stack.remove(0);
                    if (current.nodeType!=PQTree.DIRECTION_INDICATOR &&
                        pertinentRoot==null &&
                        (current.label==PQTree.LABEL_FULL ||
                         current.label==PQTree.LABEL_PARTIAL)) {
                        pertinentRoot = current;
                    }
                    if (current.label==PQTree.LABEL_FULL) {
                        if (current.nodeType!=PQTree.DIRECTION_INDICATOR &&
                            fullParent==null) {
                            fullParent = current.parent;
                            while(fullParent!=null && fullParent.label==PQTree.LABEL_FULL) {
                                fullParent = fullParent.parent;                                
                            }
                        }                        
                        if (current.nodeType!=PQTree.DIRECTION_INDICATOR) fullNodes.add(current);
                        if (current.nodeType==PQTree.LEAF_NODE) {
                            pertinentLeaves.add(current);
                        } else if (current.nodeType==PQTree.DIRECTION_INDICATOR) {
                            directionIndicators.add(current);
                        }
                    }
                    if (current.children!=null) {
                        int insertPoint = 0;
                        for(PQTree child:current.children) {
                            stack.add(insertPoint,child);
                            insertPoint++;
                        }
                    }
                }
            }
            // refine the pertinent root:
//            if (pertinentRoot!=null) 
            {
                boolean repeat;
                do {
                    repeat = false;
                    PQTree nonEmptyChild = null;
                    if (pertinentRoot.children!=null) {
                        for(PQTree child:pertinentRoot.children) {
                            if (child.nodeType==PQTree.DIRECTION_INDICATOR) continue;
                            if (child.label!=PQTree.LABEL_EMPTY) {
                                if (nonEmptyChild==null) {
                                    nonEmptyChild = child;
                                } else {
                                    nonEmptyChild = null;
                                    break;
                                }
                            }
                        }
                        if (nonEmptyChild!=null) {
                            pertinentRoot = nonEmptyChild;
                            repeat = true;
                        }
                    }
                }while(repeat);
                List<PQTree> toDelete = new LinkedList<PQTree>();
                for(PQTree indicator:directionIndicators) {
                    if (!pertinentRoot.contains(indicator)) toDelete.add(indicator);
                }
                directionIndicators.removeAll(toDelete);
            }
            if (DEBUG>=1) {
                System.out.println("  full parent: " + fullParent);
                System.out.println("  full nodes: " + fullNodes);
                System.out.println("  pertinentRoot: " + pertinentRoot);
                System.out.println("  pertinent leaves: " + pertinentLeaves);
                System.out.println("  direction indicators: " + directionIndicators);
            }
            
            // - add l1, ..., lk to the upward embedding
            embedding[vNumber-1] = new LinkedList<Integer>();
            for(PQTree leaf:pertinentLeaves) {
                if (leaf.nodeIndex==vNumber) {
                    embedding[vNumber-1].add(nodeParent.get(leaf));
                }
            }
            if (DEBUG>=1) System.out.println("- embbeding for " + vNumber + ": " + embedding[vNumber-1]);
                        
            // - if pertinent root is full, replace it by a P-node
            //   else: - add a direction indicator with lavel 'vNumber' directed from lk to l1 to perinent root
            //         - replace all full leaves by a P-node
            // - add all the virtual vertices to the vNumber vertices
            // Create the new P-node:
            // - add all the virtual vertices to the vNumber vertices
            PQTree PNode = new PQTree(vNumber,PQTree.P_NODE,null);
            for(int j = 0;j<graph.length;j++) {
                if (graph[vNode][j]==1 && stNumbering[j]>stNumbering[vNode]) {
                    PQTree leaf = new PQTree(stNumbering[j], PQTree.LEAF_NODE, PNode);
                    nodeParent.put(leaf,stNumbering[vNode]);
                }
            }
            if (DEBUG>=1) System.out.println("  new P-node will have " + PNode.children.size() + " children.");
            if (fullParent==null) {
                pqTree = PNode;
                for(PQTree di2:directionIndicators) {
                    if (di2.direction == PQTree.DIRECTION_INDICATOR_RIGHT) {
//                        if (di2.direction == PQTree.DIRECTION_INDICATOR_LEFT) {
                        if (DEBUG>=1) System.out.println("reversing the upward embedding because of: " + di2);
                        // reverse the upward embedding:
                        List<Integer> tmp = new LinkedList<Integer>();
                        tmp.addAll(embedding[di2.nodeIndex-1]);
                        embedding[di2.nodeIndex-1].clear();
                        for(int v:tmp) {
                            embedding[di2.nodeIndex-1].add(0, v);
                        }
                    }
                }
            } else {
                int insertionIndex = -1;
                // fullParent.children.removeAll(directionIndicators);
                List<PQTree> removedIndicators = new LinkedList<PQTree>();
                for(PQTree tmp:directionIndicators) {   
                    if (fullParent.recursivelyRemoveLeafNotThroughQNodes(tmp)) {
                        removedIndicators.add(tmp);
                    } else {
                        if (fullNodes.contains(tmp.parent)) {
                            removedIndicators.add(tmp); // it will be deleted, since all the fullNodes will be eliminated!
                        }
                    }
                }
                if (DEBUG>=1) System.out.println("Removed direction indicators: " + removedIndicators);
                for(PQTree node:fullNodes) {
                    int idx = fullParent.children.indexOf(node);
                    if (idx!=-1 && 
                        (insertionIndex==-1 || idx<insertionIndex)) insertionIndex = idx;
                }
                if (DEBUG>=1) System.out.println("Insertion index of new P node is: " + insertionIndex);
                fullParent.children.removeAll(fullNodes);
                if (PNode.children.size()==1) {
                    fullParent.children.add(insertionIndex,PNode.children.get(0));
                    PNode.children.get(0).parent = fullParent;
                } else {
                    fullParent.children.add(insertionIndex,PNode);
                    PNode.parent = fullParent;
                }
                if (pertinentRoot.label!=PQTree.LABEL_FULL) {                    
                    if (pertinentRoot!=fullParent) throw new Exception("pertinentRoot is partial, but doesn't match with fullParent!!!");

                    // - add a direction indicator with label 'vNumber' directed from lj to l1 to perinent root
                    PQTree di = new PQTree(vNumber, PQTree.DIRECTION_INDICATOR, fullParent);
                    di.direction = PQTree.DIRECTION_INDICATOR_LEFT;
                    if (DEBUG>=1) System.out.println("*** direction indicator added ***");
                    
                    // - add the rest of direction indicators:
                    for(PQTree di2:removedIndicators) {
                        di2.parent = fullParent;
                        fullParent.children.add(di2);
                    }                    
                } else {
                    for(PQTree di2:directionIndicators) {
                        if (pertinentRoot.contains(di2)) {
                            if (di2.direction == PQTree.DIRECTION_INDICATOR_RIGHT) {
    //                        if (di2.direction == PQTree.DIRECTION_INDICATOR_LEFT) {
                                if (DEBUG>=1) System.out.println("reversing the upward embedding because of: " + di2);
                                // reverse the upward embedding:
                                List<Integer> tmp = new LinkedList<Integer>();
                                tmp.addAll(embedding[di2.nodeIndex-1]);
                                embedding[di2.nodeIndex-1].clear();
                                for(int v:tmp) {
                                    embedding[di2.nodeIndex-1].add(0, v);
                                }
                            }
                        } else {
                            System.err.println("PlanarEmbering: this should not have hapened!");
                            // add the indicators that we should not have removed back:
//                            if (removedIndicators.contains(di2)) {
//                                fullParent.children.add(di2);
//                            }
                        }
                    }
                }

            }
            
            if (DEBUG>=1) System.out.println("PQ-tree after insertion of the new P-node:\n" + pqTree.toString(2,nodeParent));
        }
        
        return embedding;
    }
    
    
    public static List<Integer>[] translateEmbeddingToNodeIndexes(List<Integer>[] embedding, int stNumbering[]) {
        if (embedding==null) return null;
        int n = embedding.length;
        int nodeWithNumber[] = new int[n];
        for(int i = 0;i<n;i++) {
            nodeWithNumber[stNumbering[i]-1] = i;
        }
        List<Integer>[] translated = new List[n];
        
        if (DEBUG>=1) {
            System.out.println("translateEmbeddingToNodeIndexes: embedding to translate:");
            for(int i = 0;i<embedding.length;i++) {
                System.out.println((i+1) + " : " + embedding[i]);
            }
        }
        
        for(int i = 0;i<n;i++) {
            int v = nodeWithNumber[i];
            translated[v] = new LinkedList<Integer>();
            if (embedding[i]!=null) {
                for(int stNumber:embedding[i]) {
                    if (DEBUG>=1) System.out.println("translateEmbeddingToNodeIndexes: " + stNumber + " -> " + nodeWithNumber[stNumber-1]);
                    translated[v].add(nodeWithNumber[stNumber-1]);
                }
            }
        }
        
        return translated;
    }
    
    
    // This function indexes the nodes by st-numbering:
    public static List<Integer>[] extendUpwardEmbedding(List<Integer>[] upwardEmbedding) {
        if (upwardEmbedding==null) return null;
        if (DEBUG>=1) {
            System.out.println("extendUpwardEmbedding: upwardEmbedding:");
            for(int i = 0;i<upwardEmbedding.length;i++) {
                System.out.println((i+1) + " : " + upwardEmbedding[i]);
            }
        }
        
        // copy 'upwardEmbedding' onto 'embedding':
        int n = upwardEmbedding.length;
        List<Integer>[] embedding = new List[n];
        boolean newNode[] = new boolean[n];
        for(int i = 0;i<n;i++) {
            newNode[i] = true;
            embedding[i] = new LinkedList<Integer>();
            if (upwardEmbedding[i]!=null) {
                embedding[i].addAll(upwardEmbedding[i]);
            }
        }

        // since nodes are indexed by st-numbering, passing 'n' means starting with t:
        DFS(upwardEmbedding, embedding, newNode, n);
//        DFS(embedding, newNode, n);
        
        return embedding;
    }
    
    
    public static void DFS(List<Integer>[] Au, List<Integer>[] A, boolean newNode[], int y) {
        newNode[y-1] = false;
        if (Au[y-1]!=null) {
            for(Integer v:Au[y-1]) {
                A[v-1].add(0,y);
//                A[v-1].add(y);
                if (newNode[v-1]) {
                    DFS(Au, A, newNode, v);
                }
            }
        }
    }
    
    
    // this method finds the faces of an embedding:
    // It returns a list of faces, where each face is represented as the list of vertices,
    // in a clock-wise order, and starting with the lowest indexed node
    public static List<List<Integer>> faces(List<Integer>[] embedding) {
        int n = embedding.length;
        List<List<Integer>> faces = new LinkedList<List<Integer>>();
        
        for(int v1 = 0;v1<n;v1++) {
            for(int v2:embedding[v1]) {
                List<Integer> face = new LinkedList<Integer>();
                face.add(v1);
                face.add(v2);
                getFace(embedding, face);
                // rotate the face until we have the smallest element first:
                int smallest = -1;
                for(Integer v:face) {
                    if (smallest==-1 || v<smallest) smallest=v;
                }
                while(face.get(0)!=smallest) face.add(face.remove(0));
                if (!faces.contains(face)) faces.add(face);
            }
        }
        
        return faces;
    }
    
    
    static void getFace(List<Integer>[] embedding, List<Integer> face) {// , boolean clockwise) {
        int v_prev = face.get(face.size()-2);
        int v = face.get(face.size()-1);
        int idx = embedding[v].indexOf(v_prev);
        int l = embedding[v].size();
//        if (clockwise) {
            idx++;
            if (idx>=l) idx=0;
//        } else {
//            idx--;
//            if (idx<=0) idx=l-1;
//        }
        int v_next = embedding[v].get(idx);
        if (face.contains(v_next)) return;
        face.add(v_next);
//        getFace(embedding, face, !clockwise);
        getFace(embedding, face);
    }
}
