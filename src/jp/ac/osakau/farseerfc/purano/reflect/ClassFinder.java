package jp.ac.osakau.farseerfc.purano.reflect;

import com.google.common.base.Joiner;
import com.martiansoftware.jsap.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.util.*;

@Slf4j
public class ClassFinder {
	@Getter final Map<String, ClassRep> classMap= new HashMap<>();

	final Set<String> classTargets = new HashSet<>() ;
    final List<String> prefix;

    private static int MAX_LOAD_PASS = 100;

	public ClassFinder(@NotNull List<String> prefix){
        findTargetClasses(prefix);
        this.prefix=prefix;
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
//            Set<MethodRep> changedSignatures = new HashSet<>();
			for (ClassRep crep : allCreps ) {
				for (MethodRep mrep : crep.getAllMethods()) {
					if (mrep.isNeedResolve(this)) {
						if (mrep.resolve(++timestamp, this)) {
							changed = true;
                            changedMethod ++;
//                            changedSignatures.add(mrep);
						}
					}
				}
			}
            changedMethodsTrace.add(changedMethod);
            log.info(String.format("Pass: %d Classes: %s Changed Method: %d [%s]",
                    pass++,allCreps.size(),changedMethod,
                    Joiner.on(", ").join(changedMethodsTrace)));
//            final int maxdump=4;
//            if(changedMethod>maxdump){
//                MethodRep [] top = new MethodRep [maxdump];
//                int i=0;
//                for(MethodRep mid : changedSignatures){
//                    if(i>=maxdump)break;
//                    top[i++]=mid;
//                }
//                System.out.println(Joiner.on(", ").join(top));
//            }else{
//                for(MethodRep m:changedSignatures){
//                    System.out.println(Joiner.on("\n").join(m.dump(this, new Types())));
//                }
//            }
		} while (changed);

        log.info("Loaded Classes: " + Joiner.on(", ").join(loadedClassesTrace));
        log.info("Changed methods: "+Joiner.on(", ").join(changedMethodsTrace));
	}
	
	private void findTargetClasses(@NotNull Collection<String> prefixes){
		
		for(String prefix:prefixes){
			Reflections reflections = new Reflections( prefix ,new SubTypesScanner(false));
	        classTargets.addAll( reflections.getStore().getSubTypesOf(Object.class.getName()));
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

    public static void analysis(String [] targetPackage) throws MalformedURLException, FileNotFoundException{
        long start=System.currentTimeMillis();

        ClassFinder cf = new ClassFinder(Arrays.asList(targetPackage));
        cf.resolveMethods();

        ClassFinderDumpper dumpper = new LegacyDumpper(cf);

        dumpper.dump();

        log.info("Runtime :"+(System.currentTimeMillis() - start));
    }

	public static void main(@NotNull String [] args) throws MalformedURLException, FileNotFoundException, JSAPException {
        JSAP jsap = new JSAP();

        Parameter outputOp = new FlaggedOption("output")
                .setStringParser(JSAP.STRING_PARSER)
                .setRequired(false)
                .setShortFlag('o')
                .setLongFlag("output")
                .setHelp("output file path. Default to stdout.");

        Parameter helpOp = new Switch("help")
                .setShortFlag('h')
                .setLongFlag("help")
                .setHelp("Show help and usage.");

        Parameter loadPassOp = new FlaggedOption("maxLoadPass")
                .setShortFlag('p')
                .setLongFlag("max-pass")
                .setStringParser(JSAP.INTEGER_PARSER)
                .setDefault("100")
                .setHelp("Max pass number of loading class.");

        Parameter targetOp = new UnflaggedOption("targetPackage")
                .setRequired(true)
                .setGreedy(true)
                .setHelp("Target packages to be analyzed. Full qualified package name is required, eg. \"org.apache\"");

        jsap.registerParameter(outputOp);
        jsap.registerParameter(helpOp);
        jsap.registerParameter(loadPassOp);
        jsap.registerParameter(targetOp);

        JSAPResult config = jsap.parse(args);

        if (!config.success() || config.getBoolean("help")) {
            // print out specific error messages describing the problems
            // with the command line, THEN print usage, THEN print full
            // help.  This is called "beating the user with a clue stick."
            for (java.util.Iterator errs = config.getErrorMessageIterator();
                 errs.hasNext();) {
                System.err.println("Error: " + errs.next());
            }

            System.err.println();
            System.err.println("Usage: java " + ClassFinder.class.getName());
            System.err.println("                " + jsap.getUsage());
            System.err.println();
            System.err.println(jsap.getHelp());
            System.exit(config.success()?0:1);
        }

        MAX_LOAD_PASS = config.getInt("maxLoadPass");
        if(MAX_LOAD_PASS <=0 ){
            System.err.println("Max-pass smaller than 0!");
            System.exit(1);
        }

        analysis(config.getStringArray("targetPackage"));
    }
}
