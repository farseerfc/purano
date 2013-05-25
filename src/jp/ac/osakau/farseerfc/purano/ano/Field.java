package jp.ac.osakau.farseerfc.purano.ano;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface Field {
    @NotNull public boolean dependThis() default true;
    @NotNull public String[] dependFields() default {};
    @NotNull public String[] dependStaticFields() default {};
    @NotNull public String[] dependArguments() default {};

    @NotNull public String inheritedFrom() default "";

    @NotNull public Class type();
    @NotNull public Class owner();
    @NotNull public String name();
}
