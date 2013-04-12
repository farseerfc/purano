package jp.ac.osakau.farseerfc.asm.visitor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import jp.ac.osakau.farseerfc.asm.dep.ArrayStoreEffect;
import jp.ac.osakau.farseerfc.asm.dep.CallEffect;
import jp.ac.osakau.farseerfc.asm.dep.DepEffect;
import jp.ac.osakau.farseerfc.asm.dep.DepSet;
import jp.ac.osakau.farseerfc.asm.dep.DepValue;
import jp.ac.osakau.farseerfc.asm.dep.FieldDep;
import jp.ac.osakau.farseerfc.asm.dep.OtherFieldEffect;
import jp.ac.osakau.farseerfc.asm.dep.StaticFieldEffect;
import jp.ac.osakau.farseerfc.asm.dep.ThisFieldEffect;
import jp.ac.osakau.farseerfc.asm.dep.ThrowEffect;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Interpreter;

import com.google.common.base.Joiner;

public class DepInterpreter extends Interpreter<DepValue> implements Opcodes{
	
	private final DepEffect effect;
	

	public DepInterpreter(DepEffect effect) {
		super(ASM4);
		this.effect = effect;
	}
    
    private String opcode2string(int opcode){
    	List<String> result = new ArrayList<>();
		for(Field f: Opcodes.class.getFields()){
			if(!f.getName().startsWith("ACC_")){
				int v = 0;
				try {
					
					v = f.getInt(f);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					//e.printStackTrace();
				}
				if(opcode == v){
					result.add(f.getName());
				}
			}
		}
		return Joiner.on(" ").join(result);
    }

    @Override
    public DepValue newValue(final Type type) {
        if (type == null) {
            return new DepValue(null);
        }
        switch (type.getSort()) {
        case Type.VOID:
            return null;
        case Type.BOOLEAN:
        case Type.CHAR:
        case Type.BYTE:
        case Type.SHORT:
        case Type.INT:
            return new DepValue(Type.INT_TYPE);
        case Type.FLOAT:
            return new DepValue(Type.FLOAT_TYPE);
        case Type.LONG:
            return new DepValue(Type.LONG_TYPE);
        case Type.DOUBLE:
            return new DepValue(Type.DOUBLE_TYPE);
        case Type.ARRAY:
        case Type.OBJECT:
            return new DepValue(Type.getObjectType("java/lang/Object"));
        default:
            throw new Error("Internal error");
        }
    }

	@Override
	public DepValue newOperation(final AbstractInsnNode insn)
			throws AnalyzerException {
		switch (insn.getOpcode()) {
		case ACONST_NULL:
			return newValue(Type.getObjectType("null"));
		case ICONST_M1:
		case ICONST_0:
		case ICONST_1:
		case ICONST_2:
		case ICONST_3:
		case ICONST_4:
		case ICONST_5:
			return new DepValue(Type.INT_TYPE);
		case LCONST_0:
		case LCONST_1:
			return new DepValue(Type.LONG_TYPE);
		case FCONST_0:
		case FCONST_1:
		case FCONST_2:
			return new DepValue(Type.FLOAT_TYPE);
		case DCONST_0:
		case DCONST_1:
			return new DepValue(Type.DOUBLE_TYPE);
		case BIPUSH:
		case SIPUSH:
			return new DepValue(Type.INT_TYPE);
		case LDC:
			Object cst = ((LdcInsnNode) insn).cst;
			if (cst instanceof Integer) {
				return new DepValue(Type.INT_TYPE);
			} else if (cst instanceof Float) {
				return new DepValue(Type.FLOAT_TYPE);
			} else if (cst instanceof Long) {
				return new DepValue(Type.LONG_TYPE);
			} else if (cst instanceof Double) {
				return new DepValue(Type.DOUBLE_TYPE);
			} else if (cst instanceof String) {
				return newValue(Type.getObjectType("java/lang/String"));
			} else if (cst instanceof Type) {
				int sort = ((Type) cst).getSort();
				if (sort == Type.OBJECT || sort == Type.ARRAY) {
					return newValue(Type.getObjectType("java/lang/Class"));
				} else if (sort == Type.METHOD) {
					return newValue(Type
							.getObjectType("java/lang/invoke/MethodType"));
				} else {
					throw new IllegalArgumentException("Illegal LDC constant "
							+ cst);
				}
			} else if (cst instanceof Handle) {
				return newValue(Type
						.getObjectType("java/lang/invoke/MethodHandle"));
			} else {
				throw new IllegalArgumentException("Illegal LDC constant "
						+ cst);
			}
		case JSR:
			return new DepValue(Type.VOID_TYPE);
		case GETSTATIC:
			{ 
				FieldInsnNode fin = (FieldInsnNode) insn;
				DepValue v =newValue(Type.getType(fin.desc));
				v.getDeps().getStatics().add(new FieldDep(fin.desc,fin.owner,fin.name));
				
				return v;
			}
		case NEW:
			return newValue(Type.getObjectType(((TypeInsnNode) insn).desc));
			
		default:
        	System.err.println("Unknow copyOperation "+opcode2string(insn.getOpcode()));
			//throw new Error("Internal error.");
        	return null;
		}
	}

