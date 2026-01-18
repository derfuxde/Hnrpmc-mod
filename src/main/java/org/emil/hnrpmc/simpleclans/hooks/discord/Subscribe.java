package org.emil.hnrpmc.simpleclans.hooks.discord;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

/**
 * <p>Marks a {@link Method} as one that should receive DiscordSRV API events</p>
 * <p>Functionally identical to Bukkit's EventHandler annotation</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Subscribe {

    ListenerPriority priority() default ListenerPriority.NORMAL;

}
