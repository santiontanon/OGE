/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.util.List;
import java.util.ArrayList;

/**
 *
 * @author santi
 */
public class Permutations {
    public static List allPermutations(List l) {
        return allPermutations(l, 0);
    }

    public static List allPermutations(List l, int pos) {
        if (pos == l.size()-1) {
            List permutations = new ArrayList();
            List permutation = new ArrayList();
            permutation.add(l.get(pos));
            permutations.add(permutation);
            return permutations;
        } else {
            List permutations = new ArrayList();
            List basePermutations = allPermutations(l, pos+1);
            for(Object tmp:basePermutations) {
                List basePermutation = (List)tmp;
                for(int i = 0;i<basePermutation.size()+1;i++) {
                    List perm = new ArrayList();
                    perm.addAll(basePermutation);
                    perm.add(i, l.get(pos));
                    permutations.add(perm);
                }
            }
            return permutations;
        }
    }

    
    public static void main(String args[]) {
        List<Integer> l = new ArrayList();
        l.add(1);
        l.add(2);
        l.add(3);
        l.add(4);
        List<List<Integer>> permutations = allPermutations(l);
        for(List<Integer> permutation:permutations) {
            System.out.println(permutation);
        }
    }
}
