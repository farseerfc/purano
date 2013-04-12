package jp.ac.osakau.farseerfc.asm.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;


import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class TypeNameTable {
	private final Map<String, String> fullClassNames = new HashMap<>();
	private String packageName = null;
	private final Map<Character, String> desc2type = new HashMap<>();

	public TypeNameTable() {
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

	public String fullClassName(String name) {
		String className = name.substring(name.lastIndexOf('/') + 1);
		name = name.replace('/', '.');
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
		StringBuilder sb = new StringBuilder();

		sb.append("package ");
		sb.append(packageName);
		sb.append(";\n\nimport ");
		sb.append(Joiner.on(";\nimport ").join(getImports()));
		sb.append(";\n\n");
		return sb.toString();
	}
}
