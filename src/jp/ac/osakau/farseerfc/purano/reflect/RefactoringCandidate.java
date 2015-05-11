package jp.ac.osakau.farseerfc.purano.reflect;

import java.util.List;

import jp.ac.osakau.farseerfc.purano.dep.DepEffect;

import org.eclipse.jdt.core.dom.ASTNode;

import lombok.Data;

@Data
public class RefactoringCandidate {
	private ASTNode node;
	private List<String> loopVariables;
	private DepEffect effects;
}
