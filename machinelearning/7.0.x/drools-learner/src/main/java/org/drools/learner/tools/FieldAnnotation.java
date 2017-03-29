package org.drools.learner.tools;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention( RetentionPolicy.RUNTIME )
@Target( { ElementType.METHOD, ElementType.FIELD } )
public @interface FieldAnnotation {
    int readingSeq() default 0;

    boolean ignore() default false;

    boolean skip() default false;

    boolean target() default false;

    boolean discrete() default true;

    String[] values() default { "bok" };
}