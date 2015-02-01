package jp.ac.osakau.farseerfc.purano.reflect;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

import jp.ac.osakau.farseerfc.purano.ano.Purity;
import jp.ac.osakau.farseerfc.purano.dep.DepEffect;
import jp.ac.osakau.farseerfc.purano.dep.DepFrame;
import jp.ac.osakau.farseerfc.purano.dep.DepValue;
import jp.ac.osakau.farseerfc.purano.effect.ArgumentEffect;
import jp.ac.osakau.farseerfc.purano.effect.CallEffect;
import jp.ac.osakau.farseerfc.purano.effect.Effect;
import jp.ac.osakau.farseerfc.purano.effect.FieldEffect;
import jp.ac.osakau.farseerfc.purano.effect.OtherFieldEffect;
import jp.ac.osakau.farseerfc.purano.effect.StaticEffect;
import jp.ac.osakau.farseerfc.purano.util.Escaper;
import jp.ac.osakau.farseerfc.purano.util.Types;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import com.google.common.base.Joiner;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;

public class HtmlDumpper implements ClassFinderDumpper {
    private final PrintStream out;
    private final ClassFinder cf;
    private final Escaper esc;
    private final Configuration cfg;
    private final Types table;
    
    private static final boolean includeNonTargetEH = true;

    public HtmlDumpper(PrintStream out, ClassFinder cf) throws IOException {
        this.out = out;
        this.cf = cf;
        this.esc = Escaper.getHtml();
        
        this.cfg = new Configuration();
        cfg.setDirectoryForTemplateLoading(new File("templates"));
        cfg.setObjectWrapper(new DefaultObjectWrapper());
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
        cfg.setIncompatibleImprovements(new Version(2, 3, 20));
        
        this.table = new Types();
    }
    

    public List<String> dumpStatics(){
    	List<String> result = new ArrayList<>();
        int method = 0, unknown = 0, stateless = 0, stateful = 0, modifier =0 ;
        int hmethod = 0, hunknown = 0, hstateless = 0, hstateful = 0, hmodifier =0, esln = 0 , esfn=0, en = 0 ;
        int emethod = 0, eunknown = 0, estateless = 0, estateful = 0, emodifier =0, hsln = 0 , hsfn=0, hn = 0 ;
        int fieldM = 0, staticM = 0, argM = 0, nativeE = 0;
        int classes = 0;

        List<String> sb = new ArrayList<>();

        for(String clsName : cf.classMap.keySet()){
            boolean isTarget = cf.classTargets.contains(clsName);
            for(String p:cf.prefix){
                if(clsName.startsWith(p)){
                    isTarget = true;
                }
            }

            if (!includeNonTargetEH && !isTarget) {
                continue;
            }
            for(MethodRep mtd: cf.classMap.get(clsName).getAllMethods()){
                int p=mtd.purity();
                p = p & (~Purity.Native);
                if(mtd.getInsnNode().name.equals("equals") && mtd.getInsnNode().desc.equals("(Ljava/lang/Object;)Z")){
                    emethod++;
                    if(p == Purity.Unknown){
                        eunknown ++;
                    }
                    if(p == Purity.Stateless){
                        estateless ++;
                        sb.add("Equals Stateless:" + mtd.toString(new Types()));
                    }else if(p == Purity.Stateful){
                        estateful ++;
                        sb.add("Equals Stateful:" + mtd.toString(new Types()));
                    }else{
                        emodifier ++;
                        sb.add("Equals Motifier:" + mtd.toString(new Types()));
                        if(p==(Purity.Stateless | Purity.Native)){
                            esln ++ ;
                        }else if (p==(Purity.Stateless | Purity.Native)){
                            esfn ++ ;
                        }

                        if((p | Purity.Native)>0){
                            en++;
                        }
                    }
                }
                if(mtd.getInsnNode().name.equals("hashCode")&& mtd.getInsnNode().desc.equals("()I")){
                    hmethod++;
                    if(p == Purity.Unknown){
                        hunknown ++;
                    }
                    if(p == Purity.Stateless){
                        hstateless ++;
                        sb.add("hashCode Stateless:" + mtd.toString(new Types()));
                    }else if(p == Purity.Stateful){
                        hstateful ++;
                        sb.add("hashCode Stateful:" + mtd.toString(new Types()));
                    }else{
                        hmodifier ++;
                        sb.add("hashCode Modifier:" + mtd.toString(new Types()));
                        if(p==(Purity.Stateless | Purity.Native)){
                            hsln ++ ;
                        }else if (p==(Purity.Stateless | Purity.Native)){
                            hsfn ++ ;
                        }
                        if((p | Purity.Native)>0){
                            hn++;
                        }
                    }
                }
            }
            
            if (includeNonTargetEH && !isTarget) {
                continue;
            }
            
            ClassRep cls = cf.classMap.get(clsName);
            for(MethodRep mtd: cls.getAllMethods()){
                method++;
                int p=mtd.purity();
                p = p & (~Purity.Native);
                if(p == Purity.Unknown){
                    unknown ++;
                }
                if(p == Purity.Stateless){
                    stateless ++;
                }else if(p == Purity.Stateful){
                    stateful ++;
                }else{
                    modifier ++;
                }
                if((p & Purity.ArgumentModifier)>0){
                    argM ++;
                }
                if((p & Purity.FieldModifier)>0){
                    fieldM ++;
                }
                if((p & Purity.StaticModifier)>0){
                    staticM ++;
                }
                if((p & Purity.Native)>0){
                    nativeE ++;
                }

            }
            classes ++;
        }

        result.add("class " + classes);
        result.add("method "+method);
        result.add("unknown "+unknown);
        result.add("stateless "+stateless);
        result.add("stateful "+stateful);
        result.add("modifier "+modifier);

        result.add("fieldM "+fieldM);
        result.add("staticM "+staticM);
        result.add("argM "+argM);
        result.add("nativeE "+nativeE);

        result.add("emethod "+emethod);
        result.add("eunknown "+eunknown);
        result.add("estateless "+estateless);
        result.add("estateful "+estateful);
        result.add("emodifier "+emodifier);
        result.add("esln "+esln);
        result.add("esfn "+esfn);
        result.add("en "+en);

        result.add("hmethod "+hmethod);
        result.add("hunknown "+hunknown);
        result.add("hstateless "+hstateless);
        result.add("hstateful "+hstateful);
        result.add("hmodifier "+hmodifier);
        result.add("hsln "+hsln);
        result.add("hsfn "+hsfn);
        result.add("hn "+hn);
        
        return result;
    }

