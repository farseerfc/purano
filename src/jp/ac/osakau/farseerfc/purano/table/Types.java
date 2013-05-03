package jp.ac.osakau.farseerfc.purano.table;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.google.common.base.Function;
import com.google.common.base.Joiner;

public class Types {
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
}
