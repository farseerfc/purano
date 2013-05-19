package jp.ac.osakau.farseerfc.purano.ano;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
public @interface Return {
	@NotNull public String[] depends();
}
