package ru.etc1337.client.modules.impl.render.ui.api;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ElementInfo {
    String name();
    String icon() default "";
    float initX();
    float initY();
    float initHeight();
}