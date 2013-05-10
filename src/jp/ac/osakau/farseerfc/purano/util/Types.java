package jp.ac.osakau.farseerfc.purano.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import jp.ac.osakau.farseerfc.purano.reflect.ArrayStub;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class Types {
	private final Map<String, String> fullClassNames = new HashMap<>();
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
	
	public Types(boolean shorten, String pkg){
		this.shorten = shorten;
		this.packageName = pkg;
	}

	public String desc2full(String name) {
		if (name.length() == 0) {
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

	private LinkedList<String> nextDesc(String desc) {
		if (desc.length() == 0) {
			return new LinkedList<String>();
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

	public MethodDesc method2full(String desc) {
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
	
	public String dumpMethodDesc(String desc, String methodName){
		MethodDesc methodDesc = method2full(desc);
		return String.format("%s %s (%s)", methodDesc.getReturnType(),methodName,
				Joiner.on(", ").join(methodDesc.getArguments()));
	}

	public String fullClassName(String binaryName) {
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

	private Collection<String> getImports() {
		List<String> result = new ArrayList<>(fullClassNames.values());
		Collections.sort(result);
		return result;
	}

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
	
	
	public static String binaryName2NormalName(String binaryName){
		return binaryName.replace('/', '.');
	}
	
	public static String normalName2BinaryName(String normalName){
		return normalName.replace('.', '/');
	}

	public static String access2string(int access) {
		List<String> result = new ArrayList<>();
		for(Field f: Opcodes.class.getFields()){
			if(f.getName().startsWith("ACC_")){
				int v = 0;
				try {
					v = f.getInt(f);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
				if((access & v) !=0){
					result.add(String.format("%s(0x%x)",f.getName().substring(4).toLowerCase(),v));
				}
			}
		}
		return Joiner.on(" ").join(result);
	}
	

	private static final Map<String, Class<?>> primitiveClasses = new HashMap<String, Class<?>>();

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

	public static final Function<Type, Class<? extends Object>> loadClass = new Function<Type, Class<? extends Object>> (){
		@Nullable @Override
		public Class<? extends Object> apply(Type t){
			String name = t.getClassName();
			if(name.endsWith("[]")){
				name = Types.binaryName2NormalName(t.getInternalName());
			}
			try {
				return forName(name);
			} catch (ClassNotFoundException e) {
				System.err.printf("Cannot load \"%s\"\n",name);
				e.printStackTrace(System.err);
				return null;
				//throw new RuntimeException("Cannot load "+name,e);
			}
		}
	};
	
	public static Class<? extends Object> forName(String name) throws ClassNotFoundException{
	    if (primitiveClasses.containsKey(name)) {
	        return primitiveClasses.get(name);
	    } else {
			return Class.forName(name);
	    }
	}
	
	
	public static void notFound(String name,Exception e){
		//throw new RuntimeException(name,e);
		System.err.println(name);
		if(e!=null){
			e.printStackTrace(System.err);
		}
	}
}
