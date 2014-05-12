package jp.ac.osakau.farseerfc.purano.reflect;

import com.google.gson.Gson;
import jp.ac.osakau.farseerfc.purano.anomanu.Field;

import java.util.Arrays;
import java.util.List;

public class JsonDumpper {

    public static void main(String args []){
        Gson gson = new Gson();
        Field f = new Field();
        f.dependArguments = new String [] {"aaa"};
        List<Field> lf = Arrays.asList(f);
        System.out.println(gson.toJson(lf));
    }
}
