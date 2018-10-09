/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package orthographicembedding;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author santi
 */
public class PQTree {
    public static int DEBUG = 0;
    
    public static int P_NODE = 0;
    public static int Q_NODE = 1;
    public static int LEAF_NODE = 2;    
    public static int DIRECTION_INDICATOR = 3;
    
    public static int DIRECTION_INDICATOR_LEFT = 1;
    public static int DIRECTION_INDICATOR_RIGHT = 2;
    
    public static final int LABEL_NONE = -1;
    public static final int LABEL_EMPTY = 0;
    public static final int LABEL_PARTIAL = 1;
    public static final int LABEL_FULL = 2;
    
    public static final int NO_INDEX = -1;

    public int nodeIndex;   // if the nodes int he PQ-tree represent nodes in another graph,
                            // this is used to store their indexes
    public int nodeType;
    public int direction;   // left/right
    public int label;   // empty, partial, full
    public List<PQTree> children;
    public PQTree parent;

    public PQTree(int a_index, int a_type, PQTree a_parent) {
        nodeIndex = a_index;
        nodeType = a_type;
        if (nodeType==LEAF_NODE) {
            children = null;
        } else {
            children = new LinkedList<PQTree>();
        }
        parent = a_parent;
        if (parent!=null) parent.children.add(this);
        label = LABEL_NONE;
    }
    
    
    public void clearLabels() {
        label = LABEL_NONE;
        if (children!=null) {
            for(PQTree node:children) node.clearLabels();
        }
    }
    
    public List<PQTree> allNodes() {
        List<PQTree> l = new LinkedList<PQTree>();
        allNodes(l);
        return l;
    }
    
