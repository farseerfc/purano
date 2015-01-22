package jp.ac.osakau.farseerfc.purano.util;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import jp.ac.osakau.farseerfc.purano.reflect.ArrayStub;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.reflect.Field;
import java.util.*;

import lombok.Getter;

public class Types {
	private final Map<String, String> fullClassNames = new HashMap<>();
	@Nullable @Getter
    private String packageName = null;
	private static final Map<Character, String> desc2type = new HashMap<>();
	private final boolean shorten;

	static {
		desc2type.put('Z', "boolean");
		desc2type.put('C', "char");
		desc2type.put('B', "byte");
		desc2type.put('S', "Short");
		desc2type.put('I', "int");
		desc2type.put('J', "long");
		desc2type.put('F', "float");
		desc2type.put('D', "double");
		desc2type.put('V', "void");
	}
	
	public Types(){
		shorten = true;
	}
	
	public Types(boolean shorten){
		this.shorten = shorten;
	}
	
	public Types(boolean shorten, @Nullable String pkg){
		this.shorten = shorten;
		this.packageName = pkg;
	}

	@Nullable
    public String desc2full(@Nullable String name) {
		if (name == null || name.length() == 0) {
			return null; // special mark to indicate that name is at end
		}
		if (desc2type.containsKey(name.charAt(0))) {
			return desc2type.get(name.charAt(0));
		}
		if (name.charAt(0) == '[') {
			return desc2full(name.substring(1)) + "[]";
		}
		if (name.charAt(0) == 'L') {
			String classNamePath = name.substring(1, name.length() - 1);
			return fullClassName(classNamePath);
		}
		throw new IllegalArgumentException(String.format(
				"Bad type name: \"%s\"", name));
	}

	private LinkedList<String> nextDesc(@NotNull String desc) {
		if (desc.length() == 0) {
			return new LinkedList<>();
		}
		if (desc2type.containsKey(desc.charAt(0))) {
			LinkedList<String> result = nextDesc(desc.substring(1));
			result.addFirst(desc.substring(0));
			return result;
		}
		if (desc.charAt(0) == '[') {
			LinkedList<String> result = nextDesc(desc.substring(1));
			result.set(0, "[" + result.get(0));
			return result;
		}
		if (desc.charAt(0) == 'L') {
			int posSemi = desc.indexOf(';');
			String classNamePath = desc.substring(0, posSemi + 1);
			String remain = desc.substring(posSemi + 1);
			LinkedList<String> result = nextDesc(remain);
			result.addFirst(classNamePath);
			return result;
		}
		throw new IllegalArgumentException(String.format(
				"Bad desc name: \"%s\"", desc));
	}


    @NotNull
    public MethodDesc method2full(@NotNull String desc) {
		if (desc.length() < 2) {
			throw new IllegalArgumentException(String.format(
					"Bad method desc: \"%s\"", desc));
		}
		if (desc.charAt(0) != '(') {
			throw new IllegalArgumentException(String.format(
					"Bad method desc: \"%s\"", desc));
		}
		int rightParPos = desc.lastIndexOf(')');
		String retDesc = desc.substring(rightParPos + 1);
		String argDesc = desc.substring(1, rightParPos);
		List<String> args = Lists.transform(nextDesc(argDesc),
				new Function<String, String>() {
					@Override
					@Nullable
					public String apply(@Nullable String desc) {
						return desc2full(desc);
					}
				});
		return new MethodDesc(desc2full(retDesc),args);
	}
	
	public String dumpMethodDesc(@NotNull String desc, String methodName){
		MethodDesc methodDesc = method2full(desc);
		return String.format("%s %s (%s)", methodDesc.getReturnType(),methodName,
				Joiner.on(", ").join(methodDesc.getArguments()));
	}

	public String fullClassName(@NotNull String binaryName) {
		String name = Types.binaryName2NormalName(binaryName);
		if(name.startsWith("[")){
			name = ArrayStub.class.getName();
		}
		
		if(!shorten){
			return name;
		}
		
		String className = name.substring(name.lastIndexOf('.') + 1);
		if (fullClassNames.containsKey(className)) {
			if (fullClassNames.get(className).equals(name)) {
				return className;
			} else {
				return name;
			}
		} else {
			if(name.lastIndexOf('.') == -1){
				return name;
			}
			if (packageName == null) {
				// first package name is class package
				packageName = name.substring(0, name.lastIndexOf('.'));
				return className;
			}
			if (packageName.equals(name.substring(0, name.lastIndexOf('.')))) {
				// within the same package
				return className;
			}
			fullClassNames.put(className, name);
			return className;
		}
	}

	@NotNull
	public Collection<String> getImports() {
		List<String> result = new ArrayList<>(fullClassNames.values());
		Collections.sort(result);
		return result;
	}

	@NotNull
    public String dumpImports() {
		if(!shorten){
			return "";
		}
		
		StringBuilder sb = new StringBuilder();

		sb.append("package ");
		sb.append(packageName);
		sb.append(";\n\nimport ");
		sb.append(Joiner.on(";\nimport ").join(getImports()));
		sb.append(";\n\n");
		return sb.toString();
	}
	