    @Override
    public void dump() {
        Map<String, List<String>> result = new HashMap<>();
        result.put("classes", new ArrayList<>());
        for (String clsName : cf.classMap.keySet()) {
            boolean isTarget = cf.classTargets.contains(clsName) || 
            		cf.prefix.stream().anyMatch(p -> clsName.startsWith(p));
            if (!isTarget) {
                continue;
            }
            ClassRep cls = cf.classMap.get(clsName);
            result.get("classes").add(dumpClass(cls));
        }
        
        result.put("imports", new ArrayList<>(table.getImports()));
        result.put("package", Arrays.asList(table.getPackageName()==null ? "" : table.getPackageName()));
        
        result.put("stat", dumpStatics());
      
		try {
			Template tmpl = cfg.getTemplate("main.ftl");
			tmpl.process(result, new OutputStreamWriter(out));
		} catch (TemplateException | IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
    }
    
    
    public String dumpClass(ClassRep cls){
    	Map<String, Object> result = new HashMap<>();
    	result.put("name", table.fullClassName(cls.getName()));
    	result.put("methods", cls.getAllMethods().stream()
    			.map(method -> dumpMethod(method))
    			.collect(Collectors.toList()));
    	
//    	result.put("cache", Joiner.on(",").join(
//    			cls.getCacheFields().stream()
//    			.map(fd -> fd.dump(table))
//    			.collect(Collectors.toList())));
    	result.put("caches", cls.getFieldWrite().entrySet().stream()
    			.map(entry -> entry.getKey().dump(table) + ": " +
    					Joiner.on("<br/>").join(entry.getValue().stream()
    							.map(e -> table.dumpMethodDesc(e.getInsnNode().desc, e.getInsnNode().name))
    							.collect(Collectors.toList())))
    			.collect(Collectors.toList()));
    	
//    	result.put("source", cls.getSource());

//    	result.put("caches", cls.getFieldWrite().entrySet().stream()
//    			.map(entry -> entry.getKey().dump(table) + ": " +
//    					entry.getValue().size())
//    			.collect(Collectors.toList()));
    	try {
			Template tmpl = cfg.getTemplate("class.ftl");
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			tmpl.process(result, new OutputStreamWriter(out));
			return out.toString("utf-8");
		} catch (TemplateException | IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
    	return null;
    }

    
    public String dumpMethod(MethodRep method){
    	Map<String, Object> result = new HashMap<>();
    	    	
    	if(method.getMethodNode() != null){
			result.put("name", esc.methodName(method.toString(table)));
			List<String> overrides=method.getOverrided().values().stream()
					.map( rep -> rep.toString(table))
					.collect(Collectors.toList());
			result.put("overrides", overrides == null ? "" : overrides);
			
			
			result.put("resolvedCalls", new ArrayList<String>());
			result.put("unknownCalls", new ArrayList<String>());
            if(method.getDynamicEffects() != null ){
                for(MethodInsnNode insn : method.getCalls()){
                    //log.info("Load when dump {}",Types.binaryName2NormalName(insn.owner));
                    if(cf.getClassMap().containsKey(Types.binaryName2NormalName(insn.owner))){
                        MethodRep mr = cf.loadClass(Types.binaryName2NormalName(insn.owner)).
                                getMethodVirtual(MethodRep.getId(insn));
                        if(mr != null){
                            ((List)result.get("resolvedCalls")).add(mr.toString(table));
                        }
                    }else{
                    	((List)result.get("unknownCalls")).add(table.dumpMethodDesc(insn.desc,
                                        String.format("%s#%s",table.fullClassName(insn.owner),
                                        		insn.name)));
                    }
                }
                result.put("purity", esc.purity(method.dumpPurity()));
                result.put("effects", dumpEffects(method.getDynamicEffects(), method, table, "", esc));
            }else{
            	result.put("purity", Arrays.asList(""));
            	result.put("effects", Arrays.asList(""));
            }
            

			result.put("asm", dumpMethodAsm(method));
			result.put("source", dumpMethodSource(method));
			
		}else{
			return "";
		}

    	try {
			Template tmpl = cfg.getTemplate("method.ftl");
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			tmpl.process(result, new OutputStreamWriter(out));
			return out.toString("utf-8");
		} catch (TemplateException | IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
    	return "";
    }


	private String dumpMethodSource(MethodRep method) {
		String sourceCode= method.getSource();
		
		MethodDeclaration sourceNode = method.getSourceNode();
		CompilationUnit unit = method.getUnit();
		
		if(sourceNode != null){
			ASTStatementVisitor visitor = new ASTStatementVisitor(unit);
			sourceNode.accept(visitor);
		
			sourceCode += "\n<<<<<<<<<<<<<<<<<<<<\n";
			
			List<String> sourcelines = new ArrayList<>();
			for(int line : new TreeSet<Integer>(visitor.getLineMap().keySet())){
				sourcelines.add(String.format("%d: [%s]", line,
						visitor.getLineMap().get(line)
						.stream()
						.map( (x) -> String.format("\"%s\"", x.toString().trim() ))
						.collect(Collectors.joining(", "))));
			}
			sourceCode += Joiner.on("\n").join(sourcelines);
			
			String position = String.format("%s: (%d-%d):\n", 
					method.getSourceFile()==null?"":method.getSourceFile(),
							method.getSourceBegin(),
							method.getSourceEnd()
			);
			return position + sourceCode;
		}
		return "";
	}
    
    public String dumpMethodAsm(@NotNull MethodRep method){
    	ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(out);
        
        Textifier text = new Textifier(Opcodes.ASM5){
        	@Override public void visitMethodEnd(){
        		super.visitMethodEnd();
        		print(pw);
        		pw.flush();
        	}
        };
        TraceMethodVisitor tmv = new TraceMethodVisitor(text);
        method.getMethodNode().accept(tmv);
        
        pw.append("<<<<<<<<<<<<<<\n");
        
        for(DepFrame frame: method.getFrames()){
        	if(frame==null) continue;
        	int line = -1;
        	if (frame.getLine() != null){
        		line = frame.getLine().line;
        	}
        	AbstractInsnNode node = frame.getNode();
        	if(node.getOpcode()>0 && node.getOpcode()< Printer.OPCODES.length){
	        	String opcode = Printer.OPCODES[node.getOpcode()];
	        	pw.append(String.format("%5d: %s\n", line, opcode));
	        	pw.append(String.format("%s\n",frame.getEffects().dump(method, table, "", esc)));
	        	pw.flush();
        	}
        }
        
        try {
			return out.toString("utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
        return "";
    }
    
    public List<String> dumpEffects(DepEffect depeffect, @NotNull MethodRep rep, @NotNull Types table, String prefix, Escaper esc){
		List<String> deps= new ArrayList<>();

        if(!depeffect.getReturnDep().getDeps().isEmpty()){
		    deps.add(String.format("%s@%s(%s)",prefix,
				esc.annotation("Depend"),
				esc.effect(Joiner.on(", ").join(depeffect.getReturnDep().getDeps().dumpDeps(rep, table)))));
        }
        if(!depeffect.getReturnDep().getLvalue().isEmpty()){
            deps.add(String.format("%s@%s(%s)",prefix,
                    esc.annotation("Expose"),
                    esc.effect(Joiner.on(", ").join(depeffect.getReturnDep().getLvalue().dumpDeps(rep, table)))));
        }
		
		for(ArgumentEffect effect: depeffect.getArgumentEffects()){
			deps.add(effect.dump(rep, table, prefix, esc));
		}
		
		for(FieldEffect effect: depeffect.getThisField().values()){
			deps.add(effect.dump(rep, table, prefix, esc));
		}
		
		
		for(OtherFieldEffect effect: depeffect.getOtherField().values()){
			deps.add(effect.dump(rep, table, prefix, esc));
		}
		
		
		for(StaticEffect effect: depeffect.getStaticField().values()){
			deps.add(effect.dump(rep, table, prefix, esc));
		}
		
		for(CallEffect effect: depeffect.getCallEffects()){
			deps.add(effect.dump(rep, table, prefix, esc));
		}
		
		
		for(Effect effect: depeffect.getOtherEffects()){
			deps.add(effect.dump(rep, table, prefix, esc));
		}
		return deps;
	}
}