    private void allNodes(List<PQTree> l) {
        l.add(this);
        if (children!=null) {
            for(PQTree child:children) {
                child.allNodes(l);
            }
        }
    }
    
    
    public void getLeaves(List<PQTree> l) {
        if (children==null || children.isEmpty()) {
            l.add(this);
        } else {
            for(PQTree child:children) {
                child.getLeaves(l);
            }
        }
    }
    
        
    public boolean isFullLeaf(List<PQTree> S) {
        if (S.contains(this)) return true;
        return false;
    }
    
    
    public boolean isDescendant(PQTree ancestor) {
        if (ancestor==this) return true;
        if (parent==null) return false;
        return parent.isDescendant(ancestor);
    }

    
    public boolean isPertinentTree(List<PQTree> S) {
        for(PQTree node:S) {
            if (!node.isDescendant(this)) return false;
        }        
        return true;
    }
    
    
    public boolean applyTemplate(List<PQTree> S) throws Exception {
        // Node is full if: "all of its descendants are in S"
        // Node is empty if: "none of its descendants are in S"
        // Node is partial if: "some but not all of its descendants are in S"
        
        if (nodeType==DIRECTION_INDICATOR) {
            label = LABEL_FULL;
            return true;
        } else if (nodeType==LEAF_NODE) {
            // Trivial templates:
            if (isFullLeaf(S)) {
                label = LABEL_FULL;
                return true;
            } else {
                label = LABEL_EMPTY;
                return true;                
            }
        } else if (nodeType==P_NODE) {
            int counts[] = {0,0,0};
            for(PQTree child:children) {
                if (child.nodeType!=DIRECTION_INDICATOR) counts[child.label]++;
            }

            // Template P0: (Figure 7, left)
            if (counts[LABEL_EMPTY]>0 &&
                counts[LABEL_PARTIAL]==0 &&
                counts[LABEL_FULL]==0) {
                label = LABEL_EMPTY;
                if (DEBUG>=1) System.out.println("P0");
                return true;
            }
            // Template P1: (Figure 7, right)
            if (counts[LABEL_EMPTY]==0 &&
                counts[LABEL_PARTIAL]==0 &&
                counts[LABEL_FULL]>0) {
                label = LABEL_FULL;
                if (DEBUG>=1) System.out.println("P1");
                return true;
            }
            
            if (isPertinentTree(S)) {     
                // Template P2: (Figure 8)
                if (counts[LABEL_EMPTY]>0 &&
                    counts[LABEL_PARTIAL]==0 &&
                    counts[LABEL_FULL]>0) {
                    if (DEBUG>=1) System.out.println("P2");
                    PQTree P1 = new PQTree(NO_INDEX,P_NODE,null);
                    P1.label = LABEL_FULL;
                    for(PQTree child:children) {
                        if (child.label==LABEL_FULL) {
                            P1.children.add(child);
                            child.parent = P1;
                        }
                    }
                    children.removeAll(P1.children);
                    children.add(P1);
                    P1.parent = this;
                    return true;
                }
                
                // Template P4: (Figure 11)
                if (counts[LABEL_EMPTY]>=0 &&
                    counts[LABEL_PARTIAL]==1 &&
                    counts[LABEL_FULL]>=0) {
                    if (DEBUG>=1) System.out.println("P4");
                    PQTree partialChild = null;
                    for(PQTree child:children) {
                        if (child.label==LABEL_PARTIAL) {
                            partialChild = child;
                            break;
                        }
                    }
                    PQTree P1 = new PQTree(NO_INDEX,P_NODE,null);
                    P1.label = LABEL_FULL;
                    for(PQTree child:children) {
                        if (child.label==LABEL_FULL) {
                            child.parent = P1;
                            P1.children.add(child);
                            child.parent = P1;
                        }
                    }
                    children.removeAll(P1.children);
                    if (P1.children.size()==1) {
                        P1.children.get(0).parent = partialChild;
                        if (partialChild.fromEmptyToFullP()) {
//                        if (partialChild.firstChildEmptyP()) {
//                        if (partialChild.children.get(0).label==LABEL_EMPTY) {
                            partialChild.children.add(P1.children.get(0));
                            P1.children.get(0).parent = partialChild;
                        } else if (partialChild.fromFullToEmptyP()) {
                            partialChild.children.add(0,P1.children.get(0));
                            P1.children.get(0).parent = partialChild;
                        } else {
                            return false;
                        }
                    } else {
                        P1.parent = partialChild;
                        if (partialChild.fromEmptyToFullP()) {
//                        if (partialChild.firstChildEmptyP()) {
//                        if (partialChild.children.get(0).label==LABEL_EMPTY) {
                            partialChild.children.add(P1);
                            P1.parent = partialChild;
                        } else if (partialChild.fromFullToEmptyP()) {
                            partialChild.children.add(0,P1);
                            P1.parent = partialChild;
                        } else {
                            return false;
                        }
                    }
                    return true;
                }

                // Template P6: (Figure 13)
                if (counts[LABEL_EMPTY]>=0 &&
                    counts[LABEL_PARTIAL]==2 &&
                    counts[LABEL_FULL]>=0) {
                    if (DEBUG>=1) System.out.println("P6");
                    PQTree partial1 = null;
                    PQTree partial2 = null;
                    PQTree P1 = new PQTree(NO_INDEX,P_NODE,null);
                    PQTree Q1 = new PQTree(NO_INDEX,Q_NODE,null);
                    P1.label = LABEL_FULL;
                    Q1.label = LABEL_PARTIAL;
                    for(PQTree child:children) {
                        if (child.label==LABEL_PARTIAL) {
                            if (partial1==null) {
                                partial1 = child;
                            } else {
                                partial2 = child;
                            }
                        }
                        if (child.label==LABEL_FULL) {
                            P1.children.add(child);
                            child.parent = P1;
                        }
                    }
                    if (partial1.fromEmptyToFullP()) {                    
//                    if (partial1.firstChildEmptyP()) {                    
//                    if (partial1.children.get(0).label==LABEL_EMPTY) {
                        // they are in the right order:
                        for(PQTree child2:partial1.children) {
                            Q1.children.add(child2);
                            child2.parent = Q1;
                        }
                    } else if (partial1.fromFullToEmptyP()) {
                        // we need to reverse them:
                        for(int i = partial1.children.size()-1;i>=0;i--) {
                            partial1.children.get(i).reverse();
                            Q1.children.add(partial1.children.get(i));
                            partial1.children.get(i).parent = Q1;
                        }
                    } else {
                        return false;
                    }
                    if (!P1.children.isEmpty()) {
                        if (P1.children.size()==1) {
                            Q1.children.add(P1.children.get(0));
                            P1.children.get(0).parent = Q1;
                        } else {
                            Q1.children.add(P1);
                            P1.parent = Q1;
                        }
                    }
                    if (partial2.fromFullToEmptyP()) {
//                    if (partial2.firstChildFullP()) {
//                    if (partial2.children.get(0).label==LABEL_FULL) {
                        // they are in the right order:
                        for(PQTree child2:partial2.children) {
                            Q1.children.add(child2);
                            child2.parent = Q1;
                        }
                    } else if (partial2.fromEmptyToFullP()) {
                        // we need to reverse them:
                        for(int i = partial2.children.size()-1;i>=0;i--) {
                            partial2.children.get(i).reverse();
                            Q1.children.add(partial2.children.get(i));
                            partial2.children.get(i).parent = Q1;
                        }
                    } else {
                        return false;
                    }
                    children.removeAll(P1.children);
                    children.remove(partial1);
                    children.remove(partial2);
                    children.add(Q1);
                    Q1.parent = this;
                    return true;
                }
            } else {
                // Template P3:  (Figures 9, 10)
                if (counts[LABEL_EMPTY]>0 &&
                    counts[LABEL_PARTIAL]==0 &&
                    counts[LABEL_FULL]>0) {
                    if (DEBUG>=1) System.out.println("P3");
                    if (counts[LABEL_EMPTY]>1) {
                        if (counts[LABEL_FULL]>1) {
                            PQTree Pnode1 = new PQTree(NO_INDEX,P_NODE,null);
                            PQTree Pnode2 = new PQTree(NO_INDEX,P_NODE,null);
                            Pnode1.label = LABEL_EMPTY;
                            Pnode2.label = LABEL_FULL;
                            for(PQTree child:children) {
                                if (child.label==LABEL_EMPTY) {
                                    Pnode1.children.add(child);
                                    child.parent = Pnode1;
                                } else {
                                    Pnode2.children.add(child);
                                    child.parent = Pnode1;
                                }
                            }
                            children.clear();
                            children.add(Pnode1);
                            children.add(Pnode2);
                            Pnode1.parent = this;
                            Pnode2.parent = this;
                            nodeType = Q_NODE;
                            label = LABEL_PARTIAL;
                        } else {
                            PQTree Pnode1 = new PQTree(NO_INDEX,P_NODE,null);
                            Pnode1.label = LABEL_EMPTY;
                            for(PQTree child:children) {
                                if (child.label==LABEL_EMPTY) {
                                    Pnode1.children.add(child);
                                    child.parent = Pnode1;
                                }
                            }
                            children.removeAll(Pnode1.children);
                            children.add(Pnode1); 
                            Pnode1.parent = this;
                            nodeType = Q_NODE;
                            label = LABEL_PARTIAL;
                        }
                    } else {
                        if (counts[LABEL_FULL]>1) {
                            PQTree Pnode2 = new PQTree(NO_INDEX,P_NODE,null);
                            Pnode2.label = LABEL_FULL;
                            for(PQTree child:children) {
                                if (child.label==LABEL_FULL) {
                                    Pnode2.children.add(child);
                                    child.parent = Pnode2;
                                }
                            }
                            children.removeAll(Pnode2.children);
                            children.add(Pnode2);
                            Pnode2.parent = this;
                            nodeType = Q_NODE;
                            label = LABEL_PARTIAL;
                        } else {  
                            nodeType = Q_NODE;
                            label = LABEL_PARTIAL;
                        }
                    }
                    return true;
                }
                
                // Template P5: (Figure 12)
                if (counts[LABEL_EMPTY]>=0 &&
                    counts[LABEL_PARTIAL]==1 &&
                    counts[LABEL_FULL]>=0) {
                    if (DEBUG>=1) System.out.println("P5");
                    PQTree P1 = new PQTree(NO_INDEX,P_NODE,null);
                    PQTree P2 = new PQTree(NO_INDEX,P_NODE,null);
                    P1.label = LABEL_EMPTY;
                    P2.label = LABEL_FULL;
                    PQTree Q = null;
                    for(PQTree child:children) {
                        if (child.label==LABEL_EMPTY) {
                            P1.children.add(child);
                            child.parent=P1;
                        }
                        if (child.label==LABEL_FULL) {
                            P2.children.add(child);
                            child.parent=P2;
                        }
                        if (child.label==LABEL_PARTIAL) Q = child;
                    }
                    children.clear();
                    if (!P1.children.isEmpty()) {
                        if (P1.children.size()==1) {
                            children.add(P1.children.get(0));
                            P1.children.get(0).parent = this;
                        } else {
                            children.add(P1);
                            P1.parent = this;
                        }
                    }
                    if (Q.fromEmptyToFullP()) {
//                    if (Q.firstChildEmptyP()) {
//                    if (Q.children.get(0).label==LABEL_EMPTY) {
                        // they are in the right order:
                        for(PQTree child2:Q.children) {
                            children.add(child2);
                            child2.parent = this;
                        }
                    } else if (Q.fromEmptyToFullP()) {
                        // we need to reverse them:
                        for(int i = Q.children.size()-1;i>=0;i--) {
                            PQTree tmp = Q.children.get(i);
                            children.add(tmp);
                            tmp.parent = this;
                            tmp.reverse();
                        }
                    } else {
                        return false;
                    }
                    if (!P2.children.isEmpty()) {
                        if (P2.children.size()==1) {
                            children.add(P2.children.get(0));
                            P2.children.get(0).parent = this;
                        } else {
                            children.add(P2);
                            P2.parent = this;
                        }
                    }
                    nodeType = Q_NODE;
                    label = LABEL_PARTIAL;
                    return true;
                }                
            }
        } else if (nodeType==Q_NODE) {
            int counts[] = {0,0,0};
            for(PQTree child:children) {
                if (child.nodeType!=DIRECTION_INDICATOR) counts[child.label]++;
            }

            // Template Q0: 
            if (counts[LABEL_EMPTY]>0 &&
                counts[LABEL_PARTIAL]==0 &&
                counts[LABEL_FULL]==0) {
                if (DEBUG>=1) System.out.println("Q0");
                label = LABEL_EMPTY;
                return true;
            }
            // Template Q1: 
            if (counts[LABEL_EMPTY]==0 &&
                counts[LABEL_PARTIAL]==0 &&
                counts[LABEL_FULL]>0) {
                if (DEBUG>=1) System.out.println("Q1");
                label = LABEL_FULL;
                return true;
            }
            
            // (Santi: Actually pattern Q2 is a special case of Q3, so I'll implement them together)            
            // Template Q2: (Figure 14)
            // If all the full leaves are either on the right or on the left
            // Template Q3: (Figure 15):
            // If all the full leaves ate groupped together in the middle, and empty ones are on the right AND left
            if (counts[LABEL_PARTIAL]<=2) {
                if (DEBUG>=1) System.out.println("Q2/Q3");
                if (counts[LABEL_PARTIAL]==0 && (counts[LABEL_EMPTY]>0 || counts[LABEL_FULL]>0)) {
                    if (DEBUG>=1) System.out.println("Q2/Q3: 0 partials");
                    // check whether the chilren are in the right order
                    boolean goodEF = true;  // Empty -> Full order
                    boolean goodFE = true;  // Full -> Empty order
                    int previous = -1;
                    for(int i = 0;i<children.size();i++) {
                        if (children.get(i).nodeType==DIRECTION_INDICATOR) {
                            continue;
                        }
                        if (previous!=-1) {                            
                            if (children.get(previous).label==LABEL_FULL &&
                                children.get(i).label==LABEL_EMPTY) {
                                goodEF = false;
                            }
                            if (children.get(previous).label==LABEL_EMPTY &&
                                children.get(i).label==LABEL_FULL) {
                                goodFE = false;
                            }
                        }
                        previous = i;
                    }
                    if (goodEF || goodFE) {
                        label = LABEL_PARTIAL;
                        return true;
                    }
                    return false;
                } else if (counts[LABEL_PARTIAL]==1 && (counts[LABEL_EMPTY]>0 || counts[LABEL_FULL]>0)) {
                    if (DEBUG>=1) System.out.println("Q2/Q3: 1 partials");
                    // 1 partial Q-node:
                    List<PQTree> newChildrenEF = new LinkedList<PQTree>();
                    List<PQTree> newChildrenFE = new LinkedList<PQTree>();
                    List<PQTree> reverseIfEF = new LinkedList<PQTree>();
                    List<PQTree> reverseIfFE = new LinkedList<PQTree>();
                    int EFstate = 0;  // 0 : empty, 1: full
                    int FEstate = 0;  // 0 : full, 1: empty
                    for(PQTree child:children) {
                        if (child.nodeType==DIRECTION_INDICATOR) {
                            newChildrenEF.add(child);
                            newChildrenFE.add(child);
                            continue;
                        }
                        if (EFstate==0) {
                            if (child.label==LABEL_EMPTY) {
                                newChildrenEF.add(child);
                            } else if (child.label==LABEL_FULL) {
                                newChildrenEF.add(child);
                                EFstate = 1;
                            } else if (child.label==LABEL_PARTIAL) {
                                if (child.fromEmptyToFullP()) {
//                                if (child.firstChildEmptyP()) {
//                                if (child.children.get(0).label==LABEL_EMPTY) {
                                    for(PQTree child2:child.children) {
                                        newChildrenEF.add(child2);
                                    }
                                } else if (child.fromFullToEmptyP()) {
                                    for(int i = child.children.size()-1;i>=0;i--) {
                                        newChildrenEF.add(child.children.get(i));
                                        reverseIfEF.add(child.children.get(i));
                                    }
                                } else {
                                    return false;
                                }
                                EFstate = 1;
                            }
                        } else if (EFstate==1) {
                            if (child.label==LABEL_EMPTY) {
                                EFstate = 2;
                            } else if (child.label==LABEL_FULL) {
                                newChildrenEF.add(child);
                            } else if (child.label==LABEL_PARTIAL) {
                                EFstate = 2;
                            }
                        }
                        if (FEstate==0) {
                            if (child.label==LABEL_FULL) {
                                newChildrenFE.add(child);
                            } else if (child.label==LABEL_EMPTY) {
                                newChildrenFE.add(child);
                                FEstate = 1;
                            } else if (child.label==LABEL_PARTIAL) {
                                if (child.fromFullToEmptyP()) {
//                                if (child.firstChildFullP()) {
//                                if (child.children.get(0).label==LABEL_FULL) {
                                    for(PQTree child2:child.children) {
                                        newChildrenFE.add(child2);
                                    }
                                } else if (child.fromEmptyToFullP()) {
                                    for(int i = child.children.size()-1;i>=0;i--) {
                                        newChildrenFE.add(child.children.get(i));
                                        reverseIfFE.add(child.children.get(i));
                                    }
                                } else {
                                    return false;
                                }
                                FEstate = 1;
                            }
                        } else if (FEstate==1) {
                            if (child.label==LABEL_FULL) {
                                FEstate = 2;
                            } else if (child.label==LABEL_EMPTY) {
                                newChildrenFE.add(child);
                            } else if (child.label==LABEL_PARTIAL) {
                                FEstate = 2;
                            }
                        }
                    }
                    if (EFstate==1) {
                        children.clear();
                        label = LABEL_PARTIAL;
                        for(PQTree child:newChildrenEF) {
                            children.add(child);
                            child.parent = this;
                        }
                        for(PQTree child:reverseIfEF) child.reverse();
                        return true;
                    } else if (FEstate==1) {
                        children.clear();
                        label = LABEL_PARTIAL;
                        for(PQTree child:newChildrenFE) {
                            children.add(child);
                            child.parent = this;
                        }
                        for(PQTree child:reverseIfFE) child.reverse();
                        return true;
                    }
                    return false;
                } else {
                    // 2 partial Q-nodes:
                    if (DEBUG>=1) System.out.println("Q2/Q3: 2 partials");
                    List<PQTree> newChildren = new LinkedList<PQTree>();
                    int status = 0; // 0 = empty, 1 = full, 2 = empty again, 3: error
                    for(PQTree child:children) {
                        if (child.nodeType == DIRECTION_INDICATOR) {
                            newChildren.add(child);
                            continue;
                        }
                        switch(status) {
                        case 0:
                            switch(child.label) {
                            case LABEL_EMPTY: 
                                newChildren.add(child);
                                break;   
                            case LABEL_PARTIAL:
                                if (child.fromEmptyToFullP()) {
//                                if (child.firstChildEmptyP()) {                                
//                                if (child.children.get(0).label==LABEL_EMPTY) {
                                    // thye are in the right order:
                                    for(PQTree child2:child.children) {
                                        newChildren.add(child2);
                                    }
                                } else if (child.fromFullToEmptyP()) {
                                    // we need to reverse them:
                                    for(int i = child.children.size()-1;i>=0;i--) {
                                        newChildren.add(child.children.get(i));
                                        child.children.get(i).reverse();
                                    }
                                } else {
                                    return false;
                                }
                                status = 1;
                                break;
                            case LABEL_FULL:
                                newChildren.add(child);
                                status = 1;
                                break;
                            }
                            break;                            
                        case 1:
                            switch(child.label) {
                            case LABEL_EMPTY: 
                                newChildren.add(child);
                                status = 2;
                                break;
                            case LABEL_PARTIAL:
                                if (child.fromFullToEmptyP()) {
//                                if (child.firstChildFullP()) {
//                                if (child.children.get(0).label==LABEL_FULL) {
                                    // thye are in the right order:
                                    for(PQTree child2:child.children) {
                                        newChildren.add(child2);
                                    }
                                } else if (child.fromFullToEmptyP()) {
                                    // we need to reverse them:
                                    for(int i = child.children.size()-1;i>=0;i--) {
                                        newChildren.add(child.children.get(i));
                                        child.children.get(i).reverse();
                                    }
                                } else {
                                    return false;
                                }
                                status = 2;
                                break;
                            case LABEL_FULL: 
                                newChildren.add(child);
                                break;
                            }
                            break;
                        case 2:
                            switch(child.label) {
                            case LABEL_EMPTY: 
                                newChildren.add(child);
                                break;   
                            case LABEL_PARTIAL: return false;
                            case LABEL_FULL: return false;
                            }
                            break;
                        }
                    }
                    children.clear();
                    for(PQTree child:newChildren) {
                        children.add(child);
                        child.parent = this;
                    }
                    label = LABEL_PARTIAL;
                    return true;
                }
            }
                        
        }
        
        return false;
    }
    
