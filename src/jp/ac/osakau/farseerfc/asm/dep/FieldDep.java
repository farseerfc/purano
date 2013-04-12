package jp.ac.osakau.farseerfc.asm.dep;

import jp.ac.osakau.farseerfc.asm.table.TypeNameTable;
import lombok.Data;

public @Data class FieldDep {
	private final String desc;
	private final String owner;
	private final String name;
	
	public String dump(TypeNameTable table){
		return String.format("%s %s.%s", 
				table.desc2full(desc),
				table.fullClassName(owner),
				name);
	}
}
