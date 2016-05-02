package jp.ac.osakau.farseerfc.purano.reflect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import com.google.common.base.Joiner;

@Slf4j
public class ClassFinder {

	public Path findSourcePath(String name) {
		for(String srcPrefix : sourcePrefix){
			String className = name.replace(".", "/") + ".java";
			Path path = Paths.get(srcPrefix, className);
			File file = path.toFile();
			if(file.exists() && file.isFile()){
				return path;
			}
		}
		log.info("Not found source for {}", name);
		return null;
	} 
	


	@Getter final Map<String, ClassRep> classMap= new HashMap<>();

	final Set<String> classTargets = new HashSet<>() ;
    final List<String> prefix;

    private static final int MAX_LOAD_PASS = 2;
    
    private final boolean examChangedSignatures = true;
    private final boolean breakForloop = true;
	private @NotNull final List<String> sourcePrefix;

	public ClassFinder(@NotNull List<String> prefix, @NotNull List<String> sourcePrefix){
		this.sourcePrefix = sourcePrefix;
		findTargetClasses(prefix);
        this.prefix=prefix;
	}
	
	public ClassFinder(@NotNull List<String> prefix){
        this(prefix, new ArrayList<String>());
	}
	
	public ClassFinder(String string) {
		this(Arrays.asList(string));
	}

	public void resolveMethods() {
		int timestamp = 0;
		Set<ClassRep> allCreps = new HashSet<>(classMap.values());
		boolean changed;
		int pass = 0;
        int changedMethod = 0;

        List<Integer> changedMethodsTrace = new ArrayList<>();
        List<Integer> loadedClassesTrace = new ArrayList<>();
		do {
			changed = false;
			if(pass < MAX_LOAD_PASS){
			    allCreps = new HashSet<>(classMap.values());
                loadedClassesTrace.add(allCreps.size());
			}
            changedMethod = 0;
            
            Set<MethodRep> changedSignatures = new HashSet<>();
			for (ClassRep crep : allCreps ) {
				for (MethodRep mrep : crep.getAllMethods()) {
					if (mrep.isNeedResolve(this)) {
						if (mrep.resolve(++timestamp, this)) {
							changed = true;
                            changedMethod ++;
                            if(examChangedSignatures){
                            	changedSignatures.add(mrep);
                            }
						}
					}
				}
			}
            changedMethodsTrace.add(changedMethod);
            log.info(String.format("Pass: %d Classes: %s Changed Method: %d [%s]",
                    pass++,allCreps.size(),changedMethod,
                    Joiner.on(", ").join(changedMethodsTrace)));
            if(examChangedSignatures){
	            final int maxdump=2;
	            if(changedMethod>maxdump){
	                MethodRep [] top = new MethodRep [maxdump];
	                int i=0;
	                for(MethodRep mid : changedSignatures){
	                    if(i>=maxdump)break;
		                    top[i++]=mid;
	                }
//	            	log.info(Joiner.on(", ").join(top));
	            }else{
//	                for(MethodRep m:changedSignatures){
//	                	log.info(Joiner.on("\n").join(m.dump(this, new Types(), Escaper.getDummy())));
//	                }
	                if(breakForloop){
	                	break;
	                }
		        }
            }
		} while (changed);

        log.info("Loaded Classes: " + Joiner.on(", ").join(loadedClassesTrace));
        log.info("Changed methods: "+Joiner.on(", ").join(changedMethodsTrace));
	}
	
	private void findTargetClasses(@NotNull Collection<String> prefixes){
		
		for(String prefix:prefixes){
			Reflections reflections = new Reflections( prefix ,new SubTypesScanner(false));
	        classTargets.addAll( reflections.getStore().getSubTypesOf(Object.class.getName()));
	        classTargets.add(prefix);
		}
		for(String cls : classTargets){
			loadClass(cls);
		}
	}
	
