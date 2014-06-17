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
import java.util.stream.Collectors;

import jp.ac.osakau.farseerfc.purano.dep.DepEffect;
import jp.ac.osakau.farseerfc.purano.effect.ArgumentEffect;
import jp.ac.osakau.farseerfc.purano.effect.CallEffect;
import jp.ac.osakau.farseerfc.purano.effect.Effect;
import jp.ac.osakau.farseerfc.purano.effect.FieldEffect;
import jp.ac.osakau.farseerfc.purano.effect.OtherFieldEffect;
import jp.ac.osakau.farseerfc.purano.effect.StaticEffect;
import jp.ac.osakau.farseerfc.purano.util.Escaper;
import jp.ac.osakau.farseerfc.purano.util.Types;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodInsnNode;
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
    	
    	result.put("cache", Joiner.on(",").join(
    			cls.getCacheFields().getFields().stream()
    			.map(fd -> fd.dump(table))
    			.collect(Collectors.toList())));
    	
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
