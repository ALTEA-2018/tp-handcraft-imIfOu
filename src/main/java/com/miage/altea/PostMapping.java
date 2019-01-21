package com.miage.altea;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface PostMapping {
    // uri à écouter
    String uri();
}
