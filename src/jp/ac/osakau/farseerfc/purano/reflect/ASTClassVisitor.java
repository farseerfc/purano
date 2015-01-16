package jp.ac.osakau.farseerfc.purano.reflect;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jp.ac.osakau.farseerfc.purano.util.MethodDesc;
import jp.ac.osakau.farseerfc.purano.util.Types;
import lombok.extern.slf4j.Slf4j;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.jetbrains.annotations.NotNull;

import com.google.common.base.Joiner;

@Slf4j
public class ASTClassVisitor extends ASTVisitor {

	private final @NotNull ClassRep classRep;
	private CompilationUnit unit;

	public ASTClassVisitor(@NotNull ClassRep classRep) {
		this.classRep = classRep;
	}

	@Override public boolean visit(MethodDeclaration node) {
	
		if(node.isConstructor()){
			for(MethodRep methodRep : classRep.getAllMethods()){
				if(methodRep.isInit()){
					if(getMethodRepSignature(methodRep).equals(getMethodSignature(node))){
						methodRep.setSource(node.toString());
						methodRep.setSourceFile(classRep.getSourceFile());
						methodRep.setSourceBegin(unit.getLineNumber(node.getStartPosition()));
						methodRep.setSourceEnd(unit.getLineNumber(node.getStartPosition()+node.getLength()));
					}
				}
			}
		}else{
			for(MethodRep methodRep : classRep.getAllMethods()){
				if(methodRep.getInsnNode().name .equals(node.getName().toString())){
					if(getMethodRepSignature(methodRep).equals(getMethodSignature(node))){
						methodRep.setSource(node.toString());
						methodRep.setSourceFile(classRep.getSourceFile());
						methodRep.setSourceBegin(unit.getLineNumber(node.getStartPosition()));
						methodRep.setSourceEnd(unit.getLineNumber(node.getStartPosition()+node.getLength()));
					}
				}
			}
		}
		return false;
	}

	public void parse(String source) {
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setSource(source.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);

		Map<String, String> m = JavaCore.getOptions();
		// m=JavaCore.getDefaultOptions();
		m.put(JavaCore.COMPILER_SOURCE, "1.7");
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_7, m);
		parser.setCompilerOptions(m);

		unit = (CompilationUnit) parser
				.createAST(new NullProgressMonitor());
		
		unit.accept(this);
		
	}
	
	public String getMethodRepSignature(MethodRep methodRep){
		Types noPackage = new Types(true);
		MethodDesc methodDesc = noPackage.method2full(methodRep.getInsnNode().desc);
		return String.format("%s (%s)", methodDesc.getReturnType(),
				Joiner.on(", ").join(methodDesc.getArguments()));
	}
	
	public String getMethodSignature(MethodDeclaration node){
	
		String returnType = typeToString(node.getReturnType2()) ;
		List<SingleVariableDeclaration> paras = (List<SingleVariableDeclaration>) node.parameters();
		
		List<String> param = paras.stream()
				.map( vd -> typeToString(vd.getType()))
				.collect(Collectors.toList());
		
		return String.format("%s (%s)", returnType, Joiner.on(", ").join(param));
		
	}
	
	public String typeToString(Type type){
		return type == null ? "void": type.toString();
	}
}