    public boolean reduce(int index) throws Exception {
        List<PQTree> tmp = new LinkedList<PQTree>();
        List<PQTree> S = new LinkedList<PQTree>();
        getLeaves(tmp);
        for(PQTree leaf:tmp) if (leaf.nodeIndex==index) S.add(leaf);
        
        return reduce(S);
    }
    
    public boolean reduce(List<PQTree> S) throws Exception {
        Set<PQTree> processed = new LinkedHashSet<PQTree>();
        List<PQTree> queue = new LinkedList<PQTree>();
                        
        // add all the leaves of the tree to the queue
        getLeaves(queue);
        
        if (DEBUG>=2) System.out.println("PQTree.reduce: T = " + this);
        if (DEBUG>=2) System.out.println("PQTree.reduce: S = " + S);        
        
        clearLabels();

        while(!queue.isEmpty()) {
            PQTree X = queue.remove(0);
            if (DEBUG>=2) System.out.println("PQTree.reduce: processing " + X);
            if (!X.applyTemplate(S)) {
                if (DEBUG>=2) System.out.println("PQTree.reduce: no pattern mattched! FAILURE!");  
                return false;
            }
            processed.add(X);
            if (DEBUG>=2) System.out.println("PQTree.reduce: processed " + X);
            if (DEBUG>=1) sanityTest();
            
            // If all the nodes in "S" are in node descendants of X, then we are done:
            if (X.isPertinentTree(S)) {
                if (DEBUG>=2) System.out.println("PQTree.reduce: the last node contained all of S, SUCCESS!");  
                return true;
            }
            
            // If all the siblings of X have been matched, then place the parent in the queue:
            if (X.parent!=null) {
                if (processed.containsAll(X.parent.children)) queue.add(X.parent);
            }
        }
        
        return true;
    }
    
    
    public boolean fromEmptyToFullP() {
        int state = 0;
        for(PQTree child:children) {
            if (child.nodeType==DIRECTION_INDICATOR) continue;
            switch(state) {
            case 0:
                if (child.label == LABEL_FULL) state = 1;
                else if (child.label == LABEL_PARTIAL) {
                    if (!child.fromEmptyToFullP()) return false;
                }
                break;
            case 1:
                if (child.label != LABEL_FULL) return false;
                break;
            }
        }
        return true;
    }

    
    public boolean fromFullToEmptyP() {
        int state = 0;
        for(PQTree child:children) {
            if (child.nodeType==DIRECTION_INDICATOR) continue;
            switch(state) {
            case 0:
                if (child.label == LABEL_EMPTY) state = 1;
                else if (child.label == LABEL_PARTIAL) {
                    if (!child.fromFullToEmptyP()) return false;
                }
                break;
            case 1:
                if (child.label != LABEL_EMPTY) return false;
                break;
            }
        }
        return true;
    }
    
