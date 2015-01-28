package jp.ac.osakau.farseerfc.purano.dep;

import lombok.Getter;
import lombok.Setter;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.Value;

public class DepFrame<V extends Value> extends Frame<V> {
	private @Getter @Setter AbstractInsnNode node ;
	private @Getter @Setter LineNumberNode line;
	
    public DepFrame(final int nLocals, final int nStack) {
        super(nLocals, nStack);
    }

    public DepFrame(final Frame<? extends V> src) {
        super(src);
    }
}
