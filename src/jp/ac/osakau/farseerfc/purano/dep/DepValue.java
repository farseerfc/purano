package jp.ac.osakau.farseerfc.purano.dep;

import lombok.Getter;
import lombok.ToString;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.analysis.Value;

@ToString
public class DepValue implements Value {
	private final @Getter DepSet deps;
	private final @Getter Type type;
	
	public DepValue(Type type) {
		this.type = type;
		this.deps = new DepSet();
	}
	
	public DepValue(Type type,DepSet deps) {
		this.type = type;
		this.deps = new DepSet(deps);
	}
	
    public int getSize() {
        return type == Type.LONG_TYPE || type == Type.DOUBLE_TYPE ? 2 : 1;
    }
    
    public boolean isReference() {
        return type != null
                && (type.getSort() == Type.OBJECT || type.getSort() == Type.ARRAY);
    }
    
    public boolean isThis(){
    	return type.getSort() == Type.OBJECT && 
    			deps.getFields().size()==0 && 
    			deps.getStatics().size()==0 && 
    			deps.getLocals().size()==1 &&
    			deps.getLocals().contains(0);
    }

    @Override
    public boolean equals(final Object value) {
        if (value == this) {
            return true;
        } else if (value instanceof DepValue) {
            if (type == null) {
                return ((DepValue) value).type == null;
            } else {
                return type.equals(((DepValue) value).type);
            }
        } else {
            return false;
        }
    }
    

    @Override
    public int hashCode() {
        return type == null ? 0 : type.hashCode();
    }


}