	public ClassRep loadClass(@NotNull String classname){
		if(!classMap.containsKey(classname)){
			log.info("Loading {}", classname);
			if(classname.startsWith("[")){
				classMap.put(classname, new ClassRep(ArrayStub.class.getName(),this));
			}else{
				classMap.put(classname, new ClassRep(classname, this));
			}
		}
		return classMap.get(classname);
	}

	
	private void dumpForResult() {
		log.info("<<<<<<<<<<<<<<<<< Refactoring Candidates <<<<<<<<<<<<<<<<<<<");
		int count = 0;
		for (String clsName : classMap.keySet()) {
			boolean isTarget = classTargets.contains(clsName);
			for (String p : prefix) {
				if (clsName.startsWith(p)) {
					isTarget = true;
				}
			}

			if (!isTarget) {
				continue;
			}

			ClassRep classRep = classMap.get(clsName);

			for (MethodRep methodRep : classRep.getAllMethods()) {
				ASTForVisitor forv = new ASTForVisitor(methodRep);
				if (methodRep.getSourceNode() != null) {
					methodRep.getSourceNode().accept(forv);
				}

				for (RefactoringCandidate can : methodRep.getCandidates()) {
					log.info(String.format(
							"\"%s.%s\" has pure for-loop at line (%d-%d)",
							methodRep.getClassRep().getBaseName(),
							MethodRep.getId(methodRep.getInsnNode()),
							methodRep.getUnit().getLineNumber(
									can.getNode().getStartPosition()),
							methodRep.getUnit().getLineNumber(
									can.getNode().getStartPosition()
											+ can.getNode().getLength())));
					log.info(can.getNode().toString());
					
					count ++;

				}
			}
		}
		log.info(String.format("Found %d refactoring candidates", count));
	}
	
	public static void mainHtmlParser() throws IOException{
		long start=System.currentTimeMillis();
        String targetPackage []={
        		"org.htmlparser" };

		
		String targetSource [] = {
				"/home/farseerfc/purano_target/htmlparser-code/lexer/src/main/java",
				"/home/farseerfc/purano_target/htmlparser-code/parser/src/main/java",
				"/home/farseerfc/purano_target/htmlparser-code/filterbuilder/src/main/java",
				"/home/farseerfc/purano_target/htmlparser-code/sitecapturer/src/main/java",
				"/home/farseerfc/purano_target/htmlparser-code/thumbelina/src/main/java",
			};
		
		ClassFinder cf = new ClassFinder(Arrays.asList(targetPackage), Arrays.asList(targetSource));
		cf.resolveMethods();
		
		
		cf.dumpForResult();
		
        File output = new File("/tmp/htmlparser.html");
        PrintStream ps = new PrintStream(new FileOutputStream(output));
        ClassFinderDumpper dumpper = new HtmlDumpper(ps,cf);
        dumpper.dump();
        log.info("Runtime :"+(System.currentTimeMillis() - start));
	}
	
	public static void mainTomcat() throws IOException{
		long start=System.currentTimeMillis();
        String targetPackage []={
        		"org.apache.catalina" };

		
		String targetSource [] = {
				
			};
		
		ClassFinder cf = new ClassFinder(Arrays.asList(targetPackage), Arrays.asList(targetSource));
		cf.resolveMethods();
		
		
		cf.dumpForResult();
		
        File output = new File("/tmp/tomcat.html");
        PrintStream ps = new PrintStream(new FileOutputStream(output));
        ClassFinderDumpper dumpper = new HtmlDumpper(ps,cf);
        dumpper.dump();
        log.info("Runtime :"+(System.currentTimeMillis() - start));
	}

	public static void mainArgoUML() throws IOException{
		long start=System.currentTimeMillis();
        String targetPackage []={
        		"org.argouml" };

		
		String targetSource [] = {
				
			};
		
		ClassFinder cf = new ClassFinder(Arrays.asList(targetPackage), Arrays.asList(targetSource));
		cf.resolveMethods();
		
		
		cf.dumpForResult();
		
        File output = new File("/tmp/argouml.html");
        PrintStream ps = new PrintStream(new FileOutputStream(output));
        ClassFinderDumpper dumpper = new HtmlDumpper(ps,cf);
        dumpper.dump();
        log.info("Runtime :"+(System.currentTimeMillis() - start));
	}

	
	public static void mainTrove() throws IOException{
		long start=System.currentTimeMillis();
        String targetPackage []={
        		"gnu.trove" };

		
		String targetSource [] = {
				"/home/farseerfc/purano_target/trove/3.0.3/src",
			};
		
		ClassFinder cf = new ClassFinder(Arrays.asList(targetPackage), Arrays.asList(targetSource));
		cf.resolveMethods();
		
		
		cf.dumpForResult();
		
        File output = new File("/tmp/output.html");
        PrintStream ps = new PrintStream(new FileOutputStream(output));
        ClassFinderDumpper dumpper = new HtmlDumpper(ps,cf);
        dumpper.dump();
        log.info("Runtime :"+(System.currentTimeMillis() - start));
	}
	
