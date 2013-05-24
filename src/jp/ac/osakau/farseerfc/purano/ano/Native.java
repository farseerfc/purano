package jp.ac.osakau.farseerfc.purano.ano;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface Native {
    @NotNull public String inheritedFrom() default "";
}
