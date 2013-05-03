package jp.ac.osakau.farseerfc.purano.dep;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import jp.ac.osakau.farseerfc.purano.table.MethodDesc;
import jp.ac.osakau.farseerfc.purano.table.Types;
import lombok.Getter;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;

public class DepEffect {
	private final @Getter DepSet ret= new DepSet();
	private final @Getter List<ThisFieldEffect> thisField = new ArrayList<>();
	private final @Getter List<OtherFieldEffect> otherField = new ArrayList<>();
	private final @Getter List<StaticFieldEffect> staticField = new ArrayList<>(); 
	private final @Getter List<CallEffect> callEffects = new ArrayList<>();
	private final @Getter List<Effect> other = new ArrayList<>();
	


	public String dump(MethodNode methodNode, Types table){
		int argCount;
		MethodDesc p = table.method2full(methodNode.desc);
		if (((methodNode.access & Opcodes.ACC_STATIC) == 0)) {
			argCount = p.getArguments().size() + 1; // for this
		} else {
			argCount = p.getArguments().size();
		}
		
		List<String> deps= new ArrayList<>();
		for(ThisFieldEffect effect: thisField){
			deps.add(String.format("%s %s#this.%s: [%s]",
					table.desc2full(effect.getDesc()),
					table.fullClassName(effect.getOwner()),
					effect.getName(), 
					dumpDeps(methodNode, effect.getDeps(),table,argCount)));
		}
		
		
		for(OtherFieldEffect effect: otherField){
			deps.add(String.format("%s %s#%s: [%s]",
					table.desc2full(effect.getDesc()),
					table.fullClassName(effect.getOwner()),
					effect.getName(), 
					dumpDeps(methodNode, effect.getDeps(),table,argCount)));
		}
		
		
		for(StaticFieldEffect effect: staticField){
			deps.add(String.format("static %s %s#%s: [%s]",
					table.desc2full(effect.getDesc()),
					table.fullClassName(effect.getOwner()),
					effect.getName(), 
					dumpDeps(methodNode, effect.getDeps(),table,argCount)));
		}
		
		for(CallEffect effect: callEffects){
			deps.add(String.format("%sCALL %s: [%s]",
					effect.getCallType(),
					table.dumpMethodDesc(effect.getDesc(),
							String.format("%s#%s",
									table.fullClassName(effect.getOwner()), 
									effect.getName())),
					dumpDeps(methodNode, effect.getDeps(),table,argCount)));
		}
		
		
		for(Effect effect: other){
			deps.add(String.format("%s: [%s]",
					table.fullClassName(effect.toString()) , 
					dumpDeps(methodNode, effect.getDeps(),table,argCount)));
		}
		
		return String.format("Return: [%s]\n%s",
				dumpDeps(methodNode, ret,table,argCount),
				Joiner.on("\n").join(deps));
	}
	
	private String dumpDeps(final MethodNode methodNode,final DepSet dep,final Types table,final int argCount){
		List<String> argsb = new ArrayList<>();
		List<String> localsb = new ArrayList<>();
		boolean thisDep=false;
		//System.err.printf("method %s ArgCount %s localV %s\n",methodNode.name,argCount,methodNode.localVariables.size());
		if(dep.getLocals().contains(0) &&
				((methodNode.access & Opcodes.ACC_STATIC) == 0) &&
				methodNode.localVariables.size()>0){
			thisDep = true;
		}
		for(int i=thisDep?1:0;i<argCount;++i){
			if(dep.getLocals().contains(i) && methodNode.localVariables.size()>i){
				argsb.add(String.format("%s %s",
						table.desc2full(methodNode.localVariables.get(i).desc) ,
						methodNode.localVariables.get(i).name));
			}
		}
		
		if(methodNode.localVariables != null){
			for(int i=argCount;i<methodNode.localVariables.size();++i){
				if(dep.getLocals().contains(i) && methodNode.localVariables.size()>i){
					localsb.add(String.format("%s %s",
							table.desc2full(methodNode.localVariables.get(i).desc) ,
							methodNode.localVariables.get(i).name));
				}
			}
		}

		final Function<FieldDep,String> dumper = new Function<FieldDep,String>(){
			@Override @Nullable
			public String apply(@Nullable FieldDep fd) {
				return fd.dump(table);
			}
		};
		
		List<String> result=new ArrayList<>();
		if(thisDep){
			result.add("this");
		}
		if(argsb.size() > 0){
			result.add(String.format("Args: [%s]", 
					Joiner.on(", ").join(argsb)));
		}
		if(localsb.size()>0){
			result.add(String.format("Locals: [%s]", 
					Joiner.on(", ").join(localsb)));
		}
		if(dep.getFields().size() > 0){
			result.add(String.format("Fields: [%s]",
					Joiner.on(", ").join(Collections2.transform(dep.getFields(), dumper
							))));
		}
		if(dep.getStatics().size() > 0){
			result.add(String.format("Statics: [%s]",
					Joiner.on(", ").join(Collections2.transform(dep.getStatics(), dumper
							))));
		}
		
		return Joiner.on(", ").join(result);
	}

}