	public static void mainPcollections() throws IOException{
		long start=System.currentTimeMillis();
        String targetPackage []={
        		"org.pcollections" };

		
		String targetSource [] = {
				"/home/farseerfc/purano_target/pcollections/src/main/java",
			};
		
		ClassFinder cf = new ClassFinder(Arrays.asList(targetPackage), Arrays.asList(targetSource));
		cf.resolveMethods();
		
		
		cf.dumpForResult();
		
        File output = new File("/tmp/output.html");
        PrintStream ps = new PrintStream(new FileOutputStream(output));
        ClassFinderDumpper dumpper = new HtmlDumpper(ps,cf);
        dumpper.dump();
        log.info("Runtime :"+(System.currentTimeMillis() - start));
	}
	
	public static void mainMapdb() throws IOException{
		long start=System.currentTimeMillis();
        String targetPackage []={
        		"org.mapdb" };

		
		String targetSource [] = {
				"/home/farseerfc/purano_target/mapdb/src/main/java",
			};
		
		ClassFinder cf = new ClassFinder(Arrays.asList(targetPackage), Arrays.asList(targetSource));
		cf.resolveMethods();
		
		
		cf.dumpForResult();
		
        File output = new File("/tmp/output.html");
        PrintStream ps = new PrintStream(new FileOutputStream(output));
        ClassFinderDumpper dumpper = new HtmlDumpper(ps,cf);
        dumpper.dump();
        log.info("Runtime :"+(System.currentTimeMillis() - start));
	}
	
	public static void mainJodaTime() throws IOException{
		long start=System.currentTimeMillis();
        String targetPackage []={
        		"org.joda" };

		
		String targetSource [] = {
				"/home/farseerfc/purano_target/joda-time/src/main/java",
			};
		
		ClassFinder cf = new ClassFinder(Arrays.asList(targetPackage), Arrays.asList(targetSource));
		cf.resolveMethods();
		
		
		cf.dumpForResult();
		
        File output = new File("/tmp/output.html");
        PrintStream ps = new PrintStream(new FileOutputStream(output));
        ClassFinderDumpper dumpper = new HtmlDumpper(ps,cf);
        dumpper.dump();
        log.info("Runtime :"+(System.currentTimeMillis() - start));
	}
	
	public static void mainTest() throws IOException{
		long start=System.currentTimeMillis();
        String targetPackage []={
        		"test" };

		
		String targetSource [] = {
				"/home/farseerfc/workspace/purano/src",
			};
		
		ClassFinder cf = new ClassFinder(Arrays.asList(targetPackage), Arrays.asList(targetSource));
		cf.resolveMethods();
		
		
		cf.dumpForResult();
		
        File output = new File("/tmp/output.html");
        PrintStream ps = new PrintStream(new FileOutputStream(output));
        ClassFinderDumpper dumpper = new HtmlDumpper(ps,cf);
        dumpper.dump();
        log.info("Runtime :"+(System.currentTimeMillis() - start));
	}
	
	public static void mainDacapoFop() throws IOException{
		long start=System.currentTimeMillis();
        String targetPackage []={
        		"org.dacapo","org.apache.fop" };

		
		String targetSource [] = {
				"/home/farseerfc/purano_target/dacapo-src/benchmarks/bms/fop/build/fop-0.95/src/java",
				"/home/farseerfc/workspace/purano/lib/target/dacapo-src/benchmarks/bms/fop/src",
			};
		
		ClassFinder cf = new ClassFinder(Arrays.asList(targetPackage), Arrays.asList(targetSource));
		cf.resolveMethods();
		
		
		cf.dumpForResult();
		
        File output = new File("/tmp/output.html");
        PrintStream ps = new PrintStream(new FileOutputStream(output));
        ClassFinderDumpper dumpper = new HtmlDumpper(ps,cf);
        dumpper.dump();
        log.info("Runtime :"+(System.currentTimeMillis() - start));
	}

	public static void mainDacapoAvrora() throws IOException{
		long start=System.currentTimeMillis();
        String targetPackage []={
        		"org.dacapo","avrora" };

		
		String targetSource [] = {
				"/home/farseerfc/purano_target/dacapo-src/benchmarks/bms/avrora/src",
				"/home/farseerfc/workspace/purano/lib/target/dacapo-src/benchmarks/bms/avrora/src",
			};
		
		ClassFinder cf = new ClassFinder(Arrays.asList(targetPackage), Arrays.asList(targetSource));
		cf.resolveMethods();
		
		
		cf.dumpForResult();
		
        File output = new File("/tmp/output.html");
        PrintStream ps = new PrintStream(new FileOutputStream(output));
        ClassFinderDumpper dumpper = new HtmlDumpper(ps,cf);
        dumpper.dump();
        log.info("Runtime :"+(System.currentTimeMillis() - start));
	}

