package jp.ac.osakau.farseerfc.purano.test;

/**
 * Created by farseerfc on 1/27/14.
 */
import java.util.*;

class Tree implements Comparable<Tree> {
    int freq(){return 0;}
    public int compareTo(Tree tree) {
        return freq() - tree.freq();
    }
}
class Leaf extends Tree {
    char v; int f;
    void newLeaf(int freq, char val) {
        f = freq;
        v = val;
    }
    int freq() { return f; }
}
class Node extends Tree {
    Tree l, r;
    void newNode(Tree le, Tree ri) {
        l = le;
        r = ri;
    }
    int freq() {return l.freq() + r.freq();}
}
public class C {
    public static Tree build(int[] chrs) {
        PriorityQueue<Tree> trees = new PriorityQueue<Tree>();
        int i = 0;
        while (i < chrs.length){
            if (chrs[i] > 0){
                Leaf l = new Leaf();
                l.newLeaf(chrs[i], (char)i);
                trees.offer(l);
            }
            i++;
        }
        while (trees.size() > 1) {
            Node n=new Node();
            n.newNode(trees.poll(),trees.poll());
            trees.offer(n);
        }
        return trees.poll();
    }
    public static void print(Tree t, StringBuffer p) {
        if (t instanceof Leaf) {
            Leaf leaf = (Leaf)t;
            System.out.println(leaf.v + "\t" + leaf.f + "\t" + p);
        } else if (t instanceof Node) {
            Node n = (Node)t;
            p.append('0');
            print(n.l, p);
            p.deleteCharAt(p.length() - 1);
            p.append('1');
            print(n.r, p);
            p.deleteCharAt(p.length() - 1);
        }
    }
    public static void main(String[] args) {
        String test = "this is an example for huffman encoding";
        int[] chrs = new int[256];
        for (char c : test.toCharArray()) chrs[c]++;
        Tree tree = build(chrs);
        print(tree, new StringBuffer());
    }
}