package com.shade.decima.ui.action;

import com.shade.decima.model.util.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ActionRegistration {
    @NotNull
    String name();

    @NotNull
    String description() default "";

    @NotNull
    String accelerator() default "";
}
