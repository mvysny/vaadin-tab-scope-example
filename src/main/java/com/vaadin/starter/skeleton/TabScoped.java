package com.vaadin.starter.skeleton;

import java.lang.annotation.*;

/**
 * If a layout or a route has this annotation, it is tab-scoped.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Documented
public @interface TabScoped {
}
