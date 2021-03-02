package com.logistics.supply.util;

public class CommonHelper {

    private static final String EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

    public static boolean isValidEmailAddress(String email){
        return email != null &&
                email.matches(EMAIL_REGEX);
    }
}