	@NotNull
    public String dumpImportsHtml() {
		if(!shorten){
			return "";
		}
		
		StringBuilder sb = new StringBuilder();

		sb.append("package ");
		sb.append(packageName);
		sb.append(";<br/><br/>import ");
		sb.append(Joiner.on(";<br/>import ").join(getImports()));
		sb.append(";<br/><br/>");
		return sb.toString();
	}
	
	public static String binaryName2NormalName(@NotNull String binaryName){
		return binaryName.replace('/', '.');
	}
	
	public static String normalName2BinaryName(@NotNull String normalName){
		return normalName.replace('.', '/');
	}

	public static String access2string(int access) {
		List<String> result = new ArrayList<>();
		for(Field f: Opcodes.class.getFields()){
			if(f.getName().startsWith("ACC_")){
				int v = 0;
				try {
					v = f.getInt(f);
				} catch (@NotNull IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
				if((access & v) !=0){
					result.add(String.format("%s(0x%x)",f.getName().substring(4).toLowerCase(),v));
				}
			}
		}
		return Joiner.on(" ").join(result);
	}
	

	private static final Map<String, Class<?>> primitiveClasses = new HashMap<>();

	static {
	    primitiveClasses.put("byte", byte.class);
	    primitiveClasses.put("short", short.class);
	    primitiveClasses.put("char", char.class);
	    primitiveClasses.put("int", int.class);
	    primitiveClasses.put("long", long.class);
	    primitiveClasses.put("float", float.class);
	    primitiveClasses.put("double", double.class);
	    primitiveClasses.put("boolean", boolean.class);
	}

//	@org.jetbrains.annotations.Nullable
//    public static final Function<Type, Class<? extends Object>> loadClass = new Function<Type, Class<? extends Object>> (){
//		@Nullable @Override
//		public Class<? extends Object> apply(@NotNull Type t){
//			String name = t.getClassName();
//			if(name.endsWith("[]")){
//				name = Types.binaryName2NormalName(t.getInternalName());
//			}
//			try {
//				return forName(name);
//			} catch (ClassNotFoundException e) {
//				System.err.printf("Cannot load \"%s\"\n",name);
//				e.printStackTrace(System.err);
//				return null;
//				//throw new RuntimeException("Cannot load "+name,e);
//			}
//		}
//	};
	
	public static Class<?> forName(String name) throws ClassNotFoundException{
	    if (primitiveClasses.containsKey(name)) {
	        return primitiveClasses.get(name);
	    } else {
			return Class.forName(name);
	    }
	}
	
	
	public static void notFound(String name, @Nullable Exception e){
		//throw new RuntimeException(name,e);
		System.err.println(name);
		if(e!=null){
			e.printStackTrace(System.err);
		}
	}

	@Nullable
    public static Type covariant(@Nullable Type t1, @Nullable Type t2) {
		if(t1 == null){
			return t2;
		}
		if(t2 == null){
			return t1;
		}
		if(t1.equals(t2)){
			return t1;
		}
		if(t1.getSort() == Type.OBJECT || t2.getSort() == Type.OBJECT){
			return Type.getObjectType("java/lang/Object;");
		}
		if(t1.getSort() == Type.ARRAY || t2.getSort() == Type.ARRAY){
			return Type.getObjectType("Ljava/lang/Object;");
		}
        if(t1.getSort() == Type.OBJECT || t2.getSort() == Type.ARRAY){
            return Type.getObjectType("Ljava/lang/Object;");
        }
        if(t1.getSort() == Type.ARRAY || t2.getSort() == Type.OBJECT){
            return Type.getObjectType("Ljava/lang/Object;");
        }
		
		if(t1.equals(Type.DOUBLE_TYPE) || t2.equals(Type.DOUBLE_TYPE)){
			return Type.DOUBLE_TYPE;
		}
		if(t1.equals(Type.FLOAT_TYPE) || t2.equals(Type.FLOAT_TYPE)){
			return Type.FLOAT_TYPE;
		}
		if(t1.equals(Type.INT_TYPE) || t2.equals(Type.INT_TYPE)){
			return Type.INT_TYPE;
		}
		if(t1.equals(Type.SHORT_TYPE) || t2.equals(Type.SHORT_TYPE)){
			return Type.SHORT_TYPE;
		}
		if(t1.equals(Type.BYTE_TYPE) || t2.equals(Type.BYTE_TYPE)){
			return Type.BYTE_TYPE;
		}
		if(t1.equals(Type.CHAR_TYPE) || t2.equals(Type.CHAR_TYPE)){
			return Type.CHAR_TYPE;
		}
		if(t1.equals(Type.BOOLEAN_TYPE) || t2.equals(Type.BOOLEAN_TYPE)){
			return Type.BOOLEAN_TYPE;
		}
		throw new RuntimeException(String.format("Unknown covariants %s %s",t1.toString(),t2.toString()));
		//return null;
	}
}
