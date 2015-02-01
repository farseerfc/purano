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
	private @Getter @Setter DepEffect effects; 
	
    public DepFrame(final int nLocals, final int nStack) {
        super(nLocals, nStack);
        effects = new DepEffect();
    }

    public DepFrame(final Frame<DepValue> src) {
        super(src);
        effects = new DepEffect();
        effects.merge(((DepFrame)src).getEffects(), null);
    }
    
    @Override     
    public void execute(final AbstractInsnNode insn,
            final Interpreter<DepValue> interpreter) throws AnalyzerException {
    	assert(interpreter instanceof DepInterpreter);
    	DepInterpreter depint = (DepInterpreter) interpreter;
    	depint.setCurrentFrame(this);
    	super.execute(insn, depint);
    }
    
    @Override
    public boolean merge(final Frame<? extends DepValue> frame,
            final Interpreter<DepValue> interpreter) throws AnalyzerException {
        effects.merge(((DepFrame)frame).getEffects(), null);
        return super.merge(frame, interpreter);
    }
    
    @Override
    public boolean merge(final Frame<? extends DepValue> frame, final boolean[] access) {
    	effects.merge(((DepFrame)frame).getEffects(), null);
        return super.merge(frame, access);
    }

}
