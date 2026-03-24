package com.logistics.supply.annotation;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Grants access to General Manager, Admin, Procurement Manager, HOD, and Procurement Officer.
 * Use on endpoints that are visible across management and procurement roles.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAnyRole('ROLE_GENERAL_MANAGER','ROLE_ADMIN','ROLE_PROCUREMENT_MANAGER','ROLE_HOD','ROLE_PROCUREMENT_OFFICER')")
public @interface IsApprover {}
