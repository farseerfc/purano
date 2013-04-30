package jp.ac.osakau.farseerfc.purano.visitor;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodInsnNode;



public class InvokeDepMethodVisitor extends MethodVisitor implements Opcodes {
	private final @Getter List<MethodInsnNode> invokes = new ArrayList<>(); 

	public InvokeDepMethodVisitor() {
		super(Opcodes.ASM4);
	}

	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc) {
		invokes.add(new MethodInsnNode(opcode,owner,name,desc));
	}
}
