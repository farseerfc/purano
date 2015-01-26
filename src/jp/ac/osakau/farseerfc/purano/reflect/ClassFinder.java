package jp.ac.osakau.farseerfc.purano.reflect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
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

import jp.ac.osakau.farseerfc.purano.util.Escaper;
import jp.ac.osakau.farseerfc.purano.util.Types;
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
        this(prefix, Arrays.asList());
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


	public static void main(@NotNull String [] argv) throws IOException {
		long start=System.currentTimeMillis();
        String targetPackage []={
        		"mit.jolden",
        		"jp.ac.osakau.farseerfc.purano"};
//                "java.time.format.DateTimeFormatterBuilder"};
//        "org.htmlparser","java.lang.Object"dolphin };
        // "org.argouml"};
//        "org.apache.catalina","java.lang.Object"};
//        "jp.ac.osakau.farseerfc.purano","org.objectweb.asm","java.lang.Object"};
//        "jp.ac.osakau.farseerfc.purano","java.lang"}
        
		if(argv.length > 1){
			targetPackage=argv;
		}
		
		String targetSource [] = {
				"/home/farseerfc/workspace/purano/src"
			};
		
		ClassFinder cf = new ClassFinder(Arrays.asList(targetPackage), Arrays.asList(targetSource));
		cf.resolveMethods();
		
		

//        ClassFinderDumpper dumpper = new DumyDumpper();
//		  ClassFinderDumpper dumpper = new StreamDumpper(ps,cf, Escaper.getDummy());
//        ClassFinderDumpper dumpper = new LegacyDumpper(cf);
        File output = new File("/tmp/output.html");
        PrintStream ps = new PrintStream(new FileOutputStream(output));
        ClassFinderDumpper dumpper = new HtmlDumpper(ps,cf);
//		ClassFinderDumpper dumpper = new StreamDumpper(ps,cf, Escaper.getDummy());
        dumpper.dump();

        log.info("Runtime :"+(System.currentTimeMillis() - start));
	}
}
