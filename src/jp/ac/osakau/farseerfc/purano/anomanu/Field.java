package jp.ac.osakau.farseerfc.purano.anomanu;

/**
 * Created with IntelliJ IDEA.
 * User: farseerfc
 * Date: 9/18/13
 * Time: 8:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class Field {
    public boolean dependThis;
    public String[] dependFields;
    public String[] dependStaticFields;
    public String[] dependArguments;

    public String inheritedFrom;
    public Class type;
    public Class owner;
    public String name;
}
