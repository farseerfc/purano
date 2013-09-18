package jp.ac.osakau.farseerfc.purano.anomanu;
//
//import org.eclipse.core.runtime.NullProgressMonitor;
//import org.eclipse.jdt.core.ICompilationUnit;
//import org.eclipse.jdt.core.JavaCore;
//import org.eclipse.jdt.core.JavaModelException;
//import org.eclipse.jdt.core.dom.*;
//import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
//import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
//import org.eclipse.jface.text.BadLocationException;
//import org.eclipse.jface.text.Document;
//import org.eclipse.text.edits.TextEdit;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.util.Map;


/**
 * Created with IntelliJ IDEA.
 * User: farseerfc
 * Date: 9/16/13
 * Time: 10:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class JDTWrite {
//    private void addAnnotations(final ICompilationUnit cu) throws JavaModelException, BadLocationException {
//
//        // parse compilation unit
//        final ASTParser parser = ASTParser.newParser(AST.JLS4);
//        parser.setSource(cu);
//        final CompilationUnit astRoot = (CompilationUnit) parser.createAST(null);
//
//
//        // create a ASTRewrite
//        final AST ast = astRoot.getAST();
//        final ASTRewrite rewriter = ASTRewrite.create(ast);
//
//        final ListRewrite listRewrite = rewriter.getListRewrite(astRoot, CompilationUnit.TYPES_PROPERTY);
//        final NormalAnnotation eventHandlerAnnotation = astRoot.getAST().newNormalAnnotation();
//        eventHandlerAnnotation.setTypeName(astRoot.getAST().newName("CustomAnnotation"));
//        eventHandlerAnnotation.values().add(createAnnotationMember(ast, "arg1", "Blup"));
//        eventHandlerAnnotation.values().add(createQualifiedAnnotationMember(ast, "arg2", "IsWorkbenchTest", "Blab"));
//
//
//        final SingleMemberAnnotation runWithFop = astRoot.getAST().newSingleMemberAnnotation();
//        runWithFop.setTypeName(astRoot.getAST().newName("SimpleAnnotation"));
//        final TypeLiteral newTypeLiteral = astRoot.getAST().newTypeLiteral();
//        newTypeLiteral.setType(astRoot.getAST().newSimpleType(astRoot.getAST().newSimpleName("Blop")));
//        runWithFop.setValue(newTypeLiteral);
//        listRewrite.insertAt(runWithFop, 0, null);
//        listRewrite.insertAt(eventHandlerAnnotation, 0, null);
//        rewriter.rewriteAST();
//
//        final TextEdit edits = rewriter.rewriteAST();
//
//        // apply the text edits to the compilation unit
//        final Document document = new Document(cu.getSource());
//        edits.apply(document);
//
//        // this is the code for adding statements
////        cu.getBuffer().setContents(formatFileContent(document.get()));
//        cu.getBuffer().setContents(document.get());
//        cu.save(null, true);
//    }
//    protected static MemberValuePair createQualifiedAnnotationMember(final AST ast, final String name, final String value, final String value2) {
//        final MemberValuePair mV = ast.newMemberValuePair();
//        mV.setName(ast.newSimpleName(name));
//        final TypeLiteral typeLiteral = ast.newTypeLiteral();
//        final QualifiedType newQualifiedName = ast.newQualifiedType(ast.newSimpleType(ast.newSimpleName(value)), ast.newSimpleName(value2));
//        typeLiteral.setType(newQualifiedName);
//        mV.setValue(typeLiteral);
//        return mV;
//    }
//
//    protected static MemberValuePair createAnnotationMember(final AST ast, final String name, final String value) {
//
//        final MemberValuePair mV = ast.newMemberValuePair();
//        mV.setName(ast.newSimpleName(name));
//        final TypeLiteral typeLiteral = ast.newTypeLiteral();
//        typeLiteral.setType(ast.newSimpleType(ast.newSimpleName(value)));
//        mV.setValue(typeLiteral);
//        return mV;
//    }
//
//
//    private static String contentsToString(String path) {
//
//        File targetFile = new File(path);
//        byte[] b = new byte[(int) targetFile.length()];
//
//        try {
//            FileInputStream fi = new FileInputStream(targetFile);
//            fi.read(b);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return new String(b);
//    }
//
//    public static void main(String[] args) throws BadLocationException, JavaModelException {
//        String path = "/tmp/A.java";
//
//        JDTWrite writer = new JDTWrite();
//
//
//        String content = contentsToString(path);
//
//        ASTParser parser = ASTParser.newParser(AST.JLS4);
//        parser.setSource(content.toCharArray());
//        parser.setKind(ASTParser.K_COMPILATION_UNIT);
//
//        Map<String, String> m = JavaCore.getOptions();
//        // m=JavaCore.getDefaultOptions();
//        m.put(JavaCore.COMPILER_SOURCE, "1.7");
//        JavaCore.setComplianceOptions(JavaCore.VERSION_1_7, m);
//        parser.setCompilerOptions(m);
//
//        CompilationUnit unit = (CompilationUnit) parser
//                .createAST(new NullProgressMonitor());
//
//
//    }
}
