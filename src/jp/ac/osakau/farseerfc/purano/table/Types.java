package jp.ac.osakau.farseerfc.purano.table;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Opcodes;

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
}
