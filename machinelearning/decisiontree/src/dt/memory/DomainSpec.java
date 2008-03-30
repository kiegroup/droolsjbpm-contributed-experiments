package dt.memory;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface DomainSpec {
	    int readingSeq();
	    boolean target() default false;
	    boolean discrete() default true;
	    String[] values() default {"bok"};
}
