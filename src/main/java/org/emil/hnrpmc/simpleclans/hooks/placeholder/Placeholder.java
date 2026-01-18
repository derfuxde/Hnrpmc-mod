package org.emil.hnrpmc.simpleclans.hooks.placeholder;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(Placeholders.class)
public @interface Placeholder {

    String value();
    String resolver() default "method_return";
    String config() default "";
}
