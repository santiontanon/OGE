/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package orthographicembedding;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import util.Pair;

/**
 *
 * @author santi
 */
public class Blocks {
    public static int DEBUG = 0;
    
    public static Pair<HashMap<Integer,List<Integer>>,HashMap<Integer,List<Integer>>> blocks(int graph[][]) {
        if (graph.length==1) {
            if (DEBUG>=2) System.out.println("Blocks: special case of a graph with a single node.");
            // special case:
            HashMap<Integer,List<Integer>> blocks = new HashMap<>();
            HashMap<Integer,List<Integer>> cutNodes = new HashMap<>();
            List<Integer> l = new LinkedList<>();
            l.add(1);
            blocks.put(0, l);
            return new Pair<>(blocks,cutNodes);
        }
        

        // the following code assumes that nodes are numbered 1,2,... (i.e. they start at 1 not at 0!)
        // this is because the algorithms uses node numebers and their negative to determine
        // certain properties of the graph, and 0 and its negative are the same, so it wouldn't work
        int n = graph.length;
        int b[] = new int[n+1];           // one label per edge in the spanning tree
        int p[] = new int[n+1];           // this will store the spanning tree
        int d[] = new int[n+1];           // the distance of each node to the root of the tree
        boolean A[] = new boolean[n+1];   // one per node in the graph
//        Set<Integer> T = new HashSet<>();
        Set<Integer> X = new HashSet<>();
        Set<Integer> R = new HashSet<>();
        List<Integer> U = new LinkedList<>();        
        int treeroot = 1;
        
        for(int i = 1;i<=n;i++) {
            b[i] = 0;
            A[i] = false;
            if (i!=treeroot) R.add(i);
            p[i] = -1;
        }
//        T.add(treeroot);
        U.add(treeroot);
        d[treeroot] = 0;
        
        while(!U.isEmpty()) {
//            for(int i = 1;i<=n;i++) A[i] = false;
            
            List<Integer> toAddInU = new LinkedList<>();
            if (DEBUG>=2) System.out.println("----");
            if (DEBUG>=2) System.out.println("  U + X + R = " + U + " + " + X + " + " + R);
            int v = U.remove(U.size()-1);   // get the last element of U
            if (DEBUG>=2) System.out.println("  v = " + v);
            int L = 0;
            for(int z = 1;z<=n;z++) {
                if (graph[v-1][z-1]==1) {   // we have to subtract 1, since in 'graph' nodes start from 0
                    if (R.contains(z)) {
                        // add (v,z) to T???
                        R.remove(z);
                        toAddInU.add(z);
                        p[z] = v;
                        d[z] = d[v]+1;
                        b[z] = -z;
                        if (DEBUG>=2) System.out.println("  p[" + z + "] = " + v);
                        if (DEBUG>=2) System.out.println("  (" + z + "," + p[z] + ") = " + b[z]);
                    }
                }
            }
            for(int z = 1;z<=n;z++) {
                if (graph[v-1][z-1]==1) {   // we have to subtract 1, since in 'graph' nodes start from 0
                    if (U.contains(z)) {
                        // fundamental cycle detected:
                        if (DEBUG>=2) System.out.println("  Cycle: " + v + " -> " + z + " -> " + p[z] + " + Q");
                        int q = b[z];
                        b[z] = v;
                        if (DEBUG>=2) System.out.println("  (" + z + "," + p[z] + ") = " + b[z]);
                        if (q>0) A[q] = true;
                        L = Math.max(L,d[v] - d[p[z]]);
                        if (DEBUG>=2) System.out.println("  L = " + L);
                    }
                }
            }
            if (L>0) {
                if (DEBUG>=2) System.out.println("  Consolidating...");
                int k = v;
                int l = 0;
                while(k!=treeroot && l<L) {
                    if (DEBUG>=2) System.out.println("  edge: (" + k + "," + p[k] + ") with b[" + k + "]=" + b[k]);
                    if (b[k]>0) A[b[k]] = true;
                    b[k] = v;
                    if (DEBUG>=2) System.out.println("  (" + k + "," + p[k] + ") = " + b[k]);
                    k = p[k];
                    l++;
                }
                for(int t = 1;t<=n;t++) {
                    if (b[t]>0 && A[b[t]]) {
                        b[t] = v;
                        if (DEBUG>=2) System.out.println("  (" + t + "," + p[t] + ") = " + b[t]);
                    }
                }
            }
            U.addAll(toAddInU);
            X.add(v);
            if (DEBUG>=2) System.out.println("  A: " + Arrays.toString(A));
            if (DEBUG>=2) System.out.println("  b: " + Arrays.toString(b));
        }
        
        // Rename the blocks:
        HashMap<Integer,Integer> blockRenaming = new HashMap<>();
        List<Integer> trivialBlocks = new LinkedList<Integer>();
        int nextBlock = 1;
        for(int i = 1;i<=n;i++) {
            if (b[i]==0) {
                // ignore
            } else if (b[i]==-i) {
                trivialBlocks.add(i);
            } else {
                if (blockRenaming.containsKey(b[i])) {
                    b[i] = blockRenaming.get(b[i]);
                } else {
                    blockRenaming.put(b[i],nextBlock);
                    b[i] = nextBlock;
                    nextBlock++;
                }
            }
        }
        for(Integer i:trivialBlocks) {
            b[i] = nextBlock++;
            // System.out.println("trivial block!!!");
        }
        
        // translate the blocks and cutedges:
        HashMap<Integer,List<Integer>> blocks = new HashMap<>();
        HashMap<Integer,List<Integer>> cutNodes = new HashMap<>();
        for(int i = 1;i<=n;i++) {
            List<Integer> nodeBlocks = new LinkedList<>();
            //nodeBlocks.add(b[i]);
            for(int j = 1;j<=n;j++) {
                if (p[j] == i) {
                    if (!nodeBlocks.contains(b[j])) nodeBlocks.add(b[j]);
                }
            }
            if (p[i]>0) {
                if (!nodeBlocks.contains(b[i])) nodeBlocks.add(b[i]);
            }
            if (DEBUG>=2) System.out.println(i + ": " + nodeBlocks.toString());
            
            for(int blockID:nodeBlocks) {
                List<Integer> block = blocks.get(blockID);
                if (block==null) {
                    block = new LinkedList<>();
                    blocks.put(blockID,block);
                }
                block.add(i-1);
            }
            if (nodeBlocks.size()>1) {
                cutNodes.put(i-1, nodeBlocks);
            }            
        }
        
        
        return new Pair<>(blocks,cutNodes);
    }
}
