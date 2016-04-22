/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package orthographicembedding;

/**
 *
 * @author santi
 */
public class OEElement {

    /*
        // counter clock wise:
        public static final int RIGHT = 0;
        public static final int UP = 1;
        public static final int LEFT = 2;
        public static final int DOWN = 3;
     */
    // clock wise (in mathematical coordinates, in which y is inverted wrt to screen coordinates):
    public static final String directionNames[] = {"right", "down", "left", "up"};
    public static final int RIGHT = 0;
    public static final int DOWN = 1;
    public static final int LEFT = 2;
    public static final int UP = 3;

    public int v;           // the origin node
    public int dest;        // the target node
    public int angle;       // 0 : right, 1: up, 2: left, 3: down
    public int bends;       // number of left bends
    public OEElement sym;   // the symmetric element in the target node

    public int bendsToAddToSymmetric;

    public OEElement(int a_v, int a_dest, int a_angle, int a_bends) {
        v = a_v;
        dest = a_dest;
        angle = a_angle;
        bends = a_bends;
        sym = null;

        bendsToAddToSymmetric = 0;
    }

    public OEElement(int a_v, int a_dest, int a_angle, int a_bends, OEElement a_sym) {
        v = a_v;
        dest = a_dest;
        angle = a_angle;
        bends = a_bends;
        sym = a_sym;

        bendsToAddToSymmetric = 0;
    }

    public String toString() {
        String angles[] = {"right", "up", "left", "down"};
        return "(" + v + " -> " + dest + "," + angles[angle] + "," + bends + ")";
    }
}