	public static void mainDacapoXalan() throws IOException{
		long start=System.currentTimeMillis();
        String targetPackage []={
        		"org.dacapo",
        		"org.apache.xalan",
        		"org.apache.xml",
        		"org.apache.xpath",
        		};

		
		String targetSource [] = {
				"/home/farseerfc/purano_target/dacapo-src/benchmarks/bms/xalan/build/xalan-j_2_7_1/src",
				"/home/farseerfc/workspace/purano/lib/target/dacapo-src/benchmarks/bms/xalan/src",
			};
		
		ClassFinder cf = new ClassFinder(Arrays.asList(targetPackage), Arrays.asList(targetSource));
		cf.resolveMethods();
		
		
		cf.dumpForResult();
		
        File output = new File("/tmp/output.html");
        PrintStream ps = new PrintStream(new FileOutputStream(output));
        ClassFinderDumpper dumpper = new HtmlDumpper(ps,cf);
        dumpper.dump();
        log.info("Runtime :"+(System.currentTimeMillis() - start));
	}

	public static void mainDacapoPmd() throws IOException{
		long start=System.currentTimeMillis();
        String targetPackage []={
        		"org.dacapo",
        		"net.sourceforge.pmd",
        		};

		
		String targetSource [] = {
				"/home/farseerfc/purano_target/dacapo-src/benchmarks/bms/pmd/build/pmd-4.2.5/src",
				"/home/farseerfc/workspace/purano/lib/target/dacapo-src/benchmarks/bms/pmd/src",
			};
		
		ClassFinder cf = new ClassFinder(Arrays.asList(targetPackage), Arrays.asList(targetSource));
		cf.resolveMethods();
		
		
		cf.dumpForResult();
		
        File output = new File("/tmp/output.html");
        PrintStream ps = new PrintStream(new FileOutputStream(output));
        ClassFinderDumpper dumpper = new HtmlDumpper(ps,cf);
        dumpper.dump();
        log.info("Runtime :"+(System.currentTimeMillis() - start));
	}
	
	public static void main(@NotNull String [] argv) throws IOException {
		mainDacapoPmd();
//		mainDacapoAvrora();
//		mainDacapoFop();
//		mainTest();
//		mainHtmlParser();
//		mainPcollections();
//		mainJodaTime();
//		mainMapdb();
//		mainTomcat();
//		mainArgoUML();
		
//		return;
		/*
		long start=System.currentTimeMillis();
        String targetPackage []={
        		"mit.jolden.treeadd"};
//        		"jp.ac.osakau.farseerfc.purano", "org.objectweb.asm"};
//        "test"};
//                "java.time.format.DateTimeFormatterBuilder"};
//        		"org.htmlparser" };
        // "org.argouml"};
//        "org.apache.catalina","java.lang.Object"};
//        "jp.ac.osakau.farseerfc.purano","org.objectweb.asm","java.lang.Object"};
//        "jp.ac.osakau.farseerfc.purano","java.lang"}
        
		if(argv.length > 1){
			targetPackage=argv;
		}
		
		String targetSource [] = {
//				"/home/farseerfc/purano_target/htmlparser-code/lexer/src/main/java",
//				"/home/farseerfc/purano_target/htmlparser-code/parser/src/main/java",
//				"/home/farseerfc/purano_target/htmlparser-code/filterbuilder/src/main/java",
//				"/home/farseerfc/purano_target/htmlparser-code/sitecapturer/src/main/java",
//				"/home/farseerfc/purano_target/htmlparser-code/thumbelina/src/main/java",
//				
//				"/home/farseerfc/workspace/purano/lib/target/src/src",
//				"/home/farseerfc/workspace/purano/src",
			};
		
		ClassFinder cf = new ClassFinder(Arrays.asList(targetPackage), Arrays.asList(targetSource));
		cf.resolveMethods();
		
		
		cf.dumpForResult();
		

//        ClassFinderDumpper dumpper = new DumyDumpper();
//		  ClassFinderDumpper dumpper = new StreamDumpper(ps,cf, Escaper.getDummy());
//        ClassFinderDumpper dumpper = new LegacyDumpper(cf);
		
        File output = new File("/tmp/output.html");
        PrintStream ps = new PrintStream(new FileOutputStream(output));
        ClassFinderDumpper dumpper = new HtmlDumpper(ps,cf);
//		ClassFinderDumpper dumpper = new StreamDumpper(ps,cf, Escaper.getDummy());
        dumpper.dump();

        log.info("Runtime :"+(System.currentTimeMillis() - start));
        */
        
	}
}
