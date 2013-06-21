package jp.ac.osakau.farseerfc.purano.test;

/**
 * Created with IntelliJ IDEA.
 * User: farseerfc
 * Date: 6/20/13
 * Time: 9:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class A extends Base {
    private int[] m;

    public void changeA(int a[]){
        int [] local = a;
        a = new int [1];
        local[0]= 1;
    }

    public int [] getM(){
       return m;
    }

    public int [] create(){
        return new int [1];
    }

    public void modifyM(){
        changeA(getM());
    }

    public void modifyTemp(){
        changeA(create());
    }

    @Override
    public void change(){
        modifyM();
    }
}
