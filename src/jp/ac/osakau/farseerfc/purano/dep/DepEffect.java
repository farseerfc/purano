package jp.ac.osakau.farseerfc.purano.dep;

import com.google.common.base.Joiner;
import jp.ac.osakau.farseerfc.purano.effect.*;
import jp.ac.osakau.farseerfc.purano.reflect.MethodRep;
import jp.ac.osakau.farseerfc.purano.util.Escaper;
import jp.ac.osakau.farseerfc.purano.util.Types;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import java.util.*;

//@EqualsAndHashCode(callSuper=false)
public class DepEffect {
	@Nullable
    private final @Getter DepValue returnDep = new DepValue((Type)null);
	private final @Getter Map<String,FieldEffect> thisField = new HashMap<>();
	private final @Getter Map<String,OtherFieldEffect> otherField = new HashMap<>();
	private final @Getter Map<String,StaticEffect> staticField = new HashMap<>();
	private final @Getter Set<ArgumentEffect> argumentEffects = new HashSet<>();
	private final @Getter Set<CallEffect> callEffects = new HashSet<>();
	private final @Getter Set<Effect> otherEffects = new HashSet<>();
	


	public void merge(@NotNull DepEffect other,MethodRep from){
		returnDep.getLvalue().merge(other.returnDep.getLvalue());
        returnDep.getDeps().merge(other.returnDep.getDeps());

		for(FieldEffect effect:other.getThisField().values()){
			addThisField(effect.duplicate(from));
		}
		for(OtherFieldEffect effect:other.getOtherField().values()){
			addOtherField(effect.duplicate(from));
		}
		for(StaticEffect effect:other.getStaticField().values()){
			addStaticField(effect.duplicate(from));
		}
		for(ArgumentEffect effect: other.getArgumentEffects()){
			argumentEffects.add(effect.duplicate(from));
		}
		for(CallEffect effect: other.getCallEffects()){
			callEffects.add(effect.duplicate(from));
		}
		for(Effect effect: other.getOtherEffects()){
			otherEffects.add(effect.duplicate(from));
		}
	}

	public void addThisField(@NotNull FieldEffect tfe){
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
	
	public void addStaticField(@NotNull StaticEffect sfe){
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
	
	public String dump(@NotNull MethodRep rep, @NotNull Types table, String prefix, Escaper esc){

		List<String> deps= new ArrayList<>();

        if(!returnDep.getDeps().isEmpty()){
		    deps.add(String.format("%s@%s(%s)",prefix,
				esc.annotation("Depend"),
				esc.effect(Joiner.on(", ").join(returnDep.getDeps().dumpDeps(rep, table)))));
        }
        if(!returnDep.getLvalue().isEmpty()){
            deps.add(String.format("%s@%s(%s)",prefix,
                    esc.annotation("Expose"),
                    esc.effect(Joiner.on(", ").join(returnDep.getLvalue().dumpDeps(rep, table)))));
        }
		
		for(ArgumentEffect effect: argumentEffects){
			deps.add(effect.dump(rep, table, prefix, esc));
		}
		
		for(FieldEffect effect: thisField.values()){
			deps.add(effect.dump(rep, table, prefix, esc));
		}
		
		
		for(OtherFieldEffect effect: otherField.values()){
			deps.add(effect.dump(rep, table, prefix, esc));
		}
		
		
		for(StaticEffect effect: staticField.values()){
			deps.add(effect.dump(rep, table, prefix, esc));
		}
		
		for(CallEffect effect: callEffects){
			deps.add(effect.dump(rep, table, prefix, esc));
		}
		
		
		for(Effect effect: otherEffects){
			deps.add(effect.dump(rep, table, prefix, esc));
		}
		
		return Joiner.on("\n").join(deps);
	}

	public boolean isSubset(@NotNull DepEffect dec) {

		for(FieldEffect e:thisField.values()){
			if(! dec.getThisField().containsValue(e)){
				return false;
			}
		}
		
		for(OtherFieldEffect e:otherField.values()){
			if(! dec.getOtherField().containsValue(e)){
				return false;
			}
		}
		
		for(StaticEffect e:staticField.values()){
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
    public boolean equals(@Nullable Object o) {
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
