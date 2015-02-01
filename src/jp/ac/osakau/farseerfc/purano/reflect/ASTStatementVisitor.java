package jp.ac.osakau.farseerfc.purano.reflect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.print.attribute.HashAttributeSet;

import lombok.Getter;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;


public class ASTStatementVisitor extends ASTVisitor {
	
	private final @Getter CompilationUnit cu;
	private final @Getter HashMap<Integer, List<ASTNode>> lineMap;

	public ASTStatementVisitor(CompilationUnit cu){
		this.cu = cu;
		this.lineMap = new HashMap<>();
	}
	
	public boolean visitStatement(ASTNode node){
		int lineNumber = cu.getLineNumber(node.getStartPosition());
		if(lineMap.containsKey(lineNumber)){
			lineMap.get(lineNumber).add(node);
		}else{
			List<ASTNode> l = new ArrayList<>();
			l.add(node);
			lineMap.put(lineNumber, l);
		}
		return true;
	}
	
	@Override
	public boolean visit(MethodDeclaration node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(AssertStatement node) {
		return visitStatement(node);
	}

	@Override
	public boolean visit(BreakStatement node) {
		return visitStatement(node);
	}

	@Override
	public boolean visit(ContinueStatement node) {
		return visitStatement(node);
	}

	@Override
	public boolean visit(DoStatement node) {
		return visitStatement(node);
	}

	@Override
	public boolean visit(EmptyStatement node) {
		return visitStatement(node);
	}

	@Override
	public boolean visit(EnhancedForStatement node) {
		return visitStatement(node);
	}

	@Override
	public boolean visit(ExpressionStatement node) {
		return visitStatement(node);
	}

	@Override
	public boolean visit(ForStatement node) {
		return visitStatement(node);
	}

	@Override
	public boolean visit(IfStatement node) {
		return visitStatement(node);
	}

	@Override
	public boolean visit(LabeledStatement node) {
		return visitStatement(node);
	}

	@Override
	public boolean visit(ReturnStatement node) {
		return visitStatement(node);
	}

	@Override
	public boolean visit(SwitchStatement node) {
		return visitStatement(node);
	}

	@Override
	public boolean visit(SynchronizedStatement node) {
		return visitStatement(node);
	}

	@Override
	public boolean visit(TryStatement node) {
		return visitStatement(node);
	}

	@Override
	public boolean visit(TypeDeclarationStatement node) {
		return visitStatement(node);
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		return visitStatement(node);
	}

	@Override
	public boolean visit(WhileStatement node) {
		return visitStatement(node);
	}
	


	@Override
	public boolean visit(Block node) {
		return visitStatement(node);
	}
}
