package jp.ac.osakau.farseerfc.purano.reflect;

import jp.ac.osakau.farseerfc.purano.dep.DepEffect;
import jp.ac.osakau.farseerfc.purano.dep.DepFrame;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;

@Slf4j
public class ASTForVisitor extends ASTVisitor {
	
	private final @Getter MethodRep mr;

	public ASTForVisitor(MethodRep mr){
		this.mr = mr;
	}
	
	public boolean visitFor(ASTNode node){
		CompilationUnit cu = mr.getUnit();
		int lineno = cu.getLineNumber(node.getStartPosition());
		
		if(mr.getFrames()==null || mr.getFrameEffects() == null){
			return true;
		}
		
		DepEffect empty = new DepEffect();
		
		for(int i=0; i<mr.getFrames().length; ++i){
			DepFrame frame = mr.getFrames()[i];
			DepEffect effect = mr.getFrameEffects()[i];
			
			if(frame == null) {
				continue;
			}
			
			if(frame.getLine() == null){
				continue;
			}
			
			int frameLineNo = frame.getLine().line;
			
			if(frameLineNo != lineno){
				continue;
			}
			
			if(effect.equals(empty)){
				log.info(String.format("%s.%s has pure for loop at line (%d-%d)",
						mr.getClassRep().getBaseName() , 
						MethodRep.getId(mr.getInsnNode()),
						cu.getLineNumber(node.getStartPosition()),
						cu.getLineNumber(node.getStartPosition()+node.getLength())
						));
				log.info(node.toString());
			}
		}
		return true;
	}
	
	@Override
	public boolean visit(MethodDeclaration node) {
		return super.visit(node);
	}


	@Override
	public boolean visit(EnhancedForStatement node) {
		return visitFor(node);
	}

	@Override
	public boolean visit(ForStatement node) {
		return visitFor(node);
	}
}
