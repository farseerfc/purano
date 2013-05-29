package jp.ac.osakau.farseerfc.purano.dep;

import com.google.common.base.Joiner;
import jp.ac.osakau.farseerfc.purano.effect.*;
import jp.ac.osakau.farseerfc.purano.reflect.MethodRep;
import jp.ac.osakau.farseerfc.purano.util.Escape;
import jp.ac.osakau.farseerfc.purano.util.Types;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;

import java.util.*;

//@EqualsAndHashCode(callSuper=false)
public class DepEffect {
	private final @Getter DepValue returnDep = new DepValue((Type)null);
	private final @Getter Map<String,ThisFieldEffect> thisField = new HashMap<>();
	private final @Getter Map<String,OtherFieldEffect> otherField = new HashMap<>();
	private final @Getter Map<String,StaticFieldEffect> staticField = new HashMap<>(); 
	private final @Getter Set<ArgumentEffect> argumentEffects = new HashSet<>();
	private final @Getter Set<CallEffect> callEffects = new HashSet<>();
	private final @Getter Set<Effect> otherEffects = new HashSet<>();
	


	public void merge(@NotNull DepEffect other,MethodRep over){
		returnDep.getLvalue().merge(other.returnDep.getLvalue());
        returnDep.getDeps().merge(other.returnDep.getDeps());

		for(ThisFieldEffect effect:other.getThisField().values()){
			addThisField(effect.duplicate(over));
		}
		for(OtherFieldEffect effect:other.getOtherField().values()){
			addOtherField(effect.duplicate(over));
		}
		for(StaticFieldEffect effect:other.getStaticField().values()){
			addStaticField(effect.duplicate(over));
		}
		for(ArgumentEffect effect: other.getArgumentEffects()){
			argumentEffects.add(effect.duplicate(over));
		}
		for(CallEffect effect: other.getCallEffects()){
			callEffects.add(effect.duplicate(over));
		}
		for(Effect effect: other.getOtherEffects()){
			otherEffects.add(effect.duplicate(over));
		}
	}

	public void addThisField(@NotNull ThisFieldEffect tfe){
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
	
	public void addOtherField(@NotNull OtherFieldEffect ofe){
        if (!otherField.containsKey(ofe.getKey())) {
            otherField.put(ofe.getKey(), ofe);
        }
//        else {
//			DepSet ds = new DepSet();
//			ds.merge(otherField.get(ofe.getKey()).getDeps());
//			ds.merge(ofe.getDeps());
//			otherField.get(ofe.getKey()).setDeps(ds);
//			otherField.get(ofe.getKey()).getDeps().merge(ofe.getDeps());
//        }
    }
	
	public void addStaticField(@NotNull StaticFieldEffect sfe){
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
	
	public String dump(@NotNull MethodRep rep, @NotNull Types table, String prefix){

		List<String> deps= new ArrayList<>();

        if(!returnDep.getDeps().isEmpty()){
		    deps.add(String.format("%s@%s(%s)",prefix,
				Escape.annotation("ReturnDepend"),
				Escape.effect(Joiner.on(", ").join(returnDep.getDeps().dumpDeps(rep, table)))));
        }
        if(!returnDep.getLvalue().isEmpty()){
            deps.add(String.format("%s@%s(%s)",prefix,
                    Escape.annotation("ReturnLvalue"),
                    Escape.effect(Joiner.on(", ").join(returnDep.getLvalue().dumpDeps(rep, table)))));
        }
		
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

	public boolean isSubset(@NotNull DepEffect dec) {

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DepEffect depEffect = (DepEffect) o;

        return argumentEffects.equals(depEffect.argumentEffects) &&
                callEffects.equals(depEffect.callEffects) &&
                otherEffects.equals(depEffect.otherEffects) &&
                otherField.equals(depEffect.otherField) &&
                returnDep.equals(depEffect.returnDep) &&
                staticField.equals(depEffect.staticField) &&
                thisField.equals(depEffect.thisField);

    }

    @Override
    public int hashCode() {
        int result = returnDep.hashCode();
        result = 31 * result + thisField.hashCode();
        result = 31 * result + otherField.hashCode();
        result = 31 * result + staticField.hashCode();
        result = 31 * result + argumentEffects.hashCode();
        result = 31 * result + callEffects.hashCode();
        result = 31 * result + otherEffects.hashCode();
        return result;
    }
}
