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

    public static void setS(int i) {
        s = i;
    }

    @Override
    public void change() {
        setS(staticAdd());
        a.modifyM();
    }

    public void changeArg(Base[] a) {
        a[0].change();
    }

    public int thisAdd(int a) {
        return a + c;
    }

    public int staticAdd(){
        return s + c;
    }

}