    public void reverse() {
        if (nodeType==Q_NODE) {
            List<PQTree> tmp = new LinkedList<PQTree>();
            tmp.addAll(children);
            children.clear();
            
            for(PQTree child:tmp) {
                child.reverse();
                children.add(0,child);
            }
            
        } else if (nodeType==DIRECTION_INDICATOR) {
            if (nodeType==DIRECTION_INDICATOR) {
                if (DEBUG>=1) System.out.println("Reversing direction indicator: " + this);
                if (direction==DIRECTION_INDICATOR_LEFT) {
                    direction = DIRECTION_INDICATOR_RIGHT;
                } else {
                    direction = DIRECTION_INDICATOR_LEFT;

                }
            }
        }
    }
    
    
    /*
    
    public boolean firstChildEmptyP() {
        for(PQTree child:children) {
            if (child.nodeType==DIRECTION_INDICATOR) continue;
            if (child.label == LABEL_EMPTY) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }
    
    
    public boolean firstChildFullP() {
        for(PQTree child:children) {
            if (child.nodeType==DIRECTION_INDICATOR) continue;
            if (child.label == LABEL_FULL) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    */
    
    public boolean recursivelyRemoveLeaf(PQTree leaf) {
        if (children!=null) {
            if (children.remove(leaf)) return true;
            for(PQTree child:children) {
                if (child.recursivelyRemoveLeaf(leaf)) return true;
            }
        }
        return false;
    }
    
    
    public boolean recursivelyRemoveLeafNotThroughQNodes(PQTree leaf) {
        if (children!=null) {
            if (children.remove(leaf)) return true;
            for(PQTree child:children) {
                if (child.nodeType!=PQTree.Q_NODE &&
                    child.recursivelyRemoveLeafNotThroughQNodes(leaf)) return true;
            }
        }
        return false;
    }    
    