    @Override
    public DepValue copyOperation(final AbstractInsnNode insn,
            final DepValue value) throws AnalyzerException {
    	DepSet deps = value.getDeps();
    	
        switch (insn.getOpcode()){
        case ILOAD:
        	deps.getLocals().add(((VarInsnNode) insn).var);
        	return new DepValue(Type.INT_TYPE,deps);
        case LLOAD:
        	deps.getLocals().add(((VarInsnNode) insn).var);
        	return new DepValue(Type.LONG_TYPE,deps);
        case FLOAD:
        	deps.getLocals().add(((VarInsnNode) insn).var);
        	return new DepValue(Type.FLOAT_TYPE,deps);
        case DLOAD:
        	deps.getLocals().add(((VarInsnNode) insn).var);
        	return new DepValue(Type.DOUBLE_TYPE,deps);
        case ALOAD:
        	deps.getLocals().add(((VarInsnNode) insn).var);
        	return new DepValue(Type.getObjectType("java/lang/Object"), deps);
        case DUP:
        case ASTORE:
        case ISTORE:
        case LSTORE:
        case FSTORE:
        case DSTORE:
        	return value;
        default:
        	System.err.println("Unknow copyOperation "+opcode2string(insn.getOpcode()));
			//throw new Error("Internal error.");
        	return null;
        }
    }

    @Override
    public DepValue unaryOperation(final AbstractInsnNode insn,
            final DepValue value) throws AnalyzerException {
        switch (insn.getOpcode()) {
        case INEG:
        case IINC:
        case L2I:
        case F2I:
        case D2I:
        case I2B:
        case I2C:
        case I2S:
            return new DepValue(Type.INT_TYPE,value.getDeps());
        case FNEG:
        case I2F:
        case L2F:
        case D2F:
            return new DepValue(Type.FLOAT_TYPE,value.getDeps());
        case LNEG:
        case I2L:
        case F2L:
        case D2L:
            return new DepValue(Type.LONG_TYPE,value.getDeps());
        case DNEG:
        case I2D:
        case L2D:
        case F2D:
            return new DepValue(Type.DOUBLE_TYPE,value.getDeps());
        case IFEQ:
        case IFNE:
        case IFLT:
        case IFGE:
        case IFGT:
        case IFLE:
        case TABLESWITCH:
        case LOOKUPSWITCH:
        	return null;
        case IRETURN:
        case LRETURN:
        case FRETURN:
        case DRETURN:
        case ARETURN:{
        	effect.getRet().merge(value.getDeps());
        	return null;
        }
		case PUTSTATIC: {
			FieldInsnNode fin = (FieldInsnNode) insn;
			effect.getStaticField().add(
					new StaticFieldEffect(fin.desc, fin.owner, fin.name, value
							.getDeps()));
			return null;
		}
        case GETFIELD:{
        	FieldInsnNode fin = (FieldInsnNode) insn;
        	DepValue v =new DepValue(Type.getType(fin.desc), value.getDeps());
        	v.getDeps().getFields().add(new FieldDep(fin.desc,fin.owner,fin.name));
        	return v;
        }
        case NEWARRAY:
            switch (((IntInsnNode) insn).operand) {
            case T_BOOLEAN:
                return new DepValue(Type.getType("[Z"), value.getDeps());
            case T_CHAR:
                return new DepValue(Type.getType("[C"), value.getDeps());
            case T_BYTE:
                return new DepValue(Type.getType("[B"), value.getDeps());
            case T_SHORT:
                return new DepValue(Type.getType("[S"), value.getDeps());
            case T_INT:
                return new DepValue(Type.getType("[I"), value.getDeps());
            case T_FLOAT:
                return new DepValue(Type.getType("[F"), value.getDeps());
            case T_DOUBLE:
                return new DepValue(Type.getType("[D"), value.getDeps());
            case T_LONG:
                return new DepValue(Type.getType("[J"), value.getDeps());
            default:
                throw new AnalyzerException(insn, "Invalid array type");
            }
        case ANEWARRAY:{
            String desc = ((TypeInsnNode) insn).desc;
            DepValue v = new DepValue(Type.getType("[" + Type.getObjectType(desc)),value.getDeps());
            return v;
        }
        case ARRAYLENGTH:
            return new DepValue(Type.INT_TYPE, value.getDeps());
        
        case ATHROW:{
        	effect.getOther().add(new ThrowEffect(value.getDeps()));
            return null;
        }
        case CHECKCAST:{
            String desc = ((TypeInsnNode) insn).desc;
            return newValue(Type.getObjectType(desc));
        }
        case INSTANCEOF:
            return new DepValue(Type.INT_TYPE);
        case MONITORENTER:
        case MONITOREXIT:
        case IFNULL:
        case IFNONNULL:
            return null;
        default:
        	System.err.println("Unknow copyOperation "+opcode2string(insn.getOpcode()));
			//throw new Error("Internal error.");
        	return null;
        }
    }

