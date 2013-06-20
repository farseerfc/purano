package jp.ac.osakau.farseerfc.purano.test;

/**
 * Created with IntelliJ IDEA.
 * User: farseerfc
 * Date: 6/20/13
 * Time: 10:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class B extends D{
    private A a;

    public int selfRecursion(int n){
        if(n>0){
            return selfRecursion(n-1)+1;
        }
        return 1;
    }

    public int crossRecursionA(int n){
        if(n>0){
            return crossRecursionB(n-1)+1;
        }
        return 1;
    }

    public int crossRecursionB(int n){
        if(n>0){
            return crossRecursionA(n-1)*2;
        }
        return 1;
    }

    @Override
    public void change(){
        a.modifyM();
    }
}
