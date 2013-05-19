package jp.ac.osakau.farseerfc.purano.dep;

import jp.ac.osakau.farseerfc.purano.util.Types;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

public @Data class FieldDep {
	@NotNull
    private final String desc;
	@NotNull
    private final String owner;
	@NotNull
    private final String name;
	
	public String dump(@NotNull Types table){
		return String.format("%s %s.%s", 
				table.desc2full(desc),
				table.fullClassName(owner),
				name);
	}
}
