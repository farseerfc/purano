package jp.ac.osakau.farseerfc.purano.test;

public class TargetC {
    private int member;

    @Override
    public boolean equals(Object o) {
        Integer mi = new Integer(12);
        member = mi.intValue();
        return false;
    }


    void setM(int v) {
        member = v;
    }
}
