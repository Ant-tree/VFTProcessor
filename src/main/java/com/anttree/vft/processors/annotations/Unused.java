package com.anttree.vft.processors.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.TYPE, ElementType.CONSTRUCTOR})
public @interface Unused {
    String flavor() default "release";
}
