package jp.ac.osakau.farseerfc.purano.ano;

/**
 * Created with IntelliJ IDEA.
 * User: farseerfc
 * Date: 6/10/13
 * Time: 6:46 AM
 * To change this template use File | Settings | File Templates.
 */
public interface Purity {
    public static final int Unknown = 0 ;
    public static final int Stateless = 0x1 ;
    public static final int Stateful = 0x2 ;
    // Breaker

    public static final int FieldModifier = 0x10 ;
    public static final int ArgumentModifier = 0x20;
    public static final int StaticModifier = 0x40;
    public static final int Native = 0x80;
}
