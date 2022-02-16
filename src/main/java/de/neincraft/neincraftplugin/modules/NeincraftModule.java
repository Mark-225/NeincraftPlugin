package de.neincraft.neincraftplugin.modules;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface NeincraftModule {
    String id();
    String[] requiredModules() default {};
    boolean isVital() default false;
}
