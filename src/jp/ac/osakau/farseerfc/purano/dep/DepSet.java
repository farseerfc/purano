package jp.ac.osakau.farseerfc.purano.dep;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import jp.ac.osakau.farseerfc.purano.reflect.MethodRep;
import jp.ac.osakau.farseerfc.purano.util.MethodDesc;
import jp.ac.osakau.farseerfc.purano.util.Types;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;

import lombok.Data;

public @Data class DepSet {
	private final Set<FieldDep> statics = new HashSet<>();
	// this pointer, Args, and true locals, all in the locals list, by the order
	private final Set<Integer> locals = new HashSet<>();
	
	private final Set<FieldDep> fields = new HashSet<>();
	
	public DepSet(){}
	
	public DepSet(DepSet ... others){
		for(DepSet o : others){
			this.merge(o);
		}
	}

	public void merge(DepSet other){
		this.statics.addAll(other.statics);
		this.locals.addAll(other.locals);
		this.fields.addAll(other.fields);
	}
	

	public String dumpDeps(final MethodRep rep, final Types table){
		int argCount = rep.argCount();
		MethodNode methodNode = rep.getMethodNode();
		
		List<String> argsb = new ArrayList<>();
		List<String> localsb = new ArrayList<>();
		boolean thisDep=false;
		//System.err.printf("method %s ArgCount %s localV %s\n",methodNode.name,argCount,methodNode.localVariables.size());
		if(getLocals().contains(0) &&
				((methodNode.access & Opcodes.ACC_STATIC) == 0) &&
				methodNode.localVariables.size()>0){
			thisDep = true;
		}
		for(int i=thisDep?1:0;i<argCount;++i){
			if(getLocals().contains(i) && methodNode.localVariables.size()>i){
				argsb.add(String.format("%s %s",
						table.desc2full(methodNode.localVariables.get(i).desc) ,
						methodNode.localVariables.get(i).name));
			}
		}
		
		if(methodNode.localVariables != null){
			for(int i=argCount;i<methodNode.localVariables.size();++i){
				if(getLocals().contains(i) && methodNode.localVariables.size()>i){
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
		if(getFields().size() > 0){
			result.add(String.format("Fields: [%s]",
					Joiner.on(", ").join(Collections2.transform(getFields(), dumper
							))));
		}
		if(getStatics().size() > 0){
			result.add(String.format("Statics: [%s]",
					Joiner.on(", ").join(Collections2.transform(getStatics(), dumper
							))));
		}
		
		return Joiner.on(", ").join(result);
	}
	
	public boolean dependOnThis(MethodRep rep) {
		boolean thisDep=false;
		if(getLocals().contains(0) &&
				((rep.getMethodNode().access & Opcodes.ACC_STATIC) == 0) &&
				rep.getMethodNode().localVariables.size()>0){
			thisDep = true;
		}
		return thisDep;
	}

	public boolean dependOnlyLocal(MethodRep rep) {
		int argCount = rep.argCount();
		
		if(getStatics().size() > 0){
			return false;
		}
		if(getFields().size() > 0){
			return false;
		}
		for(int local : getLocals()){
			if(local<argCount){
				return false;
			}
		}
		return true;
	}
	
	public boolean dependOnlyLocalArgs() {
		if(getStatics().size() > 0){
			return false;
		}
		if(getFields().size() > 0){
			return false;
		}
		return true;
	}
	
	public boolean dependOnlyArgs(MethodRep rep){
		if(!dependOnlyLocalArgs()){
			return false;
		}
		if(dependOnlyLocal(rep)){
			return false;
		}
		return true;
	}
}