    @Override
    public DepValue binaryOperation(final AbstractInsnNode insn,
            final DepValue value1, final DepValue value2)
            throws AnalyzerException {
    	DepSet deps = new DepSet(value1.getDeps());
    	deps.merge(value2.getDeps());
        switch (insn.getOpcode()) {
        case LCMP:
        case FCMPL:
        case FCMPG:
        case DCMPL:
        case DCMPG:
        case IALOAD:
        case BALOAD:
        case CALOAD:
        case SALOAD:
        case IADD:
        case ISUB:
        case IMUL:
        case IDIV:
        case IREM:
        case ISHL:
        case ISHR:
        case IUSHR:
        case IAND:
        case IOR:
        case IXOR:
        	return new DepValue(Type.INT_TYPE, deps);
        case FALOAD:
        case FADD:
        case FSUB:
        case FMUL:
        case FDIV:
        case FREM:
        	return new DepValue(Type.FLOAT_TYPE, deps);
        case LALOAD:
        case LADD:
        case LSUB:
        case LMUL:
        case LDIV:
        case LREM:
        case LSHL:
        case LSHR:
        case LUSHR:
        case LAND:
        case LOR:
        case LXOR:
        	return new DepValue(Type.LONG_TYPE, deps);
        case DALOAD:
        case DADD:
        case DSUB:
        case DMUL:
        case DDIV:
        case DREM:
        	return new DepValue(Type.DOUBLE_TYPE, deps);
        case AALOAD:
        	return new DepValue(Type.getObjectType("java/lang/Object"), deps);
        case IF_ICMPEQ:
        case IF_ICMPNE:
        case IF_ICMPLT:
        case IF_ICMPGE:
        case IF_ICMPGT:
        case IF_ICMPLE:
        case IF_ACMPEQ:
        case IF_ACMPNE:
        	return null;
        case PUTFIELD:{
        	FieldInsnNode fin = (FieldInsnNode) insn;        	
			if (value1.isThis()) {
				effect.getThisField().add(
						new ThisFieldEffect(fin.desc, fin.owner, fin.name,
								value2.getDeps()));
			} else {
				effect.getOtherField().add(
						new OtherFieldEffect(fin.desc, fin.owner, fin.name,
								value2.getDeps(), value1.getDeps()));
			}
        	return null;
        }
        default:
        	System.err.println("Unknow copyOperation "+opcode2string(insn.getOpcode()));
			//throw new Error("Internal error.");
        	return null;
        }
    }

    @Override
    public DepValue ternaryOperation(final AbstractInsnNode insn,
            final DepValue value1, final DepValue value2,
            final DepValue value3) throws AnalyzerException {
    	DepSet deps = new DepSet(value1.getDeps());
    	deps.merge(value2.getDeps());
    	deps.merge(value3.getDeps());
    	switch(insn.getOpcode()){
    	case BASTORE:
    	case CASTORE:
    	case SASTORE:
    	case IASTORE:
    	case LASTORE:
    	case FASTORE:
    	case DASTORE:
    	case AASTORE:
    		effect.getOther().add(new ArrayStoreEffect(deps));
    		return null;
        default:
        	System.err.println("Unknow copyOperation "+opcode2string(insn.getOpcode()));
			//throw new Error("Internal error.");
        	return null;
    	}
    }

    @Override
    public DepValue naryOperation(final AbstractInsnNode insn,
            final List<? extends DepValue> values) throws AnalyzerException {
    	DepSet deps = new DepSet();
    	for(DepValue value :values){
    		deps.merge(value.getDeps());
    	}
    	String callType="";
    	switch(insn.getOpcode()){
    	case MULTIANEWARRAY:
    		return new DepValue(Type.getType(((MultiANewArrayInsnNode) insn).desc), deps);
    	case INVOKEDYNAMIC:
    		return new DepValue(Type.getReturnType(((InvokeDynamicInsnNode) insn).desc), deps);
    	case INVOKEVIRTUAL:
    		callType="V";
    	case INVOKEINTERFACE:
    		callType="I";
    	case INVOKESPECIAL:
    		callType="X";
    	case INVOKESTATIC:{
    		callType="S";
    		break;
    	}
		default:
			System.err.println("Unknow copyOperation "+opcode2string(insn.getOpcode()));
			//throw new Error("Internal error.");
        	return null;
    	}
		MethodInsnNode min = (MethodInsnNode) insn;
		CallEffect ce=new CallEffect(callType,min.desc,min.owner,min.name, deps);
		effect.getOther().add(ce);
		return new DepValue(Type.getReturnType(min.desc), deps);
    }

    @Override
    public void returnOperation(final AbstractInsnNode insn,
            final DepValue value, final DepValue expected)
            throws AnalyzerException {
    }

    @Override
    public DepValue merge(final DepValue v, final DepValue w) {
    	DepSet deps = new DepSet();
    	deps.merge(v.getDeps());
    	deps.merge(w.getDeps());
        if (!v.equals(w)) {
            return new DepValue(null, deps);
        }
        return new DepValue(v.getType(),deps);
    }
}
