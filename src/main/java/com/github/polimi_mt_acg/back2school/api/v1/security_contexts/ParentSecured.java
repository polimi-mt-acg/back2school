package com.github.polimi_mt_acg.back2school.api.v1.security_contexts;

import javax.ws.rs.NameBinding;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotates a JAX-RS API that a request performed to it is filtered accoriding to "Administrators
 * only" security policy.
 */
@NameBinding
@Retention(RUNTIME)
@Target({TYPE, METHOD})
public @interface ParentSecured {}
