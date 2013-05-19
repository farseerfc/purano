package jp.ac.osakau.farseerfc.purano.util;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public @Data class MethodDesc {
    @NotNull
    private final String returnType;
    @NotNull
    private final List<String> arguments;
}
