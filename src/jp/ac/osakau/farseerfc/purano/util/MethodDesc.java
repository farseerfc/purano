package jp.ac.osakau.farseerfc.purano.util;

import java.util.List;

import lombok.Data;

public @Data class MethodDesc {
    private final String returnType;
    private final List<String> arguments;
}
