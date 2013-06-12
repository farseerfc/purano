package jp.ac.osakau.farseerfc.purano.dep;

import jp.ac.osakau.farseerfc.purano.effect.ArgumentEffect;
import jp.ac.osakau.farseerfc.purano.effect.StaticFieldEffect;
import jp.ac.osakau.farseerfc.purano.effect.ThisFieldEffect;
import jp.ac.osakau.farseerfc.purano.reflect.MethodRep;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.analysis.Value;

@ToString
public class DepValue implements Value {

    private @NotNull @Getter @Setter DepSet deps;
    private final @Getter DepSet lvalue;
	private final @Getter Type type;
	
	public DepValue(Type type) {
		this.type = type;
		this.deps = new DepSet();
        this.lvalue = new DepSet();
	}
	
	public DepValue(Type type,DepSet deps) {
		this.type = type;
		this.deps = new DepSet(deps);
        this.lvalue = new DepSet();
	}

    DepValue(Type type,DepSet deps,DepSet lvalue) {
        this.type = type;
        this.deps = new DepSet(deps);
        this.lvalue = lvalue;
    }

    public DepValue(@NotNull DepValue value) {
        this.type = value.type;
        this.deps = new DepSet(value.deps);
        this.lvalue = new DepSet(value.lvalue);
    }

    public int getSize() {
        return type == Type.LONG_TYPE || type == Type.DOUBLE_TYPE ? 2 : 1;
    }
    
    public boolean isReference() {
        return type != null
                && (type.getSort() == Type.OBJECT || type.getSort() == Type.ARRAY);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DepValue)) return false;

        DepValue depValue = (DepValue) o;

        if (!deps.equals(depValue.deps)) return false;
        if (!lvalue.equals(depValue.lvalue)) return false;
        if (type != null ? !type.equals(depValue.type) : depValue.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = deps.hashCode();
        result = 31 * result + lvalue.hashCode();
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    public void modify(@NotNull DepEffect effect, @NotNull MethodRep method, MethodRep from) {
        for(FieldDep fd: lvalue.getFields()){
            assert(!method.isStatic());
            if(method.isStatic()){
                throw new RuntimeException("Found this field effect in static method!");
            }
            effect.addThisField(new ThisFieldEffect(fd.getDesc(),fd.getOwner(),fd.getName(),deps,from));
        }
        for(FieldDep fd: lvalue.getStatics()){
            effect.addStaticField(new StaticFieldEffect(fd.getDesc(), fd.getOwner(), fd.getName(), deps, from));
        }
        for(int local: lvalue.getLocals()){
            assert(method.isArg(local) || local == 0);

            if (method.isStatic() || local != 0) {
                effect.getArgumentEffects().add(new ArgumentEffect(local, deps, from));
            }
        }
    }
}
