package jp.ac.osakau.farseerfc.purano.reflect;

import com.google.gson.Gson;
import jp.ac.osakau.farseerfc.purano.anomanu.Field;

import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: farseerfc
 * Date: 9/18/13
 * Time: 7:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class JsonDumpper {

    public static void main(String args []){
        Gson gson = new Gson();
        Field f = new Field();
        f.dependArguments = new String [] {"aaa"};
        List<Field> lf = Arrays.asList(f);
        System.out.println(gson.toJson(lf));
    }
}
