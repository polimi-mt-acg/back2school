package com.github.polimi_mt_acg.back2school.api.v1.security_contexts;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.ws.rs.NameBinding;

/**
 * Annotates a JAX-RS API that a request performed to it is filtered according to
 * "Teacher/Administrators only" security policy.
 */
@NameBinding
@Retention(RUNTIME)
@Target({TYPE, METHOD})
public @interface TeacherAdministratorSecured {}
