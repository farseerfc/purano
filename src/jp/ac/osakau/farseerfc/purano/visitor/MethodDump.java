package jp.ac.osakau.farseerfc.purano.visitor;

import jp.ac.osakau.farseerfc.purano.util.Types;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

public class MethodDump extends MethodVisitor {
	private final Types typeNameTable;
	private final StringBuilder sb;

	public MethodDump(Types typeNameTable,StringBuilder sb) {
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
	public void visitLocalVariable(String name, @NotNull String desc,
			@Nullable String signature, Label start, Label end, int index){
		sb.append(String.format("        %s %s%s;\n",
				typeNameTable.desc2full(desc),
				name,
				signature==null?"":" /*"+signature+"*/"));
	}
	
	@Override
	public void visitLabel(Label label) {
		
	}
}
