package jp.ac.osakau.farseerfc.purano.dep;

import lombok.Getter;
import lombok.Setter;

import org.hamcrest.core.IsInstanceOf;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.Interpreter;
import org.objectweb.asm.tree.analysis.Value;

public class DepFrame extends Frame<DepValue> {
	private @Getter @Setter AbstractInsnNode node ;
	private @Getter @Setter LineNumberNode line;
	private final DepAnalyzer analyzer;
	
    public DepFrame(final int nLocals, final int nStack, final DepAnalyzer ana) {
        super(nLocals, nStack);
        this.analyzer = ana;
    }

    public DepFrame(final Frame<DepValue> src, final DepAnalyzer ana) {
        super(src);
        this.analyzer = ana;
    }
     
    public void execute(final AbstractInsnNode insn,
            final Interpreter<DepValue> interpreter, int insnNum) throws AnalyzerException {
    	assert(interpreter instanceof DepInterpreter);
    	DepInterpreter depint = (DepInterpreter) interpreter;
    	depint.setCurrentFrameEffect(analyzer.getEffects()[insnNum]);
    	super.execute(insn, depint);
    }

}
