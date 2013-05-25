package jp.ac.osakau.farseerfc.purano.dep;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import jp.ac.osakau.farseerfc.purano.reflect.MethodRep;
import jp.ac.osakau.farseerfc.purano.util.Types;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final @Data class DepSet {
	private final Set<FieldDep> statics = new HashSet<>();
	// this pointer, Args, and true locals, all in the locals list, by the order
	private final Set<Integer> locals = new HashSet<>();
	
	private final Set<FieldDep> fields = new HashSet<>();
	
	public DepSet(){}
	
	public DepSet(@NotNull DepSet ... others){
		for(DepSet o : others){
			this.merge(o);
		}
	}

	public void merge(@NotNull final DepSet other){
		this.statics.addAll(other.statics);
		this.locals.addAll(other.locals);
		this.fields.addAll(other.fields);
	}
	

	public List<String> dumpDeps(@NotNull final MethodRep rep, @NotNull final Types table){
		int argCount = rep.argCount();
		MethodNode methodNode = rep.getMethodNode();
		
		List<String> arguments = new ArrayList<>();
		List<String> locals = new ArrayList<>();
		boolean thisDep=false;
		//System.err.printf("method %s ArgCount %s localV %s\n",methodNode.name,argCount,methodNode.localVariables.size());
		if(getLocals().contains(0) &&
				((methodNode.access & Opcodes.ACC_STATIC) == 0) &&
				methodNode.localVariables.size()>0){
			thisDep = true;
		}
		for(int i=thisDep?1:0;i<argCount;++i){
			if(getLocals().contains(i) && methodNode.localVariables.size()>i){
				arguments.add(String.format("%s %s",
                        table.desc2full(methodNode.localVariables.get(i).desc),
                        methodNode.localVariables.get(i).name));
			}
		}
		
		if(methodNode.localVariables != null){
			for(int i=argCount;i<methodNode.localVariables.size();++i){
				if(getLocals().contains(i) && methodNode.localVariables.size()>i){
					locals.add(String.format("%s %s",
                            table.desc2full(methodNode.localVariables.get(i).desc),
                            methodNode.localVariables.get(i).name));
				}
			}
		}

		final Function<FieldDep,String> dumper = new Function<FieldDep,String>(){
			@Override
			public String apply(@Nullable FieldDep fd) {
                assert fd != null;
                return fd.dump(table);
			}
		};
		
		List<String> result=new ArrayList<>();
		if(thisDep){
			result.add("dependThis=true");
		}
		if(arguments.size() > 0){
			result.add(String.format("dependArguments= {\"%s\"}",
					Joiner.on("\", \"").join(arguments)));
		}
//		if(locals.size()>0){
//			result.add(String.format("Locals: [%s]",
//					Joiner.on(", ").join(locals)));
//		}
		if(getFields().size() > 0){
			result.add(String.format("dependFields= {\"%s\"}",
					Joiner.on("\", \"").join(Collections2.transform(getFields(), dumper
							))));
		}
		if(getStatics().size() > 0){
			result.add(String.format("dependStaticFields= {\"%s\"}",
					Joiner.on("\", \"").join(Collections2.transform(getStatics(), dumper
							))));
		}
		
		return result;
	}
	
	public boolean dependOnThis(@NotNull MethodRep rep) {
		if( !rep.isStatic() &&
			rep.getMethodNode().localVariables.size()>0 &&
            getLocals().contains(0) ){
			return true;
		}
		return false ;
	}

	public boolean dependOnlyLocal(@NotNull MethodRep rep) {
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
        return getStatics().size() <= 0 && getFields().size() <= 0;
    }
	
	public boolean dependOnlyArgs(@NotNull MethodRep rep){
        return dependOnlyLocalArgs() && !dependOnlyLocal(rep);
    }

    public boolean dependArgsAndFields (@NotNull MethodRep rep){
        int argCount = rep.argCount();
        for(int local : getLocals()){
            if(local<argCount){
                return false;
            }
        }
        if(getStatics().size() > 0){
            return false;
        }
        return true;
    }
}
