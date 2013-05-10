package jp.ac.osakau.farseerfc.purano.dep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jp.ac.osakau.farseerfc.purano.effect.ArgumentEffect;
import jp.ac.osakau.farseerfc.purano.effect.CallEffect;
import jp.ac.osakau.farseerfc.purano.effect.Effect;
import jp.ac.osakau.farseerfc.purano.effect.OtherFieldEffect;
import jp.ac.osakau.farseerfc.purano.effect.StaticFieldEffect;
import jp.ac.osakau.farseerfc.purano.effect.ThisFieldEffect;
import jp.ac.osakau.farseerfc.purano.reflect.MethodRep;
import jp.ac.osakau.farseerfc.purano.util.Types;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;

//@EqualsAndHashCode(callSuper=false)
public class DepEffect {
	private final @Getter DepSet ret= new DepSet();
	private final @Getter Map<String,ThisFieldEffect> thisField = new HashMap<>();
	private final @Getter Map<String,OtherFieldEffect> otherField = new HashMap<>();
	private final @Getter Map<String,StaticFieldEffect> staticField = new HashMap<>(); 
	private final @Getter Set<ArgumentEffect> argumentEffect = new HashSet<>();
	private final @Getter Set<CallEffect> callEffects = new HashSet<>();
	private final @Getter Set<Effect> otherEffects = new HashSet<>();
	


	public void merge(DepEffect other){
		ret.merge(other.ret);
		for(ThisFieldEffect effect:other.getThisField().values()){
			addThisField(effect);
		}
		for(OtherFieldEffect effect:other.getOtherField().values()){
			addOtherField(effect);
		}
		for(StaticFieldEffect effect:other.getStaticField().values()){
			addStaticField(effect);
		}
		argumentEffect.addAll(other.getArgumentEffect());
		callEffects.addAll(other.getCallEffects());
		otherEffects.addAll(other.getOtherEffects());
	}

	public void addThisField(ThisFieldEffect tfe){
		if(thisField.containsKey(tfe.getKey())){
			DepSet ds = new DepSet();
			ds.merge(thisField.get(tfe.getKey()).getDeps());
			ds.merge(tfe.getDeps());
			thisField.get(tfe.getKey()).setDeps(ds);
//			thisField.get(tfe.getKey()).setDeps(deps).getDeps().merge(tfe.getDeps());
		}else{
			thisField.put(tfe.getKey(), tfe);
		}
	}
	
	public void addOtherField(OtherFieldEffect ofe){
		if(otherField.containsKey(ofe.getKey())){
			DepSet ds = new DepSet();
			ds.merge(otherField.get(ofe.getKey()).getDeps());
			ds.merge(ofe.getDeps());
			otherField.get(ofe.getKey()).setDeps(ds);
//			otherField.get(ofe.getKey()).getDeps().merge(ofe.getDeps());
		}else{
			otherField.put(ofe.getKey(), ofe);
		}
	}
	
	public void addStaticField(StaticFieldEffect sfe){
		if(staticField.containsKey(sfe.getKey())){
			DepSet ds = new DepSet();
			ds.merge(staticField.get(sfe.getKey()).getDeps());
			ds.merge(sfe.getDeps());
			staticField.get(sfe.getKey()).setDeps(ds);
//			staticField.get(sfe.getKey()).getDeps().merge(sfe.getDeps());
		}else{
			staticField.put(sfe.getKey(), sfe);
		}
	}
	
	public String dump(MethodRep rep, Types table, String prefix){

		List<String> deps= new ArrayList<>();

		for(ArgumentEffect effect: argumentEffect){
//			System.err.println("Dump "+rep.toString(table));
			if(effect.getArgPos() < rep.getMethodNode().localVariables.size()){
				deps.add(String.format("%sArgument %s:[%s]",prefix,
						rep.getMethodNode().localVariables.get(effect.getArgPos()).name,
						effect.getDeps().dumpDeps(rep, table)
						));
			}else{
				deps.add(String.format("%sArgument #%d:[%s]",prefix,
						effect.getArgPos(),
						effect.getDeps().dumpDeps(rep, table)
						));
			}
		}
		
		for(ThisFieldEffect effect: thisField.values()){
			deps.add(String.format("%sThisField %s %s#this.%s: [%s]",prefix,
					table.desc2full(effect.getDesc()),
					table.fullClassName(effect.getOwner()),
					effect.getName(), 
					effect.getDeps().dumpDeps(rep,table)));
		}
		
		
		for(OtherFieldEffect effect: otherField.values()){
			deps.add(String.format("%sOtherField %s %s#%s.%s: [%s]",prefix,
					table.desc2full(effect.getDesc()),
					table.fullClassName(effect.getOwner()),
					effect.getLeftValueDeps().dumpDeps(rep ,table),
					effect.getName(), 
					effect.getDeps().dumpDeps(rep ,table)));
		}
		
		
		for(StaticFieldEffect effect: staticField.values()){
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

	public boolean isSubset(DepEffect dec) {

		for(ThisFieldEffect e:thisField.values()){
			if(! dec.getThisField().containsValue(e)){
				return false;
			}
		}
		
		for(OtherFieldEffect e:otherField.values()){
			if(! dec.getOtherField().containsValue(e)){
				return false;
			}
		}
		
		for(StaticFieldEffect e:staticField.values()){
			if(! dec.getStaticField().containsValue(e)){
				return false;
			}
		}
		
		for(CallEffect e:callEffects){
			if(! dec.getCallEffects().contains(e)){
				return false;
			}
		}
		
		for(Effect e:otherEffects){
			if(! dec.getOtherEffects().contains(e)){
				return false;
			}
		}
		
		return true;
	}


	@Override
	public boolean equals(Object o){
		if(o instanceof DepEffect){
			return ((DepEffect)o).equals(this);
		}
		return false;
	}
	
	public boolean equals(DepEffect other){
		if(!thisField.keySet().containsAll(other.thisField.keySet())
				&& other.thisField.keySet().containsAll(thisField.keySet())){
			return false;
		}
		if(!otherField.keySet().containsAll(other.otherField.keySet())
				&& other.otherField.keySet().containsAll(otherField.keySet())){
			return false;
		}
		if(!staticField.keySet().containsAll(other.staticField.keySet())
				&& other.staticField.keySet().containsAll(staticField.keySet())){
			return false;
		}
		if(!argumentEffect.containsAll(other .argumentEffect)){
			return false;
		}
		if(!otherEffects.containsAll(otherEffects)){
			return false;
		}
		
		return true;
	}
	
	public int hashcode()
	{
		return Objects.hashCode(thisField.keySet(),otherField.keySet(),staticField.keySet(),argumentEffect,otherEffects);
	}
}
