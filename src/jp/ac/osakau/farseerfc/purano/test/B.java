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

    @Override
    public void change(){
        a.modifyM();
    }
}
