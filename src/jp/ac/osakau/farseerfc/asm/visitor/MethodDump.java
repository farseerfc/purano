package jp.ac.osakau.farseerfc.asm.visitor;

import jp.ac.osakau.farseerfc.asm.table.TypeNameTable;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

public class MethodDump extends MethodVisitor {
	private final TypeNameTable typeNameTable;
	private final StringBuilder sb;

	public MethodDump(TypeNameTable typeNameTable,StringBuilder sb) {
		super(Opcodes.ASM4, new MethodNode());
		this.typeNameTable = typeNameTable;
		this.sb = sb;
	}

	@Override
	public void visitEnd() {
		sb.append("    }\n");
		
		
	}
	
	@Override
	public void visitCode() {
		sb.append("    {\n");
	}
	
	@Override
	public void visitLocalVariable(String name, String desc,
			String signature, Label start, Label end, int index){
		sb.append(String.format("        %s %s%s;\n",
				typeNameTable.desc2full(desc),
				name,
				signature==null?"":" /*"+signature+"*/"));
	}
	
	@Override
	public void visitLabel(Label label) {
		
	}
}
