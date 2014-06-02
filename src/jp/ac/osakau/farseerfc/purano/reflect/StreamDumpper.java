package jp.ac.osakau.farseerfc.purano.reflect;

import com.google.common.base.Joiner;

import jp.ac.osakau.farseerfc.purano.reflect.ClassFinder;
import jp.ac.osakau.farseerfc.purano.util.Escaper;
import jp.ac.osakau.farseerfc.purano.util.Types;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * StreamDumpper
 * User: farseerfc
 */
public class StreamDumpper implements ClassFinderDumpper {
    private final PrintStream out;
    private final ClassFinder cf;
    private final Escaper esc;

    public StreamDumpper(PrintStream out, ClassFinder cf, Escaper esc) {
        this.out = out;
        this.cf = cf;
        this.esc = esc;
    }

    @Override
    public void dump() {
        Types table = new Types();
        List<String> sb = new ArrayList<>();
        for (String clsName : cf.classMap.keySet()) {
            boolean isTarget = cf.classTargets.contains(clsName);
            for (String p : cf.prefix) {
                if (clsName.startsWith(p)) {
                    isTarget = true;
                }
            }
            if (!isTarget) {
                continue;
            }
            ClassRep cls = cf.classMap.get(clsName);
            out.println(Joiner.on("\n").join(cls.dump(table, esc)));
        }
        out.print(table.dumpImports());
        out.println(Joiner.on("\n").join(sb));
    }
}
