package jp.ac.osakau.farseerfc.purano.test;

/**
 * Created with IntelliJ IDEA.
 * User: farseerfc
 * Date: 6/20/13
 * Time: 10:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class B extends Base {
    private A a;

    private static int s;

    private int c;

    public void setC(int c) {
        this.c = c;
    }

    @Override
    public void change() {
        a.modifyM();
    }

    public void indirect(int a) {
        setC(a);
    }

    public void changeArg(Base[] a) {
        a[0].change();
    }

    public int thisAdd(int a) {
        return a + c;
    }

}
