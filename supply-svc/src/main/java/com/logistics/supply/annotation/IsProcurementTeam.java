package com.logistics.supply.annotation;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Grants access to Procurement Officer and Procurement Manager.
 * Use on endpoints that are exclusive to the procurement team.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAnyRole('ROLE_PROCUREMENT_OFFICER','ROLE_PROCUREMENT_MANAGER')")
public @interface IsProcurementTeam {}
