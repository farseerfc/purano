package jp.ac.osakau.farseerfc.purano.reflect;

import org.eclipse.jdt.core.dom.ASTNode;

import lombok.Data;

@Data
public class RefactoringCandidate {
	private ASTNode node;
}
