/**
 * Created by Appiaries Corporation on 15/04/13.
 * Copyright (c) 2015 Appiaries Corporation. All rights reserved.
 */

package com.appiaries.baas.sdk;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * アピアリーズ・コレクション指定用アノテーション。
 */
@Target({java.lang.annotation.ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ABCollection {
    public abstract String value() default "";
}
