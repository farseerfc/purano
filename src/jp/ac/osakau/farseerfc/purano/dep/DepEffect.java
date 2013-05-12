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
import jp.ac.osakau.farseerfc.purano.util.Escape;
import jp.ac.osakau.farseerfc.purano.util.Types;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

//@EqualsAndHashCode(callSuper=false)
public class DepEffect {
	private final @Getter DepSet ret= new DepSet();
	private final @Getter Map<String,ThisFieldEffect> thisField = new HashMap<>();
	private final @Getter Map<String,OtherFieldEffect> otherField = new HashMap<>();
	private final @Getter Map<String,StaticFieldEffect> staticField = new HashMap<>(); 
	private final @Getter Set<ArgumentEffect> argumentEffects = new HashSet<>();
	private final @Getter Set<CallEffect> callEffects = new HashSet<>();
	private final @Getter Set<Effect> otherEffects = new HashSet<>();
	


	public void merge(DepEffect other,MethodRep over){
		ret.merge(other.ret);
		for(ThisFieldEffect effect:other.getThisField().values()){
			addThisField((ThisFieldEffect)effect.duplicate(over));
		}
		for(OtherFieldEffect effect:other.getOtherField().values()){
			addOtherField((OtherFieldEffect)effect.duplicate(over));
		}
		for(StaticFieldEffect effect:other.getStaticField().values()){
			addStaticField((StaticFieldEffect)effect.duplicate(over));
		}
		for(ArgumentEffect effect: other.getArgumentEffects()){
			argumentEffects.add((ArgumentEffect)effect.duplicate(over));
		}
		for(CallEffect effect: other.getCallEffects()){
			callEffects.add((CallEffect)effect.duplicate(over));
		}
		for(Effect effect: other.getOtherEffects()){
			otherEffects.add((Effect)effect.duplicate(over));
		}
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
//			DepSet ds = new DepSet();
//			ds.merge(otherField.get(ofe.getKey()).getDeps());
//			ds.merge(ofe.getDeps());
//			otherField.get(ofe.getKey()).setDeps(ds);
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

		deps.add(String.format("%s@%s[%s]",prefix,
				Escape.annotation("Return"),
				Escape.effect(ret.dumpDeps(rep,table))));
		
		for(ArgumentEffect effect: argumentEffects){
			deps.add(effect.dump(rep, table, prefix));
		}
		
		for(ThisFieldEffect effect: thisField.values()){
			deps.add(effect.dump(rep, table, prefix));
		}
		
		
		for(OtherFieldEffect effect: otherField.values()){
			deps.add(effect.dump(rep, table, prefix));
		}
		
		
		for(StaticFieldEffect effect: staticField.values()){
			deps.add(effect.dump(rep, table, prefix));
		}
		
		for(CallEffect effect: callEffects){
			deps.add(effect.dump(rep, table, prefix));
		}
		
		
		for(Effect effect: otherEffects){
			deps.add(effect.dump(rep, table, prefix));
		}
		
		return Joiner.on("\n").join(deps);
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
		if(!argumentEffects.containsAll(other .argumentEffects)){
			return false;
		}
		if(!otherEffects.containsAll(otherEffects)){
			return false;
		}
		
		return true;
	}
	
	public int hashcode()
	{
		return Objects.hashCode(thisField.keySet(),otherField.keySet(),staticField.keySet(),argumentEffects,otherEffects);
	}
}
