package jp.ac.osakau.farseerfc.purano.util;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MethodDesc {
    public MethodDesc(@NotNull String returnType, @NotNull List<String> arguments) {
        this.returnType = returnType;
        this.arguments = arguments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MethodDesc)) return false;

        MethodDesc that = (MethodDesc) o;

        if (!arguments.equals(that.arguments)) return false;
        if (!returnType.equals(that.returnType)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = returnType.hashCode();
        result = 31 * result + arguments.hashCode();
        return result;
    }


    @NotNull
    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(@NotNull String returnType) {
        this.returnType = returnType;
    }

    @NotNull
    private String returnType;

    @NotNull
    public List<String> getArguments() {
        return arguments;
    }

    public void setArguments(@NotNull List<String> arguments) {
        this.arguments = arguments;
    }

    @NotNull
    private List<String> arguments;


}
