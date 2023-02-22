package com.logistics.supply.util;

import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.model.EmployeeRole;
import org.springframework.security.core.Authentication;

public class AuthHelper {

    public static Boolean checkAuthorityExist(Authentication authentication, EmployeeRole role)
            throws GeneralException {
        return authentication.getAuthorities().stream()
                .map(x -> x.getAuthority().equalsIgnoreCase(role.name()))
                .filter(x -> x == true)
                .findAny()
                .orElse(false);
    }
}
