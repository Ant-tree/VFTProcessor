package com.anttree.vft.processors.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.TYPE, ElementType.CONSTRUCTOR})
public @interface VisibleForTesting {
    enum Scope { PACKAGE_PRIVATE, PRIVATE, PROTECTED, PUBLIC, NONE }
    Scope scope() default Scope.NONE;
    String flavor() default "release";
}
 