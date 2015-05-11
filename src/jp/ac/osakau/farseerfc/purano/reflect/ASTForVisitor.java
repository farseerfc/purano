package jp.ac.osakau.farseerfc.purano.reflect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jp.ac.osakau.farseerfc.purano.dep.DepEffect;
import jp.ac.osakau.farseerfc.purano.dep.DepFrame;
import jp.ac.osakau.farseerfc.purano.dep.DepSet;
import jp.ac.osakau.farseerfc.purano.effect.LocalVariableEffect;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.objectweb.asm.tree.LocalVariableNode;

@Slf4j
public class ASTForVisitor extends ASTVisitor {
	
	private final @Getter MethodRep mr;

	public ASTForVisitor(MethodRep mr){
		this.mr = mr;
	}
	
	public boolean visitFor(ASTNode bodyNode, ASTNode forNode, List<String> loopVariables){
		CompilationUnit cu = mr.getUnit();
		int startLine = cu.getLineNumber(bodyNode.getStartPosition());
		int endLine = cu.getLineNumber(bodyNode.getStartPosition()+bodyNode.getLength());
		
		if(mr.getFrames()==null || mr.getFrameEffects() == null){
			return true;
		}
		
		DepEffect empty = new DepEffect();
		
		for(String loopVarName: loopVariables){
			for(LocalVariableNode node : mr.getMethodNode().localVariables){
				if(node.name.trim().equals(loopVarName)){
					empty.addLocalVariableEffect(new LocalVariableEffect(node.index, new DepSet(), null));
				}
			}
		}
		
		boolean isCandidate = true;
		
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
			
			if(frameLineNo < startLine || frameLineNo > endLine){
				continue;
			}
			
			if(!effect.isSubset(empty)){
				isCandidate = false;
			}
			
		}
		
		if(isCandidate){
			RefactoringCandidate candidate = new RefactoringCandidate();
			candidate.setNode(forNode);
			candidate.setLoopVariables(loopVariables);
			mr.getCandidates().add(candidate);
		}
		return true;
	}
	
	@Override
	public boolean visit(MethodDeclaration node) {
		return super.visit(node);
	}


	@Override
	public boolean visit(EnhancedForStatement node) {
		SingleVariableDeclaration sv = node.getParameter();
		return visitFor(node.getBody(), node, Arrays.asList(sv.getName().toString()));
	}

	@Override
	public boolean visit(ForStatement node) {
		List<Expression> inits = node.initializers();
		List<String> variableNames = new ArrayList<>();
		for(Expression exp : inits){
			if(exp instanceof VariableDeclarationExpression){
				VariableDeclarationExpression vde = (VariableDeclarationExpression) exp;
				for(Object fragment : vde.fragments()){
					VariableDeclarationFragment vdf = (VariableDeclarationFragment) fragment;
					variableNames.add(vdf.getName().toString());
				}
			}
		}
		return visitFor(node.getBody(), node, variableNames );
	}
}
