package jp.ac.osakau.farseerfc.purano.effect;

import java.util.ArrayList;
import java.util.List;

import jp.ac.osakau.farseerfc.purano.dep.DepSet;
import jp.ac.osakau.farseerfc.purano.reflect.MethodRep;
import jp.ac.osakau.farseerfc.purano.util.Types;
import lombok.EqualsAndHashCode;

import org.jetbrains.annotations.NotNull;

//@EqualsAndHashCode(callSuper=false)
public class NativeEffect extends Effect<NativeEffect> implements Cloneable{

	public NativeEffect(MethodRep from) {
		super(new DepSet(), from);
	}
	
	@NotNull
    @Override
	public NativeEffect clone() {
		return new NativeEffect(getFrom());
	}

	@NotNull
    @Override
	protected List<String> dumpEffect(MethodRep rep, Types table) {
		return new ArrayList<>();
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof NativeEffect){
			return equals((NativeEffect)other);
		}
		return false;
	}
	
	public boolean equals(NativeEffect other){
		return true;
	}
	
	@Override
	public int hashCode() {
		return 57;
	}
}
