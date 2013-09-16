package jp.ac.osakau.farseerfc.purano.reflect;

import com.google.common.base.Joiner;
import jp.ac.osakau.farseerfc.purano.reflect.ClassFinder;
import jp.ac.osakau.farseerfc.purano.util.Types;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: farseerfc
 * Date: 9/16/13
 * Time: 5:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class StreamDumpper implements ClassFinderDumpper {
    private final PrintStream out;
    private final ClassFinder cf;

    public StreamDumpper(PrintStream out, ClassFinder cf) {
        this.out = out;
        this.cf = cf;
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
            out.println(Joiner.on("\n").join(cls.dump(table)));
        }
        out.print(table.dumpImports());
        out.println(Joiner.on("\n").join(sb));
    }
}
