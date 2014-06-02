package jp.ac.osakau.farseerfc.purano.reflect;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import jp.ac.osakau.farseerfc.purano.util.Escaper;
import jp.ac.osakau.farseerfc.purano.util.Types;

import com.google.common.base.Joiner;

public class HtmlDumpper implements ClassFinderDumpper {
    private final PrintStream out;
    private final ClassFinder cf;
    private final Escaper esc;

    public HtmlDumpper(PrintStream out, ClassFinder cf) {
        this.out = out;
        this.cf = cf;
        this.esc = Escaper.getHtml();
    }

    @Override
    public void dump() {
        Types table = new Types();
        
        out.println("<html>");
        out.println("<body><div>");
        
        List<String> sb = new ArrayList<>();
        for (String clsName : cf.classMap.keySet()) {
            boolean isTarget = cf.classTargets.contains(clsName);
            for (String p : cf.prefix) {
                if (clsName.startsWith(p)) {
                    isTarget = true;
                }
            }
//            isTarget = isTarget || cf.prefix.stream().anyMatch(p -> clsName.startsWith(p));
            if (!isTarget) {
                continue;
            }
            ClassRep cls = cf.classMap.get(clsName);
            dumpClass(cls, table);
            out.println("</div><div>");
//            out.println(Joiner.on("</p><p>").join(cls.dump(table)));
        }
        out.print(table.dumpImportsHtml());
        out.println(Joiner.on("</div><hr/><div>").join(sb));
        
        out.println("</div></body>");
        out.println("</html>");
    }
    
    public void dumpClass(ClassRep cls, Types table){
    	out.println("<p>"+Joiner.on("</p><p>").join(cls.dump(table, esc))+"</p>");
    	
    }
}
