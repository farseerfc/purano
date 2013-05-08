package jp.ac.osakau.farseerfc.purano.dep;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import jp.ac.osakau.farseerfc.purano.effect.ArgumentEffect;
import jp.ac.osakau.farseerfc.purano.effect.CallEffect;
import jp.ac.osakau.farseerfc.purano.effect.Effect;
import jp.ac.osakau.farseerfc.purano.effect.OtherFieldEffect;
import jp.ac.osakau.farseerfc.purano.effect.StaticFieldEffect;
import jp.ac.osakau.farseerfc.purano.effect.ThisFieldEffect;
import jp.ac.osakau.farseerfc.purano.reflect.MethodRep;
import jp.ac.osakau.farseerfc.purano.util.MethodDesc;
import jp.ac.osakau.farseerfc.purano.util.Types;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;

@EqualsAndHashCode(callSuper=false)
public class DepEffect {
	private final @Getter DepSet ret= new DepSet();
	private final @Getter Set<ThisFieldEffect> thisField = new HashSet<>();
	private final @Getter Set<OtherFieldEffect> otherField = new HashSet<>();
	private final @Getter Set<StaticFieldEffect> staticField = new HashSet<>(); 
	private final @Getter Set<ArgumentEffect> argumentEffect = new HashSet<>();
	private final @Getter Set<CallEffect> callEffects = new HashSet<>();
	private final @Getter Set<Effect> otherEffects = new HashSet<>();
	


	public void merge(DepEffect other){
		ret.merge(other.ret);
		thisField.addAll(other.getThisField());
		otherField.addAll(other.getOtherField());
		staticField.addAll(other.getStaticField());
		argumentEffect.addAll(other.getArgumentEffect());
		callEffects.addAll(other.getCallEffects());
		otherEffects.addAll(other.getOtherEffects());
	}

	public String dump(MethodRep rep, Types table, String prefix){

		List<String> deps= new ArrayList<>();

		for(ArgumentEffect effect: argumentEffect){
			deps.add(String.format("%sArgument %s:[%s]",prefix,
					rep.getMethodNode().localVariables.get(effect.getArgPos()).name,
					effect.getDeps().dumpDeps(rep, table)
					));
		}
		
		for(ThisFieldEffect effect: thisField){
			deps.add(String.format("%sThisField %s %s#this.%s: [%s]",prefix,
					table.desc2full(effect.getDesc()),
					table.fullClassName(effect.getOwner()),
					effect.getName(), 
					effect.getDeps().dumpDeps(rep,table)));
		}
		
		
		for(OtherFieldEffect effect: otherField){
			deps.add(String.format("%sOtherField%s %s#%s: [%s]",prefix,
					table.desc2full(effect.getDesc()),
					table.fullClassName(effect.getOwner()),
					effect.getName(), 
					effect.getDeps().dumpDeps(rep ,table)));
		}
		
		
		for(StaticFieldEffect effect: staticField){
			deps.add(String.format("%sStatic %s %s#%s: [%s]",prefix,
					table.desc2full(effect.getDesc()),
					table.fullClassName(effect.getOwner()),
					effect.getName(), 
					effect.getDeps().dumpDeps(rep,table)));
		}
		
		for(CallEffect effect: callEffects){
			deps.add(String.format("%s%sCALL %s: [%s]",prefix,
					effect.getCallType(),
					table.dumpMethodDesc(effect.getDesc(),
							String.format("%s#%s",
									table.fullClassName(effect.getOwner()), 
									effect.getName())),
									effect.getDeps().dumpDeps(rep ,table)));
		}
		
		
		for(Effect effect: otherEffects){
			deps.add(String.format("%s%s: [%s]",prefix,
					table.fullClassName(effect.toString()) , 
					effect.getDeps().dumpDeps(rep,table)));
		}
		
		return String.format("%sReturn: [%s]\n%s",prefix,
				ret.dumpDeps(rep,table),
				Joiner.on("\n").join(deps));
	}


}
