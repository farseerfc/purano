package jp.ac.osakau.farseerfc.purano.dep;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import jp.ac.osakau.farseerfc.purano.table.TypeNameTable;
import jp.ac.osakau.farseerfc.purano.table.Types;

import org.apache.commons.lang3.ArrayUtils;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;


public class ClassDepVisitor extends ClassVisitor{
	private final TypeNameTable typeNameTable = new TypeNameTable();
	private final PrintWriter out;
	private final StringWriter sw = new StringWriter();
	private final PrintWriter writer = new PrintWriter(sw);
	
	public ClassDepVisitor() {
		super(Opcodes.ASM4);
		out = new PrintWriter(System.out);
	}
	
	public ClassDepVisitor(PrintWriter pw){
		super(Opcodes.ASM4);
		out = pw;
	}
	
	private void printf(String format, Object ... args){
		writer.print(String.format(format, args));
	}
	
	@Override
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		List<String> ifs = Lists.transform(Arrays.asList(interfaces),
				new Function<String,String>(){
			@Override
			@Nullable
			public String apply(@Nullable String inter) {
				return typeNameTable.fullClassName(inter);
			}}); 
		printf("%s class %s extends %s %s%s {\n",
			Types.access2string(access),
			typeNameTable.fullClassName(name),
			typeNameTable.fullClassName(superName),
			(interfaces.length==0) ?
				"" :
				"implements "+Joiner.on(", ").join(ifs),
			signature==null?"":" /*" +signature+ "*/"
			);
	}

	
	@Override 
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		printf("    %s %s\n",desc,visible?"true":"false");
		return null;
	};
	
	@Override
	public FieldVisitor	visitField(int access,
			String name, String desc, String signature, Object value){
		printf("    %s %s %s%s%s;\n",
				Types.access2string(access),
			typeNameTable.desc2full(desc),
			name,
			signature==null?"":" /*" +signature+ "*/",
			value==null?"":" = "+value);
		return null;
	}
	
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {
		if((access & Opcodes.ACC_SYNTHETIC )>0 ){
			// Avoid synthetic methods, which are generated by compiler 
			// and not visible from source codes
			return null;
		}
		
		printf("\n    %s %s%s\n{\n",
				Types.access2string(access),
				typeNameTable.dumpMethodDesc(desc, name),
				signature==null?"":" /*"+signature+"*/ ",
				(exceptions == null || exceptions.length == 0) ? "" : 
					"throws "+ Joiner.on(", ").join(exceptions));
		return new TraceMethodVisitor(new MethodNode(Opcodes.ASM4,access,name,desc,signature,exceptions){
			@Override
			public void visitEnd() {
				super.visitEnd();
				printf("}\n{\n");
				DepEffect effect = new DepEffect();
				Analyzer<DepValue> ana = new Analyzer<DepValue>(new DepInterpreter(effect));
				try {
					/*Frame<DepValue> [] frames =*/ ana.analyze("dep", this);
				} catch (AnalyzerException e) {
					e.printStackTrace();
				}
				printf("%s\n",effect.dump(this,typeNameTable));
				printf("}\n");
			}
		},new Textifier(Opcodes.ASM4){
			@Override public void visitMethodEnd() {
				print(writer); 
			}
		});
	}

	@Override
	public void visitEnd(){
		printf("}\n");
		out.print(typeNameTable.dumpImports());
		out.print(sw.toString());
		out.flush();
	}


	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		ClassDepVisitor tt = new ClassDepVisitor();
		ClassReader cr = new ClassReader("jp.ac.osakau.farseerfc.purano.test.TargetA");//readAllBytes("target/TryTree.class"));
		//TraceClassVisitor tcv = new TraceClassVisitor(tt,new Textifier(), new PrintWriter(System.err));
		cr.accept(tt, 0);
	}
	
	public static byte[] readAllBytes(String filename) throws IOException{
		try(FileInputStream fis = new FileInputStream(filename)){
			int next;
			List<Byte> bytes=new ArrayList<Byte>();
			while((next=fis.read())!=-1){
				bytes.add((byte)next);
			}
			return ArrayUtils.toPrimitive(bytes.toArray(new Byte[bytes.size()]));
		}
	}

	
}