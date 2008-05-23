package org.drools.learner.tools;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface FieldAnnotation {
	    int readingSeq();
	    boolean ignore() default false;
	    boolean target() default false;
	    boolean discrete() default true;
	    String[] values() default {"bok"};
}