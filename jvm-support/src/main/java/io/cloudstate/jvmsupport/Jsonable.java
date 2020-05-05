package io.cloudstate.jvmsupport;

import io.cloudstate.jvmsupport.impl.CloudStateAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Indicates that this class can be serialized to/from JSON. */
@CloudStateAnnotation
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Jsonable {}
