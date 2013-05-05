package jp.ac.osakau.farseerfc.purano.dep;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import jp.ac.osakau.farseerfc.purano.effect.ArgumentEffect;
import jp.ac.osakau.farseerfc.purano.effect.CallEffect;
import jp.ac.osakau.farseerfc.purano.effect.Effect;
import jp.ac.osakau.farseerfc.purano.effect.OtherFieldEffect;
import jp.ac.osakau.farseerfc.purano.effect.StaticFieldEffect;
import jp.ac.osakau.farseerfc.purano.effect.ThisFieldEffect;
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
	private final @Getter List<ThisFieldEffect> thisField = new ArrayList<>();
	private final @Getter List<OtherFieldEffect> otherField = new ArrayList<>();
	private final @Getter List<StaticFieldEffect> staticField = new ArrayList<>(); 
	private final @Getter List<ArgumentEffect> argumentEffect = new ArrayList<>();
	private final @Getter List<CallEffect> callEffects = new ArrayList<>();
	private final @Getter List<Effect> other = new ArrayList<>();
	



	public String dump(MethodNode methodNode, Types table, String prefix){

		List<String> deps= new ArrayList<>();
		
		
		
		for(ArgumentEffect effect: argumentEffect){
			deps.add(String.format("%sArgument %s:[%s]",prefix,
					methodNode.localVariables.get(effect.getArgPos()),
					effect.getDeps().dumpDeps(methodNode, table)
					));
		}
		
		for(ThisFieldEffect effect: thisField){
			deps.add(String.format("%sThisField %s %s#this.%s: [%s]",prefix,
					table.desc2full(effect.getDesc()),
					table.fullClassName(effect.getOwner()),
					effect.getName(), 
					effect.getDeps().dumpDeps(methodNode,table)));
		}
		
		
		for(OtherFieldEffect effect: otherField){
			deps.add(String.format("%sOtherField%s %s#%s: [%s]",prefix,
					table.desc2full(effect.getDesc()),
					table.fullClassName(effect.getOwner()),
					effect.getName(), 
					effect.getDeps().dumpDeps(methodNode ,table)));
		}
		
		
		for(StaticFieldEffect effect: staticField){
			deps.add(String.format("%sStatic %s %s#%s: [%s]",prefix,
					table.desc2full(effect.getDesc()),
					table.fullClassName(effect.getOwner()),
					effect.getName(), 
					effect.getDeps().dumpDeps(methodNode,table)));
		}
		
		for(CallEffect effect: callEffects){
			deps.add(String.format("%s%sCALL %s: [%s]",prefix,
					effect.getCallType(),
					table.dumpMethodDesc(effect.getDesc(),
							String.format("%s#%s",
									table.fullClassName(effect.getOwner()), 
									effect.getName())),
									effect.getDeps().dumpDeps(methodNode ,table)));
		}
		
		
		for(Effect effect: other){
			deps.add(String.format("%s%s: [%s]",prefix,
					table.fullClassName(effect.toString()) , 
					effect.getDeps().dumpDeps(methodNode,table)));
		}
		
		return String.format("%sReturn: [%s]\n%s",prefix,
				ret.dumpDeps(methodNode,table),
				Joiner.on("\n").join(deps));
	}


}
