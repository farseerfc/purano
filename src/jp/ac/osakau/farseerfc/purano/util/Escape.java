package jp.ac.osakau.farseerfc.purano.util;

public class Escape {
	public static boolean use = true;

	public static String className(String name) {
		if (!use) {
			return name;
		}
		return "\033[1;32m" + name + "\033[m";
	}

	public static String methodName(String name) {
		if (!use) {
			return name;
		}
		return "\033[1;34m" + name + "\033[m";
	}

	public static String effect(String str) {
		if (!use) {
			return str;
		}
		return "\033[1;33m" + str + "\033[m";
	}
	
	public static String from(String str){
		if (!use) {
			return str;
		}
		return "\033[1;35m" + str + "\033[m";
	}
	
	public static String annotation(String str){
		if (!use) {
			return str;
		}
		return "\033[1;36m" + str + "\033[m";
	}
}