    public boolean contains(PQTree node) {
        if (this==node) return true;
        if (children!=null) {
            for(PQTree child:children) {
                if (child.contains(node)) return true;
            }
        }
        return false;
    }    

    
    
    public boolean sanityTest() {
        List<PQTree> nodes = allNodes();
        for(PQTree node:nodes) {
            if (node.parent!=null) {
                if (!node.parent.children.contains(node)) {
                    System.out.println("sanityTest: Inconsistent parent in node: " + node);
                    return false;
                } 
                if (node.children!=null) {
                    for(PQTree node2:node.children) {
                        if (node2.parent!=node) {
                            System.out.println("sanityTest: Inconsistent parent in node: " + node2);
                            return false;
                        }
                    }
                }
            }
        }
        System.out.println("sanityTest: passed");
        return true;
    }
        
    
    public String toString() {
        return toString(0, null);
    }
    
    public String toString(LinkedHashMap<PQTree,Integer> nodeParent) {
        return toString(0,nodeParent);
    }
    
    String indentString(int indents) {
        String out = "";
        for(int i = 0;i<indents;i++) out+="  ";
        return out;
    }
    
    
    public String toString(int indents, LinkedHashMap<PQTree,Integer> nodeParent) {
        if (nodeType==DIRECTION_INDICATOR) {
            if (direction==DIRECTION_INDICATOR_LEFT) return indentString(indents) + "direction-indicator-left(" + nodeIndex + ")";
            if (direction==DIRECTION_INDICATOR_RIGHT) return indentString(indents) + "direction-indicator-right(" + nodeIndex + ")";
            return indentString(indents) + "direction-indicator-???(" + nodeIndex + ")";
        } 
        else if (nodeType==LEAF_NODE) {
            if (nodeParent!=null) {
                if (label==LABEL_FULL) return indentString(indents) + "full-leaf(" + nodeIndex + " from " + nodeParent.get(this) + ")";
                if (label==LABEL_EMPTY) return indentString(indents) + "empty-leaf(" + nodeIndex + " from " + nodeParent.get(this) + ")";
                return indentString(indents) + "leaf(" + nodeIndex + " from " + nodeParent.get(this) + ")";
            } else {
                if (label==LABEL_FULL) return indentString(indents) + "full-leaf(" + nodeIndex + ")";
                if (label==LABEL_EMPTY) return indentString(indents) + "empty-leaf(" + nodeIndex + ")";
                return indentString(indents) + "leaf(" + nodeIndex + ")";
            }
        }
        else if (nodeType==P_NODE) {
            String tmp = indentString(indents) + "P-node(" + nodeIndex + ")(\n";
            if (label==LABEL_FULL) tmp = indentString(indents) + "full-P-node(" + nodeIndex + ")(\n";
            if (label==LABEL_PARTIAL) tmp = indentString(indents) + "partial-P-node(" + nodeIndex + ")(\n";
            if (label==LABEL_EMPTY) tmp = indentString(indents) + "empty-P-node(" + nodeIndex + ")(\n";
            boolean first = true;
            for(PQTree child:children) {
                if (!first) tmp+=",\n";
                tmp += child.toString(indents+1, nodeParent);
                first = false;
            }
            return tmp + ")";
        } else {
            String tmp = indentString(indents) + "Q-Node(\n";
            if (label==LABEL_FULL) tmp = indentString(indents) + "full-Q-node(" + nodeIndex + ")(\n";
            if (label==LABEL_PARTIAL) tmp = indentString(indents) + "partial-Q-node(" + nodeIndex + ")(\n";
            if (label==LABEL_EMPTY) tmp = indentString(indents) + "empty-Q-node(" + nodeIndex + ")(\n";
            boolean first = true;
            for(PQTree child:children) {
                if (!first) tmp+=",\n";
                tmp += child.toString(indents+1, nodeParent);
                first = false;
            }
            return tmp + ")";
        }
    }

}
