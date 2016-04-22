/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package orthographicembedding;

import java.util.List;

/**
 *
 * @author santi
 */
public class OEVertex {

    public List<OEElement> embedding;
    public int v;
    public double x, y;

    public OEVertex(List<OEElement> a_embedding, int a_v, double a_x, double a_y) {
        embedding = a_embedding;
        v = a_v;
        x = a_x;
        y = a_y;
    }

    public String toString() {
        String tmp = "v(" + x + "," + y + "):";
        for (OEElement e : embedding) {
            tmp += " " + e;
        }
        return tmp;
    }
}
