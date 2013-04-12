package jp.ac.osakau.farseerfc.asm.table;

import java.util.List;

import lombok.Data;

public @Data class MethodDesc {
    private final String returnType;
    private final List<String> arguments;
}
