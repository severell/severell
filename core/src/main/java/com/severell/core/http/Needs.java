package com.severell.core.http;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface Needs {
    String[] value();
}
